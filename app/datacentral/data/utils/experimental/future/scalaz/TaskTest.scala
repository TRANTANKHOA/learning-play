package scalaz

import java.util.concurrent.{ExecutorService, Executors}

import scalaz.concurrent.Task

object TaskTest extends App {

  val t1: String = Task(printAndReturn("aaa"))(threadPool)
    .map(v => printAndReturn(v + "bbb"))
    .flatMap(v => Task(printAndReturn(v + "ccc")))
    .map(v => printAndReturn(v + "ddd"))
    .unsafePerformSync
  val c = Task(printAndReturn("aaa"))(threadPool)
  println(s"Start - ThreadId=${Thread.currentThread().getId}")
  private val threadPool: ExecutorService = Executors.newFixedThreadPool(10)

  def printAndReturn(s: String): String = {
    println(s"$s - ThreadId=${Thread.currentThread().getId}")
    s
  }

  c.attempt

  println(t1)
  threadPool.shutdown()
}