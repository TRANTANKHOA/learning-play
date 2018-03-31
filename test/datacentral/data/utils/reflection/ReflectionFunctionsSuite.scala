package datacentral.data.utils.reflection

import datacentral.data.utils.reflection.Functions._
import datacentral.data.utils.scalatest.UnitSpec

class ReflectionFunctionsSuite extends UnitSpec {

  import ReflectionFunctionsSuite._

  behavior of "ReflectionFunctions"

  "getClassName[T](obj: T)" should "return the class name of an object" in {
    val result = ReflectionFunctionsTestObject.getClassName
    assert(result === "datacentral.data.utils.reflection.ReflectionFunctionsSuite.ReflectionFunctionsTestObject")
  }

  "getClassName[T](obj: T)" should "return the class name of an instantiated class" in {
    val result = (new ReflectionFunctionsTestClass).getClassName
    assert(result === "datacentral.data.utils.reflection.ReflectionFunctionsSuite.ReflectionFunctionsTestClass")
  }

  "getClassName[T]" should "return the class name of a case class" in {
    val result = ReflectionFunctionsTestCaseClass(1).getClassName
    assert(result === "datacentral.data.utils.reflection.ReflectionFunctionsSuite.ReflectionFunctionsTestCaseClass")
  }

  "instance[T]" should "return the new instance for the class" in {
    val x = instance[ReflectionFunctionsTestCaseClass](20: Integer)
    assert(x.i == 20)
  }
}

object ReflectionFunctionsSuite {

  case class ReflectionFunctionsTestCaseClass(i: Integer)

  class ReflectionFunctionsTestClass

  object ReflectionFunctionsTestObject

}