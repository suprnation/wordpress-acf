package com.suprnation.cms.resolver

import java.util
import java.util.Collections

import com.suprnation.cms.compiler.AstCompiler
import com.suprnation.cms.executors.fields.{ParameterisedRelationshipExecutor, PrimitiveFieldTokenExecutor}
import com.suprnation.cms.executors.{ClassTokenExecutor, FieldExecutionPlan}
import com.suprnation.cms.injector.Injector.FieldInjector
import com.suprnation.cms.log.ExecutionLogger
import com.suprnation.cms.model.CmsPost
import com.suprnation.cms.repository.CmsPostMetaRepository
import com.suprnation.cms.result.Result
import com.suprnation.cms.service.{AcfFieldService, CmsPostMetaService, CmsPostService, CmsRelationshipService}
import com.suprnation.cms.store.GlobalPostCacheStore
import com.suprnation.cms.tokens._
import com.suprnation.cms.types.{PostId, Taxonomy, Term}
import com.suprnation.to.TaxonomyType.Tax
import com.suprnation.to.{NestedListType, SimpleFlatList, SimpleType, TaxonomyType}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.internal.verification.VerificationModeFactory
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}

import scala.collection.mutable

@SuppressWarnings(Array("deprecation"))
class ResolverTest extends FunSuite with Matchers with BeforeAndAfterEach {

  val executionLogger: ExecutionLogger.IgnoreAllExecutionLogger.type = ExecutionLogger.IgnoreAllExecutionLogger

  var acfFieldService: AcfFieldService = _
  var astCompiler: AstCompiler = _
  var cmsPostMetaRepository: CmsPostMetaRepository = _
  var cmsPostService: CmsPostService = _
  var cmsPostMetaService: CmsPostMetaService = _
  var cmsRelationshipService: CmsRelationshipService = _
  var store: GlobalPostCacheStore = _

  test("shouldGetAllPrimitiveFieldsAtOneGo") {
    val clazz = classOf[SimpleType]
    val postType = "a"
    val token = PostToken(postType, clazz, List(
      FieldToken(FieldInjector(clazz.getDeclaredField("longReference")), classOf[java.lang.Long]),
      FieldToken(FieldInjector(clazz.getDeclaredField("integerReference")), classOf[java.lang.Integer]),
      FieldToken(FieldInjector(clazz.getDeclaredField("stringReference")), classOf[java.lang.String]),
      FieldToken(FieldInjector(clazz.getDeclaredField("longPrimitive")), classOf[java.lang.Long]),
      FieldToken(FieldInjector(clazz.getDeclaredField("intPrimitive")), classOf[java.lang.Integer])
    ))

    val posts = List(new CmsPost(1L), new CmsPost(2L))
    when(cmsPostService.findByType("a")).thenReturn(posts)

    val fieldResults: Map[PostId, Map[CmsFieldToken, Result[Nothing]]] =
      posts.map(post => post.getId -> token.fields.map {
        t: CmsFieldToken => t -> Result.notFoundInDatabase
      }.toMap).toMap

    val postIdsFilter: Set[PostId] = posts.map(_.getId).toSet
    when(acfFieldService.getPrimitiveField(token.fields, postIdsFilter)).thenReturn(fieldResults)

    ClassTokenExecutor(token, token.fields.map(PrimitiveFieldTokenExecutor(_)(acfFieldService, executionLogger)))(acfFieldService, astCompiler, cmsPostMetaRepository,
      cmsPostService, cmsPostMetaService, cmsRelationshipService, executionLogger).execute()(store)

    verify(cmsPostService, VerificationModeFactory.only()).findByType(postType)
    verify(acfFieldService, VerificationModeFactory.only()).getPrimitiveField(token.fields, postIdsFilter)
  }

