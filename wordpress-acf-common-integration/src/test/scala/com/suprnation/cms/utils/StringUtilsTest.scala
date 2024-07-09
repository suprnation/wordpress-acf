package com.suprnation.cms.utils

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class StringUtilsTest extends AnyFunSuite with Matchers {

  test("shouldConvertFromCamelCaseToLowerDashCase") {
    StringUtils.convertToLowerDashCaseFromCamelCase("testingThisOne") shouldBe "testing-this-one"
    StringUtils.convertToLowerDashCaseFromCamelCase("TestingThisOne") shouldBe "testing-this-one"
  }

  test("shouldConvertFromLowerDashCaseToCamelCase") {
    StringUtils.convertToCamelCaseFromLowerHyphen("testing-this-one") shouldBe "testingThisOne"
    StringUtils.convertToCamelCaseFromLowerHyphen("Testing-this-one") shouldBe "testingThisOne"
    StringUtils.convertToCamelCaseFromLowerHyphen("Testing-This-One") shouldBe "testingThisOne"
  }

}
