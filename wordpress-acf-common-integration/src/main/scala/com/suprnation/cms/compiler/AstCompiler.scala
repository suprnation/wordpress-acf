package com.suprnation.cms.compiler

import com.suprnation.cms.ast.AstBuilder
import com.suprnation.cms.executors._
import com.suprnation.cms.executors.fields.{ParameterisedListExecutor, ParameterisedRelationshipExecutor, PostFieldTokenExecutor, PrimitiveFieldTokenExecutor}
import com.suprnation.cms.marker.CmsPostIdentifier
import com.suprnation.cms.repository.{CmsPostMetaRepository, CmsTermRelationshipRepository}
import com.suprnation.cms.service.{AcfFieldService, CmsPostMetaService, CmsPostService, CmsRelationshipService}
import com.suprnation.cms.tokens._
import com.suprnation.cms.log.ExecutionLogger

class AstCompiler(implicit
                  astBuilder: AstBuilder,
                  acfFieldService: AcfFieldService,
                  cmsPostService: CmsPostService,
                  cmsPostMetaRepository: CmsPostMetaRepository,
                  cmsPostMetaService: CmsPostMetaService,
                  cmsRelationshipService: CmsRelationshipService,
                  cmsTermRelationshipRepository: CmsTermRelationshipRepository,
                 ) {

  private implicit val self: AstCompiler = this
  private implicit val executionLogger: ExecutionLogger = ExecutionLogger.IgnoreAllExecutionLogger

  def compile[T](clazz: Class[T]): ClassTokenExecutor[FieldExecutionPlan[CmsFieldToken, _], T] = {
    val token = this.astBuilder.tokenize(clazz)
    token match {
      case postToken: PostToken[T] =>
        val executors = postToken.fields.map {
          case fieldToken: FieldToken => PrimitiveFieldTokenExecutor(fieldToken)
          case flatListToken: ShallowListToken[_] => PrimitiveFieldTokenExecutor(flatListToken)
          case parameterisedListToken: ParameterisedListToken[CmsPostIdentifier] => ParameterisedListExecutor[CmsPostIdentifier](parameterisedListToken)
          case postToken: PostFieldToken[_] => PostFieldTokenExecutor(postToken)
          case parameterisedRelationshipExecutor: ParameterisedRelationshipToken[_] => ParameterisedRelationshipExecutor(parameterisedRelationshipExecutor)
        }
        ClassTokenExecutor(postToken, executors)
    }
  }
}