  test("shouldGetAllParameterisedListsAtOneGo") {
    val clazz = classOf[NestedListType]
    val postType = "nlt"
    val token = PostToken(postType, clazz, List(
      ParameterisedListToken("s", FieldInjector(classOf[NestedListType].getDeclaredField("list")), classOf[java.util.List[Object]], classOf[SimpleFlatList])
    ))

    val posts = List(new CmsPost(1L), new CmsPost(2L))
    val referencedPostId: PostId = 3L
    when(cmsPostService.findByType(postType)).thenReturn(posts)
    val fieldResults: Map[PostId, Map[CmsFieldToken, Result[Nothing]]] =
      posts.map(post => post.getId -> token.fields.map {
        t: CmsFieldToken => t -> Result(Collections.singletonList(referencedPostId)).asInstanceOf[Result[Nothing]]
      }.toMap).toMap
    val postIdsFilter = posts.map(_.getId).toSet
    when(acfFieldService.getPrimitiveField(token.fields, postIdsFilter)).thenReturn(fieldResults)

    val mockSubExecution = mock(classOf[ClassTokenExecutor[FieldExecutionPlan[CmsFieldToken, _], SimpleFlatList]])
    val referencedPostsFilter = posts.map(_ => referencedPostId).toSet
    when(mockSubExecution.filter(referencedPostsFilter)).thenReturn(mockSubExecution)
    val subResult: Result[util.List[SimpleFlatList]] = Result(Collections.emptyList())
    when(mockSubExecution.execute(any())(any())).thenReturn(subResult)

    when(astCompiler.compile(classOf[SimpleFlatList])).thenReturn(mockSubExecution)

    ClassTokenExecutor(token, token.fields.map(PrimitiveFieldTokenExecutor(_)(acfFieldService, executionLogger)))(acfFieldService, astCompiler, cmsPostMetaRepository,
      cmsPostService, cmsPostMetaService, cmsRelationshipService, executionLogger).execute()(store)

    verify(cmsPostService, VerificationModeFactory.only()).findByType(postType)
    verify(acfFieldService, VerificationModeFactory.only()).getPrimitiveField(token.fields, postIdsFilter)
  }

  test("shouldGetAllTaxonomiesAtOneGo") {
    val clazz = classOf[TaxonomyType]
    val relationshipToken = ParameterisedRelationshipToken("taxonomy", FieldInjector(clazz.getDeclaredField("terms")), classOf[java.util.List[Object]], classOf[Tax])
    val postType = "tt"
    val token = PostToken(postType, clazz, List(relationshipToken))

    val posts = List(new CmsPost(1L), new CmsPost(2L))
    when(cmsPostService.findByType(postType)).thenReturn(posts)
    val fieldResults: Map[PostId, Map[CmsFieldToken, Result[Nothing]]] = posts.map(_.getId -> Map[CmsFieldToken, Result[Nothing]]()).toMap
    val postIdsFilter = posts.map(_.getId).toSet
    when(acfFieldService.getPrimitiveField(List.empty, postIdsFilter)).thenReturn(fieldResults)

    val result: Map[Taxonomy, Map[PostId, Result[java.util.List[Term]]]] =
      token.fields.map {
        case ParameterisedRelationshipToken(taxonomy, _, _, _) => taxonomy -> posts.map(_.getId -> Result(new util.ArrayList[Term]())).toMap
      }.toMap
    val taxonomies = Set("taxonomy")
    val postIds = posts.map(_.getId).toSet
    when(cmsRelationshipService.getRelationshipsForPostsAndTaxonomies(postIds, taxonomies)).thenReturn(result)

    ClassTokenExecutor(token, List(ParameterisedRelationshipExecutor(relationshipToken)(executionLogger, cmsRelationshipService)))(acfFieldService, astCompiler, cmsPostMetaRepository,
      cmsPostService, cmsPostMetaService, cmsRelationshipService, executionLogger).execute()(store)

    verify(cmsPostService, VerificationModeFactory.only()).findByType(postType)
    verify(cmsRelationshipService, VerificationModeFactory.only()).getRelationshipsForPostsAndTaxonomies(postIds, taxonomies)
    verify(acfFieldService, VerificationModeFactory.only()).getPrimitiveField(List.empty, postIdsFilter)
  }

  override protected def beforeEach(): Unit = {
    acfFieldService = mock(classOf[AcfFieldService])
    astCompiler = mock(classOf[AstCompiler])
    cmsPostMetaRepository = mock(classOf[CmsPostMetaRepository])
    cmsPostService = mock(classOf[CmsPostService])
    cmsPostMetaService = mock(classOf[CmsPostMetaService])
    cmsRelationshipService = mock(classOf[CmsRelationshipService])
    store = new mutable.HashMap()
  }
}
