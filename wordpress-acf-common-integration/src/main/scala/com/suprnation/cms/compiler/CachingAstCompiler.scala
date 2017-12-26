package com.suprnation.cms.compiler

import com.suprnation.cms.ast.AstBuilder
import com.suprnation.cms.executors.{ClassTokenExecutor, FieldExecutionPlan}
import com.suprnation.cms.repository.{CmsPostMetaRepository, CmsTermRelationshipRepository}
import com.suprnation.cms.service.{AcfFieldService, CmsPostMetaService, CmsPostService, CmsRelationshipService}
import com.suprnation.cms.tokens.CmsFieldToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.mutable

@Component
class CachingAstCompiler @Autowired()(implicit
                                      astBuilder: AstBuilder,
                                      acfFieldService: AcfFieldService,
                                      cmsPostService: CmsPostService,
                                      cmsPostMetaRepository: CmsPostMetaRepository,
                                      cmsPostMetaService: CmsPostMetaService,
                                      cmsRelationshipService: CmsRelationshipService,
                                      cmsTermRelationshipRepository: CmsTermRelationshipRepository) {

  private val cache = mutable.HashMap[Class[_], ClassTokenExecutor[FieldExecutionPlan[CmsFieldToken, _], _]]()

  def compile[T](clazz: Class[T]): ClassTokenExecutor[FieldExecutionPlan[CmsFieldToken, _], _] =
    cache.getOrElseUpdate(clazz, new AstCompiler().compile(clazz))
}
