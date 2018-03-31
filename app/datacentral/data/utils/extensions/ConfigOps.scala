package datacentral.data
package utils.extensions

import java.sql.Date

import com.typesafe.config.{Config, ConfigMergeable, ConfigObject}
import datacentral.data.utils.extensions.TryExtensions._

import scala.collection.JavaConverters._
import scala.util.{Success, Try}


final class ConfigOps(val self: Config) extends AnyVal {
  def hasNonEmptyList(path: String): Boolean =
    Try(self.getList(path).asScala.nonEmpty).getOrElse(false)

  def hasNonEmptySet(path: String): Boolean =
    Try(self.getConfig(path).root.entrySet().asScala.nonEmpty).getOrElse(false)

  def getStringTupleSeqSafely(path: String): Try[Seq[(String, String)]] = Try {
    val tupleConfig = self.getConfig(path)
    tupleConfig
      .root.entrySet().asScala.toSeq
      .map(entry => (entry.getKey, tupleConfig.getString(entry.getKey)))
  }

  def getStringSafely(path: String): Try[String] =
    Try(self.getString(path))

  def getIntSafely(path: String): Try[Int] =
    Try(self.getInt(path))

  def getLongSafely(path: String): Try[Long] =
    Try(self.getLong(path))

  def getDoubleSafely(path: String): Try[Double] =
    Try(self.getDouble(path))

  def getDateSafely(path: String): Try[Date] =
    Try(Date.valueOf(self.getString(path)))

  def getStringSeqSafely(path: String): Try[Seq[String]] =
    Try(self.getStringList(path).asScala)

  def getConfigFor(path: String): Try[Config] =
    Try(self.getConfig(path))

  def getConfigSeqSafely(path: String): Try[Seq[Config]] =
    Try(self.getConfigList(path).asScala)

  def getConfigObjectSafely(path: String): Try[ConfigObject] =
    Try(self.getObject(path))

  def getItemsSafely[A](path: String, func: Config => Try[A]): Try[Seq[A]] =
    getConfigSeqSafely(path).flatMap { configs => configs.map(func).sequence.map(_.toSeq) }

  def withFallbackSafely(default: ConfigMergeable): Try[Config] =
    Try(self.withFallback(default))

  def getOption[A](path: String, func: Config => String => Try[A]): Try[Option[A]] =
    if (self.hasPath(path))
      func(self)(path).map(Some.apply)
    else
      Success(None)
}
