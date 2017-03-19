package datacentral.data.transform.string


case class SmartStrings(override val seq: Seq[String] = Seq.empty[String]) extends StringSequence(seq: Seq[String]) {

  override type Self = SmartStrings

  def fromSeq(seq: Seq[String]): SmartStrings = SmartStrings(seq)

}