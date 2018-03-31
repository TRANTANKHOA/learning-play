package datacentral

import com.typesafe.config.Config
import datacentral.data.utils.extensions.{ConfigOps, ThrowableOps}

import scala.language.implicitConversions

package object data {

  implicit def toThrowableOps(throwable: Throwable): ThrowableOps = new ThrowableOps(throwable)

  implicit def toConfigOps(config: Config): ConfigOps = new ConfigOps(config)
}
