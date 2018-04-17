package datacentral.data.utils.spark.dataframe

import java.time.LocalDate

import datacentral.data.utils.config.ConfigProvider
import datacentral.data.utils.spark.extensions.Jar
import datacentral.data.utils.spark.session.SharedContext
import org.apache.spark.sql.DataFrame

import scala.collection.mutable
import scala.util._

sealed trait DataEntity {
  def transform: Try[DataFrame]
}

trait ExtractDataEntity extends DataEntity {
  val path: String
}


object Transformer extends App {
  val invokedPath = args.head

  PersistedDataEntity.allEntities.get(invokedPath) match {
    case Some(entity) => entity.transform.map {
      _
        .write.mode("overwrite").parquet(PersistedDataEntity.getFullPath(invokedPath))
    }
    case None => throw new Exception(s"Unknown path: $invokedPath")
  }
}

object PersistedDataEntity {
  def getFullPath(path: String): String = {
    val runDate: LocalDate = LocalDate.parse(ConfigProvider.defaultConfig.getString("run_date"))
    val basePath: String = ConfigProvider.defaultConfig.getString("base_path")
    s"$basePath/$runDate/$path"
  }

  val allEntities: mutable.Map[String, PersistedDataEntity] = mutable.Map.empty
}

trait PersistedDataEntity extends DataEntity {

  val path: String
  PersistedDataEntity.allEntities += (path -> this)

  // used to modify the file path for different dates
  private lazy val fullPath = PersistedDataEntity.getFullPath(s"$path.parquet")
  // a place-holder for the `DataFrame` result when handling concurrent requests of `persistedDataFrame`
  private var result: Try[DataFrame] = Failure(new Exception("Empty result"))

  /**
    * This method can use the `path` to reused the result of a previously successful run of `transform`, which is
    * useful when recovering from a partially failed run or providing the same result to multiple entities in the pipeline
    *
    * @return
    */
  def persistedDataFrame: Try[DataFrame] = {

    if (SharedContext.exists(s"$fullPath/_SUCCESS")) {
      SharedContext.readParquet(fullPath)
    } else this.synchronized {
      // no `success` file found but maybe the `result` is busy running the transform already, so we check the result in
      // this block, which is executed as a single atomic command with regard to other competing threads. This means
      // other threads will wait for this block to finalise before gaining access to the `result` to get the dataframe
      if (result.isSuccess)
        result
      else {
        // trigger the `result` first time, or simply retry after a failure
        result = Try {
          val exitCode: Int = Jar(Transformer).launcher(Seq(path))(Jar.CLIENT_MODE).waitFor()
          if (exitCode != 0) throw new RuntimeException(s"fail at writing to $fullPath with exit code: $exitCode")
          else SharedContext.spark.read.parquet(fullPath)
        }

        result
      }
    }
  }
}

trait TransformDataEntity extends DataEntity

trait LoadDataEntity extends DataEntity {
  def load(df: DataFrame): Try[Unit]
}