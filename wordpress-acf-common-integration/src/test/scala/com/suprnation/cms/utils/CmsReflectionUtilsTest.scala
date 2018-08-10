package com.suprnation.cms.utils

import com.suprnation.enums.{SampleEnum, SampleEnumWithValueOf}
import org.scalatest.{FunSuite, Matchers}

class CmsReflectionUtilsTest extends FunSuite with Matchers {

  test("shouldConvertEnumValueWhenEnumIsValid") {
    CmsReflectionUtils.instantiateEnum(classOf[SampleEnum], "NORMAL") shouldBe SampleEnum.NORMAL
  }

  test("shouldThrowExceptionWhenEnumIsInvalid") {
    intercept[Throwable] {
      CmsReflectionUtils.instantiateEnum(classOf[SampleEnum], "null")
    }
  }

  test("shouldConvertEnumValueWhenEnumIsValidAndValueOfIsDefined") {
    CmsReflectionUtils.instantiateEnum(classOf[SampleEnum], "NORMAL") shouldBe SampleEnum.NORMAL
  }

  test("shouldReturnNullWhenEnumValueMethodAllowsUndefinedValues") {
    CmsReflectionUtils.instantiateEnum(classOf[SampleEnumWithValueOf], "null") shouldBe null
  }
}
