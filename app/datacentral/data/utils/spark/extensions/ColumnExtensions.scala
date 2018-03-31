package datacentral.data.utils.spark.extensions

import org.apache.spark.sql.Column

object ColumnExtensions {

  implicit class ColumnWrapper[A](val self: Column) extends AnyVal {

    def isNullOrZero: Column = self.isNull || self === 0

    def isNotNullOrZero: Column = self.isNotNull && self =!= 0
  }

}
