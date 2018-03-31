package scala6

import java.util.concurrent.Executors

import scala.concurrent.{Await, ExecutionContext, Future}
import scala1.WaitTimes

/**
  * Deadlock solved
  */
object DeadlockSolved extends App {
  private implicit val executionContext = ExecutionContext.fromExecutorService(threadPool)

  val r = for {
    i <- Future {
      fut
    }
  } yield i
  private val threadPool = Executors.newFixedThreadPool(1)
  private val threadPool2 = Executors.newFixedThreadPool(1)
  private val executionContext2 = ExecutionContext.fromExecutorService(threadPool2)

  private def fut = {
    Await.result(Future {
      1 + 1
    }(executionContext2), WaitTimes.time)
  }

  println(Await.result(r, WaitTimes.time))

  threadPool2.shutdown()
  threadPool.shutdown()
}
