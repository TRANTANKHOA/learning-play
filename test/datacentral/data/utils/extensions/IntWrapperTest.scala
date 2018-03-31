package datacentral.data.utils.extensions

import datacentral.data.utils.extensions.IntExtensions.IntWrapper
import org.joda.time.LocalDate
import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.concurrent.duration._

class IntWrapperTest extends FunSuite with BeforeAndAfterEach {

  override def beforeEach() {

  }

  override def afterEach() {

  }

  test("testWeeks") {
    assert(1.weeks == 7.days)
  }

  test("testIsEven") {
    assert((-2).isEven)
  }

  test("testToLocalDateOption") {
    assert((-1).toLocalDateOption.isEmpty)
  }

  test("testToLocalDate") {
    assert(20170527.toLocalDate == new LocalDate(2017, 5, 27))
  }

  test("testToBoolean") {
    assert(1.toBoolean)
  }

  test("testIsOdd") {
    assert(1.isOdd)
  }

  test("testToBooleanOption") {
    assert(3.toBooleanOption.isEmpty)
  }

  test("testIfWithinRange") {
    assert(
      5.ifWithinRange(Range(1, 10)).contains(5) && (-5).ifWithinRange(Range(1, 10)).isEmpty
    )
  }
}
