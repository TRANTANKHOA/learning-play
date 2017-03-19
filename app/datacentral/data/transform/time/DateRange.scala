package datacentral.data.transform.time

import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

case class DateRange(startDate: LocalDate, endDate: LocalDate) extends TimeConcept {
  val dateString: Seq[String] = Seq(startDate, endDate).map(_.format(yyyyMMdd))

  def isSingular: Boolean = duration == 1

  def duration: Long = DAYS.between(startDate, endDate) + 1

  def isOutOfRangeFrom(anotherDateRange: DateRange): Boolean = this.startDate.isAfter(anotherDateRange.endDate) || this.endDate.isBefore(anotherDateRange.startDate)

  def isSubRangeOf(anotherDateRange: DateRange): Boolean = !(this.startDate.isBefore(anotherDateRange.startDate) || this.endDate.isAfter(anotherDateRange.endDate))

  def cutDatesOutsideOf(anotherDateRange: DateRange): DateRange = DateRange(if (this.startDate.isBefore(anotherDateRange.startDate)) anotherDateRange.startDate else this.startDate, if (this.endDate.isAfter(anotherDateRange.endDate)) anotherDateRange.endDate else this.endDate)
}
