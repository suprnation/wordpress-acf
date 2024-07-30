package com.suprnation.cms.resolvers

import com.suprnation.cms.cache._
import com.suprnation.cms.result.Result
import com.suprnation.cms.tokens.CmsFieldToken
import com.suprnation.cms.context.Cache
import com.suprnation.cms.types.PostId

class GlobalFieldCacheInner(val context: GlobalFieldCache) {

  def merge(newGlobalFieldCache: GlobalFieldCache): GlobalFieldCache = {
    val postIds = newGlobalFieldCache.keySet.intersect(context.keySet)
    val (commonPosts, newPosts) = newGlobalFieldCache.partition {
      case (postId, _) => postIds.contains(postId)
    }

    val (_, oldPosts) = context.partition {
      case (postId, _) => postIds.contains(postId)
    }

    oldPosts ++ newPosts ++ commonPosts.map {
      case (postId, newContext) => postId -> context(postId).merge(newContext)
    }
  }

  def withCachedValue(postId: PostId, cmsFieldToken: CmsFieldToken, result: Result[_]): (Result[_], GlobalFieldCache) = {
    (result, this.merge(Map(postId -> Map(cmsFieldToken -> result))))
  }

  def getOrEmpty(postId: PostId): FieldCache = context.getOrElse(postId, Cache())

  def getAllCached(postIds: List[PostId]): GlobalFieldCache = {
    val cachedPostIds = postIds.toSet.intersect(this.context.keySet)

    this.context.filter {
      case (key, _) => cachedPostIds.contains(key)
    }
  }
}
