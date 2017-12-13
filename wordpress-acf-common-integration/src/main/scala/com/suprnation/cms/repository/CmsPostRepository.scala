package com.suprnation.cms.repository

import java.{lang, util}

import com.suprnation.cms.model.CmsPost
import org.springframework.data.jpa.repository.JpaRepository

trait CmsPostRepository extends JpaRepository[CmsPost, lang.Long] {

  def findByTypeAndStatusIn(postType: String, status: lang.Iterable[String]): util.List[CmsPost]

  def findByTypeAndIdInAndStatusIn(postType: String, ids: lang.Iterable[lang.Long], status: util.List[String]): util.List[CmsPost]

}
