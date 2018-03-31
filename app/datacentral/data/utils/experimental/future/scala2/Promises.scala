package scala2

import java.util.concurrent.Executors

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala1.WaitTimes

object Promises extends App {
  val p = Promise[Int]()
  private implicit val executionContext = ExecutionContext.fromExecutorService(threadPool)
  val promisedFuture = p.future
  val producer = Future {
    val i = 1
    println(s"Start producer $i - ThreadId=${Thread.currentThread().getId}")
    Thread.sleep(10000)
    p success i
    println(s"End producer $i - ThreadId=${Thread.currentThread().getId}")
  }(executionContext)
  val consumer = promisedFuture onSuccess {
    case r => println(s"End consumer with value $r - ThreadId=${Thread.currentThread().getId}")
  }
  private val threadPool = Executors.newFixedThreadPool(10)


  Await.result(producer, WaitTimes.time)
  threadPool.shutdown()
}
