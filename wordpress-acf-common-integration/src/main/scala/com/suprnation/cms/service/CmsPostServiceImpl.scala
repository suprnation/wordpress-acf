package com.suprnation.cms.service

import java.util
import java.util.Collections

import com.suprnation.cms.enums.CmsPostStatus
import com.suprnation.cms.model.CmsPost
import com.suprnation.cms.repository.CmsPostRepository
import com.suprnation.cms.types.PostId
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

@Service
class CmsPostServiceImpl @Autowired()(cmsPostRepository: CmsPostRepository)
  extends CmsPostService {

  implicit val self: CmsPostServiceImpl = this

  val dateTimeFormat: String = "yyyy-MM-dd hh:mm:ss"

  def findByType(postType: String): Iterable[CmsPost] = this.cmsPostRepository.findByTypeAndStatusIn(postType, util.Arrays.asList(CmsPostStatus.publish.name, CmsPostStatus.inherit.name)).asScala

  def findByTypeAndModifiedDateGte(postType: String, gte: DateTime): Iterable[CmsPost] =
    this.cmsPostRepository.findByTypeAndStatusInAndModifiedGmtGte(postType, util.Arrays.asList(CmsPostStatus.publish.name, CmsPostStatus.inherit.name), gte.toString(dateTimeFormat)).asScala

  def findByTypeAndModifiedDateLte(postType: String, lte: DateTime): Iterable[CmsPost] =
    this.cmsPostRepository.findByTypeAndStatusInAndModifiedGmtLte(postType, util.Arrays.asList(CmsPostStatus.publish.name, CmsPostStatus.inherit.name), lte.toString(dateTimeFormat)).asScala

  def findByTypeAndModifiedDateGteAndModifiedDateLte(postType: String, gte: DateTime, lte: DateTime): Iterable[CmsPost] =
    this.cmsPostRepository.findByTypeAndStatusInAndModifiedGmtGteAndModifiedGmtLte(postType, util.Arrays.asList(CmsPostStatus.publish.name, CmsPostStatus.inherit.name), gte.toString(dateTimeFormat), lte.toString(dateTimeFormat)).asScala

  def findByTypeAndIdIn(postType: String, ids: Iterable[PostId]): Iterable[CmsPost] = this.cmsPostRepository.findByTypeAndIdInAndStatusIn(postType, ids.asJava, Collections.singletonList(CmsPostStatus.publish.name)).asScala
}
