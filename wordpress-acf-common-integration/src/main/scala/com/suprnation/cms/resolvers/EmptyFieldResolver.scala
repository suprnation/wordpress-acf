package com.suprnation.cms.resolvers

import com.suprnation.cms.cache.GlobalFieldCache
import com.suprnation.cms.store.GlobalPostCacheStore
import com.suprnation.cms.tokens.CmsFieldToken

class EmptyFieldResolver[S] extends FieldResolver[S] {
  override def beforeAllExecution(fields: List[CmsFieldToken], filter: S)(implicit globalFieldCache: GlobalFieldCache, store: GlobalPostCacheStore): GlobalFieldCache = globalFieldCache
}
