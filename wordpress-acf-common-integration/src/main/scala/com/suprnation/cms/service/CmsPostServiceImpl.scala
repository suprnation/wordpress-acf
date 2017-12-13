package com.suprnation.cms.service

import java.util
import java.util.Collections

import com.suprnation.cms.model.CmsPost
import com.suprnation.cms.enums.CmsPostStatus
import com.suprnation.cms.repository.{CmsPostMetaRepository, CmsPostRepository, CmsTermRelationshipRepository}
import com.suprnation.cms.types.PostId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

@Service
class CmsPostServiceImpl @Autowired()(cmsPostRepository: CmsPostRepository,
                                      cmsPostMetaRepository: CmsPostMetaRepository,
                                      cmsTermRelationshipRepository: CmsTermRelationshipRepository)
  extends CmsPostService {

  implicit val self: CmsPostServiceImpl = this

  def findByType(postType: String): Iterable[CmsPost] = this.cmsPostRepository.findByTypeAndStatusIn(postType, util.Arrays.asList(CmsPostStatus.publish.name, CmsPostStatus.inherit.name)).asScala

  def findByTypeAndIdIn(postType: String, ids: Iterable[PostId]): Iterable[CmsPost] = this.cmsPostRepository.findByTypeAndIdInAndStatusIn(postType, ids.asJava, Collections.singletonList(CmsPostStatus.publish.name)).asScala

}
