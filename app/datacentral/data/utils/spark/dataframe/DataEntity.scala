package datacentral.data.utils.spark.dataframe

import java.time.LocalDate

import datacentral.data.utils.config.ConfigProvider
import datacentral.data.utils.spark.session.SharedContext
import org.apache.spark.sql.DataFrame

import scala.util._

sealed trait DataEntity {
  def transform: Try[DataFrame]
}

trait ExtractDataEntity extends DataEntity {
  val path: String
}

trait PersistedDataEntity extends DataEntity {

  val path: String

  // used to modify the file path for different dates
  private val runDate: LocalDate = LocalDate.parse(ConfigProvider.defaultConfig.getString("run_date"))
  private val basePath: String = ConfigProvider.defaultConfig.getString("base_path")
  private val fullPath = s"$basePath/$runDate/$path.parquet"
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
        result = transform.map { dataFrame =>
          dataFrame.write.mode("overwrite").parquet(fullPath)
          dataFrame.sparkSession.read.parquet(fullPath)
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