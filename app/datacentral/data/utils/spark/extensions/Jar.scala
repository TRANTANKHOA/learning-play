package datacentral.data.utils.spark.extensions

import java.io.File

import datacentral.data.utils.reflection.Functions.ReflectiveObject
import org.apache.spark.launcher.SparkLauncher

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

case class Jar(className: String, path: String) {

  def launcher(args: Seq[String]): Process = new SparkLauncher()
    .directory(new File(Jar.workingDirectory))
    .setAppName(className)
    .setMaster("yarn") // TODO make this configurable
    .setDeployMode("cluster")
    .setJavaHome(Jar.javaHome)
    .setConf("spark.yarn.appMasterEnv.JAVA_HOME", Jar.javaHome)
    .setConf("spark.executorEnv.JAVA_HOME", Jar.javaHome)
    .setMainClass(className)
    .setAppResource(path)
    .addAppArgs(args: _*)
    .setVerbose(true)
    .launch()
}

object Jar {
  val javaHome: String = System.getProperty("java.home")
  val workingDirectory: String = System.getProperty("user.dir")

  def apply[A: ClassTag : TypeTag](obj: A): Jar = new Jar(obj.getClassName, obj.getJarPath)
}