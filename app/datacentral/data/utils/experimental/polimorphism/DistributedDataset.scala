package datacentral.data.utils.experimental.polimorphism

/**
  * This example was found in here:
  * https://speakerd.s3.amazonaws.com/presentations/36600d20b96f42bfb1aa7ffcd54708a3/slides_westheide.pdf
  *
  * @tparam A
  */
trait DistributedDataset[A] {
  val seq: Seq[A]

  def foldLeft[B](z: B)(op: (B, A) => B): B = ???

  def count: Int = ???
}

object DistributedDataset {
  def apply[A](xs: Seq[A]): DistributedDataset[A] = ???
}
