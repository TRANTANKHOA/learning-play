package data.warehouse

import org.apache.spark.SparkConf
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.concurrent.TrieMap

object SparkContextBuilder {

  private val conf = new SparkConf()
    .setMaster("local[*]")
    .setAppName("Local Spark Server")
    .set("spark.scheduler.mode", "FAIR")
    .set("spark.sql.crossJoin.enabled", "true") // only acceptable in local spark
    .set("spark.cores.max", "20") // one user can grab at most 20 cores in one timex

  val spark: SparkSession = SparkSession.builder.config(conf).getOrCreate
  val dfMap: TrieMap[Long, (DataFrame, StructType)] = new TrieMap[Long, (DataFrame, StructType)]

  def getDataframe(datasetInfo: DatasetInfo): (DataFrame, StructType) = {

    if (!dfMap.contains(datasetInfo.id)) {

      val df = spark.read.parquet(datasetInfo.datasetPath).cache
      val schema: StructType = df.schema
      dfMap += (datasetInfo.id -> (df, schema))

      (df, schema)
    }
    else {
      dfMap(datasetInfo.id)
    }
  }

  def getContext: SparkSession = {
    spark
  }
}
