package datacentral.data.warehouse

case class Dataset(id: Long = 0, name: String = "", location: Option[Int] = None) {
  private val fileLocation: String = location match {
    case Some(1) => "hdfs://localhost:9000"
    case _ => sys.env("HOME")
  }
  private val filePath: String = location match {
    case Some(1) => "/spark-warehouse/"
    case _ => "/Downloads/"
  }
  val datasetPath: String = fileLocation + filePath + name
}