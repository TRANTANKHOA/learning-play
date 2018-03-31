package datacentral.data.utils.reflection

import scala.reflect.api.JavaUniverse
import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._
import scala.reflect.{ClassTag, classTag}
import scala.tools.reflect.ToolBox
import scala.util.Try

object Functions {

  val universe: JavaUniverse = scala.reflect.runtime.universe
  val mirror: universe.Mirror = universe.runtimeMirror(getClass.getClassLoader)
  //noinspection TypeAnnotation
  val toolbox = runtimeMirror(getClass.getClassLoader).mkToolBox()

  implicit class ReflectiveObject[T: ClassTag : TypeTag](obj: T) {
    lazy val getType: Type = typeOf[T]
    lazy val getClassName: String = mirror.classSymbol(obj.getClass).fullName
    lazy val getJarPath: String = {
      new java.io.File(
        classTag[T]
          .runtimeClass
          .getProtectionDomain
          .getCodeSource
          .getLocation
          .getPath
      ).getAbsolutePath
    }

    def isSubtypeOf[S: TypeTag](anotherClass: Class[S]): Boolean = typeOf[T] <:< typeOf[S]

    def isConformedWith[S: TypeTag](anotherClass: Class[S]): Boolean = typeOf[T] weak_<:< typeOf[S]

    def isSameTypeWith[S: TypeTag](anotherClass: Class[S]): Boolean = typeOf[T] =:= typeOf[S]

    def getAllValsAsInstancesOf[S]: Try[Iterable[S]] = Try {
      val instanceMirror = currentMirror.reflect(obj)

      instanceMirror.symbol.asClass.typeSignature.members
        .filter(s => s.isTerm && s.asTerm.isAccessor)
        .map(instanceMirror reflectMethod _.asMethod)
        .map(_.apply().asInstanceOf[S])
    }

  }

  def compile[T: TypeTag](obj: T): T = {
    val compiledCode = toolbox.compile(getRunTimeTree(obj))
    compiledCode().asInstanceOf[T]
  }

  def parse[S](externalCode: String): S = {
    val tree = toolbox.parse(externalCode)
    val compiledCode = toolbox.compile(tree)
    compiledCode().asInstanceOf[S]
  }

  def eval[T: TypeTag](obj: T): Any = toolbox.eval(getRunTimeTree(obj))

  def getRunTimeTree[T: TypeTag](obj: T): Tree = reify(obj).tree

  /**
    * Untested
    *
    * @param args
    * @tparam T
    * @return
    */
  def instance[T: ClassTag](args: AnyRef*): T = {
    val cls = implicitly[ClassTag[T]].runtimeClass
    val constructor = cls.getConstructors.head
    val value = constructor.newInstance(args: _*)
    value.asInstanceOf[T]
  }

  def instance(name: String)(args: AnyRef*): Any =
    Class.forName(name).getConstructors.head.newInstance(args: _*)

  def getClassName[T: ClassTag]: String = implicitly[ClassTag[T]].runtimeClass.getName
}