package datacentral.data.utils.spark.extensions

import datacentral.data.utils.extensions.sequences.TupleExtensions._
import datacentral.data.utils.spark.SparkUnitSpec

class KeySuiteTest extends SparkUnitSpec {

  import qc.implicits._

  behavior of "innerJoin"
  it should "give correct number of rows" in {
    val n = 10
    val m = 5
    val p = 25
    val cust = "customer_id" -> (1 to n).map(_.toString)
    val store = "store_id" -> (1 to m).map(_.toString)
    val prod = "prod_id" -> (1 to p).map(_.toString)
    val keySuite = KeySuite(Some(m), store.key)
    val customerDF = cust.value.zipWithIndex.map(cus => (cus._1, store.value(cus._2 % m))).toDF(cust.key, store.key)
    val productDF = prod.value.zipWithIndex.map(pro => (pro._1, store.value(pro._2 % m))).toDF(prod.key, store.key)
    // every store has n/m customers and p/m products so this data frame should have n*p/m rows
    val dataFrame = keySuite.innerJoin(customerDF)(productDF)
    assert(dataFrame.count().toInt == (n * p) / m)
  }

  behavior of "leftJoin"
  it should "testInnerJoin" in {

  }

  behavior of "sort"
  it should "testInnerJoin" in {

  }
}
