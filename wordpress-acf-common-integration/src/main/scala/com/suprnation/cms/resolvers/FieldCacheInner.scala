package com.suprnation.cms.resolvers

import com.suprnation.cms.cache.FieldCache
import com.suprnation.cms.model.CmsPost
import com.suprnation.cms.result.Result
import com.suprnation.cms.tokens.CmsFieldToken

class FieldCacheInner(val fieldCache: FieldCache) {
  def merge[T](obj: CmsPost, tokens: List[CmsFieldToken]): FieldCache = {
    tokens.foldLeft(fieldCache)((context, cmsField) => {
      if (cmsField.fieldName == "id") {
        context + (cmsField -> Result(obj.getId))
      } else if (cmsField.fieldName == "title") {
        context + (cmsField -> Result(obj.getTitle))
      } else if (cmsField.fieldName == "post-content") {
        context + (cmsField -> Result(obj.getContent))
      } else if (cmsField.fieldName == "post-name") {
        context + (cmsField -> Result(obj.getName))
      } else if (cmsField.fieldName == "date-modified") {
        context + (cmsField -> Result(obj.getModified))
      } else if (cmsField.fieldName == "status") {
        context + (cmsField -> Result(obj.getStatus))
      } else if (cmsField.fieldName == "name") {
        context + (cmsField -> Result(obj.getName))
      } else {
        context
      }
    })
  }

  def withUpdatedField(cmsFieldToken: CmsFieldToken, obj: Object): FieldCache = {
    this.fieldCache + (cmsFieldToken -> Result(obj))
  }

  def hasAllFields(fieldsNames: Set[CmsFieldToken]): Boolean = fieldCache.keySet.intersect(fieldsNames).size == fieldCache.size


  def merge(globalFieldCache: FieldCache): FieldCache = {
    val newContext: FieldCache = globalFieldCache.keys.foldLeft(fieldCache)((context, key) => {
      (context.getOrElse(key, Result.searchInDatabase), globalFieldCache(key)) match {
        case (_, b) => context + (key -> b)
      }

    })
    newContext
  }

}


