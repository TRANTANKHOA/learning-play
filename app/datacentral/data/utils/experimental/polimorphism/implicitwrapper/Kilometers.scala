package datacentral.data.utils.experimental.polimorphism.implicitwrapper

case class Kilometers(override val value: BigDecimal) extends Quantity[Kilometers](value) {

  override def unit(x: BigDecimal): Kilometers = Kilometers(x)
}