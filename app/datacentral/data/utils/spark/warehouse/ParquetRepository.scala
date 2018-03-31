package datacentral.data.utils.spark.warehouse

import datacentral.data.utils.logging.Logging
import datacentral.data.utils.spark.dataframe.DataFrameFile
import datacentral.data.utils.spark.warehouse.ParquetRepository.RepositoryConfig
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

import scala.collection.concurrent.TrieMap

case class ParquetRepository(repositoryConfig: RepositoryConfig) extends DataFrameRepository with Logging {

  val dfMap: TrieMap[String, DataFrame] = new TrieMap[String, DataFrame]

  def fullPath(path: String = "", location: Option[Int] = None): String = {

    val fileLocation: String = location match {
      case Some(1) => "hdfs://localhost:9000"
      case _ => sys.env("HOME")
    }

    val filePath: String = location match {
      case Some(1) => "/spark-warehouse/"
      case _ => "/Downloads/"
    }

    fileLocation + filePath + path
  }

  override def write(path: String, dataFrame: DataFrame): DataFrameFile = {
    val full = fullPath(path)
    dataFrame.write.mode(SaveMode.Overwrite).parquet(full)
    dfMap += (full -> dataFrame.cache)
    DataFrameFile(path)
  }

  override def read(target: DataFrameFile): DataFrame = {
    val path = fullPath(target.path)
    if (dfMap.contains(path)) {
      dfMap(path)
    } else {
      val df = if (exists(target)) {
        repositoryConfig.sparkSession.read.parquet(path).cache
      } else throw new RuntimeException(s"$target does not exist!")
      dfMap += (path -> df)
      df
    }
  }

  override def exists(target: DataFrameFile): Boolean = repositoryConfig.fileSystem.exists(new Path(fullPath(target.path)))
}

object ParquetRepository {

  case class RepositoryConfig(sparkSession: SparkSession, fileSystem: FileSystem)

}