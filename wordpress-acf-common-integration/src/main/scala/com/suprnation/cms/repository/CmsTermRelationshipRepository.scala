package com.suprnation.cms.repository

import java.{lang, util}

import com.suprnation.cms.model.{CmsTermRelationship, CmsTermRelationshipPk, RelationshipResult}
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

trait CmsTermRelationshipRepository extends JpaRepository[CmsTermRelationship, CmsTermRelationshipPk] {

  @Query(value = "SELECT r.object_id as postId, " +
    "   GROUP_CONCAT(term.slug) as terms, " +
    "   tax.taxonomy as taxonomy " +
    "FROM wordpress.wp_term_relationships r " +
    "JOIN wordpress.wp_terms term " +
    "  ON r.term_taxonomy_id = term.term_id " +
    "JOIN wordpress.wp_term_taxonomy tax " +
    "  ON term.term_id = tax.term_taxonomy_id " +
    "WHERE r.object_id IN :postIds " +
    "  and tax.taxonomy in :taxonomies " +
    "GROUP BY r.object_id, tax.taxonomy", nativeQuery = true)
  def getRelationshipsForPost(@Param(value = "postIds") postIds: lang.Iterable[lang.Long], @Param(value = "taxonomies") taxonomies: lang.Iterable[String]): util.List[RelationshipResult]

}
