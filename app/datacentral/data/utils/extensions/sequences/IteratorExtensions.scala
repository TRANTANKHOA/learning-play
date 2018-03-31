package datacentral.data.utils.extensions.sequences

import datacentral.data.utils.functional.SmartFunctions.recursWith

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer
import scala.language.{higherKinds, implicitConversions}
import scala.util.{Failure, Success, Try}

/**
  * INTRO: "An iterator is not a collection, but rather a way to access the elements of a collection one by one."
  * This is a very important distinction. Iterator is mutable and traversable once. Hence it must only be used once
  * and throw away, except with `next` and `hasNext`.
  *
  * The extension methods provided in this object are intended for use in consuming iterators given in Spark partitions.
  * Due to the possibly large size of any given partitions, these methods are implemented with these considerations
  *
  * 1. Loading an entire iterator (or any proportion of it considered at the same scale) into memory is not allowed.
  *
  * 2. The output must be an iterator that is built on-demand, one element after another without loading any elements
  * more that necessary to build the current output element.
  *
  * 3. Traversing the original input iterator (or any proportion of it considered at the same scale) repeatedly while
  * building the output elements will be slow and is not allowed.
  *
  * 4. Possible implementations are tail-recursive, tail-lazy recursion schemes or building iterator with `next` and
  * `hasNext`.
  *
  * 5. At current version of Scala (2.11.8), chaining `duplicate` calls while processing an iterator will lead to stack
  * overflow error. So this is also not allowed.
  *
  * WARNING:
  * Iterator trait has many surprising or easy-to-misunderstood behaviors.
  * For example, checking size would necessarily empty an iterator:
  * {{{
  * scala> val it = List("a","b","c").iterator
  * it: Iterator[String] = non-empty iterator
  * *
  * scala> it.size
  * res4: Int = 3
  * *
  * scala> it.size
  * res5: Int = 0
  * }}}
  *
  * Another example:
  * {{{
  * scala> val it = List(1,2,3,4,5,6,7).iterator
  * it: Iterator[Int] = non-empty iterator
  * *
  * scala> it.filter(_ > 3)
  * res0: Iterator[Int] = non-empty iterator
  * *
  * scala> it.toList
  * res1: List[Int] = List(5, 6, 7) // where is 4?
  * }}}
  */
object IteratorExtensions {

  /**
    * Template for building an iterator of `A` elements
    *
    * @param charge produces the next batch of `A`s or an empty sequence, should not throw error here
    * @tparam A
    * @return Iterator[A] which is lazy and low memory consumption
    */
  def buildIteratorWith[A](
                            charge: => Traversable[A]
                          ): Iterator[A] = new Iterator[A] {

    private val currentElements: ArrayBuffer[A] = new ArrayBuffer[A]

    def hasNext: Boolean = if (currentElements.nonEmpty)
      true
    else Try(charge) match {
      case Success(values) =>
        currentElements ++= values
        currentElements.nonEmpty
      case Failure(_) => false
    }

    def next(): A = if (hasNext)
      currentElements.remove(0)
    else
      throw new RuntimeException("Calling next on an empty iterator")
  }

  /**
    * A different version for use in spark, because sometime the
    * charger produce the entire iterator by itself.
    *
    * @param charge
    * @tparam A
    * @return
    */
  def buildIteratorBy[A](
                          charge: => Iterator[A]
                        ): Iterator[A] = new Iterator[A] {

    private var currentElements: Iterator[A] = Iterator.empty

    def hasNext: Boolean = if (currentElements.nonEmpty)
      true
    else {
      currentElements = charge
      currentElements.nonEmpty
    }

    def next(): A = if (hasNext)
      currentElements.next()
    else
      throw new RuntimeException("Calling next on an empty iterator")
  }

