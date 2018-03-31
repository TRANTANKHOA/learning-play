package datacentral.data.utils.extensions.datetime

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime}

import datacentral.data.utils.extensions.datetime.LocalDateExtensions._

/* The date objects we interact with will be java.sql since those are supported by the DataFrame API
 * This object will allow joda methods to be used under the covers to perform date arithmetic,
 * whilst keeping our instances as java.sql objects.
 */
object SqlDateExtensions {

  implicit class DateWrapper(val self: Date) extends AnyVal {

    def minusDays(days: Int): Date = toSQLDate(_.minusDays(days))

    def minusWeeks(weeks: Int): Date = toSQLDate(_.minusWeeks(weeks))

    def minusMonths(months: Int): Date = toSQLDate(_.minusMonths(months))

    def minusYears(years: Int): Date = toSQLDate(_.minusYears(years))

    def plusDays(days: Int): Date = toSQLDate(_.plusDays(days))

    def plusWeeks(weeks: Int): Date = toSQLDate(_.plusWeeks(weeks))

    private def toSQLDate(function: LocalDate => LocalDate) = function(self.toLocalDate).toSqlDate

    def plusMonths(months: Int): Date = toSQLDate(_.plusMonths(months))

    def plusYears(years: Int): Date = toSQLDate(_.plusYears(years))
  }

  implicit class TimestampWrapper(val self: Timestamp) extends AnyVal {

    def toDate: Date = self.toLocalDateTime.toLocalDate.toSqlDate

    def minusDays(days: Int): Timestamp = toSQLTimestamp(_.minusDays(days))

    def minusWeeks(weeks: Int): Timestamp = toSQLTimestamp(_.minusWeeks(weeks))

    def minusMonths(months: Int): Timestamp = toSQLTimestamp(_.minusMonths(months))

    def minusYears(years: Int): Timestamp = toSQLTimestamp(_.minusYears(years))

    def plusDays(days: Int): Timestamp = toSQLTimestamp(_.plusDays(days))

    private def toSQLTimestamp(function: LocalDateTime => LocalDateTime) = function(self.toLocalDateTime).toSqlTime

    def plusWeeks(weeks: Int): Timestamp = toSQLTimestamp(_.plusWeeks(weeks))

    def plusMonths(months: Int): Timestamp = toSQLTimestamp(_.plusMonths(months))

    def plusYears(years: Int): Timestamp = toSQLTimestamp(_.plusYears(years))
  }

}
