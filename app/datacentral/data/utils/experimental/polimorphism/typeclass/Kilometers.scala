package datacentral.data.utils.experimental.polimorphism.typeclass

/**
  * When typed-class pattern is used, your classes do not need to be aware of the extensions available to it. This is
  * because no inheritance mechanism is used. That's why typed-class pattern can work for any, even unforeseen types
  *
  * @param value
  */
case class Kilometers(value: BigDecimal)