  implicit class IteratorWrapper[+A](val iterator: Iterator[A]) {

    /** Groups the input iterator into chunks of contiguous elements, using the provided
      * equality operation. It's recommended to use this method on a sorted iterator if possible
      *
      * @param equal The equality operation which is used to compare each element in the input iterator with the element
      *              which follows it. When this returns false, the element on the left hand side will be placed in the contiguous block,
      *              and a new contiguous block will be started for the element on the right hand side.
      * @return a Stream containing the sequences of elements in each continuous group
      */
    def contiguousGroups(equal: (A, A) => Boolean): Iterator[Seq[A]] = {
      val buffer = iterator.buffered
      buildIteratorWith[Seq[A]](
        charge = if (buffer.hasNext) {
          val head = buffer.next()
          val tail: Seq[A] = buffer.takeWhileBuffer(equal(head, _)).toSeq
          Traversable(head +: tail)
        } else Traversable.empty[Seq[A]]
      )
    }

    /**
      * A different version for use in spark, because sometime the
      * charger produce the entire iterator by itself.
      */
    def contiguousGroupsIter(equal: (A, A) => Boolean): Iterator[Iterator[A]] = {
      var buffer = iterator.buffered
      buildIteratorBy[Iterator[A]] {
        if (buffer.nonEmpty) {
          val head = buffer.head
          val (pre, suf) = buffer.span(equal(head, _))
          buffer = suf.buffered
          require(pre.nonEmpty, s"Your predicate produces a constant FALSE value, remaining values = ${buffer.mkString("|")}")
          Iterator(pre)
        } else Iterator.empty

      }
    }

    /**
      * Grouping elements with the same key into a tuple if they are next ot each other. This is an adaptation of the
      * conventional `groupBy` method to the given requirements mentioned in the introduction.
      *
      * @param key a function to compute the key for each element
      * @tparam Key type of key
      * @return a new iterator over the groups
      */
    def contiguousGroupBy[Key](key: A => Key): Iterator[(Key, Seq[A])] = {
      val buffer = iterator.buffered
      buildIteratorWith[(Key, Seq[A])](
        charge = if (buffer.hasNext) {
          val head = buffer.next()
          val thisKey = key(head)
          val tail: Seq[A] = buffer.takeWhileBuffer(thisKey == key(_)).toSeq
          Traversable((thisKey, head +: tail))
        } else Traversable.empty[(Key, Seq[A])]
      )
    }

    /**
      * A different version for use in spark, because sometime the
      * charger produce the entire iterator by itself.
      */
    def groupIterBy[Key](key: A => Key): Iterator[(Key, Iterator[A])] = {
      var buffer = iterator.buffered
      buildIteratorBy[(Key, Iterator[A])] {
        if (buffer.nonEmpty) {
          val thisKey = key(buffer.head)
          val (pre, suf) = buffer.span(thisKey == key(_))
          buffer = suf.buffered
          Iterator((thisKey, pre))
        } else Iterator.empty
      }
    }

    /**
      * Common template for building new iterator from contiguous element in the current iterator
      *
      * Recursive implementation will not satisfy Spark's requirement since all results will be kept in memory
      *
      * This actually performs much faster!
      *
      * @param buildKey produces a key set for the head or the next elements
      * @param checkKey checks the keys between the head and the next elements
      * @param keepVal  keeps the value of the head or the next elements
      * @param buildOut produces the `next` output from the accumulator and the head's key
      * @tparam IN  needs not to be the same as `A`
      * @tparam OUT
      * @tparam KEY is more like a container for the actual key
      * @return new Iterator[OUT]
      */
    @deprecated("Use `buildIteratorWith` instead", "09-10-2017")
    private def build[IN, OUT, KEY](
                                     buildKey: A => KEY,
                                     checkKey: (KEY, KEY) => Boolean,
                                     keepVal: KEY => IN,
                                     buildOut: (ArrayBuffer[IN], KEY) => OUT
                                   ) = new Iterator[OUT] {

      def zipHead: Try[KEY] = if (iterator.isEmpty)

      /**
        * isEmpty will trigger hasNext, which if the iterator is coming from Spark then will trigger a side-effect
        * called getNext, so the following Try need to be in such a way. This is bad on the part of Spark implementation
        */
        Failure(new NoSuchElementException)
      else {
        val head = iterator.next
        Success(buildKey(head))
      }

      var tryHead: Try[KEY] = zipHead

      def hasNext: Boolean = tryHead match {
        case Success(_) => true;
        case Failure(_) => false
      }

      import scala.util.control.Breaks.{break, breakable}

      def next(): OUT = {
        tryHead match {
          case Success(head) =>
            val accumulator = new ArrayBuffer[IN]
            accumulator += keepVal(head)
            breakable {
              while (true) {
                val tryNext: Try[KEY] = zipHead
                // end of iterator or found new head
                if (tryNext.isFailure || !checkKey(tryNext.get, head)) {
                  tryHead = tryNext
                  break
                } else {
                  accumulator += keepVal(tryNext.get) // found a match and accumulate
                }
              }
            }
            buildOut(accumulator, head)
          case Failure(e) => throw e
        }
      }
    }

    /**
      * Spliting the head and tail of the iterator in two and wrap in an option
      *
      * @return
      */
    def getHeadnTailOption: Option[(A, Iterator[A])] = if (iterator.hasNext) Some((iterator.next, iterator)) else None

    /** Consumes an iterator, processing elements which match the predicate in batches up to batchSize.
      *
      * The returned iterator outputs elements which do not match the
      * discriminator predicate as soon as they are encountered in the input
      * iterator. Elements which do match are buffered until either the batch
      * size is reached, or the end of the input is encountered. Once either of
      * these things happen, the buffered batch of elements is processed using
      * the processor function, and then output in the order in which they were
      * encountered in the input iterator.
      *
      * @param batchSize the size of batches to process.
      * @param predicate a predicate which determines which elements of the
      *                  input iterator are considered for batch processing.
      * @param processor a function which transforms a batch of elements which
      *                  match the discriminator predicate.
      * @return an iterator which contains elements which do not meet the
      *         discriminator predicate in iterated order, intermingled with batches of
      *         processed elements.
      */
    def batchProcessSubset[B >: A](
                                    batchSize: Int,
                                    predicate: A => Boolean,
                                    processor: IndexedSeq[A] => IndexedSeq[B]): Iterator[B] = {
      require(batchSize > 0, s"batchSize must be a positive number, but was $batchSize")

      new Iterator[B] {

        /**
          * This object should automatically `charge`, `process` and `discharge` elements for the output iterator on-demand
          */
        private object Cache {
          private var in = new ArrayBuffer[A]
          private var out = new ArrayBuffer[B]

          def canCharge: Boolean = out.isEmpty && in.length < batchSize

          def canProcess: Boolean = out.isEmpty && (in.length >= batchSize || (iterator.isEmpty && in.nonEmpty))

          def canDischarge: Boolean = out.nonEmpty

          def nonEmpty: Boolean = canDischarge || canProcess

          def charge(a: A): Unit =
            if (canCharge)
              in += a
            else
              throw new RuntimeException("Cannot charge")

          def process: Unit = {
            out ++= processor(in)
            in.clear
          }

          def discharge: B = out.headOption match {
            case Some(head) =>
              out = out.drop(1)
              head
            case None => throw new RuntimeException("Cannot discharge")
          }
        }

        def hasNext: Boolean = iterator.hasNext || Cache.nonEmpty

        @tailrec
        def next: B =
          if (Cache.canDischarge)
            Cache.discharge
          else if (Cache.canProcess) {
            Cache.process // after process, we should be able to discharge again
            next
          } else {
            // must keep on charging
            val nxt = iterator.next
            if (predicate(nxt)) {
              Cache.charge(nxt)
              next
            } else
              nxt // easy return
          }
      }
    }
  }

