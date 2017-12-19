package com.suprnation.cms.executors

import com.suprnation.cms.resolvers.{EmptyFieldResolver, FieldResolver}
import com.suprnation.cms.result.Result
import com.suprnation.cms.tokens.CmsFieldToken
import com.suprnation.cms.context.MutableCache
import com.suprnation.cms.log.ExecutorLogging
import com.suprnation.cms.store.GlobalPostCacheStore
import com.suprnation.cms.types.PostId

trait ExecutionPlan[T, S, C <: MutableCache[_, _]] extends ExecutorLogging {
  def filter(s: S): ExecutionPlan[T, S, C]

  def execute(depth: Int)(implicit cache: C): Result[T]

  def interceptor: FieldResolver[S] = new EmptyFieldResolver

}

trait ClassExecutionPlan[T] extends ExecutionPlan[T, Set[PostId], GlobalPostCacheStore]

abstract class FieldExecutionPlan[+R <: CmsFieldToken, T](val filter: Option[PostId] = Option.empty) extends ExecutionPlan[T, Option[PostId], GlobalPostCacheStore] {
  def fieldToken: R

  def executeForPostId(postId: PostId, depth: Int)(implicit store: GlobalPostCacheStore): Result[T]

  def execute(depth: Int)(implicit store: GlobalPostCacheStore): Result[T] = {
    filter match {
      case None => throw new IllegalStateException(s"${this.getClass.getName} cannot be executed without providing a filter value")
      case Some(postId) => executeForPostId(postId, depth)
    }
  }
}