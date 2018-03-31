package datacentral.data.utils.concurent

import java.time.LocalDateTime

import datacentral.data.utils.concurent.Composable.Bind
import datacentral.data.utils.concurent.ResumableTask.KeyManager
import datacentral.data.utils.concurent.Task.{FutureTried, TaskRepeatable}
import datacentral.data.utils.concurent.TaskComposableTest.{MockKeys, concurrentNotes, _}
import datacentral.data.utils.extensions.IntExtensions.IntWrapper
import datacentral.data.utils.extensions.sequences.TupleExtensions._
import org.scalatest.{BeforeAndAfterEach, FunSuite}
import org.spark_project.jetty.util.MultiException

import scala.collection.immutable.SortedMap
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.Try

object TaskComposableTest {
  // some keys
  val ignoredKeys = Seq("D", "E")
  val alphabet = Seq("a", "b", "c", "d", "e")
  //noinspection TypeAnnotation
  val integers = 0 to 5
  val rootKey = "root"
  val finalKey = "final"

  // pseudo KeyManager
  object MockKeys extends KeyManager {
    //noinspection TypeAnnotation
    private val keys = mutable.Map[String, Boolean](
      ignoredKeys.map(key => key -> true): _* // these keys should disable computation for a,b, e.g. those tasks are cached
    )

    def reset: Unit = {
      keys.clear
      keys ++= ignoredKeys.map(key => key -> true)
    }

    def print: String = keys.toSeq.sortBy(_.key).map(each => s"${each.key} - ${each.value}").mkString(" | ")

    def read(key: String): Boolean = keys.getOrElse(key, false)

    def write(key: String, start: LocalDateTime, end: LocalDateTime, error: Option[Throwable]): Try[Unit] = Try {
      val success = error.isEmpty
      keys.+=((key, success))
      println(s"written a key ${key -> success}")
    }

    def pop(key: String): Unit = keys -= key
  }

  // We will use this array of buffers to mark the execution of all tasks to see which ones of them are ignored or not
  object concurrentNotes {
    val map: SortedMap[String, ArrayBuffer[String]] = SortedMap(
      (integers.map2String ++ alphabet ++ alphabet.map(_.toUpperCase) :+ rootKey :+ finalKey)
        .map(key => key -> new ArrayBuffer[String]()): _*)

    def apply(key: String): ArrayBuffer[String] = map(key)

    def clear: Unit = map.values.foreach(_.clear)

    def isEmpty: Boolean = values.isEmpty

    def nonEmpty: Boolean = values.nonEmpty

    def values: Seq[String] = map.values.flatten.toSeq

    override def toString: String = map.mapValues(_.mkString("|")).mkString("\n")
  }

}

class TaskComposableTest extends FunSuite with BeforeAndAfterEach {

  implicit val keyManager: KeyManager = MockKeys

  // all tasks to be defined
  var integerTasks: IndexedSeq[Task[Int]] = _
  var alphabetTasks: Seq[Task[String]] = _
  var unitTasks: Map[String, ResumableTask] = _
  var finalTask: ResumableTask = _

  // At first, we will declare 3 separate trees of tasks with final task of type `Resumable` and keyed as A,B and C
  override def beforeEach(): Unit = {
    // Initialise
    concurrentNotes.clear
    keyManager.reset
    println("BEFORE TEST")
    println(concurrentNotes)
    println(keyManager.print)

    // Each of these tasks will put a number (0 - 5) to `arrayBuffer`
    integerTasks = for (int <- integers) yield {
      Task[Int]({
        concurrentNotes(int.toString).append(int.toString)
        int
      })
    }

    //noinspection ScalaUnusedSymbol
    // just checking if for-comprehension works
    val t2 = for {
      t <- integerTasks.head
    } yield 2 * t

    // Each of these tasks will put a character (a,b,c) to `arrayBuffer`, while (d,e) are ignored by the `keyManager`
    alphabetTasks = alphabet.map(str => {
      val aTask: Task[String] = Task[String]({
        concurrentNotes(str).append(str)
        str
      })

      // let's declare some conditional dependencies between alphabet and integer tasks
      integerTasks.zipIndex
        .foreach(task => aTask.dependsOn(task.value)
        (bind = Bind(when = if (str == "a")
          task.index.isEven
        else if (str == "b")
          task.index.isOdd
        else true)))

      aTask
    })

    // These unitTasks are the end of 3 separate trees. The dependencies are automatically wired using `andThen` method.
    // Alternatively, separate calls to `dependsOn` needed if input tasks are consumed in the by-name parameter
    // `out: => OUT`
    unitTasks = alphabetTasks
      .zipAsKey(alphabet.map(_.toUpperCase))
      .map(task => {
        task.key -> task.value.mapWith(task.key, routine = {
          str => concurrentNotes(task.key).append(str.toUpperCase)
        })

      }).toMap

    // This `finalTask` aggregate all `alphabetTasks`
    finalTask = ResumableTask(finalKey, routine = concurrentNotes(finalKey).append(
      alphabetTasks.map(
        _.getThenMap(
          _.+(finalKey).toUpperCase)): _*)
    ).dependsOn(alphabetTasks: _*)
    // Since `alphabetTasks` is consumed outside of `andThen`, we need to wire the dependencies manually
  }

