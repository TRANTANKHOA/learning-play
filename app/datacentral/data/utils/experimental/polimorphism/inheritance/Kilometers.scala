package datacentral.data.utils.experimental.polimorphism.inheritance

case class Kilometers(value: BigDecimal) extends Quantity[Kilometers] {
  override def unit(x: BigDecimal): Kilometers = Kilometers(x)
}
