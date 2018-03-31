package datacentral.data.utils.extensions

import datacentral.data.utils.extensions.sequences.IteratorExtensions._
import datacentral.data.utils.scalatest.UnitSpec

class IteratorExtensionsSuite extends UnitSpec {
  behavior of "decapitate"
  it should "return None when the iterator is empty" in {
    val x = Iterator.empty
    val result = x.getHeadnTailOption
    result shouldBe None
  }

  it should "return the head and an empty iterator when there is one element" in {
    // Arrange:
    val x = Iterator.single("hello")

    // Act:
    val result = x.getHeadnTailOption

    // Assert:
    result shouldBe defined
    val (head, body) = result.get
    head should equal("hello")
    body shouldBe empty
  }

  it should "return the head and an iterable iterator when there are two elements" in {
    // Arrange:
    val x = Iterator.range(13, 27)

    // Act:
    val result = x.getHeadnTailOption

    // Assert:
    result shouldBe defined
    val (head, body) = result.get
    head should equal(13)
    body.toArray should contain theSameElementsInOrderAs (14 until 27)
  }

  behavior of "groupBy"
  it should "return an empty sequence when input is empty." in {
    // Arrange:
    val input: Iterator[Int] = Iterator.empty

    // Act:
    val output = input.contiguousGroupBy(identity).toArray

    // Assert:
    output shouldBe empty
  }

  it should "return a sequence with one element when the input has one element." in {
    // Arrange:
    val input = Iterator(5)

    // Act:
    val output = input.contiguousGroupBy(identity).values.map(_.toSeq).toArray

    // Assert:
    output should contain theSameElementsAs Seq(Seq(5))
  }

  it should "return a sequence with one contiguous block when all two elements in the input are equal." in {
    // Arrange:
    val input = Seq(7, 7) // Also the expected lone element of the output
    val inputIterator = input.toIterator

    // Act:
    val output = inputIterator.contiguousGroupBy(identity).values.map(_.toSeq).toArray

    // Assert:
    output should contain theSameElementsAs Seq(input)
  }

  it should "return a sequence with two contiguous blocks when the two elements in the input are unequal." in {
    // Arrange:
    val input = Iterator(3, 7)

    // Act:
    val output = input.contiguousGroupBy(identity).values.map(_.toSeq).toArray

    // Assert:
    output should contain theSameElementsInOrderAs Seq(Seq(3), Seq(7))
  }

  it should "correctly group a sequence of 3 pairs of equal elements." in {
    // Arrange:
    val input = Iterator(11, 11, 13, 13, 17, 17)

    // Act:
    val output = input.contiguousGroupBy(identity).values.map(_.toSeq).toArray

    // Assert:
    output should contain theSameElementsInOrderAs Seq(Seq(11, 11), Seq(13, 13), Seq(17, 17))
  }

  it should "correctly group a sequence of 4 contiguous blocks of varying length." in {
    // Arrange:
    val input = Iterator(23, 23, 23, 19, 19, 19, 19, 19, 31, 31, 31, 31, 31, 31, 29, 29, 29, 29)

    // Act:
    val output = input.contiguousGroupBy(identity).values.map(_.toSeq).toArray

    // Assert:
    output should contain theSameElementsInOrderAs Seq(
      Seq(23, 23, 23),
      Seq(19, 19, 19, 19, 19),
      Seq(31, 31, 31, 31, 31, 31),
      Seq(29, 29, 29, 29))
  }

  behavior of "contiguousGroups"
  it should "return every element in its own block when the equality operator returns false." in {
    // Arrange:
    val input = Iterator(37, 37, 37, 37)

    // Act:
    val output: Seq[Seq[Int]] = input.contiguousGroups((_, _) => false).toSeq

    // Assert:
    output should contain theSameElementsInOrderAs Seq(Seq(37), Seq(37), Seq(37), Seq(37))
  }

