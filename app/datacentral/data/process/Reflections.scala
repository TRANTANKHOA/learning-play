package datacentral.data.process

import scala.reflect.runtime._

trait Reflections {
  def getAllValsAsInstancesOf[T]: Iterable[T] = try {
    val im = currentMirror reflect this
    im.symbol.asClass.typeSignature.members
      .filter(s => s.isTerm && s.asTerm.isAccessor)
      .map(im reflectMethod _.asMethod)
      .map(_.apply().asInstanceOf[T])
  } catch {
    case e: Throwable => throw new RuntimeException("Check if input type is correct")
  }
}
