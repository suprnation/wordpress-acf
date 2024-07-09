package com.suprnation.cms.resolvers

import java.util

import com.suprnation.cms.cache._
import com.suprnation.cms.compiler.AstCompiler
import com.suprnation.cms.log.{ExecutionLogger, MultipleResultCacheMetric}
import com.suprnation.cms.marker.CmsPostIdentifier
import com.suprnation.cms.result.{CachedValue, NotFoundInDb, Result}
import com.suprnation.cms.store.GlobalPostCacheStore
import com.suprnation.cms.tokens._
import com.suprnation.cms.types.PostId
import com.suprnation.cms.utils.TypeUtils

import scala.collection.JavaConverters._


case class ParameterisedListResolver(depth: Int)(implicit astCompiler: AstCompiler,
                                                 executionLogger: ExecutionLogger)
  extends FieldResolver[Set[PostId]] {

  override def beforeAllExecution(fields: List[CmsFieldToken], postIds: Set[PostId])(implicit globalFieldCache: GlobalFieldCache, store: GlobalPostCacheStore): GlobalFieldCache = {
    val eligibleFields = fields.filter {
      case _: ParameterisedListToken[_] => true
      case _: PostFieldToken[_] => true
      case _ => false
    }

    val groupedPostTypes: Map[String, List[CmsFieldToken]] = eligibleFields.groupBy {
      case ParameterisedListToken(postType, _, _, _) => postType
      case PostFieldToken(postType, _, _) => postType
    }

    groupedPostTypes.foldLeft(globalFieldCache)((globalAcc, groupedPostType) => {
      val groupPostTypeAggregate = groupedPostType match {
        case (_, localTokens: List[CmsFieldToken]) =>
          // For each of the token we will try to retrieve them from the cache.
          // Since this resolver is chained to a field resolver we are guaranteed that the shallow list is present.

          val firstParameterisedTypeToken = localTokens.head.asInstanceOf[CmsFieldTokenWithPostType[AnyRef]]
          val postIdsToSearch = getPostIds(eligibleFields, postIds, firstParameterisedTypeToken.parameterisedType).toSet
          logExecutionWithPostIds(depth, groupedPostType._2.head, MultipleResultCacheMetric(0, postIdsToSearch.size), postIdsToSearch)
          if (postIdsToSearch.nonEmpty) {
            val clazz = firstParameterisedTypeToken.parameterisedType

            val (groupedExtractedResults, childrenFound) =
              if (classOf[CmsPostIdentifier].isAssignableFrom(clazz)) {
                val result = astCompiler.compile(clazz).filter(postIdsToSearch).execute(depth + 1)(store)
                val childrenFound = result match {
                  case CachedValue(value) =>
                    value.asScala.filter(_.isInstanceOf[CmsPostIdentifier]).map(_.asInstanceOf[CmsPostIdentifier].getWordpressId).toList
                  case _ => List()
                }

                val groupedExtractedResults = (result match {
                  case CachedValue(value) => value.asScala.filter(_.isInstanceOf[CmsPostIdentifier]).map(_.asInstanceOf[CmsPostIdentifier])
                  case _ => List.empty[CmsPostIdentifier]
                }).groupBy(cmsPost => cmsPost.getWordpressId)
                (groupedExtractedResults, childrenFound)
              } else {
                val groupedExtractedResults =
                  postIdsToSearch.map(postId => postId -> astCompiler.compile(clazz).filter(Set(postId)).execute(depth + 1)(store))
                  .map {
                    case (postId, result) => result match {
                      case CachedValue(post) => postId -> post.asScala.headOption
                      case _ => postId -> None
                    }
                  }
                  .filter {
                    case (_, Some(_)) => true
                    case _ => false
                  }
                  .map {
                    case (postId, presentOptional) => postId -> List(presentOptional.get)
                  }
                  .toMap
                val childrenFound = groupedExtractedResults.keys.toList
                (groupedExtractedResults, childrenFound)
              }

            mapFields(localTokens, postIds, globalAcc, childrenFound)((postId) => {
              groupedExtractedResults.get(postId) match {
                case Some(value) => value.head
                case _ => throw new IllegalStateException("Expected to find result in the global cache.  ")
              }
            })
          }
          else {
            EmptyGlobalFieldCache
          }
      }
      globalAcc.merge(groupPostTypeAggregate)
    })
  }

  def getPostIds[R](fields: List[CmsFieldToken], postIds: Set[PostId], parameterisedType: Class[_])
                   (implicit globalFieldCache: GlobalFieldCache): List[PostId] = {
    fields
      .filter {
        case t: CmsFieldTokenWithPostType[_] => t.parameterisedType == parameterisedType
        case _ => false
      }
      .flatMap(field => {
        postIds.flatMap(postId => {
          val result = globalFieldCache(postId)(field)
          result match {
            case CachedValue(value) =>
              field match {
                case ParameterisedListToken(_, _, _, _) => value.asInstanceOf[java.util.Collection[PostId]].asScala.toList
                case PostFieldToken(_, _, _) => List(value.asInstanceOf[PostId])
              }
            case _ => List()
          }
        })
      })
  }

  def mapFields[R](fields: List[CmsFieldToken], postIds: Set[PostId], globalFieldCache: GlobalFieldCache, childrenFound: List[PostId])(mapFn: (PostId) => R): GlobalFieldCache = {
    fields.foldLeft(globalFieldCache)((acc, field) => {
      val newGlobalFieldCache = postIds.foldLeft(acc)((acc, postId: PostId) => {
        val cachedShallowList = globalFieldCache(postId)(field)
        val updateGlobalFieldCache: GlobalFieldCache = cachedShallowList match {
          case CachedValue(value) =>
            field match {
              case ParameterisedListToken(_, _, collectionClass, _) =>
                val objects = TypeUtils.instantiateCollection(collectionClass.asInstanceOf[Class[util.Collection[R]]])
                value.asInstanceOf[java.util.Collection[PostId]].asScala.filter(childrenFound.contains(_)).foreach(postId => objects.add(mapFn(postId)))
                Map(postId -> Map(field -> Result(objects)))
              case PostFieldToken(_, _, _) =>
                val childPostId = value.asInstanceOf[PostId]
                if (childrenFound.contains(childPostId)) {
                  val mappedValue = mapFn(value.asInstanceOf[PostId])
                  Map(postId -> Map(field -> Result(mappedValue)))
                } else {
                  Map(postId -> Map(field -> NotFoundInDb))
                }

            }
          case _ => EmptyGlobalFieldCache
        }
        acc.merge(updateGlobalFieldCache)
      })

      acc.merge(newGlobalFieldCache)
    })
  }
}