  object IteratorWrapper {
    def apply[A](seq: Traversable[A]): IteratorWrapper[A] = new IteratorWrapper(seq.toIterator)
  }

  implicit class BufferedIteratorWrapper[+A](val iterator: BufferedIterator[A]) extends AnyVal {

    /** Performs the same function as Iterator.span, but immediately reads the prefix into a buffer.
      *
      * This is intended to overcome a limitation with span, where because both results are iterators,
      * it must provide two new instances which refer back to the original. When span is called successively
      * on its suffix to consume an iterator, this can produce a stack overflow exception for large sizes.
      *
      * @param predicate the test predicate.
      * @return (prefix, iterator) a pair consisting of the prefix and the same iterator instance advanced
      */
    def consumeSpan(predicate: A => Boolean): (Traversable[A], BufferedIterator[A]) = {
      val prefix = takeWhileBuffer(predicate)

      (prefix, iterator)
    }

    // This function has the same functionality with iterator.span(p) but gives Seq[A] for the qualified elements
    // and leave the remaining iterator in-place. If possible, consider using built-in `span` as a better method
    def takeWhileBuffer(checkElement: A => Boolean): Traversable[A] = recursWith[A, A, BufferedIterator, Traversable](
      remainder = iterator,
      accumulator = Traversable.empty,
      split = (remainder: BufferedIterator[A]) => {
        val out = remainder.next
        (remainder, out)
      },
      append = (accumulator: Traversable[A], out: A) => accumulator ++ Traversable(out),
      predicate4split = (remainder: BufferedIterator[A], _) => checkElement(remainder.head)
    )

    /**
      * Discard the leading elements that do not pass `checkElement`
      *
      * @param checkElement
      * @return
      */
    def discardWhileBuffer(checkElement: A => Boolean): Traversable[A] = takeWhileBuffer(!checkElement(_))
  }

  object BufferedIteratorWrapper {
    def apply[A](seq: Traversable[A]): BufferedIteratorWrapper[A] = new BufferedIteratorWrapper[A](seq.toIterator.buffered)
  }

  implicit class KeyValueIterator[K, V](val iterator: Iterator[(K, V)]) extends AnyVal {
    def unzip: (Iterator[K], Iterator[V]) = {
      val (it1, it2) = iterator.duplicate
      (it1.keys, it2.values)
    }

    def keys: Iterator[K] = iterator.map(_._1)

    def values: Iterator[V] = iterator.map(_._2)
  }

  object KeyValueIterator {
    def apply[K, V](seq: Traversable[V], key: V => K): KeyValueIterator[K, Traversable[V]] = new KeyValueIterator[K, Traversable[V]](
      seq.groupBy(key).toIterator
    )
  }

}