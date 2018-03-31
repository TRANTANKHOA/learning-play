package datacentral.data.utils.extensions

case class NameScore(name: String = "", score: Double = 0) extends Ordered[NameScore] {

  def isNull: Boolean = name.length + score == 0

  def compare(that: NameScore): Int = this.score compare that.score

  def toMap: (String, Double) = name -> score

  override def toString: String = s"[$name, $score]"
}
