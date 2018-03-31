package datacentral.data.utils.extensions

import java.time.LocalDate

import datacentral.data.utils.extensions.datetime.LocalDateExtensions.LocalDateWrapper
import org.scalatest.FunSuite

class LocalDateWrapperTest extends FunSuite {

  val testDate = LocalDate.of(2017, 5, 17)

  test("testEndOfWeek") {
    assert(testDate.endOfWeek == LocalDate.of(2017, 5, 21)) // Sunday
  }

  test("testStartOfWeek") {
    assert(testDate.startOfWeek == LocalDate.of(2017, 5, 15)) // Monday
  }
}