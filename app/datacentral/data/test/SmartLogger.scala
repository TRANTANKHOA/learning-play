package datacentral.data.test

import play.api.Logger

trait SmartLogger {

  // Logger definition
  val logger = Logger(this.getClass.getName)
  // Take note to print out in Exception message
  val notepad = NoteTaker(name = this.getClass.getName)

  def getCallerName: String = getCallers.apply(5)

  def tryAndLog[X, Y](x: X, f: X => Y, functionName: String): Y = {

    notepad.clear
    notepad.add(s"Run $functionName with this input: ${x.toString}")

    try {
      val result = f(x)
      notepad.add("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<FINAL RESULTS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n" + result.toString)
      notepad.addLap("Get result")
      notepad.toFile("/tmp/" + this.getClass.getName + "." + functionName)
      result
    } catch {
      case e: Throwable =>
        notepad.addLap(s"Get exception at $functionName")
        val (message, stackTrace) = getExceptionInfo(e)
        notepad.add(s"$message\n${if (!message.contains(stackTrace.last.toString)) stackTrace.mkString("\n") else ""}")
        throw new RuntimeException(notepad.toString)
    }
  }

  def getExceptionInfo(e: Throwable): (String, Seq[StackTraceElement]) = (e.getMessage, e.getStackTrace.filter(_.toString.contains("datacentral")))

  def getStackTraceFrom(e: Throwable): Nothing = {
    notepad.addLap(s"Get exception at $getNextCallerName")
    val (message, stackTrace) = getExceptionInfo(e)
    notepad.add(s"$message\n${stackTrace.mkString("\n")}")
    throw new RuntimeException(notepad.toString)
  }

  def getNextCallerName: String = getCallers.apply(7)

  // Do not use inside a Future
  def getCallers: Array[String] = Thread.currentThread.getStackTrace.map(_.getMethodName)
}
