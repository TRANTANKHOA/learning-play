package datacentral.data.testUtils

import java.time.{LocalDateTime, ZoneId}

import play.api.Logger

trait SmartLogger {
  // Logger definition
  val logger: Logger = Logger(this.getClass.getName)
  val logsFolder = "/tmp/logs/datacentral/"
  // Take note to print out in Exception message
  val notepad = NoteTaker(name = this.getClass.getName)

  implicit class SmartObject[T](obj: T) {
    def getClassName: String = obj.getClass.getName
  }

  implicit class ThrowableHelper(e: Throwable) {
    def getStackTraces: Array[StackTraceElement] = e.getStackTrace.filter(_.toString.contains("datacentral")) match {
      case Array.empty  => e.getStackTrace
      case some => some
    }

    def saveTimeOutReport(implicit currentNotes: NoteTaker) {
      notepad.copyFrom(currentNotes)
      e.saveToNotePad
      notepad.toFile(logsFolder + "timeout/" + this.getClassName + "." + getNextCallerName)
    }

    def saveToNotepadThenThrow(file: String = ""): Nothing = e match {
      case e: Throwable =>
        e.saveToNotePad
        if (file.nonEmpty) notepad.toFile(logsFolder + "errors/" + file)
        throw new RuntimeException(notepad.toString)
    }

    def saveToNotePad {
      notepad.addLap(s"Get ${e.getClassName} at $getNextCallerName")
      val message: String = e.getMessage
      val stackTrace: Array[StackTraceElement] = e.getStackTraces
      notepad.add(
        s"Encountered $message\n" +
          s"at system time = ${LocalDateTime.now(ZoneId.of("Australia/Sydney")).toString}\n" +
          s"${if (!message.contains(stackTrace.last.toString)) stackTrace.mkString("\n") else ""}")
    }
  }

  // Do not use inside a Future
  def getCallers: Array[String] = Thread.currentThread
    .getStackTrace
    .filter(_.toString.contains("datacentral"))
    .map(_.getMethodName)

  def getCallerName: String = getCallers.apply(4)

  def getNextCallerName: String = getCallers.apply(5)

  def tryAndLog[X, Y](x: X, f: X => Y, functionName: String): Y = {

    notepad.clear
    notepad.add(s"Run $functionName with this input: $x")

    try {
      val result = f(x)
      notepad.addLap("Get result")
      notepad.addSection("FINAL RESULTS")
      notepad.add(result.toString)
      notepad.toFile(logsFolder + this.getClassName + "." + functionName)
      result
    } catch {
      case e: Throwable => e.saveToNotepadThenThrow(file = this.getClassName + "." + functionName)
    }
  }
}
