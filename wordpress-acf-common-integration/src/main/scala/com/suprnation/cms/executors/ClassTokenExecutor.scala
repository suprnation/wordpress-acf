package com.suprnation.cms.executors

import java.util
import com.suprnation.cms.cache._
import com.suprnation.cms.compiler.AstCompiler
import com.suprnation.cms.interop._
import com.suprnation.cms.log._
import com.suprnation.cms.marker.CmsPostIdentifier
import com.suprnation.cms.model.CmsPost
import com.suprnation.cms.repository.CmsPostMetaRepository
import com.suprnation.cms.resolvers.{ChainedFieldResolver, ParameterisedListResolver, ParameterisedRelationshipResolver, PrimitiveFieldResolver}
import com.suprnation.cms.result.{CachedValue, NotFoundInDb, Result, SearchInDatabase}
import com.suprnation.cms.service.{AcfFieldService, CmsPostMetaService, CmsPostService, CmsRelationshipService}
import com.suprnation.cms.store.GlobalPostCacheStore
import com.suprnation.cms.tokens._
import com.suprnation.cms.types.PostId
import com.suprnation.cms.utils.CmsReflectionUtils

import java.time.ZonedDateTime
import scala.collection.JavaConverters._


case class ClassTokenExecutor[+S <: FieldExecutionPlan[CmsFieldToken, _], T ]
(postToken: PostToken[T],
 fieldExecutors: List[S],
 filters: Set[PostId] = Set.empty,
 gte: Option[ZonedDateTime] = Option.empty,
 lte: Option[ZonedDateTime] = Option.empty)
