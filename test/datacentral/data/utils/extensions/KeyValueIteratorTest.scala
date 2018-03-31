package datacentral.data.utils.extensions

import datacentral.data.utils.extensions.IntExtensions.IntWrapper
import datacentral.data.utils.extensions.sequences.IteratorExtensions.KeyValueIterator
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class KeyValueIteratorTest extends FunSuite with BeforeAndAfterEach {
  val n = 10
  val seq: IndexedSeq[Int] = (1 to n).flatMap(int => Iterator.fill(int)(int)) // (1,2,2,3,3,3,4,4,4,4,5,... etc)
  var wrapper: KeyValueIterator[Boolean, Traversable[Int]] = _

  override def beforeEach() {
    wrapper = KeyValueIterator(seq, key = (v: Int) => v.isOdd)
  }

  override def afterEach() {

  }

  test("testKeys") {
    val result = wrapper.keys
    assert(!result.next)
    assert(result.next)
  }

  test("testValues") {
    val result = wrapper.values
    assert(result.next.forall(_.isEven))
    assert(result.next.forall(_.isOdd))
  }

  test("testUnzip") {
    val (keys, seqs) = wrapper.unzip
    assert(!keys.next)
    assert(keys.next)
    assert(seqs.next.forall(_.isEven))
    assert(seqs.next.forall(_.isOdd))
  }

  test("testIterator") {
    assert(wrapper.iterator.length == 2)
    assert(wrapper.iterator.isEmpty) // iterator should be empty after one use
  }

}
