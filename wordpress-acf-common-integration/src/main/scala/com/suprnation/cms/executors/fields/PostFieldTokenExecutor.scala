package com.suprnation.cms.executors.fields

import java.{lang, util}

import com.suprnation.cms.compiler.AstCompiler
import com.suprnation.cms.executors.FieldExecutionPlan
import com.suprnation.cms.log.{ExecutionLogger, NotFoundInCache}
import com.suprnation.cms.marker.CmsPostIdentifier
import com.suprnation.cms.result.{CachedValue, NotFoundInDb, Result, SearchInDatabase}
import com.suprnation.cms.service.{CmsPostMetaService, CmsPostService}
import com.suprnation.cms.store.GlobalPostCacheStore
import com.suprnation.cms.tokens.{CmsFieldToken, ParameterisedListToken, PostFieldToken}
import com.suprnation.cms.types.PostId

import scala.collection.JavaConverters._

case class PostFieldTokenExecutor[T <: CmsPostIdentifier](override val fieldToken: PostFieldToken[T], override val filter: Option[PostId] = Option.empty)
                                                         (implicit
                                                          astCompiler: AstCompiler,
                                                          cmsPostService: CmsPostService,
                                                          cmsPostMetaService: CmsPostMetaService,
                                                          executionLogger: ExecutionLogger)
  extends FieldExecutionPlan[CmsFieldToken, T] {

  override def filter(s: Option[lang.Long]): PostFieldTokenExecutor[T] = (s, filter) match {
    case (Some(newValue), Some(oldValue)) if newValue == oldValue => this
    case (Some(newValue), Some(oldValue)) if newValue != oldValue => throw new IllegalStateException(s"Field $fieldToken has a filter already defined.  ")
    case (Some(_), None) => PostFieldTokenExecutor(fieldToken, s)
    case (None, _) => this
  }

  override def executeForPostId(postId: PostId, depth: Int)(implicit store: GlobalPostCacheStore): Result[T] = {
    logExecution(depth + 1, fieldToken, NotFoundInCache)
    val parameterisedListExecutor = new ParameterisedListExecutor[T](
      ParameterisedListToken[T](fieldToken.postToken.postType, fieldToken.injector, classOf[util.List[Object]], fieldToken.source.asInstanceOf[Class[T]]),
      filter)

    parameterisedListExecutor.execute(depth + 1) match {
      case CachedValue(value: java.util.List[T]) => value.asScala.toList match {
        case head :: Nil => Result(head)
        case Nil => NotFoundInDb
        case v => throw new IllegalStateException(s"Post Type was expected to yield one result [value: $v]");
      }
      case NotFoundInDb => NotFoundInDb
      case SearchInDatabase => throw new IllegalStateException("Post Type was expected to yield one result");
    }
  }
}
