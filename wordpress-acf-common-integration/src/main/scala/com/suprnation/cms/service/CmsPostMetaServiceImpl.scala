package com.suprnation.cms.service

import com.suprnation.cms.model.CmsPostMeta
import com.suprnation.cms.repository.CmsPostMetaRepository
import com.suprnation.cms.types.PostId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

@Service
class CmsPostMetaServiceImpl @Autowired()(cmsPostMetaRepository: CmsPostMetaRepository)
  extends CmsPostMetaService {

  override def findByMetaKeyIn(postMetaKeys: Iterable[String]): Iterable[CmsPostMeta] =
    this.cmsPostMetaRepository.findByMetaKeyIn(postMetaKeys.asJava).asScala

  override def findByMetaKeyInAndIdIn(postType: Iterable[String], ids: Iterable[PostId]): Iterable[CmsPostMeta] =
    this.cmsPostMetaRepository.findByMetaKeyInAndPostIdIn(postType.asJava, ids.asJava).asScala
}
