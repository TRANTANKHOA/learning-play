package datacentral.data.process

import javax.inject.Inject

import com.google.inject.Singleton
import play.Application
import play.api.Environment

@Singleton
class FilesIO @Inject()(environment: Environment, application: Application) {

  val home: java.io.File = application.path()

  def recursiveListFiles(f: java.io.File): Array[java.io.File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

  def listAllFilesIn(path: String): Array[java.io.File] = recursiveListFiles(new java.io.File(home.toPath.toString + path))

  val skins: Array[java.io.File] = listAllFilesIn("/public/LTETemplates/dist/css/skins")
}