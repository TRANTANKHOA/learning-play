package datacentral.data.utils.reflection

import datacentral.data.utils.reflection.Functions._
import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.reflect.runtime.universe._

class ReflectiveObjectTest extends FunSuite with BeforeAndAfterEach {

  object TestObject

  class TestClass

  case class TestCaseClass(int: Int)

  override def beforeEach() {

  }

  override def afterEach() {

  }

  test("testIsSameTypeWith") {
    assert("string".isSameTypeWith(classOf[String]))
  }

  test("testCompile") {
    assert(Functions
             .compile[TestCaseClass](TestCaseClass(0))
             .int == 0)
  }

  test("testParse") {
    assert(Functions.parse[Seq[Int]]("Seq(0)").head == 0)
  }

  test("testGetRunTimeTree") {
    assert(Functions.getRunTimeTree(TestCaseClass(0)).nonEmpty) // I don't know what to do with a tree but 'eval' pass, so this should be okay
  }

  test("testIsSubtypeOf") {
    assert(Seq(1).isSubtypeOf(classOf[PartialFunction[Int, Int]]))
  }

  test("testEval") {
    assert(Functions.eval(Seq(1)).asInstanceOf[Seq[Int]] == Seq(1))
  }

  test("testGetType") {
    assert(Seq(1).getType.toString == typeOf[Seq[Int]].toString)
  }

  test("testIsConformedWith") {
    assert(1.isConformedWith(classOf[Double]))
  }

  test("testGetClassName") {
    assert(TestObject.getClassName.toString == "datacentral.data.utils.reflection.ReflectiveObjectTest.TestObject")
  }

  test("testGetJarPath") {
    assert(TestObject.getJarPath contains "/target/scala-2.11/test-classes")
  }
}
