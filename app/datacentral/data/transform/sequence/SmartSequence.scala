package datacentral.data.transform.sequence

trait SmartSequence {
  def getFirstAndLastElementsFrom[T](seq: Seq[T]): Seq[T] = if (seq.length > 1) {
    Seq(seq.head, seq.last)
  } else {
    seq
  }
}
