package datacentral.data.utils.extensions.sequences

import scala.language.{higherKinds, reflectiveCalls}

object WrapperExtensions {

  implicit class MapTwice[
  A[D] <: {def map[E](func : (D => E)) : A[E]},
  B[D] <: {def map[E](func : (D => E)) : B[E]},
  C
  ](self: A[B[C]]) {
    def mapResult[D](transform: C => D): A[B[D]] = self.map(_.map(transform))
  }

}
