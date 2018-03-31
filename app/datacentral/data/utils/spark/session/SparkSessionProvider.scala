package datacentral.data.utils.spark.session

import org.apache.spark.SparkContext
import org.apache.spark.sql.{SQLContext, SparkSession}

trait SparkSessionProvider {

  protected def ss: SparkSession

  private lazy val sc: SparkContext = ss.sparkContext

  private lazy val qc: SQLContext = ss.sqlContext
}