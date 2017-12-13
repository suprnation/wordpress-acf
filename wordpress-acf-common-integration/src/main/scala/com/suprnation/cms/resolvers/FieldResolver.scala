package com.suprnation.cms.resolvers

import com.suprnation.cms.cache.GlobalFieldCache
import com.suprnation.cms.log.ResolverLogging
import com.suprnation.cms.tokens.CmsFieldToken
import com.suprnation.cms.store.GlobalPostCacheStore

trait FieldResolver[S] extends ResolverLogging {
  def beforeAllExecution(fields: List[CmsFieldToken], filter: S)(implicit globalFieldCache: GlobalFieldCache, store: GlobalPostCacheStore): GlobalFieldCache
}


object FieldResolver {
  def empty[S]() = new EmptyFieldResolver[S]
}