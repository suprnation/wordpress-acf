package com.suprnation.cms.service

import com.suprnation.cms.model.CmsPost
import com.suprnation.cms.types.PostId

import java.time.ZonedDateTime

trait CmsPostService {

  def findByType(postType: String): Iterable[CmsPost]

  def findByTypeAndModifiedDateGte(postType: String, gte: ZonedDateTime): Iterable[CmsPost]

  def findByTypeAndModifiedDateLte(postType: String, lte: ZonedDateTime): Iterable[CmsPost]

  def findByTypeAndModifiedDateGteAndModifiedDateLte(postType: String, gte: ZonedDateTime, lte: ZonedDateTime): Iterable[CmsPost]

  def findByTypeAndIdIn(postType: String, ids: Iterable[PostId]): Iterable[CmsPost]

}
