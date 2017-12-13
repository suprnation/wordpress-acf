package com.suprnation.cms

import com.suprnation.cms.marker.CmsPostIdentifier
import com.suprnation.cms.resolvers.{FieldCacheInner, GlobalFieldCacheInner}
import com.suprnation.cms.result.Result
import com.suprnation.cms.tokens.CmsFieldToken
import com.suprnation.cms.context.Cache
import com.suprnation.cms.types.{PostId, Taxonomy, Term}

package object cache {
  type GlobalPostCache = Cache[PostId, Result[CmsPostIdentifier]]
  type GlobalRelationshipCache = Cache[Taxonomy, PostRelationshipCache]

  type GlobalFieldCache = Cache[PostId, FieldCache]

  val EmptyGlobalPostCache = Map.empty[PostId, Result[CmsPostIdentifier]]
  val EmptyGlobalFieldCache = Map.empty[PostId, FieldCache]
  val EmptyFieldCache = Map.empty[CmsFieldToken, Result[_]]
  type PostRelationshipCache = Map[PostId, Result[java.util.List[Term]]]

  type FieldCache = Cache[CmsFieldToken, Result[_]]

  implicit def toFieldCacheInner(fieldCache: FieldCache): FieldCacheInner = new FieldCacheInner(fieldCache)

  implicit def toFieldExecutionContext(fieldCacheInner: FieldCacheInner): FieldCache = fieldCacheInner.fieldCache

  implicit def toGlobalFieldCacheInner(globalFieldCache: GlobalFieldCache): GlobalFieldCacheInner = new GlobalFieldCacheInner(globalFieldCache)

  implicit def toGlobalFieldCache(globalFieldCacheInner: GlobalFieldCacheInner): GlobalFieldCache = globalFieldCacheInner.context

}
