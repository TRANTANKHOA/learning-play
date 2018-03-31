package datacentral.data.utils.experimental.polimorphism.implicitwrapper

import datacentral.data.utils.experimental.polimorphism.DistributedDataset

/**
  * We need `Quantity[A]` to be a separated type from, says, `Quantity[B]` but they will share some functionality from
  * `Quantity[_]`
  *
  * @param value
  * @tparam A
  */
abstract class Quantity[A <: Quantity[A]](val value: BigDecimal) {

  /**
    * We need this to instantiate an `a: A` because we can't access its default constructor
    * from here.
    *
    * @param x
    * @return
    */
  def unit(x: BigDecimal): A

  def +(y: A): A = unit(value + y.value)

  def -(y: A): A = unit(value - y.value)

  def *(y: BigDecimal): A = unit(value * y)

  def /(y: BigDecimal): A = unit(value / y)
}

object Quantity {

  implicit class QuantitySequence[A <: Quantity[A]](xs: DistributedDataset[A]) {

    def mean(zero: A): A = sum(zero) / xs.count

    def sum(zero: A): A = xs.foldLeft(zero)(_ + _)
  }

}