package datacentral.data.utils.functional

import scala.annotation.tailrec
import scala.language.{higherKinds, reflectiveCalls}

trait RecursiveMethods {

  /**
    * Lazy recursion is nicer, but here is the tail-recursive version.
    *
    * @param remainder       is the input sequence to be processed
    * @param accumulator
    * @param split           is the real work need to be done on the sequence
    * @param append
    * @param predicate4split is the condition to stop processing early
    * @tparam IN
    * @tparam OUT
    * @tparam Collection type of collection of the input, e.g. iterator, buffer etc.
    * @tparam Collector  type of collection of the output, e.g. iterator, buffer etc.
    * @return
    */
  @tailrec
  final def recursWith[IN, OUT,
  Collection[_] <: {def isEmpty : Boolean}, // recurs on any collection with a 'nonEmpty' method defined.
  Collector[_] // give the output collection a different type
  ](
     remainder: Collection[IN],
     accumulator: Collector[OUT],
     split: Collection[IN] => (Collection[IN], OUT),
     append: (Collector[OUT], OUT) => Collector[OUT],
     predicate4split: (Collection[IN], Collector[OUT]) => Boolean = (_: Collection[IN], _: Collector[OUT]) => true
   ): Collector[OUT] =
  if (!remainder.isEmpty) {
    if (predicate4split(remainder, accumulator)) {
      val (nextRemainder, out) = split(remainder)
      recursWith(nextRemainder, append(accumulator, out), split, append, predicate4split)
    } else
      accumulator
  } else
    accumulator

  /** Tail-lazy recursion is a valid alternative to tail-recursion pattern
    *
    * @param remainder is the input sequence to be processed
    * @param split     is the real work need to be done on the sequence
    * @param predicate is the condition on the current remainder to stop processing early
    * @tparam IN         type of input elements
    * @tparam OUT        type of output elements
    * @tparam Collection type of collection of the input, e.g. iterator, buffer etc.
    * @return a stream of output values
    */
  def lazyRecursiveWith[IN, OUT, Collection[_] <: {def isEmpty : Boolean}]
  (
    remainder: Collection[IN],
    split: Collection[IN] => (Collection[IN], OUT),
    predicate: (Collection[IN] => Boolean) = (_: Collection[IN]) => true
  ): Stream[OUT] =
    if (!remainder.isEmpty) {
      if (predicate(remainder)) {
        val (nextRemainder, out) = split(remainder)
        Stream.cons(out, lazyRecursiveWith(nextRemainder, split, predicate))
      } else
        Stream.empty
    } else Stream.empty
}
