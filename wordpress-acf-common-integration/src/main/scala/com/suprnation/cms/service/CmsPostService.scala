package com.suprnation.cms.service

import com.suprnation.cms.model.CmsPost
import com.suprnation.cms.types.PostId

trait CmsPostService {

  def findByType(postType: String): Iterable[CmsPost]

  def findByTypeAndIdIn(postType: String, ids: Iterable[PostId]): Iterable[CmsPost]

}
