package datacentral.data.utils.extensions

import datacentral.data.utils.functional.SmartFunctions
import datacentral.data.utils.reflection.Functions.ReflectiveObject
import org.spark_project.jetty.util.MultiException

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object TryExtensions {

  implicit class ListTry[A](list: Traversable[Try[A]]) {

    def sequence: Try[Traversable[A]] = list.collect { case Failure(exception) => exception } match {
      case Nil => Success(list.map(_.get))
      case seq =>
        val multiException = new MultiException
        seq.foreach(multiException.add)
        Failure(multiException)
    }
  }

  implicit class ThrowableHelper(e: Throwable) {

    def getImprovedMessages: String = getAllErrors
      .map(each =>
        s"${each.getCause.getClassName}: " +
        s"${each.getMessage}\n\tat " +
        s"${each.getMyStackTrace.mkString("\n\tat ")}")
      .distinct.mkString("\n")

    def getMyStackTrace: Seq[String] = e.getStackTrace.map(_.toString).filter(_.contains("datacentral"))

    def getAllErrors: Stream[Throwable] = SmartFunctions.lazyRecursiveWith[Throwable, Seq[Throwable], Seq](
      remainder = Seq(e),
      split = (remainder: Seq[Throwable]) => {
        val childExceptions: Seq[Throwable] = remainder
          .filter(_.isInstanceOf[MultiException])
          .flatMap(_.asInstanceOf[MultiException].getThrowables.asScala)
        val out = (remainder ++ childExceptions).filterNot(_.isInstanceOf[MultiException])
        val nextRemainder = childExceptions.filter(_.isInstanceOf[MultiException])
        (nextRemainder, out)
      }).flatten
  }

  case class InformativeRuntimeError(e: Throwable) extends RuntimeException(e.getImprovedMessages, e)

}