(implicit acfFieldService: AcfFieldService,
 astCompiler: AstCompiler,
 cmsPostMetaRepository: CmsPostMetaRepository,
 cmsPostService: CmsPostService,
 cmsPostMetaService: CmsPostMetaService,
 cmsRelationshipService: CmsRelationshipService,
 executionLogger: ExecutionLogger)
  extends ClassExecutionPlan[util.List[T]] {

  override def filter(s: Set[PostId]): ClassTokenExecutor[S, T] = {
    if (this.gte.nonEmpty || this.lte.nonEmpty)
      throw new IllegalStateException("Cannot use Post Id filtering when GTE and LTE operators are defined.  ")
    new ClassTokenExecutor[S, T](postToken, this.fieldExecutors, this.filters ++ s, this.gte, this.lte)
  }

  def gte(gte: ZonedDateTime):ClassTokenExecutor[S, T]  = {
    if (this.filters.nonEmpty)
      throw new IllegalStateException("Cannot use GTE filter with Post Id filtering")
    new ClassTokenExecutor[S, T](postToken, this.fieldExecutors, this.filters, Option(gte), this.lte)
  }

  def lte(lte: ZonedDateTime): ClassTokenExecutor[S, T] = {
    if (this.filters.nonEmpty)
      throw new IllegalStateException("Cannot use GTE filter with Post Id filtering")
    new ClassTokenExecutor[S, T](postToken, this.fieldExecutors, this.filters, this.gte, Option(lte))
  }

  def execute(depth: Int = 0)(implicit store: GlobalPostCacheStore): Result[java.util.List[T]] = {
    logNewExecution(depth, postToken, filters)
    val globalPostCache: GlobalPostCache = store.getOrElse(this.postToken.postType, EmptyGlobalPostCache)
    val (result, newGlobalPostCache) = this.execute(globalPostCache, depth)
    store += (this.postToken.postType -> newGlobalPostCache)
    result
  }

  private[this] def execute(globalPostCache: GlobalPostCache, depth: Int)(implicit store: GlobalPostCacheStore): (Result[java.util.List[T]], GlobalPostCache) = {
    val (cachedPosts, nonCachedPosts) = getCachedAndNonCachedPosts(globalPostCache)
    val (loaded, newGlobalPostCache) = retrieveNonCachedPosts(globalPostCache, nonCachedPosts.toList, depth + 1)
    val foundCachedPosts = cachedPosts.collect { case CachedValue(value: T) => value }
    val mergedValues = foundCachedPosts ++ loaded.asScala
    val missing = filters.diff(mergedValues.filter(_.isInstanceOf[CmsPostIdentifier]).map(_.asInstanceOf[CmsPostIdentifier].getWordpressId).toSet)
    logExecution(depth, postToken, MultipleResultCacheMetric(loaded.size(), nonCachedPosts.size - loaded.size()), multipart = true)
    (Result(mergedValues), missing.foldLeft(newGlobalPostCache)((acc, postId) =>
      acc + (postId -> NotFoundInDb)
    ))
  }

  private[this] def getCachedAndNonCachedPosts(globalPostCache: GlobalPostCache): (List[Result[CmsPostIdentifier]], Iterable[CmsPost]) = {
    val (cachedPosts, nonCachedPosts) =
      if (filters.isEmpty) {

        val postsFoundInDatabase = if (gte.nonEmpty && lte.nonEmpty) {
          cmsPostService.findByTypeAndModifiedDateGteAndModifiedDateLte(postToken.postType, gte.get, lte.get)
        } else if (gte.nonEmpty && lte.isEmpty) {
          cmsPostService.findByTypeAndModifiedDateGte(postToken.postType, gte.get)
        } else if (gte.isEmpty && lte.nonEmpty) {
          cmsPostService.findByTypeAndModifiedDateLte(postToken.postType, lte.get)
        } else {
          cmsPostService.findByType(postToken.postType)
        }

        val (loaded, remaining) = postsFoundInDatabase.partition(cmsPost => globalPostCache.keySet.contains(cmsPost.getWordpressId))
        val loadedFromCache = loaded.map(cmsPost => globalPostCache(cmsPost.getWordpressId)).toList
        (loadedFromCache, remaining)

      } else {
        // From the filtered list we could have already loaded some posts.  Let's only retrieve what we need atm.
        val (loaded, remainingWordpressIds) = this.filters.partition(globalPostCache.keySet.contains(_))
        val remaining = if (remainingWordpressIds.nonEmpty) {
          cmsPostService.findByTypeAndIdIn(postToken.postType, remainingWordpressIds)
        } else {
          List()
        }
        (loaded.map(l => globalPostCache(l)).toList, remaining)
      }
    (cachedPosts, nonCachedPosts)
  }

  private[this] def retrieveNonCachedPosts(globalPostCache: GlobalPostCache, nonCachedPosts: List[CmsPost], depth: Int)(implicit store: GlobalPostCacheStore): (java.util.List[T], GlobalPostCache) = {
    if (nonCachedPosts.nonEmpty) {
      val optimisedFieldResolver = ChainedFieldResolver(PrimitiveFieldResolver(depth))
        .after(ParameterisedListResolver(depth))
        .after(ParameterisedRelationshipResolver(depth))

      val postIds: Set[PostId] = nonCachedPosts.map(_.getWordpressId).toSet
      val newGlobalFieldCache: GlobalFieldCache = optimisedFieldResolver.beforeAllExecution(postToken.fields, postIds)(EmptyGlobalFieldCache, store).merge(
        nonCachedPosts.foldLeft(EmptyGlobalFieldCache)((globalContext, cmsPost) => {
          globalContext.merge(Map(cmsPost.getId -> EmptyFieldCache.merge(cmsPost, postToken.fields)))
        })
      )

      val (resolvedPosts, _) = nonCachedPosts.foldLeft((List.empty[T], newGlobalFieldCache))((acc, cmsPost) => {
        val (posts, newGlobalFieldCache) = acc
        val instance: T = CmsReflectionUtils.construct(postToken.source)
        logInstantiation(depth, postToken, cmsPost.getId)


        (posts ++ List(instance), this.fieldExecutors.foldLeft(newGlobalFieldCache)((globalExecutionContext, executor) => {
          val (optional, mergedFilterContext) = getFromCacheOrSearch(cmsPost.getWordpressId, executor, depth + 1)(globalExecutionContext, store)
          optional match {
            case CachedValue(fieldValue: Object) =>
              executor.fieldToken.injector.inject(instance.asInstanceOf[AnyRef], fieldValue)
              mergedFilterContext
            case _ =>
              mergedFilterContext
          }
        }))
      })

      // Fold in the results to the global post cache so that we can find them later.
      val newGlobalPostCache = resolvedPosts.filter(_.isInstanceOf[CmsPostIdentifier]).map(_.asInstanceOf[CmsPostIdentifier])
        .foldLeft(globalPostCache)((acc, postInstance) => {
        acc + (postInstance.getWordpressId -> Result(postInstance))
      })

      // Return the results and the new folded results.
      (resolvedPosts, newGlobalPostCache)
    } else {
      (List(), globalPostCache)
    }
  }

  def getFromCacheOrSearch(postId: java.lang.Long, fieldExecutionPlan: FieldExecutionPlan[CmsFieldToken, _], depth: Int)
                          (implicit globalFieldCache: GlobalFieldCache, store: GlobalPostCacheStore): (Result[_], GlobalFieldCache) = {
    val fieldToken = fieldExecutionPlan.fieldToken
    val cachedValue: Result[_] = globalFieldCache.getOrEmpty(postId).getOrElse(fieldToken, Result.searchInDatabase).asInstanceOf[Result[java.util.List[T]]]
    logExecutionOnBehalf(fieldExecutionPlan, depth, fieldToken, CacheMetric(cachedValue))
    cachedValue match {
      case CachedValue(_) => (cachedValue, globalFieldCache)
      case NotFoundInDb => (cachedValue, globalFieldCache)
      case SearchInDatabase =>
        val value = fieldExecutionPlan.filter(Option(postId)).execute(depth)
        globalFieldCache.withCachedValue(postId, fieldExecutionPlan.fieldToken, value)
    }
  }
}