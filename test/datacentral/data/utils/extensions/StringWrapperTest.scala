package datacentral.data.utils.extensions

import datacentral.data.utils.extensions.string.StringExtensions.StringWrapper
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class StringWrapperTest extends FunSuite with BeforeAndAfterEach {

  override def beforeEach() {}

  override def afterEach() {}

  test("testToJodaOption") {
    assert(
      "27 JAN 2017".toJodaOption("dd MMM yyyy").isDefined &&
      "57 JAN 2017".toJodaOption("dd MMM yyyy").isEmpty
    )
  }

  test("testToShortOption") {
    assert(
      "57".toShortOption.contains(57.toShort) &&
      "s".toShortOption.isEmpty
    )
  }

  test("testToByteOption") {
    assert(
      "57".toByteOption.contains(57.toByte) &&
      "s".toByteOption.isEmpty
    )
  }

  test("testToIntOption") {
    "57".toIntOption.contains(57) &&
    "s".toIntOption.isEmpty
  }

  test("testToLongOption") {
    "57".toLongOption.contains(57.toLong) &&
    "s".toLongOption.isEmpty
  }

  test("testToFloatOption") {
    "57".toFloatOption.contains(57.toFloat) &&
    "s".toFloatOption.isEmpty
  }

  test("testToDoubleOption") {
    "57".toDoubleOption.contains(57.toDouble) &&
    "s".toDoubleOption.isEmpty
  }

  test("testToBooleanOption") {
    "1".toBooleanOption.contains(true) &&
    "2".toBooleanOption.isEmpty
  }

  test("testToDecimalOption") {
    "57".toDecimalOption.contains(BigDecimal(57)) &&
    "s".toDecimalOption.isEmpty
  }

}
