package scala3

import java.util.concurrent.Executors

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Success
import scala1.WaitTimes

object Trampolining extends App {
  private implicit val executionContext = ExecutionContext.fromExecutorService(threadPool)

  val future1 = Future {
    println(s"Inside Future: ThreadId=${Thread.currentThread().getId}")
    1 + 1
  }

  println(s"Outside Future: ThreadId=${Thread.currentThread().getId}")
  private val threadPool = Executors.newFixedThreadPool(10)
  future1.onComplete {
    case Success(_) => println(s"Inside onComplete: ThreadId=${Thread.currentThread().getId}")
  }
  future1.map { _ => println(s"Inside Map: ThreadId=${Thread.currentThread().getId}") }


  Await.result(future1, WaitTimes.time)
  threadPool.shutdown()
}
