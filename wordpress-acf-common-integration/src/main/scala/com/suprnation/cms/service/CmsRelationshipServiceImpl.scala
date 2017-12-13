package com.suprnation.cms.service

import java.util

import com.suprnation.cms.cache.{GlobalRelationshipCache, PostRelationshipCache}
import com.suprnation.cms.repository.CmsTermRelationshipRepository
import com.suprnation.cms.result.Result
import com.suprnation.cms.types.{PostId, Taxonomy, Term}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

@Service
class CmsRelationshipServiceImpl @Autowired()(cmsTermRelationshipRepository: CmsTermRelationshipRepository) extends CmsRelationshipService {
  override def getRelationshipsForPostsAndTaxonomies(postIds: Set[PostId], taxonomies: Set[Taxonomy]): GlobalRelationshipCache = {
    if (postIds.isEmpty || taxonomies.isEmpty) Map.empty
    else {
      val taxonomyToLongToStrings: Map[Taxonomy, PostRelationshipCache] = cmsTermRelationshipRepository.getRelationshipsForPost(postIds.asJava, taxonomies.asJava).asScala
        .groupBy(_.getTerms)
        .map {
          case (taxonomy, results) =>
            taxonomy -> results.map(row => (row.getPostId, wrapToResult(row.getTaxonomy.split(",").toSet)))
              .toMap
        }

      val map = taxonomies.map(taxonomy => {
        val dbPostTaxonomies: PostRelationshipCache = taxonomyToLongToStrings.getOrElse(taxonomy, Map.empty)
        taxonomy -> postIds.map(postId => postId -> dbPostTaxonomies.getOrElse(postId, wrapToResult(Set.empty))).toMap
      }).toMap
      map
    }

  }

  def wrapToResult(terms: Set[Term]): Result[util.List[Term]] = if (terms.isEmpty) Result.notFoundInDatabase else Result(terms.toList.asJava)
}
