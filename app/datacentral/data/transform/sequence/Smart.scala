package datacentral.data.transform.sequence

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import datacentral.data.transform.time.{DateRange, TimeConcept}

import scala.util.matching.Regex

trait Smart {

  implicit class Sequence[X](xs: Traversable[X]) {
    def getFirstAndLastElementsFrom: Traversable[X] = if (xs.size > 1) Seq(xs.head, xs.last) else xs


    def crossTupple[Y](ys: Traversable[Y]): Traversable[(X, Y)] = for {x <- xs; y <- ys} yield (x, y)

    def cross(ys: Traversable[X]): Traversable[List[X]] = for {x <- xs; y <- ys} yield x :: y :: Nil
  }

  implicit class DoubleSequence[X](xss: Traversable[Traversable[X]]) {
    def recursiveCross: Traversable[List[X]] = {
      require(xss.size < 20)
      xss match {
        case Nil => List(Nil)
        case head :: tail => for (xh <- head; xt <- tail.recursiveCross) yield xh :: xt
      }
    }
  }

  implicit class StringSequence(seq: Seq[String])(implicit val valueSet: Set[String] = Set.empty[String]) {

    val listNonEmptyString: Seq[String] = seq.map(_.trim).filter(_.nonEmpty)

    val length: Int = listNonEmptyString.length

    // sequence builders

    def toSeq: Seq[String] = listNonEmptyString

    def distinct: Seq[String] = listNonEmptyString.distinct

    def mkString(separator: String = ", "): String = listNonEmptyString.mkString(separator)

    override def toString: String = listNonEmptyString.toString + " from types: " + valueSet.toString

    def getTypeNumberAsSeq(number: Int): Seq[String] = Seq(getTypeNumber(number))

    // get elements
    def getTypeNumber(number: Int): String = if (number < valueSet.size) valueSet.toSeq(number max 0) else ""

    def getValueNumberAsSeq(number: Int): Seq[String] = Seq(getValueNumber(number))

    def getValueNumber(number: Int): String = if (number < listNonEmptyString.length) listNonEmptyString(number max 0) else ""

    def getFirstType: String = valueSet.intersect(this.toSet).toSeq.getFirstString

    def toSet: Set[String] = listNonEmptyString.distinct.toSet

    def getFirstString: String = listNonEmptyString.headOption.getOrElse("")

    // boolean conditions
    def nonEmpty: Boolean = listNonEmptyString.nonEmpty

    def isEmpty: Boolean = listNonEmptyString.isEmpty

    def containsAnyOf(testSeq: Seq[String]): Boolean = testSeq.exists(item => this.contains(item))

    def contains(item: String): Boolean = listNonEmptyString.contains(item.trim)

    // editing
    def replaceNonAlphaNumericWith(s: String): Seq[String] = listNonEmptyString.map(_.replaceNonAlphaNumericWith(s))

    def addSpacesToBothEnds: Seq[String] = listNonEmptyString.map(_.addSpacesToBothEnds)

    def removeMultipleSpaces: Seq[String] = listNonEmptyString.map(_.removeMultipleSpaces)

    def toLowerCase: Seq[String] = listNonEmptyString.map(_.toLowerCase)

    def toUpperCase: Seq[String] = listNonEmptyString.map(_.toUpperCase)
  }

  implicit class RichString(string: String) {
    def replaceNonAlphaNumericWith(s: String): String = string.trim.replaceAll("[^a-z&&[^0-9]]", s)

    def removeMultipleSpaces: String = string.replaceAll("[\\s]+", " ")

    def addSpacesToBothEnds: String = " " + string.trim + " "
  }

  implicit class Date(dateString: String) {
    // Regex expressions
    private val isYearFirst: Regex = "(\\d{4})-(\\d\\d?)-(\\d\\d?)".r
    private val isDayFirst: Regex = "(\\d\\d?)-(\\d\\d?)-(\\d{4})".r

    // Local date builder
    def toLocalDateWith(format: DateTimeFormatter): LocalDate = {
      LocalDate.parse(dateString, format)
    }

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

  implicit class DateSequence(seq: Seq[String]) extends TimeConcept {

    implicit val localDateOrdering: Ordering[LocalDate] = Ordering.by(_.toEpochDay)

    def getAllTimeTypes: Seq[String] = seq.listNonEmptyString.map(
      date => try {
        getDateTypeByStringLength(date)
      } catch {
        case _: Throwable => ""
      }
    )

    def firstTimeType: String = getAllTimeTypes.getFirstString

    def sortedYears: Seq[LocalDate] = seq
      .filter(date => date.length == 4)
      .map(year => getFirstDayOfYear(year.toInt))
      .sorted

    def sortedMonths: Seq[LocalDate] = seq
      .filter(date => date.length == 8)
      .map(month => getFirstDayOfMonth(month))
      .sorted

    def sortedDays: Seq[LocalDate] = (seq
      .filter(date => date.length == 10)
      .map(date => LocalDate.parse(date, yyyyMMdd)) ++
      seq.filter(date => date.length == 11)
        .map(date => LocalDate.parse(date, ddMMMyyyy)))
      .sorted


    def sortedWeeks: Seq[LocalDate] = sortedDays.map(getFirstDateOfWeekOf)

    def firstAndLastDates: Seq[LocalDate] =
      if (firstTimeType == dateTypesList.day)
        Seq(
          sortedDays.head,
          sortedDays.last
        )
      else if (firstTimeType == dateTypesList.month)
        Seq(
          sortedMonths.head,
          sortedMonths.last.plusDays(-1).plusMonths(1)
        )
      else if (firstTimeType == dateTypesList.year)
        Seq(
          sortedYears.head,
          sortedYears.last.plusDays(-1).plusYears(1)
        )
      else
        Seq.empty[LocalDate]

    def dateRange: Option[DateRange] = if (firstAndLastDates.nonEmpty)
      Some(DateRange(firstAndLastDates.head, firstAndLastDates.last))
    else
      None

    def duration: Long = dateRange match {
      case Some(range) => range.duration
      case None => 0
    }

    override def toString: String = toStringSeq.toString

    def toStringSeq: Seq[String] = seq.listNonEmptyString

    def getDateFormatter: DateTimeFormatter =
      if (firstTimeType == dateTypesList.month)
        mmm_yyyy
      else if (firstTimeType == dateTypesList.year)
        yyyy
      else
        yyyyMMdd

    def getSortedDatesAsString: Seq[String] = {
      val dates = if (firstTimeType == dateTypesList.day)
        firstAndLastDates.map(_.format(yyyyMMdd))
      else if (firstTimeType == dateTypesList.month)
        firstAndLastDates.map(_.format(mmm_yyyy))
      else if (firstTimeType == dateTypesList.year)
        firstAndLastDates.map(_.format(yyyy))
      else
        Seq.empty[String]

      dates.distinct
    }
  }

}