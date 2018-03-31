package datacentral.data.utils.config

import com.typesafe.config.{Config, ConfigFactory}
import datacentral.data.utils.reflection.Functions.ReflectiveObject

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

trait ConfigProvider {
  def configOf[A: ClassTag : TypeTag](obj: A): Config = ConfigProvider.defaultConfig.getConfig(obj.getClassName)
}

object ConfigProvider {
  lazy val defaultConfig: Config = ConfigFactory.load()
}