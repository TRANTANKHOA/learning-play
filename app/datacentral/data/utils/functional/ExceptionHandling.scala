package datacentral.data.utils.functional

import datacentral.data.utils.extensions.TryExtensions._

import scala.util.control.Exception.catching
import scala.util.{Failure, Success, Try}

trait ExceptionHandling {
  // out gets evaluated each call, just like a function, see by-name parameter
  def getSomeOrNone[OUT](out: => OUT): Option[OUT] = catching(classOf[Throwable]).opt(out)

  // tryTwice
  def tryTwiceOrPrintStackTrace[OUT](f: => OUT): Try[OUT] = Try {
    f
  } recover {
                                                              case _: Throwable => Try {
                                                                f
                                                              } // try again
                                                            } match {
    case Failure(e: Throwable) =>
      InformativeRuntimeError(e).printStackTrace()
      Failure(e)
    case o: Success[OUT@unchecked] => o
  }

  // if function may fail, and when it does you are sure of why or you don't care, you can wrap it it an Option as this.
  class FunctionOption[IN, OUT](function: IN => OUT) {
    def apply(in: IN): Option[OUT] = getSomeOrNone(function(in))
  }

  object FunctionOption {
    def apply[IN, OUT](function: IN => OUT): FunctionOption[IN, OUT] = new FunctionOption(function)
  }

}
