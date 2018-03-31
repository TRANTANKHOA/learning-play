package datacentral.data.utils.experimental.polimorphism.inheritance

import datacentral.data.utils.experimental.polimorphism.DistributedDataset

trait Quantity[A <: Quantity[A]] {
  def value: BigDecimal

  def unit(x: BigDecimal): A

  def +(y: A): A = unit(value + y.value)

  def -(y: A): A = unit(value - y.value)

  def *(y: BigDecimal): A = unit(value * y)

  def /(y: BigDecimal): A = unit(value / y)
}

object Quantity {
  def mean[A <: Quantity[A]](xs: DistributedDataset[A], zero: A): A =
    sum(xs, zero) / xs.count

  def sum[A <: Quantity[A]](xs: DistributedDataset[A], zero: A): A =
    xs.foldLeft(zero)(_ + _)
}

