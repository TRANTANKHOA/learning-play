package datacentral.data.utils.logging

import datacentral.data.utils.extensions.NameScore

case class Timer() {

  private var startTime = System.currentTimeMillis()
  private var lastTime: Long = startTime
  private var lapSequence: Seq[Lap] = Seq.empty

  def addLap(marker: String): Unit = {
    val thistime = System.currentTimeMillis()
    lapSequence ++= Seq(Lap(thistime - lastTime, thistime - startTime, marker))
    lastTime = thistime
  }

  def getFinalLap: NameScore = lapSequence.lastOption match {
    case Some(Lap(_, lap, mark)) => NameScore(mark, lap)
    case None => NameScore()
  }

  override def toString: String = lapSequence.mkString(" | ")

  case class Lap(duration: Long, lap: Long, marker: String) {
    override def toString: String = "[" + marker + ", lap = " + lap.toString + ", dur. = " + duration.toString + "]"
  }

  def nonEmpty: Boolean = lapSequence.nonEmpty

  def reset(): Unit = {
    startTime = System.currentTimeMillis()
    lastTime = startTime
    lapSequence = Seq.empty[Lap]
  }
}