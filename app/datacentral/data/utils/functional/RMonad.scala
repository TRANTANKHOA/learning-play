package datacentral.data.utils.functional

import scala.language.{higherKinds, implicitConversions, reflectiveCalls}

abstract class RMonad[Conf, A] {

  val read: Conf => A

  def config(implicit conf: Conf): A = read(conf)

  def map[B](convert: A => B): RMonad[Conf, B] = RMonad(read andThen convert)

  def flatMap[C](convert: A => RMonad[Conf, C]): RMonad[Conf, C] = RMonad(conf => convert(read(conf)).read(conf))

  def expandConfig[BiggerConf](localise: BiggerConf => Conf): RMonad[BiggerConf, A] = RMonad(localise andThen read)
}

object RMonad {

  def unit[Conf, A](a: => A): RMonad[Conf, A] = RMonad(_ => a)

  def apply[Conf, A](func: Conf => A): RMonad[Conf, A]
  = new RMonad[Conf, A] {
    override val read: (Conf => A) = func
  }


  implicit def funcToReader[Conf, A](func: Conf => A): RMonad[Conf, A] = RMonad(func)

  implicit class WrappedReader[Conf, A, B[C] <: {def map[D](func : (C => D)) : B[D]}](wrapper: B[RMonad[Conf, A]]) {

    def mapTraverse[D](transform: A => D): RMonad[Conf, B[D]] = wrapper.mapResult(transform).traverse

    def traverse: RMonad[Conf, B[A]] = RMonad(conf => wrapper.map(_.read(conf)))

    def mapResult[D](transform: A => D): B[RMonad[Conf, D]] = wrapper.map(_.map(transform))
  }

}