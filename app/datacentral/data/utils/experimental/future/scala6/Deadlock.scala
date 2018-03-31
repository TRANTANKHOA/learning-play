package scala6

import java.util.concurrent.Executors

import scala.concurrent.{Await, ExecutionContext, Future}
import scala1.WaitTimes


/**
  * Deadlock
  */
object Deadlock extends App {
  val r = for {
    i <- Future {
      fut
    }
  } yield i
  private implicit val executionContext = ExecutionContext.fromExecutorService(threadPool)
  private val threadPool = Executors.newFixedThreadPool(2)

  private def fut = {
    Await.result(Future {
      1 + 1
    }, WaitTimes.time)
  }

  println(Await.result(r, WaitTimes.time))

  threadPool.shutdown()
}