  it should "not throw a stack overflow exception when grouping a large amount of data" in {
    // Our implementation using stream is not tail recursive - and so relies on the call by name
    // semantics of Stream to prevent stack overflow exceptions, i.e. the tail of the stream is stored
    // by reference so the recursive calls are not executed until we call '_.toArray'. However, since
    // toArray (or similar methods) is not a recursive construct, no stackOverflow should occur
    // Arrange:
    val inputSeq: IndexedSeq[Long] =
    for {
      value <- 1L to 1000000L
      _ <- 1 to 2
    } yield value

    val expectedRows: Seq[IndexedSeq[Long]] = inputSeq.grouped(2).toSeq

    // Act:
    val output = inputSeq.toIterator.contiguousGroups(_ == _).toSeq

    // Assert:
    output should contain theSameElementsInOrderAs expectedRows
  }

  behavior of "batchProcessSubset"
  it should "throw an exception when the batch size is less than 1." in {
    // Arrange:
    val x = Iterator[Int]()
    val batchSize = 0

    // Act & Assert:
    an[IllegalArgumentException] should be thrownBy
    x.batchProcessSubset(
      batchSize = batchSize,
      predicate = _ => true,
      processor = identity)
  }

  it should "return an empty iterator when provided with an empty iterator" in {
    // Arrange:
    val x = Iterator[Int]()

    // Act:
    val processed = x.batchProcessSubset(
      batchSize = 1,
      predicate = _ => true,
      processor = identity).toArray

    // Assert:
    processed shouldBe empty
  }

  it should "return the input collection in the same order when the batch size is 1." in {
    // Arrange:
    val x = Seq(1, 2, 4, 5)
    val discriminator = (_: Int) % 2 == 0

    // Act:
    val processed = x.toIterator.batchProcessSubset(
      batchSize = 1,
      predicate = discriminator,
      processor = identity).toArray

    // Assert:
    processed should contain theSameElementsInOrderAs x
  }

  it should "process a single batch of elements which meet the discriminator" in {
    // Arrange:
    val x = Seq(1, 2, 3, 4, 5, 6)
    val discriminator = (_: Int) % 2 == 0
    val processor = (x: IndexedSeq[Int]) => x.map {
      _ * 2
    }
    val batchSize = 3
    val expectedOutput = Seq(1, 3, 5, 4, 8, 12)

    // Act:
    val processed = x.toIterator.batchProcessSubset(
      batchSize = batchSize,
      predicate = discriminator,
      processor = processor).toArray

    // Assert:
    processed should contain theSameElementsInOrderAs expectedOutput
  }

  it should "process two batches of elements which meet the discriminator" in {
    // Arrange:
    val x = Seq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    val discriminator = (_: Int) % 2 == 0
    val processor = (x: IndexedSeq[Int]) => x.map {
      _ * 2
    }
    val batchSize = 3
    val expectedOutput = Seq(1, 3, 5, 4, 8, 12, 7, 9, 11, 16, 20, 24)

    // Act:
    val processed = x.toIterator.batchProcessSubset(
      batchSize = batchSize,
      predicate = discriminator,
      processor = processor).toArray

    // Assert:
    processed should contain theSameElementsInOrderAs expectedOutput
  }

  it should "return an element not matching the discriminator that at " +
            "the tail of the input immediately after a batch." in {
    // Arrange:
    val x = Seq(1, 2, 3, 4, 5, 6, 7)
    val discriminator = (_: Int) % 2 == 0
    val processor = (x: IndexedSeq[Int]) => x.map {
      _ * 2
    }
    val batchSize = 3
    val expectedOutput = Seq(1, 3, 5, 4, 8, 12, 7)

    // Act:
    val processed = x.toIterator.batchProcessSubset(
      batchSize = batchSize,
      predicate = discriminator,
      processor = processor).toArray

    // Assert:
    processed should contain theSameElementsInOrderAs expectedOutput
  }

  it should "process and return a batch which is not full upon reaching the end of its input" in {
    // Arrange
    val x = Seq(1, 2, 3)
    val discriminator = (_: Int) % 2 == 0
    val processor = (x: IndexedSeq[Int]) => x.map {
      _ * 2
    }
    val batchSize = 2
    val expectedOutput = Seq(1, 3, 4)

    // Act:
    val processed = x.toIterator.batchProcessSubset(
      batchSize = batchSize,
      predicate = discriminator,
      processor = processor).toArray

    // Assert:
    processed should contain theSameElementsInOrderAs expectedOutput
  }
}
