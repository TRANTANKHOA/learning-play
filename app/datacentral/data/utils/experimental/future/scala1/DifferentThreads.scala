package scala1

import java.util.concurrent.Executors

import scala.concurrent.{Await, ExecutionContext, Future}


/**
  * Different Threads
  */
object DifferentThreads extends App {

  val future1 = Future {
    println(s"Inside Future: ThreadId=${Thread.currentThread().getId}")
    1 + 1
  }(executionContext)
  private val threadPool = Executors.newFixedThreadPool(1)

  println(s"Outside Future: ThreadId=${Thread.currentThread().getId}")
  private val executionContext = ExecutionContext.fromExecutorService(threadPool)


  Await.result(future1, WaitTimes.time)
  threadPool.shutdown()
}
