package com.suprnation.cms.repository

import java.{lang, util}

import com.suprnation.cms.model.CmsPostMeta
import org.springframework.data.jpa.repository.JpaRepository

trait CmsPostMetaRepository extends JpaRepository[CmsPostMeta, lang.Long] {

  def findByMetaKeyIn(postMetaKeys: lang.Iterable[String]): util.List[CmsPostMeta]

  def findByMetaKeyInAndPostIdIn(postType: lang.Iterable[String], ids: lang.Iterable[lang.Long]): util.List[CmsPostMeta]

}
