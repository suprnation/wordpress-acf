package com.suprnation.cms.service

import java.util
import java.util.Collections
import com.suprnation.cms.enums.CmsPostStatus
import com.suprnation.cms.model.CmsPost
import com.suprnation.cms.repository.CmsPostRepository
import com.suprnation.cms.types.PostId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.collection.JavaConverters._

@Service
class CmsPostServiceImpl @Autowired()(cmsPostRepository: CmsPostRepository)
  extends CmsPostService {

  implicit val self: CmsPostServiceImpl = this

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")

  def findByType(postType: String): Iterable[CmsPost] = this.cmsPostRepository.findByTypeAndStatusIn(postType, util.Arrays.asList(CmsPostStatus.publish.name, CmsPostStatus.inherit.name)).asScala

  def findByTypeAndModifiedDateGte(postType: String, gte: ZonedDateTime): Iterable[CmsPost] =
    this.cmsPostRepository.findByTypeAndStatusInAndModifiedGmtGte(postType, util.Arrays.asList(CmsPostStatus.publish.name, CmsPostStatus.inherit.name), gte.format(dateTimeFormatter)).asScala

  def findByTypeAndModifiedDateLte(postType: String, lte: ZonedDateTime): Iterable[CmsPost] =
    this.cmsPostRepository.findByTypeAndStatusInAndModifiedGmtLte(postType, util.Arrays.asList(CmsPostStatus.publish.name, CmsPostStatus.inherit.name), lte.format(dateTimeFormatter)).asScala

  def findByTypeAndModifiedDateGteAndModifiedDateLte(postType: String, gte: ZonedDateTime, lte: ZonedDateTime): Iterable[CmsPost] =
    this.cmsPostRepository.findByTypeAndStatusInAndModifiedGmtGteAndModifiedGmtLte(postType, util.Arrays.asList(CmsPostStatus.publish.name, CmsPostStatus.inherit.name), gte.format(dateTimeFormatter), lte.format(dateTimeFormatter)).asScala

  def findByTypeAndIdIn(postType: String, ids: Iterable[PostId]): Iterable[CmsPost] = this.cmsPostRepository.findByTypeAndIdInAndStatusIn(postType, ids.asJava, Collections.singletonList(CmsPostStatus.publish.name)).asScala
}
