package datacentral.data.utils.extensions

import datacentral.data.utils.extensions.IntExtensions.IntWrapper
import datacentral.data.utils.extensions.sequences.IteratorExtensions.IteratorWrapper
import datacentral.data.utils.extensions.sequences.TupleExtensions.TraversableOnceWrapper
import org.scalatest.{BeforeAndAfterEach, FunSuite}


class IteratorWrapperTest extends FunSuite with BeforeAndAfterEach {

  val n = 1000 // give a sequence of length 500500
  val seq: IndexedSeq[Int] = (1 to n).flatMap(int => Iterator.fill(int)(int)) // (1,2,2,3,3,3,4,4,4,4,5,... etc)
  var wrapper: IteratorWrapper[Int] = _

  override def beforeEach(): Unit = {
    wrapper = IteratorWrapper(seq) // re-instantiate the wrapper for each test
  }

  override def afterEach() {}

  test("testBatchProcessSubset") {
    val result: Seq[Int] = wrapper.batchProcessSubset(
      batchSize = 4,
      predicate = (int: Int) => int.isOdd,
      processor = (seq: IndexedSeq[Int]) => seq.map(_ * 2) // should multiply the odd number only
    ).toSeq

    assert(result.length == seq.length)
    assert(result.forall(_.isEven))
    assert(result.sum == (seq.filter(_.isOdd).sum * 2 + seq.filter(_.isEven).sum))
  }

  test("testGroupContigousElementsBy") {
    val result: Iterator[(Boolean, Seq[Int])] = wrapper.contiguousGroupBy(_.isEven)

    assert(result.length == n)
    assert(result.zipWithKey(_._2.head)
      .forall(tuple => {
        tuple.value._1 == tuple.key.isEven && tuple.value._2 == Seq.fill(tuple.key)(tuple.key)
      }))
  }

  test("testGroupContigousElementsBy should give no stack overflow error") {
    val num = seq.length
    val result: Seq[(Int, Seq[Int])] = (1 to num).iterator.contiguousGroupBy(identity).toSeq
    assert(result.size == num)
    assert(result.forall(tuple => tuple._2 == Seq(tuple._1)))
  }

  test("testContiguousGroups") {
    val result: Stream[Seq[Int]] = wrapper.contiguousGroups(_ == _).toStream // put each block of n x 'n' in a separate sequence

    assert(result.forall(group => group.length == group.head))
    assert(result.length == n)
    assert(result.head == Seq(1))
    assert(result.last == Seq.fill(n)(n))
  }

  test("testContiguousGroups should give no stack overflow error") {
    val output: Seq[Seq[Int]] = wrapper.contiguousGroups((_, _) => false).toSeq
    assert(output.length == seq.length)
    assert(
      output.zip(seq).forall(pair => pair._1 == Seq(pair._2))
    )
  }

  test("testIterator") {
    assert(wrapper.iterator.length == (1 to n).sum)
    assert(wrapper.iterator.isEmpty) // iterator should be empty after one use
  }

  test("testGetHeadnTailOption") {
    val (int, iter) = wrapper.getHeadnTailOption.get

    assert(int == 1)
    assert(iter.next() == 2)
    assert(iter.next() == 2)
    assert(iter.next() == 3)
  }
}