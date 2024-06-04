package com.suprnation.cms.executors

import com.suprnation.cms.compiler.AstCompiler
import com.suprnation.cms.executors.fields.PrimitiveFieldTokenExecutor
import com.suprnation.cms.injector.Injector.FieldInjector
import com.suprnation.cms.log.ExecutionLogger
import com.suprnation.cms.model.CmsPost
import com.suprnation.cms.repository.CmsPostMetaRepository
import com.suprnation.cms.result.Result
import com.suprnation.cms.service.{AcfFieldService, CmsPostMetaService, CmsPostService, CmsRelationshipService}
import com.suprnation.cms.store.{GlobalPostCacheStore, InMemoryStore}
import com.suprnation.cms.tokens.{CmsFieldToken, FieldToken, PostToken}
import com.suprnation.cms.types.PostId
import com.suprnation.to.SimpleType
import org.mockito.Mockito.{mock, verify, when}
import org.mockito.internal.verification.VerificationModeFactory
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}

import java.time.ZonedDateTime

class ClassTokenExecutorTest extends FunSuite with Matchers with BeforeAndAfterEach {

  val executionLogger: ExecutionLogger.IgnoreAllExecutionLogger.type = ExecutionLogger.IgnoreAllExecutionLogger

  var acfFieldService: AcfFieldService = _
  var astCompiler: AstCompiler = _
  var cmsPostMetaRepository: CmsPostMetaRepository = _
  var cmsPostService: CmsPostService = _
  var cmsPostMetaService: CmsPostMetaService = _
  var cmsRelationshipService: CmsRelationshipService = _
  var store: GlobalPostCacheStore = _

  test("shouldNotAllowPostIdFilterWithGte") {
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
    val postIdsFilter: Set[PostId] = posts.map(_.getId).toSet

    val date = ZonedDateTime.now()

    intercept[IllegalStateException] {
      ClassTokenExecutor(
        token,
        token.fields.map(PrimitiveFieldTokenExecutor(_)(acfFieldService, executionLogger)),
        postIdsFilter
      )(acfFieldService, astCompiler, cmsPostMetaRepository, cmsPostService, cmsPostMetaService, cmsRelationshipService, executionLogger)
        .gte(date)
        .execute()(store)
    }

  }

  test("shouldNotAllowPostIdFilterWithLte") {
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
    val postIdsFilter: Set[PostId] = posts.map(_.getId).toSet

    val date = ZonedDateTime.now()

    intercept[IllegalStateException] {
      ClassTokenExecutor(
        token,
        token.fields.map(PrimitiveFieldTokenExecutor(_)(acfFieldService, executionLogger)),
        postIdsFilter
      )(acfFieldService, astCompiler, cmsPostMetaRepository, cmsPostService, cmsPostMetaService, cmsRelationshipService, executionLogger)
        .lte(date)
        .execute()(store)
    }
  }

  test("shouldNotAllowPostIdFilterByPostId") {
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

    val postIdsFilter: Set[PostId] = posts.map(_.getId).toSet
    when(cmsPostService.findByTypeAndIdIn("a", postIdsFilter)).thenReturn(posts)

    val fieldResults: Map[PostId, Map[CmsFieldToken, Result[Nothing]]] =
      posts.map(post => post.getId -> token.fields.map {
        t: CmsFieldToken => t -> Result.notFoundInDatabase
      }.toMap).toMap

    when(acfFieldService.getPrimitiveField(token.fields, postIdsFilter)).thenReturn(fieldResults)

    ClassTokenExecutor(
      token,
      token.fields.map(PrimitiveFieldTokenExecutor(_)(acfFieldService, executionLogger)),
      postIdsFilter
    )(acfFieldService, astCompiler, cmsPostMetaRepository, cmsPostService, cmsPostMetaService, cmsRelationshipService, executionLogger)
      .execute()(store)

    verify(cmsPostService, VerificationModeFactory.only()).findByTypeAndIdIn(postType, postIdsFilter)

  }

  test("shouldGetByTypeFilteredByGte") {
    val clazz = classOf[SimpleType]
    val postType = "a"
    val token = PostToken(postType, clazz, List(
      FieldToken(FieldInjector(clazz.getDeclaredField("longReference")), classOf[java.lang.Long]),
      FieldToken(FieldInjector(clazz.getDeclaredField("integerReference")), classOf[java.lang.Integer]),
      FieldToken(FieldInjector(clazz.getDeclaredField("stringReference")), classOf[java.lang.String]),
      FieldToken(FieldInjector(clazz.getDeclaredField("longPrimitive")), classOf[java.lang.Long]),
      FieldToken(FieldInjector(clazz.getDeclaredField("intPrimitive")), classOf[java.lang.Integer])
    ))

    val date = ZonedDateTime.now()

    val posts = List(new CmsPost(1L), new CmsPost(2L))
    when(cmsPostService.findByTypeAndModifiedDateGte("a", date)).thenReturn(posts)

    val fieldResults: Map[PostId, Map[CmsFieldToken, Result[Nothing]]] =
      posts.map(post => post.getId -> token.fields.map {
        t: CmsFieldToken => t -> Result.notFoundInDatabase
      }.toMap).toMap

    val postIdsFilter: Set[PostId] = posts.map(_.getId).toSet
    when(acfFieldService.getPrimitiveField(token.fields, postIdsFilter)).thenReturn(fieldResults)

    ClassTokenExecutor(
      token,
      token.fields.map(PrimitiveFieldTokenExecutor(_)(acfFieldService, executionLogger))
    )(acfFieldService, astCompiler, cmsPostMetaRepository, cmsPostService, cmsPostMetaService, cmsRelationshipService, executionLogger)
      .gte(date)
      .execute()(store)

    verify(cmsPostService, VerificationModeFactory.only()).findByTypeAndModifiedDateGte(postType, date)

  }

