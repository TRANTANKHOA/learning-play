package datacentral.data.utils.experimental.polimorphism.implicitwrapper

case class Kilograms(override val value: BigDecimal) extends Quantity[Kilograms](value) {

  override def unit(x: BigDecimal): Kilograms = Kilograms(x)
}
