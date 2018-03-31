package datacentral.data.utils.process

import java.io.File

import datacentral.data.utils.functional.{RMonad, SmartFunctions}
import play.Application
import play.api.Environment

case class FilesIO(environment: Environment, application: Application) {

  val home: File = application.path()

  def toFile(path: String): File = new File(home.toPath.toString + path)

  def listAllFiles(file: File*): Stream[File] = SmartFunctions.lazyRecursiveWith[File, Seq[File], Seq](
    remainder = file.toSeq,
    split = (rem: Seq[File]) => {
      val all = rem.flatMap(_.listFiles()) ++ rem
      val newRem = all.filter(_.isDirectory)
      val out = all.filterNot(_.isDirectory)
      (newRem, out)
    }
  ).flatten

  def listAllFiles(path: String): Stream[File] = listAllFiles(toFile(path))
}

object FilesIO {

  case class Config(environment: Environment, application: Application)

  /**
    * This is a light way to do dependency injection
    *
    * @return
    */
  def build: RMonad[Config, FilesIO] = RMonad(conf => FilesIO(conf.environment, conf.application))
}