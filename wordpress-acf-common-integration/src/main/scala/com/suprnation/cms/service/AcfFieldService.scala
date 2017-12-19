package com.suprnation.cms.service

import com.suprnation.cms.model.CmsPostMeta
import com.suprnation.cms.result.Result
import com.suprnation.cms.tokens.CmsFieldToken
import com.suprnation.cms.types.PostId

trait AcfFieldService {

  // Primitive Fields - this includes shallow lists
  def getPrimitiveField[T](cmsFieldToken: CmsFieldToken, postId: PostId): Result[T]

  def getPrimitiveField[T](cmsFieldTokens: List[CmsFieldToken], postIds: Set[PostId]): Map[PostId, Map[CmsFieldToken, Result[T]]]
  def processPrimitiveField[T](cmsPostMeta: CmsPostMeta, cmsFieldToken: CmsFieldToken): Result[T]

  // Lists
}
