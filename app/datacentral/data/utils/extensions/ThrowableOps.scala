package datacentral.data
package utils.extensions

import datacentral.data.utils.functional.RecursiveMethods
import org.spark_project.jetty.util.MultiException

import scala.collection.JavaConverters._

final class ThrowableOps(e: Throwable) extends RecursiveMethods {

  def getImprovedMessages: String = getAllErrors
    .map(each =>
      s"${each.getClass.getName}: " +
      s"${each.getMessage}\n\tat " +
      s"${each.getStackTrace("datacentral").mkString("\n\tat ")}")
    .distinct.mkString("\n")

  def getStackTrace(packageName: String): Seq[String] = e.getStackTrace.map(_.toString).filter(_.contains(packageName))

  def getAllErrors: Stream[Throwable] = lazyRecursiveWith[Throwable, Seq[Throwable], Seq](
    remainder = Seq(e),
    split = (remainder: Seq[Throwable]) => {
      val childExceptions: Seq[Throwable] = remainder.flatMap {
        case me: MultiException => me.getThrowables.asScala
        case _ => Nil
      }
      val out = (remainder ++ childExceptions).filterNot(_.isInstanceOf[MultiException])
      val nextRemainder = childExceptions.filter(_.isInstanceOf[MultiException])
      (nextRemainder, out)
    }).flatten
}

case class PipelineRuntimeError(e: Throwable) extends RuntimeException(e.getImprovedMessages, e)


