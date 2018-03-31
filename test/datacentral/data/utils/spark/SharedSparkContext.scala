package datacentral.data.utils.spark

import java.io.File

import datacentral.data.utils.spark.session.SparkSessionProvider
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{BeforeAndAfterAll, Suite}

trait SharedSparkContext extends BeforeAndAfterAll with SparkSessionProvider {
  self: Suite =>

  @transient protected[spark] var sparkSession: SparkSession = _

  override protected def ss: SparkSession = sparkSession

  protected lazy val qc: SQLContext = ss.sqlContext
  protected lazy val sc: SparkContext = ss.sparkContext

  private val conf = new SparkConf()
    .setAppName("LocalSparkTest")
    .setMaster("local[*]")
    .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .set("spark.files.useFetchCache", "false")
    .set("spark.scheduler.mode", "FAIR")
    .set("spark.kryo.registrator", "datacentral.data.utils.kryo.Registrator") // faster serialisation in Spark
    .set("spark.sql.warehouse.dir", new File("spark-warehouse").getAbsolutePath)
    .set("spark.sql.shuffle.partitions", "2")
    .set("spark.sql.crossJoin.enabled", "true")
    .set("spark.cores.max", "20")

  override def beforeAll() {
    sparkSession = SparkSession.builder().config(conf).getOrCreate()
    super.beforeAll()
  }

  override def afterAll() {
    if (sparkSession != null) {
      sparkSession.stop()
    }

    // To avoid Akka rebinding to the same port, since it doesn't unbind immediately on shutdown
    System.clearProperty("spark.driver.port")

    sparkSession = null
    super.afterAll()
  }
}
