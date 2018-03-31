package datacentral.data.utils.reflection

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/**
  * Not tested
  */
object Macros {

  def desugar(expr: Any) = macro _desugar

  def _desugar(c: blackbox.Context)(expr: c.Expr[Any]): c.Expr[Unit] = {
    import c.universe._
    println(show(expr.tree))
    reify {}
  }
}
