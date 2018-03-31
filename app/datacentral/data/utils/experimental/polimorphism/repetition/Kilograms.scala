package datacentral.data.utils.experimental.polimorphism.repetition

import datacentral.data.utils.experimental.polimorphism.DistributedDataset

case class Kilograms(value: BigDecimal) {
  def +(y: Kilograms): Kilograms = Kilograms(value + y.value)

  def -(y: Kilograms): Kilograms = Kilograms(value - y.value)

  def *(y: BigDecimal): Kilograms = Kilograms(value * y)

  def /(y: BigDecimal): Kilograms = Kilograms(value / y)
}

object Kilograms {
  val zero: Kilograms = Kilograms(BigDecimal(0))

  def mean(xs: DistributedDataset[Kilograms]): Kilograms =
    sum(xs) / xs.count

  def sum(xs: DistributedDataset[Kilograms]): Kilograms =
    xs.foldLeft(zero)(_ + _)
}