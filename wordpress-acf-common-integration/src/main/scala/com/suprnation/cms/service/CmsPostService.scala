package com.suprnation.cms.service

import com.suprnation.cms.model.CmsPost
import com.suprnation.cms.types.PostId
import org.joda.time.DateTime

trait CmsPostService {

  def findByType(postType: String): Iterable[CmsPost]

  def findByTypeAndModifiedDateGte(postType: String, gte: DateTime): Iterable[CmsPost]

  def findByTypeAndModifiedDateLte(postType: String, lte: DateTime): Iterable[CmsPost]

  def findByTypeAndModifiedDateGteAndModifiedDateLte(postType: String, gte: DateTime, lte: DateTime): Iterable[CmsPost]

  def findByTypeAndIdIn(postType: String, ids: Iterable[PostId]): Iterable[CmsPost]

}
