package datacentral.data.utils.spark.extensions

import datacentral.data.utils.extensions.sequences.IteratorExtensions
import datacentral.data.utils.spark.extensions.DataFrameExtensions.DataFrameWrapper
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row}

case class KeySuite(
                     numPartitions: Option[Int], key: String, otherKeys: String*
                   ) {

  val allKeys: Seq[String] = key +: otherKeys

  /**
    * Key columns must not be nullable, otherwise rows with null keys are filtered out
    */
  val sort: DataFrame => DataFrame = (dataFrame: DataFrame) => {
    val fields: Array[StructField] = dataFrame.schema.fields
    val illegalArguments: Seq[String] = allKeys.filterNot(fields.map(_.name).contains)
    require(illegalArguments.isEmpty, s"Illegal keys found: ${illegalArguments.mkString(", ")}")
    dataFrame.transform { df =>
      if (numPartitions.nonEmpty) {
        df.sortWithRepartition(key, otherKeys: _*)(numPartitions)
      } else
        df.sortWithRepartition(key, otherKeys: _*)
    }
  }

  def innerJoin(right: DataFrame): DataFrame => DataFrame = join(right)(leftJoin = false)

  def leftJoin(right: DataFrame): DataFrame => DataFrame = join(right)()

  private def join(right: DataFrame)(leftJoin: Boolean = true): DataFrame => DataFrame = {
    (left: DataFrame) => {
      val rightDiff: Array[StructField] = right.schema.fields.diff(left.schema.fields)
      if (rightDiff.isEmpty)
        sort(left)
      else {
        val rightRDD = this.sort(right).rdd
        val leftRDD = this.sort(left).rdd

        def getKeys(row: Row): String = allKeys.map(row.getAs[String]).mkString("|")

        val rdd: RDD[Row] = leftRDD.zipPartitions(rightRDD) { (leftIter, rightIter) =>
          var leftBuffer = leftIter.buffered
          var rightBuffer: BufferedIterator[Row] = rightIter.buffered
          IteratorExtensions.buildIteratorBy[Row] {
            if (leftBuffer.nonEmpty) {
              val currentKeys: String = getKeys(leftBuffer.head)

              def checkElement(row: Row): Boolean = getKeys(row) == currentKeys

              val (currentLeftRows, leftTail) = leftBuffer.span(checkElement)
              leftBuffer = leftTail.buffered

              val (prefix, rightTail) = rightBuffer
                .span(!checkElement(_))._2 // just throw away the non-match rightIter rows
                .span(checkElement)
              rightBuffer = rightTail.buffered
              // can't do for comprehension with iterator because the iterator is exhausted after just the first round
              val currentRightRows = prefix.toList

              if (currentRightRows.isEmpty)
                if (leftJoin) currentLeftRows.map(Row.merge(_, Row(rightDiff.map(_ => null): _*))).toIterator else Iterator.empty
              else {
                for {
                  lr <- currentLeftRows
                  rr <- currentRightRows
                } yield Row.merge(lr, Row(rightDiff.map(structField => rr.getAs[Any](structField.name)): _*))
              }
            } else Iterator.empty
          }
        }

        left.sqlContext.createDataFrame(
          rdd,
          StructType(
            left.schema.fields ++ (if (leftJoin) rightDiff.map(field => field.copy(nullable = true)) else rightDiff)))
      }
    }
  }
}