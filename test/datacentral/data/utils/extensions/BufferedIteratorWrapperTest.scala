package datacentral.data.utils.extensions

import datacentral.data.utils.extensions.sequences.IteratorExtensions.BufferedIteratorWrapper
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class BufferedIteratorWrapperTest extends FunSuite with BeforeAndAfterEach {
  val n = 10
  val seq: IndexedSeq[Int] = (1 to n).flatMap(int => Iterator.fill(int)(int)) // (1,2,2,3,3,3,4,4,4,4,5,... etc)
  var wrapper: BufferedIteratorWrapper[Int] = _


  override def beforeEach() {
    wrapper = BufferedIteratorWrapper(seq) // re-instantiate the wrapper for each test
  }

  override def afterEach() {

  }

  test("testTakeWhileBuffer") {
    val result: Seq[Int] = wrapper.takeWhileBuffer(
      checkElement = element => element < 4
    ).toSeq // should collect the initial sequence until the last '3'

    val remain = wrapper.iterator.toSeq // should begins with 4 x '4' block and so on

    (1 to 3).foreach(int =>
      assert(result.count(_ == int) == int)
    )

    (4 to n).foreach(int =>
      assert(remain.count(_ == int) == int)
    )
  }

  test("testIterator") {
    assert(wrapper.iterator.head == 1)
    assert(wrapper.iterator.length == (1 to n).sum)
    assert(wrapper.iterator.isEmpty) // iterator should be empty after one use
  }

  test("testConsumeSpan") {
    val (result, rem) = wrapper.consumeSpan(
      element => element < 4
    ) // should collect the initial sequence until the last '3'

    val remain = rem.toSeq // should begins with 4 x '4' block and so on

    (1 to 3).foreach(int =>
      assert(result.count(_ == int) == int)
    )

    (4 to n).foreach(int =>
      assert(remain.count(_ == int) == int)
    )
  }
}