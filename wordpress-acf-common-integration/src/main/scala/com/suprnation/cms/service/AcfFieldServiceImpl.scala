package com.suprnation.cms.service

import com.suprnation.cms.model.CmsPostMeta
import com.suprnation.cms.result.Result
import com.suprnation.cms.tokens.CmsFieldToken
import com.suprnation.cms.types.PostId
import com.suprnation.cms.utils.TypeUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AcfFieldServiceImpl(@Autowired() cmsPostMetaService: CmsPostMetaService)
  extends AcfFieldService {

  def getPrimitiveField[T](cmsFieldToken: CmsFieldToken, postId: PostId): Result[T] = {
    this.getPrimitiveField(List(cmsFieldToken), List(postId))(postId)(cmsFieldToken)
  }


  def getPrimitiveField[T](cmsFieldTokens: List[CmsFieldToken], postIds: List[PostId]): Map[PostId, Map[CmsFieldToken, Result[T]]] = {
    val groupedFields = cmsFieldTokens.groupBy(_.fieldName)
    groupedFields.foreach {
      case (fieldName, tokens) =>
        if (tokens.size > 1)
          throw new IllegalStateException(s"[FieldName: $fieldName] is used by more than one [Field: $tokens]. ")
    }

    val rawCmsPostMetas = cmsPostMetaService.findByMetaKeyInAndIdIn(groupedFields.keys, postIds)
    val posts: Map[PostId, Iterable[CmsPostMeta]] = rawCmsPostMetas.groupBy(_.getPostId)


    // Fill in the gaps.
    postIds.foldLeft(posts)((acc: Map[PostId, Iterable[CmsPostMeta]], postId) => {
      acc.get(postId) match {
        case Some(_) => acc
        case None => acc + (postId -> Iterable.empty[CmsPostMeta])
      }
    }).map {
      case (postId, cmsPostMetas) =>
        postId -> cmsFieldTokens.foldLeft(Map[CmsFieldToken, Result[T]]())((acc, cmsFieldToken) => {
          val values = cmsPostMetas.filter(_.getMetaKey == cmsFieldToken.fieldName)
          if (values.size > 1) {
            throw new IllegalStateException(s"Field [$cmsFieldToken] has multiple [Values: $values] for primitive type")
          } else {
            acc + (cmsFieldToken -> (values.headOption match {
              case Some(value) => processPrimitiveField[T](value, cmsFieldToken)
              case None => Result.notFoundInDatabase
            }))
          }
        })
    }
  }

  def processPrimitiveField[T](cmsPostMeta: CmsPostMeta, cmsFieldToken: CmsFieldToken): Result[T] = {
    Result(TypeUtils.convert(cmsPostMeta.getMetaValue, cmsFieldToken).asInstanceOf[T])
  }

}
