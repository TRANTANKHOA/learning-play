package scala4

import akka.actor.ActorSystem
import akka.pattern.after

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Future, _}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object TimeoutAkka extends App {

  lazy val f = Future {
    Thread.sleep(2000);
    "Done"
  }
  lazy val t = after(duration = 3 second, using = system.scheduler)(Future.failed(new TimeoutException("Future timed out!")))
  val system = ActorSystem("theSystem")
  val fWithTimeout = Future firstCompletedOf Seq(f, t)

  fWithTimeout.onComplete {
    case Success(x) => terminate(x)
    case Failure(error) => terminate(error.getMessage)
  }


  private def terminate(x: String) = {
    println(x)
    system.terminate()
  }

}
