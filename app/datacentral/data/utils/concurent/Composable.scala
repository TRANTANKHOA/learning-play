package datacentral.data.utils.concurent

import java.time.{Duration, LocalDateTime}

import datacentral.data.utils.concurent.Composable.{Bind, FutureTry, Status, StatusSeq}
import datacentral.data.utils.concurent.Task.FutureTried
import datacentral.data.utils.functional.SmartFunctions.lazyRecursiveWith
import org.spark_project.jetty.util.MultiException

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * The `Composable` trait provides extensions that can be used to defined a directed acyclic graph (DAG) of
  * computational tasks by registering dependencies for each task when it's defined. A computational task is different
  * from a function in that it is allowed to have side-effect, is concurrent and wrapped in a try-catch. Providing
  * composability to generic computational tasks is useful in that it enable a programing scheme where
  *
  * - task are executed in parallel by default
  * - task with side-effect can also be composed with flatmap, for-comprehension .. etc.
  * - each task is executed once and only once.
  * - only relevant tasks are triggered and failures are propagated within the DAG efficiently.
  *
  * This base trait should not be concerned with the actual IN/OUT types of the task at hand. It only has a
  * `completion: Status`
  */
abstract class Composable {

  // These attributes and methods provide auto-dependencies management
  protected lazy val dependencies = new ArrayBuffer[Composable]
  // This section should give you the timing of the task upon finishing the execution
  protected lazy val timerStart: LocalDateTime = LocalDateTime.now
  protected lazy val timerStop: LocalDateTime = LocalDateTime.now
  val completion: Status
  protected var started: Boolean = false

  /**
    * Declaring this `Composable` task as a dependency of any number of other tasks.
    *
    * This operation can be made optional by supplying a custom boolean flag for `when`
    *
    * @param others composable tasks
    * @param bind   is an optional conditional flag (defaulted to `true`) use to turn the dependency bindings on and off
    * @return the original `Composable` task with its dependencies added to `others` tasks
    */
  def asDependencyOf(others: Composable*)(implicit bind: Bind = Bind()): this.type = {
    if (bind.when)
      others.toSeq.foreach(_.dependsOn(this))
    this
  }

  /**
    * Register any number of `Composable` tasks as dependencies for this current task. This method should
    * throw an error when either the task has already commenced or the resulting DAG contains any directed-cycles
    *
    * This operation can be made optional by supplying a custom boolean flag for `when`
    *
    * @param others composable tasks
    * @param bind   is an optional conditional flag (defaulted to `true`) use to turn the dependency bindings on and off
    * @return the original `Composable` task with new dependencies added
    */
  def dependsOn(others: Composable*)(implicit bind: Bind = Bind()): this.type = if (started)
    throw new RuntimeException(s"Could not register ${others.toSeq.mkString(" | ")} tasks after ${this} task has already started.")
  else {
    if (bind.when) {
      others.toSeq.foreach(composable =>
        if (composable.dependencyStream.contains(this))
          throw new RuntimeException("This task depends on itself! Cannot run.")
        else
          dependencies.append(composable)
      )

      // collect all the distinct trees automatically as we build up the DAG
      Composable.Tree.add(this)
    }

    this
  }

  /**
    * The stream of dependencies' UUIDs need to be evaluated on-demand to prevent dead-lock dependency loop. Hence,
    * storing this stream in a cache is not possible.
    *
    * @return a `Stream` of `UUID` for all registered dependencies of the current task
    */
  def dependencyStream: Stream[Composable] = lazyRecursiveWith[Composable, Seq[Composable], Seq](
    remainder = this.dependencies,
    split = (remainder: Seq[Composable]) => {
      val out = remainder
      val nextRemainder = remainder.flatMap(_.dependencies)
      (nextRemainder, out)
    }
  ).flatten

  def hasStarted: Boolean = started

  def duration(implicit ec: ExecutionContext): FutureTry[Long] = completion.mapResult(_ => Duration.between(timerStart, timerStop).toMillis)

  protected def resolveDepsAndExecuteWithTimer[A](out: => A)(implicit ec: ExecutionContext): FutureTry[A] = dependencies
    .map(_.completion)
    .consolidate
    .mapResult(_ => timerStart)
    .mapResult(_ => out)
    .mapResult(o => {
      timerStop; o
    })
}

object Composable {
  type FutureTry[OUT] = Future[Try[OUT]]
  type Status = FutureTry[Unit]

  implicit class Comparable(task: Composable) {

    /**
      * Compare the runtime of 2 tasks, tried using scalaz here but it was hard..
      *
      * This can be use in benchmarking performance
      *
      * @param another
      * @return the ratio between runtime of `another` task over `task`
      */
    def isNtimesFasterThan(another: Composable)(implicit ec: ExecutionContext): Task[Double] = Task[Double](
      another.duration.get.toDouble / task.duration.get.toDouble)
      .dependsOn(task, another)

  }

  implicit class StatusSeq(seq: Traversable[Status])(implicit ec: ExecutionContext) {

    /**
      * Aggregate the sequence into a single status which can contain a `MultiException`
      *
      * @return
      */
    def consolidate: Status = Future.sequence(seq)
      .map(statusBuffer => {
        val errors: Traversable[Throwable] = statusBuffer.collect({
          case Failure(exception: MultiException) => exception.getThrowables.asScala
          case Failure(exception: Throwable) => mutable.Buffer(exception)
        }).flatten

        if (errors.isEmpty)
          Success(Unit)
        else {
          val multiException = new MultiException
          errors.toSeq.distinct.foreach(multiException.add)
          Failure(multiException)
        }
      })
  }

  /**
    * Contains the implicit boolean flag for conditional binding. This allows users to provide their own conditional
    * flag when needed or simply forget about it when they don't. The case class avoid accidentally reading `Boolean`
    * implicits from the environment
    *
    * @param when is defaulted to `true` for binding
    */
  case class Bind(when: Boolean = true)

  /**
    * This object will automatically collect a list of all distinct trees in the DAG. Note that dangling tasks which has
    * no connections to other tasks are not listed in this object by default, but can be `add` manually.
    */
  object Tree {
    // Collector of all distinct trees
    private var roots = new ArrayBuffer[Composable]()

    def add(newTree: Composable): Unit = if (!dependencies.contains(newTree)) {
      // removed the roots that are now dependencies of the newTree
      roots = roots.collect {
        case tree: Composable if !newTree.dependencies.contains(tree) => tree
      }

      roots.append(newTree)
    }

    // List all dependencies
    def dependencies: ArrayBuffer[Composable] = roots.flatMap(_.dependencyStream)

    def runAll: Seq[Status] = roots.map(_.completion)
  }

}