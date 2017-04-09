package datacentral.data.testUtils

import datacentral.data.warehouse.{Dataset, SparkAPI}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.json.{JSONArray, JSONException, JSONObject}

trait TestData {

  trait AnnaTestService extends SmartLogger {
    logger.info(s"${this.getClass.getName} Begin ...")
    implicit val spark: SparkSession = SparkAPI.spark
    val testData: (DataFrame, StructType) = SparkAPI.getDataframe(Dataset())

    def validateJSON(test: String): Boolean = try {
      new JSONObject(test)
      true
    } catch {
      case _: JSONException => try {
        new JSONArray(test)
        true
      } catch {
        case (_: JSONException) => false
      }
    }
  }

}


