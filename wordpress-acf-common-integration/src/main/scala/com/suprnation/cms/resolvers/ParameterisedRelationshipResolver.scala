package com.suprnation.cms.resolvers

import java.util.Collections

import com.suprnation.cms.cache.{GlobalFieldCache, GlobalRelationshipCache, _}
import com.suprnation.cms.log.{ExecutionLogger, MultipleResultCacheMetric}
import com.suprnation.cms.result.Result
import com.suprnation.cms.service.CmsRelationshipService
import com.suprnation.cms.store.GlobalPostCacheStore
import com.suprnation.cms.tokens.{CmsFieldToken, ParameterisedRelationshipToken}
import com.suprnation.cms.types.{PostId, Taxonomy}
import com.suprnation.cms.utils.TypeUtils

case class ParameterisedRelationshipResolver(depth: Int)
                                            (implicit cmsRelationshipService: CmsRelationshipService,
                                             executionLogger: ExecutionLogger)
  extends FieldResolver[List[PostId]] {
  implicit val relationshipResolver: ParameterisedRelationshipResolver = this

  override def beforeAllExecution(fields: List[CmsFieldToken], filter: List[PostId])(implicit globalFieldCache: GlobalFieldCache, store: GlobalPostCacheStore): GlobalFieldCache = {
    if (filter.isEmpty || !fields.exists {
      case ParameterisedRelationshipToken(_, _, _, _) => true
      case _ => false
    }) {
      globalFieldCache
    } else {
      val relationshipTokens = fields
        .filter {
          case ParameterisedRelationshipToken(_, _, _, _) => true
          case _ => false
        }
        .map(_.asInstanceOf[ParameterisedRelationshipToken[_]])

      val taxonomyTokenMap: Map[Taxonomy, ParameterisedRelationshipToken[_]] = relationshipTokens.map(field => field.taxonomy -> field).toMap

      val (taxonomiesToFetch: Set[Taxonomy], postIdsToFetch: Set[PostId]) =
        relationshipTokens
          .map(token => token.taxonomy -> filter.filter(postId => !globalFieldCache.getOrElse(postId, Map.empty).contains(token)))
          .foldLeft(Set.empty[Taxonomy], Set.empty[PostId])((acc, toFetch) => (acc._1 + toFetch._1, acc._2 ++ toFetch._2))

      logExecutionRelationships(depth, relationshipTokens, MultipleResultCacheMetric(filter.size - postIdsToFetch.size, postIdsToFetch.size), postIdsToFetch)
      val dbResults: GlobalRelationshipCache = cmsRelationshipService.getRelationshipsForPostsAndTaxonomies(postIdsToFetch, taxonomiesToFetch)
      dbResults.foldLeft(globalFieldCache)((acc, dbCache) => {
        val dbContext = dbCache match {
          case (taxonomy, cache) =>
            val token: ParameterisedRelationshipToken[_] = taxonomyTokenMap(taxonomy)
            cache.map {
              case (postId, terms) =>
                postId -> Map(token.asInstanceOf[CmsFieldToken] -> Result(TypeUtils.convertToEnum(token.source, terms.getOrElse(Collections.emptyList()), token.parameterisedType.asInstanceOf[Class[Enum[_]]])))
            }
        }
        acc.merge(dbContext)
      })
    }
  }
}
