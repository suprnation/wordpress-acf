package com.suprnation.cms.executors.fields

import java.util

import com.suprnation.cms.executors.FieldExecutionPlan
import com.suprnation.cms.log.{ExecutionLogger, NotFoundInCache}
import com.suprnation.cms.result.{CachedValue, NotFoundInDb, Result}
import com.suprnation.cms.service.CmsRelationshipService
import com.suprnation.cms.store.GlobalPostCacheStore
import com.suprnation.cms.tokens.ParameterisedRelationshipToken
import com.suprnation.cms.types.{PostId, Term}
import com.suprnation.cms.utils.TypeUtils


case class ParameterisedRelationshipExecutor[T](fieldToken: ParameterisedRelationshipToken[T], override val filter: Option[PostId] = Option.empty)
                                               (implicit executionLogger: ExecutionLogger,
                                                cmsRelationshipService: CmsRelationshipService)
  extends FieldExecutionPlan[ParameterisedRelationshipToken[T], java.util.Collection[T]] {

  override def executeForPostId(postId: PostId, depth: Int)(implicit store: GlobalPostCacheStore): Result[java.util.Collection[T]] = {
    logExecution(depth, fieldToken, NotFoundInCache)
    val taxonomy = fieldToken.taxonomy
    val value: Result[util.List[Term]] = cmsRelationshipService.getRelationshipsForPostsAndTaxonomies(Set(postId), Set(taxonomy))(taxonomy)(postId)
    value match {
      case NotFoundInDb => NotFoundInDb
      case CachedValue(terms) => Result(
        TypeUtils.convertToEnum(fieldToken.source, terms.asInstanceOf[util.List[T]], fieldToken.parameterisedType.asInstanceOf[Class[Enum[_]]]))
    }
  }

  override def filter(s: Option[PostId]): ParameterisedRelationshipExecutor[T] = (s, filter) match {
    case (Some(newValue), Some(oldValue)) if newValue == oldValue => this
    case (Some(newValue), Some(oldValue)) if newValue != oldValue => throw new IllegalStateException(s"Field $fieldToken has a filter already defined.  ")
    case (Some(_), None) => ParameterisedRelationshipExecutor(fieldToken, s)
    case (None, _) => this
  }

}
