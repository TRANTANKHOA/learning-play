package datacentral.data
package utils.spark.extensions

import java.io.File

import datacentral.data.utils.reflection.Functions.ReflectiveObject
import org.apache.spark.launcher.SparkLauncher

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

case class Jar(className: String, path: String) {

  def launcher(args: Seq[String])(
    deployMode: String = Jar.CLUSTER_MODE
  ): Process = {
    val stepName = className + path + args.hashCode.toString
    pureconfig.loadConfig[Map[String, String]]("svc.spark.config").toTry.get
      .foldLeft(
        new SparkLauncher()
          .directory(new File(Jar.workingDirectory))
          .setAppName(stepName)
          .setMaster("yarn") // TODO make this configurable
          .setDeployMode(deployMode)
          .setJavaHome(Jar.javaHome)
          .setConf("spark.yarn.appMasterEnv.JAVA_HOME", Jar.javaHome)
          .setConf("spark.executorEnv.JAVA_HOME", Jar.javaHome))((a, b) => (a.setConf _).tupled(b))
      .setMainClass(className)
      .setAppResource(path)
      .addAppArgs(args: _*)
      .setVerbose(true)
      .redirectError()
      .redirectOutput(new File(
        Jar.workingDirectory + s"/$className@${java.time.LocalTime.now()} --${args.mkString(" --")}.log"))
      .launch()
  }
}

object Jar {
  val CLUSTER_MODE = "cluster"
  val CLIENT_MODE = "client"
  val javaHome: String = System.getProperty("java.home")
  val workingDirectory: String = System.getProperty("user.dir")

  def apply[A: ClassTag : TypeTag](obj: A): Jar = new Jar(obj.getClassName, obj.getJarPath)
}