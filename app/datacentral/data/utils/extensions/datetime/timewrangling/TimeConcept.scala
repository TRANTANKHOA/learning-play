package datacentral.data.utils.extensions.datetime.timewrangling

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import java.time.{LocalDate, LocalDateTime, ZoneId}

import datacentral.data.utils.extensions.sequences.SequenceExtensions.{Date, RichString}

import scala.util.Random

trait TimeConcept {
  // default formats
  val yyyyMMdd: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  val ddMMMyyyy: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

  val mmm_yyyy: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

  val yyyy: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy")
  // Time ranges
  val years: Seq[String] = (2012 to 2016).map(_.toString)
  val months: Seq[String] = Seq("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
  val mapMonth = Map(
    "jan" -> "01",
    "feb" -> "02",
    "mar" -> "03",
    "apr" -> "04",
    "may" -> "05",
    "jun" -> "06",
    "jul" -> "07",
    "aug" -> "08",
    "sep" -> "09",
    "oct" -> "10",
    "nov" -> "11",
    "dec" -> "12"
  )
  val dateStringLengths = Map(
    10 -> dateTypesList.day,
    11 -> dateTypesList.day,
    8 -> dateTypesList.month,
    4 -> dateTypesList.year
  )
  val dateTypeToGranularity = Map(
    dateTypesList.day -> granularityList.day,
    dateTypesList.month -> granularityList.month,
    dateTypesList.year -> granularityList.year
  )

  // Local time getter
  def now: LocalDateTime = LocalDateTime.now(ZoneId.of("Australia/Sydney"))

  def today: LocalDate = LocalDate.now(ZoneId.of("Australia/Sydney"))

  // Random generations
  def getRandomFormatter: DateTimeFormatter = Seq(yyyyMMdd, mmm_yyyy, yyyy).apply(Random.nextInt(3))

  def randomDate(from: LocalDate = LocalDate.of(2010, 12, 31)): LocalDate = {
    val diff = DAYS.between(from, LocalDate.of(2020, 12, 31))
    from.plusDays(Random.nextInt(diff.toInt).toLong)
  }

  // String builder
  def getFirstDayOfMonth(month: String): LocalDate = month.length match {
    case 8 => LocalDate.parse(month.slice(4, 8) + "-" + monthToNum(month.slice(0, 3)) + "-01")
    case _ => throw new RuntimeException("UnknownStringLength")
  }

  def monthToNum(mon: String): String = mapMonth get mon.toLowerCase match {
    case Some(month) => month
    case None => throw new RuntimeException("ValueOfMonthIsOutOfRange")
  }

  def getFirstDayOfYear(year: Int): LocalDate = LocalDate.of(year, 1, 1)

  def getLastDayOfYear(year: Int): LocalDate = LocalDate.of(year, 12, 31)

  def asDateString(localDate: LocalDate = today, dateTimeFormatter: DateTimeFormatter = yyyyMMdd): String = localDate.format(dateTimeFormatter)

  def getStandardDateStringFrom(date: String): String = date.replaceNonAlphaNumericWith("-").getStandardDateString

  // Test if the date related string is date month or year it is like given date are converted to standard format already
  def getDateTypeByStringLength(x: String): String = dateStringLengths.get(x.length) match {
    case Some(dateType) => dateType
    case None => throw new RuntimeException("UnknownStringLength")
  }

  def getGranularityFromDateType(tpe: String): String = dateTypeToGranularity(tpe)

  object granularityList {
    // created to provide auto completion and avoid typos everywhere else
    val day = "daily"
    val week = "weekly"
    val month = "monthly"
    val year = "yearly"
    val values = Seq(day, week, month, year)
  }

  object dateTypesList {
    // created to provide auto completion and avoid typos everywhere else
    val day = "date"
    val month = "month"
    val year = "year"
  }
}