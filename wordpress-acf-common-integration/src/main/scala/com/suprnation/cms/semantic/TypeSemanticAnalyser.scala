package com.suprnation.cms.semantic

import com.suprnation.cms.annotations
import com.suprnation.cms.ast.AstBuilder
import com.suprnation.cms.enums.CmsPostStatus
import com.suprnation.cms.tokens._

/**
  * The TypeSemanticAnalyser will go through each field and make sure that we can retrieve such a field.
  */
class TypeSemanticAnalyser extends SemanticAnalyser {
  val exceptionClasses: Set[Class[_]] = Set(classOf[CmsPostStatus])

  override def validate(tokens: List[CmsFieldToken]): List[UnsupportedType] = {
    tokens.map {
      case t@ParameterisedListToken(postType, injector, collectionClass, parameterisedType) =>
        Option(
          if (postType == null || postType.isEmpty) {
            List(UnsupportedType(t, "PostType is of invalid length"))
          } else {
            if (!parameterisedType.isAnnotationPresent(classOf[annotations.PostType])) {
              List(UnsupportedType(t, s"PostType is not annotated with @PostType"))
            } else if (!AstBuilder.collections.contains(injector.targetClass)) {
              List(UnsupportedType(t, s"Collection class should be one of ${AstBuilder.collections}"))
            } else if (!AstBuilder.collections.contains(collectionClass)) {
              List(UnsupportedType(t, s"Collection class should be one of ${AstBuilder.collections}"))
            } else {
              null
            }
          })

      case t@ParameterisedRelationshipToken(taxonomy, injector, collectionClass, clazz) =>
        Option(
          if (taxonomy == null || taxonomy.isEmpty) {
            List(UnsupportedType(t, "Taxonomy is of invalid length"))
          } else if (!clazz.isEnum) {
            List(UnsupportedType(t, "Taxonomy class is not an enum"))
          } else if (!AstBuilder.collections.contains(injector.targetClass)) {
            List(UnsupportedType(t, s"Collection class should be one of ${AstBuilder.collections}"))
          } else if (!AstBuilder.collections.contains(collectionClass)) {
            List(UnsupportedType(t, s"Collection class should be one of ${AstBuilder.collections}"))
          } else {
            null
          })
      case t@FieldToken(_, primitive) =>
        Option(
          if ((!AstBuilder.primitives.contains(primitive) && !primitive.isEnum) && !exceptionClasses.contains(primitive)) {
            List(UnsupportedType(t, "Target class is not one of the supported primitive types"))
          } else {
            null
          })
      case t@ShallowListToken(_, source, clazz) =>
        Option(if (!AstBuilder.primitives.contains(clazz) && !clazz.isEnum) {
          List(UnsupportedType(t, s"Parameterised type should be one of ${AstBuilder.primitives}"))
        } else if (!AstBuilder.collections.contains(source)) {
          List(UnsupportedType(t, s"Collection class should be one of ${AstBuilder.collections}"))
        } else {
          null
        })
      case t@PostFieldToken(postType, injector, postToken) =>
        Option(
          if (postType == null || postType.isEmpty) {
            List(UnsupportedType(t, "PostType is of invalid length"))
          } else {
            this.validate(postToken.fields)
          })
    }
      .filter(_.isDefined)
      .flatMap(_.get)
  }
}
