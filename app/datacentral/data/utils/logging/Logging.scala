package datacentral.data.utils.logging

import play.api.Logger

// Logger definition
trait Logging {
  @transient protected val logger: Logger = Logger(this.getClass.getName)
  val logsFolder = "/tmp/logs/datacentral/"
}
