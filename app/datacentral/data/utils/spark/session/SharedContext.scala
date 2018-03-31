package datacentral.data
package utils.spark.session

import datacentral.data.utils.config.ConfigProvider
import datacentral.data.utils.hadoop.HadoopFileSystemProvider
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util._

object SharedContext extends HadoopFileSystemProvider {

  private lazy val sparkConf: SparkConf = {
    val conf = new SparkConf()

    if (ConfigProvider.defaultConfig.hasPath("svc.spark.config"))
      conf.setAll(ConfigProvider.defaultConfig.getStringTupleSeqSafely("svc.spark.config").get)
    else
      conf
  }

  lazy val spark: SparkSession = SparkSession.builder()
    .enableHiveSupport()
    .config(sparkConf).getOrCreate()

  def exists(path: String): Boolean = fs.exists(new Path(path))

  def readParquet(path: String): Try[DataFrame] = Try(spark.read.parquet(path))

  def readCSV(path: String): Try[DataFrame] = Try {
    spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(path)
  }

  /**
    * Reading tab-separated text file
    *
    * @param path
    * @return
    */
  def readTSV(path: String): Try[DataFrame] = Try {
    spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .option("delimiter", "\t")
      .csv(path)
  }
}
