package com.suprnation.cms.utils

import com.google.common.base.CaseFormat

object StringUtils {

  def convertToLowerDashCaseFromCamelCase(source:String): String = {
    CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, source)
  }

  def convertToCamelCaseFromLowerHyphen(source:String): String = {
    CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, source)
  }

}
