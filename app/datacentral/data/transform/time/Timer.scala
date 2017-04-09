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

  def getFinalLap: NameScore = lapSequence.lastOption match {
    case Some(Lap(dur, lap, mark)) => NameScore(mark, lap)
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

case class NameScore(name: String = "", score: Double = 0) extends Ordered[NameScore] {

  def isNull: Boolean = name.length + score == 0

  def compare(that: NameScore): Int = this.score compare that.score

  def toMap: (String, Double) = name -> score

  override def toString: String = s"[$name, $score]"
}