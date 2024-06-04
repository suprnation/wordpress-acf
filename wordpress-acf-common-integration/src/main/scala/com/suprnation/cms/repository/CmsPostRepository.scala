package com.suprnation.cms.repository

import java.{lang, util}

import com.suprnation.cms.model.CmsPost
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

trait CmsPostRepository extends JpaRepository[CmsPost, lang.Long] {

  def findByTypeAndStatusIn(postType: String, status: lang.Iterable[String]): util.List[CmsPost]

  @Query(value = "SELECT * FROM wp_posts " +
    "WHERE post_type = :postType " +
    "AND post_status in ( :status ) " +
    "AND CAST(post_modified_gmt AS DATETIME) >= CAST(:gte AS DATETIME) ", nativeQuery = true)
  def findByTypeAndStatusInAndModifiedGmtGte(@Param("postType") postType: String, @Param("status") status: lang.Iterable[String], @Param("gte") gte: String): util.List[CmsPost]

  @Query(value = "SELECT * FROM wp_posts " +
    "WHERE post_type = :postType " +
    "AND post_status in ( :status ) " +
    "AND CAST(post_modified_gmt AS DATETIME) <= CAST(:lte AS DATETIME) ", nativeQuery = true)
  def findByTypeAndStatusInAndModifiedGmtLte(@Param("postType") postType: String, @Param("status") status: lang.Iterable[String], @Param("lte") lte: String): util.List[CmsPost]

  @Query(value = "SELECT * FROM wp_posts " +
    "WHERE post_type = :postType " +
    "AND post_status in ( :status ) " +
    "AND CAST(post_modified_gmt AS DATETIME) >= CAST(:gte AS DATETIME) " +
    "AND CAST(post_modified_gmt AS DATETIME) <= CAST(:lte AS DATETIME) ", nativeQuery = true)
  def findByTypeAndStatusInAndModifiedGmtGteAndModifiedGmtLte(@Param("postType") postType: String, @Param("status") status: lang.Iterable[String], @Param("gte") gte: String, @Param("lte") lte: String): util.List[CmsPost]

  def findByTypeAndIdInAndStatusIn(postType: String, ids: lang.Iterable[lang.Long], status: util.List[String]): util.List[CmsPost]

}
