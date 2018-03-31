package datacentral.data.utils.experimental.polimorphism.typeclass

/**
  * Typed-class pattern put all the extensibility-related code into this one file. Some people like it a lot.
  *
  * Also notice that in this pattern, all the functionality is separated from the actual data. Generally, in functional
  * programming, there is no grouping between data and its functionality. You define all functionality here and also
  * provide "implicit evidence" that these functionality are applicable to the data you want to use.
  *
  * @tparam A
  */
trait Quantity[A] {
  def value(x: A): BigDecimal

  def unit(x: BigDecimal): A

  def zero: A = unit(BigDecimal(0))

  def plus(x: A, y: A): A = unit(value(x) + value(y))

  def minus(x: A, y: A): A = unit(value(x) - value(y))

  def times(x: A, y: BigDecimal): A = unit(value(x) * y)

  def div(x: A, y: BigDecimal): A = unit(value(x) / y)
}

import datacentral.data.utils.experimental.polimorphism.DistributedDataset
import org.joda.time.Duration

object Quantity {

  /**
    * Provide familiar operators through implicit conversions
    *
    * @param a
    * @tparam A
    */
  implicit class QuantityOps[A: Quantity](a: A) {
    val quantity: Quantity[A] = implicitly[Quantity[A]]

    def +(a2: A): A = quantity.plus(a, a2)

    def -(a2: A): A = quantity.minus(a, a2)

    def *(y: BigDecimal): A = quantity.times(a, y)

    def /(y: BigDecimal): A = quantity.div(a, y)
  }

  /**
    * Note that the implementation of `sum` and `mean` is fixed, regardless of the data type `A`.
    *
    * In other words, you don't need to override them. Or, actually you can't override them. You can't use one and
    * override the other in one of your class. This the the cost of subscribing yourself to functional programming.
    *
    * @param xs
    * @tparam A
    * @return
    */
  def mean[A: Quantity](xs: DistributedDataset[A]): A = {
    val quantity = implicitly[Quantity[A]]
    quantity.div(sum(xs), xs.count)
  }

  /**
    * @param xs
    * @tparam A has a view-bound to `Quantity`, which allows us to derive the implicit instance `quantity`
    * @return
    */
  def sum[A: Quantity](xs: DistributedDataset[A]): A = {
    val quantity = implicitly[Quantity[A]]
    xs.foldLeft(quantity.zero)(quantity.plus)
  }

  /**
    * This will allow us to calculate `sum` and `mean` over e.g. `Kilograms` and `Kilometers`
    */
  implicit lazy val kilogramQuantity: Quantity[Kilograms] = Quantity.evidence(Kilograms)(_.value)
  implicit lazy val kilometerQuantity: Quantity[Kilometers] = Quantity.evidence(Kilometers)(_.value)

  /**
    * This will also allow us to do so on `Option[Kilograms]` and etc.
    *
    * @tparam A
    * @return
    */
  implicit final def quantityOption[A: Quantity]: Quantity[Option[A]] =
    new Quantity[Option[A]] {
      val e: Quantity[A] = implicitly[Quantity[A]]

      override def value(x: Option[A]): BigDecimal = e.value(x match {
        case Some(a) => a
        case None => e.zero
      })

      override def unit(x: BigDecimal): Option[A] = Some(e.unit(x))
    }

  /**
    * This is how we provide extensions to predefined classes/types e.g. `Duration`
    */
  implicit lazy val durationQuantity: Quantity[Duration] = new Quantity[Duration] {
    override val zero: Duration = Duration.ZERO

    override def value(x: Duration): BigDecimal = x.getMillis

    override def unit(x: BigDecimal): Duration = Duration.millis(x.toLong)

    // you can override these but it's not strictly necessary
    //    override def plus(x: Duration, y: Duration): Duration = x.plus(y)
    //
    //    override def minus(x: Duration, y: Duration): Duration = x.minus(y)
  }

  // now it looks nicer when used with Duration. Hence the selling point here for predefined types
  val durations: DistributedDataset[Duration] =
    DistributedDataset(Seq(
      Duration.standardMinutes(3),
      Duration.standardSeconds(17),
      Duration.standardSeconds(5),
      Duration.standardHours(1)))
  val meanDuration: Duration = Quantity.mean(durations)
  val sumDuration: Duration = Quantity.sum(durations)

  /**
    * Template for creating implicit evidences
    *
    * @param unitF
    * @param valueF
    * @tparam A
    * @return
    */
  def evidence[A](unitF: BigDecimal => A)(valueF: A => BigDecimal): Quantity[A] = new Quantity[A] {
    override def value(x: A): BigDecimal = valueF(x)

    override def unit(x: BigDecimal): A = unitF(x)
  }
}