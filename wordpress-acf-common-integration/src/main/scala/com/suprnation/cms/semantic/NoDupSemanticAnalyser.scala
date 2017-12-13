package com.suprnation.cms.semantic

import com.suprnation.cms.tokens.CmsFieldToken

/**
  * Make sure that the model does not have any duplicate fields.  This usually indicates ambiguity.
  */
class NoDupSemanticAnalyser extends SemanticAnalyser {
  override def validate(tokens: List[CmsFieldToken]): Iterable[Violation] = {
    tokens.groupBy(_.fieldName)
      .filter {
        case (_, Nil) => true
        case (_, _) => false
      }
      .map {
        case (_, dupeTokens) => DuplicateAlias(dupeTokens)
      }
  }
}
