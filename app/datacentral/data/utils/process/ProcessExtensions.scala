package datacentral.data.utils.process

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

object ProcessExtensions {

  implicit class ProcessWrapper(val process: Process) extends AnyVal {

    def exitCode(implicit executor: ExecutionContext): Future[Int] = Future {
      process.waitFor()
    }

    def stderrIterator: Iterator[String] = {
      Source.fromInputStream(process.getErrorStream).getLines()
    }

    def stdoutIterator: Iterator[String] = {
      Source.fromInputStream(process.getInputStream).getLines()
    }
  }

}
