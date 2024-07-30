package com.suprnation.cms.resolvers

import com.suprnation.cms.cache._
import com.suprnation.cms.log.{ExecutionLogger, MultipleResultCacheMetric}
import com.suprnation.cms.service.AcfFieldService
import com.suprnation.cms.store.GlobalPostCacheStore
import com.suprnation.cms.tokens._
import com.suprnation.cms.types.PostId

case class PrimitiveFieldResolver(depth: Int)(implicit acfFieldService: AcfFieldService,
                                              executionLogger: ExecutionLogger)
  extends FieldResolver[Set[PostId]] {
  override def beforeAllExecution(fields: List[CmsFieldToken], filter: Set[PostId])(implicit globalFieldCache: GlobalFieldCache, store: GlobalPostCacheStore): GlobalFieldCache = {
    val eligibleFields = fields.filter {
      case _: FieldToken => true
      case _: ShallowListToken[_] => true
      case _: ParameterisedListToken[_] => true
      case _: PostFieldToken[_] => true
      case _ => false
    }
    logExecutionFieldTokens(depth, fields, MultipleResultCacheMetric(0, filter.size), filter)
    globalFieldCache.merge(acfFieldService.getPrimitiveField(eligibleFields, filter))
  }
}