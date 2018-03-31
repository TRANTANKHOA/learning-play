package datacentral.data.utils.concurent

import java.time.LocalDateTime

import datacentral.data.utils.concurent.ResumableTask.KeyManager
import slick.jdbc.SQLiteProfile.api._
import slick.jdbc.meta.MTable
import slick.lifted.ProvenShape

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

class SlickKeyManager(database: Database) extends KeyManager {

  import SlickKeyManager._

  private lazy val tables: Seq[MTable] = await(database.run(MTable.getTables(steps.baseTableRow.tableName)))

  private var queue: Future[Unit] = if (tables.isEmpty) database.run(steps.schema.create) else Future(Unit)

  /**
    * Empty all keys
    */
  override def reset: Unit = awaitQueue(database.run(steps.delete))

  private def awaitQueue[A](task: => Future[A]): A = {
    val newQueue = queue.flatMap(_ => task)
    queue = newQueue.map(_ => Unit)
    await(newQueue)
  }

  private def await[A](task: => Future[A]): A = Await.result(task, Duration.Inf)

  /**
    * @return a string representation of all keys
    */
  override def print: String = awaitQueue(database.run(steps.result).map("List of all keys: \n\t" + _.mkString("\n\t")))

  /**
    * Read the persisted `key` from disk to tell whether this computation has finished successfully before
    *
    * @return `true` if this computation has finished successfully before, and `false` otherwise
    */
  override def read(key: String): Boolean = {
    val row: Seq[(String, String, String, String, Boolean)] =
      awaitQueue(database.run(steps.filter(_.key === key).result))

    if (row.isEmpty) false else row.head._5
  }

  /**
    * Write the computation status to a disk, e.g. key -> true/false. This method need not throw any exceptions, hence
    * the output type as such
    *
    * We probably can create a dashboard for reporting the historical statuses of the DAG by writing these info. to
    * a database.
    *
    * @param key   is needed, so we can't log `NoneResumableTask`
    * @param start time
    * @param end   time
    * @param error possible error message of the task
    */
  override def write(key: String, start: LocalDateTime, end: LocalDateTime, error: Option[Throwable]): Try[Unit] =
    Try {
      val errorMsg = error match {
        case None => "Success"
        case Some(e) => e.getMessage
      }
      val success = error.isEmpty
      val newStep = (key, start.toString, end.toString, errorMsg, success)

      awaitQueue {
        val action: DBIO[Unit] = DBIO.seq(steps += newStep)
        database.run(action)
      }
    }

  /**
    * Delete the entry for this `key`
    */
  override def pop(key: String): Unit = awaitQueue(database.run(steps.filter(_.key === key).delete))

}

object SlickKeyManager {

  //noinspection TypeAnnotation
  val steps = TableQuery[PipelineRecords]

  class PipelineRecords(tag: Tag) extends Table[(String, String, String, String, Boolean)](tag, "PIPELINE_RECORDS") {
    // Every table needs a * projection with the same type as the table's type parameter
    def * : ProvenShape[(String, String, String, String, Boolean)] = (key, start, end, error, success)

    def key: Rep[String] = column[String]("KEY", O.PrimaryKey)

    def start: Rep[String] = column[String]("START")

    def end: Rep[String] = column[String]("END")

    def error: Rep[String] = column[String]("ERROR")

    def success: Rep[Boolean] = column[Boolean]("SUCCESS")
  }

}
