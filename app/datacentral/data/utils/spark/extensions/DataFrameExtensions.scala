package datacentral.data.utils.spark.extensions

import java.util.UUID

import datacentral.data.utils.concurent.Task
import datacentral.data.utils.functional.RMonad
import datacentral.data.utils.spark.dataframe.{DataField, DataFrameFile}
import datacentral.data.utils.spark.warehouse.DataFrameRepository
import datacentral.data.utils.stats.BinomialDistribution
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.functions.rand
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.{DataFrame, Encoder, Row, SparkSession}

import scala.util.{Failure, Success, Try}

object DataFrameExtensions {

  implicit class DataFrameWrapper(dataframe: DataFrame) {

    lazy val fieldsMap: Map[String, StructField] = dataframe.schema.map(field => (field.name, field)).toMap

    def buildWriter(path: String): RMonad[DataFrameRepository, DataFrameFile] = RMonad(_.write(path, dataframe))

    /**
      * Quick way to retrieve the `structField` instead of the `column` as in `df(name)`
      *
      * @param name
      * @return
      */
    def fieldOption(name: String): Option[DataField[_]] = fieldsMap.get(name).map(DataField.apply)

    case class CheckPointedParquetFile(path: String, spark: SparkSession, structType: StructType) {
      def dataFrame: DataFrame = {
        val df = spark.read.parquet(path).rdd
        spark.createDataFrame(df, structType)
      }
    }

    def checkPoint(path: String): Task[CheckPointedParquetFile] = {
      val uniquePath: String = path + "." + UUID.randomUUID // avoid concurrency issues
      import scala.concurrent.ExecutionContext.Implicits.global
      Task(dataframe.write.mode("overwrite").parquet(uniquePath))
        .map(_ => CheckPointedParquetFile(uniquePath, dataframe.sparkSession, dataframe.schema))
    }


    /**
      * Set nullable property of columns.
      */
    def setNullable(settings: Map[String, Boolean]): DataFrame = {

      val newSchema = StructType(
        dataframe.schema.map(field =>
          if (settings.get(field.name).isDefined)
            field.copy(nullable = settings(field.name))
          else field))

      dataframe.sqlContext.createDataFrame(dataframe.rdd, newSchema)
    }

    def verify(df: DataFrame): Try[DataFrame] = matchWith(df.schema) map (_ => df)

    // Match and return the newSchema or an exception otherwise
    def matchWith(another: StructType): Try[StructType] = {
      val thisList = dataframe.schema.fields
      val thatList = another.fields
      val diffList = thatList.diff(thisList) ++ thisList.diff(thatList)
      if (diffList.isEmpty)
        Success(another)
      else
        Failure(new RuntimeException(s"Schema ${another.treeString} does not match expected structType: diffList = " +
                                     s"${diffList.mkString(" | ")}"))
    }

    /**
      * Join two dataframes by
      * - repartition
      * - sort
      * - zip partitions
      * - join within partitions
      *
      * @param right         dataFrame
      * @param numPartitions optional
      * @param key           used in repartition and sorting
      * @param otherKeys     used in sorting
      * @return left-joined dataFrame, non-match rows on right are thrown away and non-match rows on left are filled
      *         with `null`s
      */
    def leftJoinWithRepartition(
                                 right: DataFrame,
                                 numPartitions: Option[Int],
                                 key: String,
                                 otherKeys: String*): DataFrame = {

      def keySuite = KeySuite(numPartitions, key, otherKeys: _*)

      keySuite.leftJoin(right)(dataframe)
    }

    def innerJoinWithRepartition(
                                  right: DataFrame,
                                  numPartitions: Option[Int],
                                  key: String,
                                  otherKeys: String*): DataFrame = {

      def keySuite = KeySuite(numPartitions, key, otherKeys: _*)

      keySuite.innerJoin(right)(dataframe)
    }

    def sortWithRepartition(
                             key: String,
                             otherKeys: String*
                           )(implicit numPartitions: Option[Int] = None): DataFrame = dataframe
      .transform(numPartitions match {
        case Some(int) => _.repartition(int, dataframe(key))
        case None => _.repartition(dataframe(key))
      })
      .sortWithinPartitions(key, otherKeys: _*)

    /**
      * The built-in `mapPartitions` will ask for a `RowEncoder` and even then won't honor the original schema, e.g.
      * nullability settings.
      *
      * @param func
      * @return
      */
    def mapPartitionsWithSchema(func: Iterator[Row] => Iterator[Row])
                               (implicit schema: StructType = dataframe.schema): DataFrame = {
      implicit val encoder: Encoder[Row] = RowEncoder(schema)
      dataframe.sparkSession
        .createDataFrame(
          dataframe.mapPartitions(func)(encoder).rdd,
          schema
        )
    }


    /**
      * Take a fixed number of rows using a uniformly random distribution
      *
      * @param numRows
      * @return the same dataframe with the given `numRows`
      */
    def takeRandom(numRows: Long): DataFrame = {
      val cachedDF = dataframe.cache()
      val totalCount: Long = cachedDF.count()
      val sixSigma = 6 * BinomialDistribution(totalCount, numRows).deviation
      val fraction = (numRows + sixSigma) / totalCount
      val samples = if (fraction <= 0.8)
        cachedDF.sample(withReplacement = false, fraction)
      else cachedDF

      val random = "randomDoubleInUnitInterval"

      samples
        .withColumn(random, rand())
        .orderBy(random)
        .limit(numRows.toInt)
        .drop(random)
    }

    def getCountsOf(keys: String*): RDD[(String, Int)] = dataframe.rdd
      .map(row => (keys.toSeq.map(key => row.getAs[String](key)).mkString("|"), 1))
      .reduceByKey(_ + _)
  }

}