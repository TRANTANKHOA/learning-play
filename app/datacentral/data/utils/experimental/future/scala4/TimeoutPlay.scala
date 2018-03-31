package scala4

import play.api.libs.concurrent.Promise

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.language.postfixOps


object TimeoutPlay extends App {


  private def get(): Future[Option[Boolean]] = {
    val timeoutFuture = Promise.timeout(None, Duration("1s"))
    val mayBeHaveData = Future {
      // do something
      Some(true)
    }

    // if timeout occurred then None will be result of method
    Future.firstCompletedOf(List(mayBeHaveData, timeoutFuture))
  }

  import scala.concurrent.duration._


  println(Await.result(get(), 3 seconds))

}
