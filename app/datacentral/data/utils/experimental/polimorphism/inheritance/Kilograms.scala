package datacentral.data.utils.experimental.polimorphism.inheritance

case class Kilograms(value: BigDecimal) extends Quantity[Kilograms] {
  override def unit(x: BigDecimal): Kilograms = Kilograms(x)
}
