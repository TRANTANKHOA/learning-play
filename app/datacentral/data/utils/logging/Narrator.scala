package datacentral.data.utils.logging

import java.time.{LocalDateTime, ZoneId}

import datacentral.data.utils.reflection.Functions.ReflectiveObject

/**
  * These are in-memory facilities used to note down the narrative of the execution path. It is flexible, quick and
  * dirty and mutable
  */
trait Narrator extends Logging {

  // Take note to print out in Exception message
  val notepad = NoteTaker(name = this.getClass.getName)

  implicit class ThrowableHelper(e: Throwable) {
    def saveTimeOutReport(caller: String): Unit = {
      e.saveToNotePad(caller)
      notepad.toFile(logsFolder + "timeout/" + this.getClassName + "." + caller)
    }

    def saveToNotepadThenThrow(caller: String = ""): Nothing = {
      e.saveToNotePad(caller)
      if (caller.nonEmpty) notepad.toFile(logsFolder + "errors/" + caller)
      throw new RuntimeException(notepad.toString)
    }

    def saveToNotePad(caller: String): Unit = {
      notepad.addLap(s"Get ${e.getClassName} at $caller")
      val message: String = e.getMessage
      val stackTrace: Seq[StackTraceElement] = e.getDataCentralStackTrace
      notepad.add(s"Encountered $message\n" + s"at system time = ${LocalDateTime.now(ZoneId.of("Australia/Sydney")).toString}\n" + s"${if (notepad.hasStackTrace) "" else stackTrace.mkString("\n")}")
      notepad.hasStackTrace = true
    }

    def getDataCentralStackTrace: Seq[StackTraceElement] = {
      e.getStackTrace.filter(_.toString.contains("datacentral")).toSeq match {
        case Nil => val allStack = e.getStackTrace.toSeq
          println(s"allStack = ${allStack.mkString("\n")}")
          allStack
        case someStacks => someStacks
      }
    }
  }

  def getCallerName: String = getCallers(0).getOrElse("scalaNativeCaller")

  // Do not use inside a Future
  def getCallers: (Int) => Option[String] = Thread.currentThread.getStackTrace
    .filter(_.getClassName.contains("datacentral"))
    .filter(!_.getClassName.contains("ThrowableHelper"))
    .filter(item => !(Narrator.allMethods :+ "apply").contains(item.getMethodName))
    .map(_.toString).lift

  def getNextCallerName: String = getCallers(1).getOrElse("scalaNativeCaller")

  def tryAndLog[X, Y](x: X, f: X => Y, functionName: String): Y = {

    notepad.clear
    notepad.add(s"Run $functionName with this input: $x")

    try {
      val result = f(x)
      notepad.addLap("Get result")
      notepad.addSection("FINAL RESULTS")
      notepad.add(result.toString)
      notepad.toFile(logsFolder + functionName)
      result
    } catch {
      case e: Throwable => e.saveToNotepadThenThrow(caller = functionName)
    }
  }
}

object Narrator extends Narrator {
  val allMethods: Array[String] = this.getClass.getMethods.map(_.getName)
}
