package datacentral.data.utils.spark.warehouse

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime, LocalTime, Period}

import datacentral.data.utils.spark.warehouse.ParquetRepository.RepositoryConfig
import org.apache.spark.sql.types.StructType
import org.scalacheck.Gen

import scala.annotation.tailrec
import scala.util.Random

case class GenConfig(
                      schema: StructType,
                      repositoryConfig: RepositoryConfig,
                      booleanGen: Gen[Boolean] = GenConfig.genBoolean,
                      stringGen: Gen[String] = GenConfig.genString,
                      intGen: Gen[Int] = GenConfig.genInt,
                      longGen: Gen[Long] = GenConfig.genLong,
                      doubleGen: Gen[Double] = GenConfig.genDouble,
                      decimalGen: Gen[BigDecimal] = GenConfig.genDecimal,
                      dateGen: Gen[Date] = GenConfig.genDate(),
                      timeStampGen: Gen[Timestamp] = GenConfig.genTimeStamp()
                    )

object GenConfig {

  val genBoolean: Gen[Boolean] = Gen.oneOf(Seq(true, false))
  val strings: Seq[String] = new Random(0).distinctStrings(num = 6, length = 2)
  val genString: Gen[String] = Gen.oneOf(strings)
  val genInt: Gen[Int] = Gen.choose(1, 5000)
  val genLong: Gen[Long] = genInt.map(_.toLong)
  val genDouble: Gen[Double] = genInt.map(_ / 10) // gives 0.1 - 500
  val genDecimal: Gen[BigDecimal] = genDouble.map(d => BigDecimal.decimal(d))

  lazy val now: LocalDate = LocalDate.now

  def genLocalDate(
                    from: LocalDate = now.minusMonths(3),
                    to: LocalDate = now.plusMonths(1)
                  ): Gen[LocalDate] = Gen
    .choose(0, Period.between(from, to).getDays)
    .map(int => from.plusDays(int))

  def genDate(localDate: Gen[LocalDate] = genLocalDate()): Gen[Date] = localDate map Date.valueOf

  val genLocalTime: Gen[LocalTime] = for {
    hour <- Gen.choose(0, 23)
    minute <- Gen.choose(0, 59)
    second <- Gen.choose(0, 59)
  } yield LocalTime.of(hour, minute, second)

  def genLocalDateTime(
                        localDate: Gen[LocalDate] = genLocalDate()
                      ): Gen[LocalDateTime] = for {
    date <- localDate
    time <- genLocalTime
  } yield LocalDateTime.of(date, time)

  def genTimeStamp(
                    localDateTime: Gen[LocalDateTime] = genLocalDateTime()
                  ): Gen[Timestamp] = localDateTime map Timestamp.valueOf

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
    def stringStream(length: Int): Stream[String] = Stream continually random.alphanumeric.take(length).mkString("")

    def either(one: String, another: String): String = if (random.nextBoolean()) one else another
  }

}