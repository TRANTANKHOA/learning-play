package datacentral
import com.typesafe.config.Config
import datacentral.data.utils.extensions._
import pureconfig.error.ConfigReaderFailures

import scala.language.implicitConversions
import scala.util.Either

package object data {

  implicit def toThrowableOps(throwable: Throwable): ThrowableOps = new ThrowableOps(throwable)

  implicit def toPureConfigOps[A](self: Either[ConfigReaderFailures, A]): PureConfigOps[A] = new PureConfigOps(self)

  implicit def toConfigOps(config: Config): ConfigOps = new ConfigOps(config)


}
