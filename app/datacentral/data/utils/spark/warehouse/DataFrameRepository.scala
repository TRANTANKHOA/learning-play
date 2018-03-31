package datacentral.data.utils.spark.warehouse

import datacentral.data.utils.functional.RMonad
import datacentral.data.utils.spark.dataframe.DataFrameFile
import datacentral.data.utils.spark.warehouse.ParquetRepository.RepositoryConfig
import org.apache.spark.sql.DataFrame

/**
  * Handling the read/write/path for parquet files
  */
trait DataFrameRepository {

  def write(path: String, dataFrame: DataFrame): DataFrameFile

  def read(target: DataFrameFile): DataFrame

  def exists(target: DataFrameFile): Boolean

}

object DataFrameRepository {

  def build: RMonad[RepositoryConfig, DataFrameRepository] = RMonad(fileSystem => ParquetRepository(fileSystem))
}