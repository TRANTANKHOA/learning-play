package datacentral.data.transform.time

case class Timer() {
  private var startTime = System.currentTimeMillis()
  private var lastTime: Long = startTime
  private var lapSequence: Seq[Lap] = Seq.empty

  def addLap(marker: String): Unit = {
    val thistime = System.currentTimeMillis()
    lapSequence ++= Seq(Lap(thistime - lastTime, thistime - startTime, marker))
    lastTime = thistime
  }

  override def toString: String = lapSequence.mkString(" | ")

  def nonEmpty: Boolean = lapSequence.nonEmpty

  def reset(): Unit = {
    startTime = System.currentTimeMillis()
    lastTime = startTime
    lapSequence = Seq.empty[Lap]
  }

  case class Lap(duration: Long, lap: Long, marker: String) {
    override def toString: String = "[" + marker + ", lap = " + lap.toString + ", dur. = " + duration.toString + "]"
  }

}