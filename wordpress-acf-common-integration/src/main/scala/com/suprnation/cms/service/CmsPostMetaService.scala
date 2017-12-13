package com.suprnation.cms.service

import com.suprnation.cms.model.CmsPostMeta
import com.suprnation.cms.types.PostId

trait CmsPostMetaService {

  def findByMetaKeyIn(postMetaKeys: Iterable[String]): Iterable[CmsPostMeta]

  def findByMetaKeyInAndIdIn(postType: Iterable[String], ids: Iterable[PostId]): Iterable[CmsPostMeta]
}
