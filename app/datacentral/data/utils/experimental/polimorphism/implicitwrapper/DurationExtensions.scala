package datacentral.data.utils.experimental.polimorphism.implicitwrapper

import datacentral.data.utils.experimental.polimorphism.DistributedDataset
import datacentral.data.utils.experimental.polimorphism.implicitwrapper.Quantity.QuantitySequence
import org.joda.time.Duration

object DurationExtensions {

  implicit class Milliseconds(val duration: Duration)
    extends Quantity[Milliseconds](duration.getMillis) {

    override def unit(x: BigDecimal): Milliseconds = Milliseconds(Duration.millis(x.toLong))
  }

  implicit class DurationSeq(xs: DistributedDataset[Duration]) {

    val dataset: DistributedDataset[Milliseconds] =
      DistributedDataset(xs.seq.map(new Milliseconds(_)))

    /**
      * So this way of retrospective extension is essentially relying on wrappers, which
      * are clumsy but it allows more flexibility if you want some slightly different behaviors
      *
      * @param zero
      * @return
      */
    def mean(zero: Duration): Duration = dataset.mean(toMiliseconds(zero)).duration

    def toMiliseconds(zero: Duration): Milliseconds = new Milliseconds(zero)

    def sum(zero: Duration): Duration = dataset.sum(toMiliseconds(zero)).duration
  }

  /**
    * We spend quite a bit of trouble with the wrappers, but the end results are okay.
    */
  val durations: DistributedDataset[Duration] =
    DistributedDataset(Seq(
      Duration.standardMinutes(3),
      Duration.standardSeconds(17),
      Duration.standardSeconds(5),
      Duration.standardHours(1))
    )

  val meanDuration: Duration = durations.mean(Duration.ZERO)
}