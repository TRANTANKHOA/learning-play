package scalaz

import java.util.concurrent.{ExecutorService, Executors}

import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz.concurrent.Task

object TaskTimedTest extends App {

  val t1: String = Task(printAndReturn("aaa"))(threadPool)
    .timed(3 second)
    .map(v => printAndReturn(v + "bbb"))
    .unsafePerformSync
  private val threadPool: ExecutorService = Executors.newFixedThreadPool(10)


  println(s"Start - ThreadId=${Thread.currentThread().getId}")

  def printAndReturn(s: String): String = {
    println(s"$s - ThreadId=${Thread.currentThread().getId}")
    Thread.sleep(2000)
    s
  }

  println(t1)
  threadPool.shutdown()
}