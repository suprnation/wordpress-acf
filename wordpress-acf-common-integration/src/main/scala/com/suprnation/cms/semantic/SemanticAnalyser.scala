package com.suprnation.cms.semantic

import com.suprnation.cms.tokens.CmsFieldToken

trait SemanticAnalyser {
  def validate(tokens: List[CmsFieldToken]): Iterable[Violation]
}


sealed abstract class Violation {
  def explain: String
}

case class DuplicateAlias(violatingFields: Iterable[CmsFieldToken]) extends Violation {
  override val explain = s"The same alias exists on multiple fields on field tokens $violatingFields"
}

case class UnsupportedType(violatingFields: CmsFieldToken, reason: String) extends Violation {
  override val explain: String = s"$reason $violatingFields"
}

