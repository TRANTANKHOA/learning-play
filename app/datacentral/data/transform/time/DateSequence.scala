package datacentral.data.transform.time

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import datacentral.data.transform.sequence.SmartSequence
import datacentral.data.transform.string.SmartStrings

case class DateSequence(seq: Seq[Date] = Seq.empty[Date]) extends TimeConcept with SmartSequence {

  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.by(_.toEpochDay)

  val smartStringSequence: SmartStrings = new SmartStrings fromSeq seq.map(_.dateString)

  val nonEmpty: Boolean = smartStringSequence.nonEmpty

  val length: Int = smartStringSequence.length

  val allTimeTypes: SmartStrings = new SmartStrings fromSeq smartStringSequence.listNonEmptyString.map(
    date => try {
      getDateTypeByStringLength(date)
    } catch {
      case _: Throwable => ""
    }
  )

  val firstTimeType: String = allTimeTypes.getFirstString

  val sortedYears: Seq[LocalDate] = getFirstAndLastElementsFrom(
    this.seq.filter(date => date.stringLength == 4)
      .map(year => getFirstDayOfYear(year.dateString.toInt))
      .sorted
  )

  val sortedMonths: Seq[LocalDate] = getFirstAndLastElementsFrom(
    this.seq.filter(date => date.stringLength == 8)
      .map(month => getFirstDayOfMonth(month.dateString))
      .sorted
  )

  val sortedDays: Seq[LocalDate] = getFirstAndLastElementsFrom(
    (this.seq.filter(date => date.stringLength == 10)
      .map(date => LocalDate.parse(date.dateString, yyyyMMdd)) ++
      this.seq.filter(date => date.stringLength == 11)
        .map(date => LocalDate.parse(date.dateString, ddMMMyyyy))).sorted
  )

  val sortedWeeks: Seq[LocalDate] = sortedDays.map(getFirstDateOfWeekOf)
  val firstAndLastDates: Seq[LocalDate] =
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
  val dateRange: Option[DateRange] = if (firstAndLastDates.nonEmpty)
    Some(DateRange(firstAndLastDates.head, firstAndLastDates.last))
  else
    None
  val duration: Long = dateRange match {
    case Some(range) => range.duration
    case None => 0
  }

  def fromString(date: String): DateSequence = fromSeq(Seq(date))

  def fromSeq(dates: Seq[String]): DateSequence = DateSequence(
    seq = dates.map(date => Date(dateString = date))
  )

  override def toString: String = toStringSeq.toString

  def toStringSeq: Seq[String] = smartStringSequence.listNonEmptyString

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
