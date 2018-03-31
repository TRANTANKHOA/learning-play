package datacentral.data.utils.spark.dataframe

import java.sql.Date

import datacentral.data.utils.spark.extensions.RowExtensions.RowWrapper
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, _}

/**
  * Provide the Scala type for the given `structField`
  */
case class DataField[A](
                         nullable: Boolean, dataType: DataType, name: String, metadata: Metadata, comment: Option[String]
                       ) {

  def getAsOption(row: Row): Option[A] = row.getAsOption(this)
}

object DataField {

  /**
    * convert the `DataType` into Scala types
    *
    * @param structField
    * @return
    */
  def apply(structField: StructField): DataField[_] = {

    def withScalaType[A] = new DataField[A](
      nullable = structField.nullable,
      dataType = structField.dataType,
      name = structField.name,
      metadata = structField.metadata,
      comment = structField.getComment()
    )

    structField.dataType match {
      case _: StringType => withScalaType[String]
      case _: DoubleType => withScalaType[Double]
      case _: IntegerType => withScalaType[Int]
      case _: DecimalType => withScalaType[BigDecimal]
      case _: LongType => withScalaType[Long]
      case _: DateType => withScalaType[Date]
      case missing => throw new RuntimeException(s"Missing dataType ${missing.typeName}") // TODO make this comprehensive
    }
  }
}