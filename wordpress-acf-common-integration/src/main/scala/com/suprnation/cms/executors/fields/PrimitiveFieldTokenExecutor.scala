package com.suprnation.cms.executors.fields

import java.lang

import com.suprnation.cms.executors.FieldExecutionPlan
import com.suprnation.cms.result.Result
import com.suprnation.cms.service.AcfFieldService
import com.suprnation.cms.tokens.CmsFieldToken
import com.suprnation.cms.log.{ExecutionLogger, NotFoundInCache}
import com.suprnation.cms.store.GlobalPostCacheStore
import com.suprnation.cms.types.PostId

case class PrimitiveFieldTokenExecutor[T](override val fieldToken: CmsFieldToken, override val filter: Option[PostId] = Option.empty)
                                         (implicit acfFieldService: AcfFieldService,
                                          executionLogger: ExecutionLogger)
  extends FieldExecutionPlan[CmsFieldToken, T] {

  override def executeForPostId(postId: PostId, depth: Int)(implicit store: GlobalPostCacheStore): Result[T] = {
    logExecution(depth, fieldToken, NotFoundInCache)
    acfFieldService.getPrimitiveField(fieldToken, postId)
  }

  override def filter(s: Option[lang.Long]): PrimitiveFieldTokenExecutor[T] = (s, filter) match {
    case (Some(newValue), Some(oldValue)) if newValue == oldValue => this
    case (Some(newValue), Some(oldValue)) if newValue != oldValue => throw new IllegalStateException(s"Field $fieldToken has a filter already defined.  ")
    case (Some(_), None) => PrimitiveFieldTokenExecutor(fieldToken, s)
    case (None, _) => this
  }


}
