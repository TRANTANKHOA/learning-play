package datacentral.data.utils.spark.extensions

import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.DataType
import org.apache.spark.sql.{Column, DataFrame, Dataset}

import scala.reflect.runtime.universe._

object DatasetExtensions {

  implicit class DatasetWrapper[A](val self: Dataset[A]) extends AnyVal {

    def leftJoin(right: Dataset[_], joinExprs: Column): DataFrame = {
      self.join(right, joinExprs, joinType = "left")
    }

    def joinAndMergeColumns(right: Dataset[_], key: String*): DataFrame = {
      self.join(right, key, "inner")
    }

    def leftJoinAndMergeColumns(right: Dataset[_], key: String*): DataFrame = {
      self.join(right, key, "left")
    }

    def fullJoinAndMergeColumns(right: Dataset[_], key: String*): DataFrame = {
      self.join(right, key, "full")
    }

    def withColumnConverted(columnName: String, dataType: DataType): DataFrame = {
      withColumnConverted(columnName, (_: Column).cast(dataType))
    }

    def withColumnConverted(columnName: String, conversion: Column => Column): DataFrame = self
      .withColumn(
        columnName,
        conversion(self(columnName)))

    def withColumnConverted[RT: TypeTag, A: TypeTag](columnName: String, conversion: A => RT): DataFrame = {
      withColumnConverted(columnName, udf[RT, A](conversion).apply(_: Column))
    }

    def withDerivedColumn(columnName: String, f: Dataset[A] => Column): DataFrame = {
      val column = f(self)
      self.withColumn(columnName, column)
    }

    def withColumns(columns: Column*): DataFrame = {
      columns.foldLeft(self.toDF())(_.select(col("*"), _))
    }

    def dropColumns(columns: Column*): DataFrame = columns.foldLeft(self.toDF)(_.drop(_))

    def repartitionAndSortWithinPartitions(numPartitions: Int, key: String, sort: String*): Dataset[A] = {
      self.repartition(numPartitions, key).sortWithinPartitions(key, sort: _*)
    }

    def repartition(numPartitions: Int, key: String): Dataset[A] = {
      self.repartition(numPartitions, self(key))
    }

    def repartitionAndSortWithinPartitions(key: String, sort: String*): Dataset[A] = {
      self.repartition(key).sortWithinPartitions(key, sort: _*)
    }

    def repartition(key: String): Dataset[A] = {
      self.repartition(self(key))
    }

    def broadcast(): Dataset[A] = org.apache.spark.sql.functions.broadcast(self)

    def isSorted[B](mapping: A => B)(implicit ordering: Ordering[B]): Boolean = {
      import ordering._
      import self.sparkSession.implicits._
      self.mapPartitions(rows => {
        val isSorted: Boolean = rows
          .map(mapping)
          .sliding(2) // all adjacent pairs
          .forall {
          case x :: y :: Nil => x <= y
          case _ => true
        }

        Iterator(isSorted)
      }).reduce(_ && _)
    }
  }

}