  override def afterEach(): Unit = {
    println("AFTER TEST")
    println(concurrentNotes)
    println(keyManager.print)
  }

  test("testResumableTasks should be ignored when its key is stored in the KeyManager") {
    unitTasks.values.foreach(_.result.get)
    assert(concurrentNotes.values.intersect(ignoredKeys).isEmpty)
  }

  test("testReadWriteClearKeys that c should be written and read properly") {
    // test clear, read
    assert(keyManager.read("D"))
    keyManager.pop("D")
    assert(!keyManager.read("D"))
    // test write
    assert(!keyManager.read("C"))
    unitTasks("C").result.get // should write key "C" to MockKeys
    Await.result(Future(assert(keyManager.read("C"))), Duration.Inf) // need to verify the key in a Future
  }

  test("testStartedFlag that no task should be executed at this point despite calling `run` when declaring `unitTasks`") {
    assert(concurrentNotes.isEmpty &&
           integerTasks.none(_.hasStarted) &&
           alphabetTasks.none(_.hasStarted) &&
           unitTasks.values.none(_.hasStarted))
  }

  test("testRun should trigger only tasks leading up to the current tree") {
    unitTasks("A").result.get
    assert(
      concurrentNotes.values.diff(Seq("0", "2", "4", "a", "A")).isEmpty
    )
  }

  test("testCompletion should trigger only tasks leading up to the current tree") {
    unitTasks("B").completion.get
    assert(
      concurrentNotes.values.diff(Seq("5", "3", "1", "b", "B")).isEmpty
    )
  }

  test("testDependenciesResolution that no task could register a dependency on the finalTask") {
    val upstreamTasks: Seq[Composable] = integerTasks ++ alphabetTasks
    assert(finalTask.dependencyStream.distinct.length == upstreamTasks.length)
    assert(
      upstreamTasks.none(task => Try(task dependsOn finalTask) isSuccess)
    )
  }

  test("testAndThen should not repeat any computation") {
    unitTasks("C").result.get
    assert(
      concurrentNotes.values.groupBy(identity).values.none(_.length != 1)
    )
  }

  test("testDependedBy that `root` task is executed only once and cannot depend on any integerTasks") {
    val rootTask = Task[Unit](
      concurrentNotes(rootKey).append(rootKey)
    ).asDependencyOf(integerTasks: _*)

    assert(
      integerTasks.none(task => Try(rootTask dependsOn task) isSuccess)
    )
    finalTask.result.get
    assert(concurrentNotes.values.count(_ == rootKey) == 1)
  }

  test("testRun that when `root` task fail, none of the other tasks get executed and the errors are unique") {

    // The following two tasks are declared and stored in the dependency list of other tasks, so no need for `val` here
    Task[Unit]({
      concurrentNotes(rootKey).append(rootKey)
      throw new RuntimeException("I want this task fails")
    }).asDependencyOf(integerTasks: _*)

    Task[Unit](
      throw new RuntimeException("I also want this task fails")
    ).asDependencyOf(alphabetTasks: _*)

    val fin = finalTask.result

    // repeated exceptions are filtered to produce list of 2 unique ones
    assert(Await.result(
      fin.map(status =>
        status.map(_ => false).recover {
          case me: MultiException => me.size == 2
          case _ => false
        }.get), Duration.Inf))
    // fin.get // run this line to see the exceptions printout,
    assert(concurrentNotes.values.diff(Seq(rootKey)).isEmpty)
  }

  test("testRun that a task with no dependencies can run fine as well") {
    assert(Task[Unit](
      concurrentNotes(rootKey).append(rootKey)
    ).get.isInstanceOf[Unit])
  }

  test("testDuration that each task should automatically measure its own duration correctly") {
    val d = Task[Unit](
      Thread.sleep(5000)
    ).duration.get
    println(s"duration = $d")
    assert(d >= 5000)
  }

  test("testComparable that two tasks can be compared w.r.t. runtime") {
    val t = 50
    val m = 10
    val a = Task[Unit](Thread.sleep(t))
    val b = Task[Unit](Thread.sleep(t * m))
    val c = a.isNtimesFasterThan(b).get
    println(s"ratio = $c")
    println(s"a last ${a.duration.get}")
    println(s"b last ${b.duration.get}")
    assert(c == b.duration.get.toDouble / a.duration.get.toDouble)
  }

  test("testTree holds all distinct trees and can trigger all, except dangling tasks") {
    Composable.Tree.runAll
    Thread.sleep(500)
    finalTask +: unitTasks.values.toSeq forall (_.hasStarted)
  }

  test("testTaskRepeatable") {
    var int: Int = 0
    // Obviously, one can write the entire `while`-loop inside a TaskNotResumable and gets the same result.
    val result = new TaskRepeatable[Int](
      check = {
        int <= 10
      },
      repeat = {
        int += 1
        int
      },
      maxIter = 8
    ).get

    assert(result == 8)
  }
}
