package datacentral.data.transform.time

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import scala.util.matching.Regex

case class Date(dateString: String = "") {

  // Regex expressions
  val isYearFirst: Regex = "(\\d{4})-(\\d\\d?)-(\\d\\d?)".r
  val isDayFirst: Regex = "(\\d\\d?)-(\\d\\d?)-(\\d{4})".r
  val stringLength: Int = dateString.length

  def replaceNonAlphaNumeric: Date = Date(
    this.dateString.trim.replaceAll("[^a-z&&[^0-9]]", "-")
  )

  // Local date builder
  def toLocalDateWith(format: DateTimeFormatter): LocalDate = {
    LocalDate.parse(dateString, format)
  }

  def fromString(string: String): Date = Date(string)

  def getStandardDateString: String = {
    val output = isYearFirst.findFirstIn(dateString) match {
      case Some(isYearFirst(year, month, day)) => Array(year, month, day)
      case None => isDayFirst.findFirstIn(dateString) match {
        case Some(isDayFirst(day, month, year)) => Array(year, month, day)
        case None ⇒ Array("")
      }
    }

    if (output.length == 3) {
      // Reorder month vs day
      if (output(1).toInt > 12) {
        val x = output(2)
        output(2) = output(1)
        output(1) = x
      }
      // Filling '0' in month,day
      for (i ← 1 until 3 if output(i).length < 2) {
        output(i) = "0" + output(i)
      }
    }

    output.mkString("-")
  }
}
