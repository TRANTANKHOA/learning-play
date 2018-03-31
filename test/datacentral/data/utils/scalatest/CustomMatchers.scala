package datacentral.data.utils.scalatest

import org.scalatest.Matchers

import scala.collection.GenTraversable
import scala.util.Try

object CustomMatchers extends Matchers {

  implicit class TryMatcher[A](a: Try[A]) {

    def matchSuccess(expected: A)
                    (implicit equality: (A, A) => Boolean = _ === _): Unit = {
      val found = a.get
      assert(equality(found, expected))
    }

    def assertSuccess(): Unit = assert(a.isSuccess)

    def assertFailure(message: String): Unit = assert(a.isFailure, message)
  }

  implicit class TriedSequenceMatcher(res: Try[GenTraversable[_]]) extends SequenceMatcher(res.get)

  implicit class SequenceMatcher(found: GenTraversable[_]) {

    def containTheSameElementsAs(expected: GenTraversable[_]): Unit = {
      val missingInLeft = (found.toSeq diff expected.toSeq, "actual")
      val missingInRight = (expected.toSeq diff found.toSeq, "expected")
      val errMsg: String = List(missingInLeft, missingInRight)
        .filter(_._1.nonEmpty)
        .map { case (fields, entity) => s"Element(s) missing from $entity: $fields" }
        .mkString("\n")

      assert(errMsg.isEmpty, s"Actual [$expected] do not match expected [$found]\n" + errMsg)
    }
  }

}
