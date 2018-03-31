package datacentral.data.utils.spark.dataframe

import datacentral.data.utils.functional.RMonad
import datacentral.data.utils.spark.warehouse.DataFrameRepository
import org.apache.spark.sql.DataFrame

case class DataFrameFile(path: String) extends RMonad[DataFrameRepository, DataFrame] {
  override val read: (DataFrameRepository => DataFrame) = repo => repo.read(this)
}












