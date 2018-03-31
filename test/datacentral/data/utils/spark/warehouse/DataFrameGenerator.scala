package datacentral.data.utils.spark.warehouse

import datacentral.data.utils.functional.RMonad
import datacentral.data.utils.spark.dataframe.{DataField, DataFrameFile}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalacheck.Gen

import scala.collection.JavaConverters._

object DataFrameGenerator {

  def dataFrameGen(path: String, numRows: Int = 100): RMonad[GenConfig, Gen[DataFrameFile]] = RMonad(genConfig => {

    def sampleGen(field: DataField[_]): RMonad[GenConfig, Gen[Any]] = RMonad(conf => {
      val gen = field.dataType match {
        case _: StringType => conf.stringGen
        case _: IntegerType => conf.intGen
        case _: DoubleType => conf.doubleGen
        case _: DecimalType => conf.decimalGen
        case _: DateType => conf.dateGen
        case dataType: DataType => throw new RuntimeException(s"Please implement a generator for type ${dataType.simpleString}")
      }

      if (field.nullable)
        Gen.frequency((1, Gen.const(null)), (4, gen))
      else gen
    })

    def rowGen: Gen[Row] = Gen.sequence(
      genConfig.schema.fields
        .map(field => sampleGen(DataField(field)).read(genConfig)))
      .map(Row.apply(_))

    val rddGen: Gen[RDD[Row]] = Gen
      .sequence(Seq.fill(numRows)(rowGen))
      .map(rows => genConfig.repositoryConfig.sparkSession.sparkContext.parallelize(rows.asScala))

    val dfGen = rddGen.map(df => genConfig.repositoryConfig.sparkSession.createDataFrame(df, genConfig.schema))

    dfGen.map(dataFrame => {
      ParquetRepository(genConfig.repositoryConfig).write(path, dataFrame)
      DataFrameFile(path)
    })
  })
}