  test("shouldGetByTypeFilteredByLte") {
    val clazz = classOf[SimpleType]
    val postType = "a"
    val token = PostToken(postType, clazz, List(
      FieldToken(FieldInjector(clazz.getDeclaredField("longReference")), classOf[java.lang.Long]),
      FieldToken(FieldInjector(clazz.getDeclaredField("integerReference")), classOf[java.lang.Integer]),
      FieldToken(FieldInjector(clazz.getDeclaredField("stringReference")), classOf[java.lang.String]),
      FieldToken(FieldInjector(clazz.getDeclaredField("longPrimitive")), classOf[java.lang.Long]),
      FieldToken(FieldInjector(clazz.getDeclaredField("intPrimitive")), classOf[java.lang.Integer])
    ))

    val date = ZonedDateTime.now()

    val posts = List(new CmsPost(1L), new CmsPost(2L))
    when(cmsPostService.findByTypeAndModifiedDateLte("a", date)).thenReturn(posts)

    val fieldResults: Map[PostId, Map[CmsFieldToken, Result[Nothing]]] =
      posts.map(post => post.getId -> token.fields.map {
        t: CmsFieldToken => t -> Result.notFoundInDatabase
      }.toMap).toMap

    val postIdsFilter: Set[PostId] = posts.map(_.getId).toSet
    when(acfFieldService.getPrimitiveField(token.fields, postIdsFilter)).thenReturn(fieldResults)

    ClassTokenExecutor(
      token,
      token.fields.map(PrimitiveFieldTokenExecutor(_)(acfFieldService, executionLogger))
    )(acfFieldService, astCompiler, cmsPostMetaRepository, cmsPostService, cmsPostMetaService, cmsRelationshipService, executionLogger)
      .lte(date)
      .execute()(store)

    verify(cmsPostService, VerificationModeFactory.only()).findByTypeAndModifiedDateLte(postType, date)

  }

  test("shouldGetByTypeFilteredByGteAndLte") {
    val clazz = classOf[SimpleType]
    val postType = "a"
    val token = PostToken(postType, clazz, List(
      FieldToken(FieldInjector(clazz.getDeclaredField("longReference")), classOf[java.lang.Long]),
      FieldToken(FieldInjector(clazz.getDeclaredField("integerReference")), classOf[java.lang.Integer]),
      FieldToken(FieldInjector(clazz.getDeclaredField("stringReference")), classOf[java.lang.String]),
      FieldToken(FieldInjector(clazz.getDeclaredField("longPrimitive")), classOf[java.lang.Long]),
      FieldToken(FieldInjector(clazz.getDeclaredField("intPrimitive")), classOf[java.lang.Integer])
    ))

    val date = ZonedDateTime.now()

    val posts = List(new CmsPost(1L), new CmsPost(2L))
    when(cmsPostService.findByTypeAndModifiedDateGteAndModifiedDateLte("a", date, date)).thenReturn(posts)

    val fieldResults: Map[PostId, Map[CmsFieldToken, Result[Nothing]]] =
      posts.map(post => post.getId -> token.fields.map {
        t: CmsFieldToken => t -> Result.notFoundInDatabase
      }.toMap).toMap

    val postIdsFilter: Set[PostId] = posts.map(_.getId).toSet
    when(acfFieldService.getPrimitiveField(token.fields, postIdsFilter)).thenReturn(fieldResults)

    ClassTokenExecutor(
      token,
      token.fields.map(PrimitiveFieldTokenExecutor(_)(acfFieldService, executionLogger))
    )(acfFieldService, astCompiler, cmsPostMetaRepository, cmsPostService, cmsPostMetaService, cmsRelationshipService, executionLogger)
      .gte(date)
      .lte(date)
      .execute()(store)

    verify(cmsPostService, VerificationModeFactory.only()).findByTypeAndModifiedDateGteAndModifiedDateLte(postType, date, date)

  }

  override protected def beforeEach(): Unit = {
    acfFieldService = mock(classOf[AcfFieldService])
    astCompiler = mock(classOf[AstCompiler])
    cmsPostMetaRepository = mock(classOf[CmsPostMetaRepository])
    cmsPostService = mock(classOf[CmsPostService])
    cmsPostMetaService = mock(classOf[CmsPostMetaService])
    cmsRelationshipService = mock(classOf[CmsRelationshipService])
    store = InMemoryStore.newStore
  }

}
