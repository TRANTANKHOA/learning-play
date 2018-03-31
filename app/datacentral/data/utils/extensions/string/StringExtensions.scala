package datacentral.data.utils.extensions.string

import datacentral.data.utils.functional.SmartFunctions.{CachedFunction, getSomeOrNone}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.json.{JSONArray, JSONException, JSONObject}

import scala.annotation.tailrec
import scala.math.BigDecimal
import scala.util.Random

object StringExtensions {

  private object Cache {

    case class FormatString(format: String, str: String)

    val toJodaOption = CachedFunction(
      (formatString: FormatString) => getSomeOrNone(DateTimeFormat.forPattern(formatString.format).parseDateTime(formatString.str)))

    val toBooleanOption = CachedFunction(
      (str: String) => getSomeOrNone(str.toBoolean))

    val toByteOption = CachedFunction(
      (str: String) => getSomeOrNone(str.toByte))

    val toShortOption = CachedFunction(
      (str: String) => getSomeOrNone(str.toShort))

    val toIntOption = CachedFunction(
      (str: String) => getSomeOrNone(str.toInt))

    val toLongOption = CachedFunction(
      (str: String) => getSomeOrNone(str.toLong))

    val toFloatOption = CachedFunction(
      (str: String) => getSomeOrNone(str.toFloat))

    val toDoubleOption = CachedFunction(
      (str: String) => getSomeOrNone(str.toDouble))

    val toDecimalOption = CachedFunction(
      (str: String) => getSomeOrNone(BigDecimal(str)))
  }

  implicit class StringWrapper(val str: String) extends AnyVal {

    def isValidatedJSON: Boolean = try {
      new JSONObject(str)
      true
    } catch {
      case _: JSONException => try {
        new JSONArray(str)
        true
      } catch {
        case (_: JSONException) => false
      }
    }

    def truncateWithDots(maxLength: Int): String = str.take(maxLength) + (if (str.length > maxLength) " ..." else "")

    def toJodaOption(format: String): Option[DateTime] = Cache.toJodaOption(Cache.FormatString(format, str))

    def toBooleanOption: Option[Boolean] = Cache.toBooleanOption(str)

    def toByteOption: Option[Byte] = Cache.toByteOption(str)

    def toShortOption: Option[Short] = Cache.toShortOption(str)

    def toIntOption: Option[Int] = Cache.toIntOption(str)

    def toLongOption: Option[Long] = Cache.toLongOption(str)

    def toFloatOption: Option[Float] = Cache.toFloatOption(str)

    def toDoubleOption: Option[Double] = Cache.toDoubleOption(str)

    def toDecimalOption: Option[BigDecimal] = Cache.toDecimalOption(str)
  }

  implicit class RandomString(random: Random) {

    def distinctStrings(num: Int, length: Int): Seq[String] = {
      @tailrec
      def recur(acc: Seq[String]): Seq[String] = num - acc.length match {
        case int if int > 0 =>
          val newAcc = acc ++ random.stringStream(length).take(int).distinct
          recur(newAcc)
        case _ => acc
      }

      recur(Seq.empty)
    }

    /**
      * Produce an infinite iterator of strings with fixed length
      *
      * @param length
      * @return
      */
    def stringStream(length: Int): Stream[String] = Stream continually random.nextString(length)

    def either(one: String, another: String): String = if (random.nextBoolean()) one else another
  }

}
