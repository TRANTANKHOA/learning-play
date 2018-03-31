package datacentral.data.utils.spark.extensions

import datacentral.data.utils.functional.SmartFunctions.getSomeOrNone
import datacentral.data.utils.spark.dataframe.DataField
import org.apache.spark.sql.Row

object RowExtensions {

  implicit class RowWrapper(val row: Row) extends AnyVal {

    def getAsOption[A](index: Int): Option[A] =
      if (row.isNullAt(index))
        None
      else
        getSomeOrNone(row.getAs[A](index))


    def getAsOption[A](fieldName: String): Option[A] = getAsOption[A](
      row.fieldIndex(fieldName)
    )

    def getAsOption[A](dataField: DataField[A]): Option[A] = getAsOption[A](dataField.name)
  }

}
