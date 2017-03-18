package data.warehouse

case class DatasetInfo(id: Long, name: String, location: Option[Int]){
  private val fileLocation: String = location match {
    case Some(1) => "hdfs://localhost:9000"
    case _ => sys.env("HOME")
  }

  private val filePath: String = location match {
    case Some(1) => "/spark-warehouse/"
    case _ => "/Downloads/"
  }

  val datasetPath = fileLocation + filePath + name
}