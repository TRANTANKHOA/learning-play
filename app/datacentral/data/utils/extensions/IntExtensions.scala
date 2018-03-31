package datacentral.data.utils.extensions

import datacentral.data.utils.functional.SmartFunctions.{CachedFunction, getSomeOrNone}
import org.joda.time.LocalDate

import scala.concurrent.duration._

object IntExtensions {

  private object Cache {
    val localDate = CachedFunction(
      (int: Int) => {
        val year = int / 10000
        val month = (int - (year * 10000)) / 100
        val day = int - (year * 10000) - (month * 100)
        new LocalDate(year, month, day)
      }
    )
    val localDateOption = CachedFunction(
      (int: Int) => getSomeOrNone(int.toLocalDate)
    )
    val booleanOption = CachedFunction(
      (int: Int) => getSomeOrNone(int.toBoolean)
    )
  }

  implicit class IntWrapper(val self: Int) extends AnyVal {

    def toLocalDate: LocalDate = Cache.localDate(self)

    def toLocalDateOption: Option[LocalDate] = Cache.localDateOption(self)

    /**
      * @param range The range in which to return the value
      * @return Some(i) if the value falls within the range, or None if it falls outside.
      **/
    def ifWithinRange(range: Range): Option[Int] = {
      if (range.contains(self)) Some(self)
      else None
    }

    def isEven: Boolean = self % 2 == 0

    def isOdd: Boolean = !isEven

    def weeks: Duration = (self * 7).days

    def toBoolean: Boolean = self match {
      case 1 => true
      case 0 => false
      case _ => throw new RuntimeException(s"$self is neither TRUE or FALSE")
    }

    def toBooleanOption: Option[Boolean] = Cache.booleanOption(self)
  }

}
