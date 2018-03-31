package datacentral.data.utils.experimental.polimorphism.repetition

import datacentral.data.utils.experimental.polimorphism.DistributedDataset

case class Kilometers(value: BigDecimal) {
  def +(y: Kilometers): Kilometers = Kilometers(value + y.value)

  def -(y: Kilometers): Kilometers = Kilometers(value - y.value)

  def *(y: BigDecimal): Kilometers = Kilometers(value * y)

  def /(y: BigDecimal): Kilometers = Kilometers(value / y)
}

object Kilometers {
  val zero: Kilometers = Kilometers(BigDecimal(0))

  def mean(xs: DistributedDataset[Kilometers]): Kilometers =
    sum(xs) / xs.count

  def sum(xs: DistributedDataset[Kilometers]): Kilometers =
    xs.foldLeft(zero)(_ + _)
}