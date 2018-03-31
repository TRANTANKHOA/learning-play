package datacentral.data.utils.experimental.polimorphism.inheritance

import datacentral.data.utils.experimental.polimorphism.DistributedDataset
import org.joda.time.Duration

/* Adapter pattern is used when we want to retrospectively provide extension to some prior type, e.g. `Duration` */
case class Milliseconds(underlying: Duration) extends
  Quantity[Milliseconds] {
  override def value: BigDecimal = underlying.getMillis

  override def unit(x: BigDecimal): Milliseconds =
    Milliseconds(Duration.millis(x.toLong))
}

object Milliseconds {

  /**
    * The criticism here is that you lose the type information of `Duration`, also the instantiation is more verbose
    */
  val durations: DistributedDataset[Milliseconds] =
    DistributedDataset(Seq(
      Milliseconds(Duration.standardMinutes(3)),
      Milliseconds(Duration.standardSeconds(17)),
      Milliseconds(Duration.standardSeconds(5)),
      Milliseconds(Duration.standardHours(1))))
  val meanDuration = Quantity.mean(durations, Milliseconds(Duration.ZERO))
}

