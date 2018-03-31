package datacentral.data.utils.concurent

import java.time.LocalDateTime

import datacentral.data.utils.concurent.Composable.Status
import datacentral.data.utils.concurent.ResumableTask.KeyManager
import datacentral.data.utils.extensions.sequences.TupleExtensions.KeyValue
import datacentral.data.utils.functional.RMonad

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * For a task that only returns `Unit`, we may want to persist the status of computation to disk with a given `key`,
  * knowing that this task does not need to be repeated every run.
  *
  * Once a task is persisted, e.g. to a database or disk, we may also need a way to manually purge the disk to rerun
  * all tasks.
  */
abstract class ResumableTask(implicit executionContext: ExecutionContext) extends Task[Unit] {

  /**
    * Read the `key` from disk and decide whether to proceed with the required computation for `compute: => UNIT`
    */
  override lazy val result: Status = {
    started = true // mark this task as `started` so that no more dependencies can be registered against it
    if (readStatus)
      Future(Success(Unit)) // by-pass the current task if the `key` returns a `true`
    else writeStatus {
      resolveDepsAndExecuteWithTimer(compute)
    }
  }
  val key: String // a unique string identifier used for persisting the status of this task computation to disk
  protected val keyManager: KeyManager // is the facility to manage cached tasks

  /**
    * read the persisted `key` from disk to tell whether this computation has finished successfully before
    *
    * @return `true` if this computation has finished successfully before, and `false` otherwise
    */
  def readStatus: Boolean = keyManager.read(key)

  /**
    * Write the computation status to a disk, e.g. key -> true/false. With time, we can build a dashboard to
    * report on the progress of the DAG execution, or simply send the info. to Slack
    */
  private def writeStatus(status: Status): Status = {
    status.map {
      case Success(_) => keyManager.write(key, timerStart, timerStop, None)
      case Failure(e) => keyManager.write(key, timerStart, timerStop, Some(e))
    }

    status
  }
}

object ResumableTask {

  /**
    * Provide the Reader pattern for dependency injection
    *
    * @param key
    * @param routine
    * @return
    */
  def build(key: String, routine: => Unit): RMonad[Config, ResumableTask] = RMonad(conf =>
    ResumableTask(key, routine)(conf.executionContext, conf.keyManager))

  /**
    * Give the default implementation of `TaskResumable` that wrap around a `routine: => Unit` with a `key` provided
    *
    * @param key
    * @param routine
    * @param ec
    * @param keyManager
    * @return
    */
  def apply(key: String, routine: => Unit)(
    implicit ec: ExecutionContext, keyManager: KeyManager
  ): ResumableTask = new DefaultImpl(key, routine)(ec, keyManager)

  trait KeyManager {
    /**
      * Empty all keys
      */
    def reset: Unit

    /**
      * @return a string representation of all keys
      */
    def print: String

    /**
      * Read the persisted `key` from disk to tell whether this computation has finished successfully before
      *
      * @return `true` if this computation has finished successfully before, and `false` otherwise
      */
    def read(key: String): Boolean

    /**
      * Write the computation status to a disk, e.g. key -> true/false. This method need not throw any exceptions, hence
      * the output type as such
      *
      * We probably can create a dashboard for reporting the historical statuses of the DAG by writing these info. to
      * a database.
      *
      * @param key   is needed, so we can't log `TaskNotResumable`
      * @param start time
      * @param end   time
      * @param error possible error message of the task
      */
    def write(key: String, start: LocalDateTime, end: LocalDateTime, error: Option[Throwable]): Try[Unit]

    /**
      * Delete the entry for this `key`
      */
    def pop(key: String): Unit

  }

  case class Config(executionContext: ExecutionContext, keyManager: KeyManager)

  /**
    * This is the default implementation for `TaskNotResumable`
    *
    * @param routine is the required computation to perform by this task
    * @param ec      implicit execution context for Scala Future
    */
  private class DefaultImpl(val key: String, routine: => Unit)(
    implicit val ec: ExecutionContext,
    implicit val keyManager: KeyManager
  ) extends ResumableTask {
    override protected def compute: Unit = routine
  }

  object KeyManager {

    def build: KeyManager = InMemoryManager

    // pseudo KeyManager
    implicit object InMemoryManager extends KeyManager {
      //noinspection TypeAnnotation
      private val keys = mutable.Map[String, Boolean]()

      def reset: Unit = keys.clear

      def print: String = keys.toSeq.sortBy(_.key).map(each => s"${each.key} - ${each.value}").mkString(" | ")

      def read(key: String): Boolean = keys.getOrElse(key, false)

      def write(key: String, start: LocalDateTime, end: LocalDateTime, error: Option[Throwable]): Try[Unit] = Try {
        val success = error.isEmpty
        keys.+=((key, success))
        println(s"written a key ${key -> success}")
      }

      def pop(key: String): Unit = keys -= key
    }

  }

}