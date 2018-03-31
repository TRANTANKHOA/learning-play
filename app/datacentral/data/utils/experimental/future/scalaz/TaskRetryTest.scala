package scalaz

import java.util.concurrent.{ExecutorService, Executors}

import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz.concurrent.Task

object TaskRetryTest extends App {

  val t1: String = Task(randomException)(threadPool)
    .retry(Seq(2 seconds), throwableToBoolean)
    .map(v => printAndReturn(v + "bbb"))
    .unsafePerformSync
  private val threadPool: ExecutorService = Executors.newFixedThreadPool(10)
  private val throwableToBoolean: Throwable => Boolean = {
    case e: ArithmeticException => {
      println(e)
      true
    }
  }
  var count = 0


  def randomException: String = {
    count += 1
    count match {
      case 1 => throw new IllegalArgumentException()
      case 2 => throw new ArithmeticException()
      case 3 => printAndReturn("aaa")
    }
  }

  println(s"Start - ThreadId=${Thread.currentThread().getId}")

  def printAndReturn(s: String): String = {
    println(s"$s - ThreadId=${Thread.currentThread().getId}")
    s
  }

  println(t1)
  threadPool.shutdown()
}