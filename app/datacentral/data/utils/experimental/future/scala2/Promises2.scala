package scala2

import java.util.concurrent.Executors

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala1.WaitTimes

/**
  * More different Threads. One consumer does not block the other
  */
object Promises2 extends App {
  val p = Promise[Int]()
  private implicit val executionContext = ExecutionContext.fromExecutorService(threadPool)
  val promisedFuture = p.future
  private implicit val executionContext2 = ExecutionContext.fromExecutorService(threadPool2)
  val producer = Future {
    val i = 1
    println(s"Start producer $i - ThreadId=${Thread.currentThread().getId}")
    Thread.sleep(1000)
    p success i
    println(s"End producer $i - ThreadId=${Thread.currentThread().getId}")
  }(executionContext)
  val consumer = Future {
    println(s"First consumer - ThreadId=${Thread.currentThread().getId}")
    promisedFuture.onSuccess({
      case r => println(s"End consumer with value $r - ThreadId=${Thread.currentThread().getId}")
    })(executionContext2)
  }(executionContext2)
  val consumer2 = Future {
    println(s"Other consumer - ThreadId=${Thread.currentThread().getId}")
  }(executionContext2)
  private val threadPool = Executors.newFixedThreadPool(1)
  private val threadPool2 = Executors.newFixedThreadPool(1)


  Await.result(producer, WaitTimes.time)
  Await.result(consumer, WaitTimes.time)
  Await.result(consumer2, WaitTimes.time)
  threadPool.shutdown()
  threadPool2.shutdown()
}
