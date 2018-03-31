package datacentral.data.utils.functional

import java.time.{LocalDateTime, ZoneId}

import org.slf4j.Logger

import scala.collection.concurrent.TrieMap

trait SideEffects {

  case class DoNot(cache: Boolean = false)

  case class CachedFunction[-IN, +OUT](function: IN => OUT) {
    private[this] val cache = TrieMap.empty[IN, OUT]

    def apply(in: IN)(implicit doNot: DoNot = DoNot()): OUT =
      if (doNot.cache)
        function(in)
      else cache.getOrElse(in, {
        val out = function(in)
        cache += ((in, out))
        out
      })
  }

  case class Loggable[-IN, +OUT](function: IN => OUT)(implicit logger: Logger) extends (IN => OUT) {
    def apply(in: IN): OUT = {
      logger.info(function.getClass.getName)
      logger.info(LocalDateTime.now(ZoneId.of("Australia/Sydney")).toString)
      logger.info(s"IN = $in")
      val start = System.currentTimeMillis()
      val out = function(in)
      val duration = System.currentTimeMillis() - start
      logger.info(s"OUT = $out\nDuration = ${duration / 1e3} sec.")
      out
    }
  }

}
