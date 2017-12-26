package com.suprnation.cms.executors.fields

import com.suprnation.cms.compiler.AstCompiler
import com.suprnation.cms.executors.FieldExecutionPlan
import com.suprnation.cms.interop._
import com.suprnation.cms.log.{ExecutionLogger, NotFoundInCache}
import com.suprnation.cms.result.Result
import com.suprnation.cms.service.{CmsPostMetaService, CmsPostService}
import com.suprnation.cms.store.GlobalPostCacheStore
import com.suprnation.cms.tokens.{ParameterisedListToken, ShallowListToken}
import com.suprnation.cms.types.PostId
import com.suprnation.cms.utils.{CmsReflectionUtils, TypeUtils}

import scala.collection.JavaConverters._

case class ParameterisedListExecutor[T](override val fieldToken: ParameterisedListToken[T], override val filter: Option[PostId] = Option.empty)
                                                            (implicit
                                                             astCompiler: AstCompiler,
                                                             cmsPostService: CmsPostService,
                                                             cmsPostMetaService: CmsPostMetaService,
                                                             executionLogger: ExecutionLogger)
  extends FieldExecutionPlan[ParameterisedListToken[T], java.util.List[T]] {

  override def executeForPostId(postId: PostId, depth: Int)(implicit store: GlobalPostCacheStore): Result[java.util.List[T]] = {
    logExecution(depth, fieldToken, NotFoundInCache)
    val cmsPostMetas = cmsPostMetaService.findByMetaKeyInAndIdIn(List(fieldToken.fieldName), List(postId))
    if (cmsPostMetas.size == 1) {
      val value = cmsPostMetas.head.getMetaValue
      val postIds: java.util.List[PostId] = TypeUtils.convert(value,
        ShallowListToken(fieldToken.injector, fieldToken.source, classOf[PostId])
      ).asInstanceOf[java.util.List[PostId]]
      resolveValue(postIds.asScala.toSet, depth)
    } else {
      Result(List.empty[T])
    }
  }

  def resolveValue(postIds: Set[PostId], depth: Int)(implicit store: GlobalPostCacheStore): Result[java.util.List[T]] = {
    if (postIds.nonEmpty) {
      val innerCompiler = astCompiler.compile[T](fieldToken.parameterisedType)
      val compiler = innerCompiler.filter(postIds)
      compiler.execute(depth + 1)
    } else {
      Result(CmsReflectionUtils.construct(fieldToken.source))
    }
  }

  override def filter(s: Option[PostId]): ParameterisedListExecutor[T] = (s, filter) match {
    case (Some(newValue), Some(oldValue)) if newValue == oldValue => this
    case (Some(newValue), Some(oldValue)) if newValue != oldValue => throw new IllegalStateException(s"Field $fieldToken has a filter already defined.  ")
    case (Some(_), None) => ParameterisedListExecutor(fieldToken, s)
    case (None, _) => this
  }


}
