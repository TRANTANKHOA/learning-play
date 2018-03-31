package scala5

import java.util.concurrent.Executors

import scala.concurrent.{Await, ExecutionContext, Future}
import scala1.WaitTimes

/**
  * Sequencing Futures
  */
object CollectionFutures extends App {
  val seq = 0 to 10
  private implicit val executionContext = ExecutionContext.fromExecutorService(threadPool)
  val r = for {
    s <- seq
    if s % 2 == 0
  } yield for {
    _ <- Future {
      println(s"future 1 : $s - ThreadId=${Thread.currentThread().getId}")
    }
    _ = println("print 3")
    _ <- Future {
      println(s"future 2 : $s - ThreadId=${Thread.currentThread().getId}")
    }
  } yield s"$s"
  private val threadPool = Executors.newFixedThreadPool(1)


  println(Await.result(Future.sequence(r), WaitTimes.time))

  threadPool.shutdown()
}
