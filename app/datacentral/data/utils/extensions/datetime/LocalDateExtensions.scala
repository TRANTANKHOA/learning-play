package datacentral.data.utils.extensions.datetime

import java.sql.{Date, Timestamp}
import java.time.temporal.{IsoFields, TemporalField, WeekFields}
import java.time.{LocalDate, LocalDateTime}
import java.util.{Locale, TimeZone}

object LocalDateExtensions {

  lazy val UTC: TimeZone = TimeZone.getTimeZone("UTC")
  lazy val dayOfWeek: TemporalField = WeekFields.of(Locale.US).dayOfWeek

  implicit class LocalDateWrapper(val date: LocalDate) extends AnyVal {

    def endOfWeek: LocalDate = date.startOfWeek.plusDays(6)

    def startOfWeek: LocalDate = date.`with`(dayOfWeek, 1)

    def toSqlDate: Date = Date.valueOf(date)

    def getWeekNumber: Int = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

    def getQuarterNumber: Int = 1 + ((getMonthNumber - 1) * 4 / 12)

    def getMonthNumber: Int = date.getMonthValue
  }

  implicit class LocalDateTimeWrapper(val time: LocalDateTime) extends AnyVal {

    def toSqlTime: Timestamp = Timestamp.valueOf(time)
  }

}
