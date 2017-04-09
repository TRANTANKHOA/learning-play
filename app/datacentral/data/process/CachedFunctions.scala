package datacentral.data.process

import scala.collection.concurrent.TrieMap

class CachedFunctions[-IN, +OUT](f: IN => OUT) extends (IN => OUT) {

  private[this] val cache = TrieMap.empty[IN, OUT]

  def apply(x: IN): OUT = {
    cache.getOrElse(x, {
      val y = f(x)
      cache += ((x, y))
      y
    })
  }
}

object CachedFunctions {
  def apply[In, Out](f: In => Out) = new CachedFunctions(f)
}
