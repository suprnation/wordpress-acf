package com.suprnation.cms.resolvers

import com.suprnation.cms.cache.{GlobalFieldCache, _}
import com.suprnation.cms.log
import com.suprnation.cms.repository.CmsPostMetaRepository
import com.suprnation.cms.store.GlobalPostCacheStore
import com.suprnation.cms.tokens.CmsFieldToken
import com.suprnation.cms.types.PostId

import scala.annotation.tailrec
import scala.collection.mutable

object ChainedFieldResolver {
  def apply(contextResolvers: FieldResolver[Set[PostId]])(implicit cmsPostMetaRepository: CmsPostMetaRepository, executionLogger: log.ExecutionLogger) = new ChainedFieldResolver(List(contextResolvers))
}

class ChainedFieldResolver(contextResolvers: List[FieldResolver[Set[PostId]]])
                          (implicit cmsPostMetaRepository: CmsPostMetaRepository,
                           executionLogger: log.ExecutionLogger)
  extends FieldResolver[Set[PostId]] {


  def after(contextResolver: FieldResolver[Set[PostId]]): ChainedFieldResolver = {
    new ChainedFieldResolver(this.contextResolvers ++ Set(contextResolver))
  }

  override def beforeAllExecution(fields: List[CmsFieldToken], filter: Set[PostId])(implicit globalFieldCache: GlobalFieldCache, store: GlobalPostCacheStore): GlobalFieldCache = {
    @tailrec
    def beforeAllExecutionInner(contextResolvers: List[FieldResolver[Set[PostId]]], globalExecutionContext: mutable.HashMap[PostId, FieldCache]): Unit =
      contextResolvers match {
        case Nil =>
        case head :: _ =>
          val alreadyCachedGlobalExecutionContext =
            globalExecutionContext.filter {
              case (_, executionContext) => !executionContext.hasAllFields(fields.toSet)
            }

          val newGlobalExecutionContext = head.beforeAllExecution(fields,
            filter.diff(alreadyCachedGlobalExecutionContext.keySet), // This is the actual set of interested posts which have the missing fields.
          )(globalExecutionContext.toMap, store)

          beforeAllExecutionInner(contextResolvers.tail, globalExecutionContext ++= newGlobalExecutionContext)
      }

    val acc = new mutable.HashMap[PostId, FieldCache]()
    beforeAllExecutionInner(contextResolvers, acc ++= globalFieldCache)
    acc.toMap
  }
}
