package com.suprnation.cms.ast

import java.util

import com.suprnation.cms.annotations.{PostType, Taxonomy}
import com.suprnation.cms.enums.CmsPostStatus
import com.suprnation.cms.injector.Injector
import com.suprnation.cms.marker.CmsPostIdentifier
import com.suprnation.cms.semantic.{NoDupSemanticAnalyser, TypeSemanticAnalyser}
import com.suprnation.cms.tokens._
import com.suprnation.cms.utils.CmsReflectionUtils
import org.joda.time.DateTime
import org.springframework.stereotype.Component

object AstBuilder {
  val primitives: Set[Class[_]] = Set(
    classOf[java.lang.Integer],
    classOf[java.lang.Boolean],
    classOf[java.lang.Long],
    classOf[java.lang.Double],
    classOf[java.lang.String],
    classOf[DateTime])

  val collections: Set[Class[_]] = Set(classOf[java.util.List[_]], classOf[java.util.Set[_]])
}

@Component
class AstBuilder() {
  val collectionsParameterisedType = List(classOf[java.lang.Integer], classOf[java.lang.Long])

  val semanticAnalysers = List(new NoDupSemanticAnalyser, new TypeSemanticAnalyser)

  def isPrimitive(clazz: Class[_]): Boolean = AstBuilder.primitives.contains(clazz)

  def isCollection(clazz: Class[_]): Boolean = AstBuilder.collections.contains(clazz)

  def isPrimitiveCollection(clazz: Class[_], parameterisedType: Option[Class[_]]): Boolean = isCollection(clazz) &&
    isPrimitive(parameterisedType match {
      case None => throw new IllegalStateException(s"No parameterised type for class $clazz")
      case Some(paramClass) => paramClass
    })

  def isPostType(value: Class[_]): Boolean = CmsReflectionUtils.getAnnotationValue(value, classOf[PostType], (postType: PostType) => postType.value).isDefined

  def isTaxonomy(clazz: Class[_]): Boolean = CmsReflectionUtils.getAnnotationValue(clazz, classOf[Taxonomy], (taxonomy: Taxonomy) => taxonomy.value).isDefined

  def tokenizeRoot[T <: CmsPostIdentifier](implicit clazz: Class[T]): PostToken[T] = {
    val token = tokenize(clazz)
    token match {
      case pt: PostToken[T] => pt
      case _ => throw new IllegalStateException(s"[Clazz: ${clazz.getName}] needs to generate a PostToken.  Are you sure that you have annotated this class correctly?")
    }
  }

  def tokenize[T <: CmsPostIdentifier](implicit clazz: Class[T]): CmsToken = {
    val postType = CmsReflectionUtils.getStrictAnnotationValue(clazz, classOf[PostType], (pt: PostType) => pt.value())
    val token = CmsReflectionUtils.getAllFieldsIncludedInherited(clazz)
      .filterNot(field => java.lang.reflect.Modifier.isStatic(field.getModifiers))
      .foldLeft(PostToken(postType, clazz))((postToken, field) => {
        val injector = Injector.getInjectorForField(field, clazz)
        val targetClass = injector.targetClass
        val parameterisedTypeOption = injector.parameterisedType
        if (isPrimitive(targetClass) || targetClass == classOf[CmsPostStatus] || (targetClass.isEnum && !isTaxonomy(targetClass))) {
          postToken.withField(FieldToken(injector, targetClass))
        } else if (isPrimitiveCollection(targetClass, parameterisedTypeOption)) {
          postToken.withField(ShallowListToken(injector, targetClass.asInstanceOf[Class[_ <: util.Collection[_]]], parameterisedTypeOption.get))
        } else if (isCollection(targetClass)) {
          // Retrieve the parametrized type.
          val parameterisedType = parameterisedTypeOption.get
          if (isTaxonomy(parameterisedType)) {
            postToken.withField(ParameterisedRelationshipToken(CmsReflectionUtils.getStrictAnnotationValue(parameterisedType, classOf[Taxonomy], (pt: Taxonomy) => pt.value()), injector, targetClass.asInstanceOf[Class[_ <: util.Collection[Object]]], parameterisedType))
          } else {
            if (parameterisedType.isEnum) {
              postToken.withField(ShallowListToken(injector, targetClass.asInstanceOf[Class[_ <: util.Collection[_]]], parameterisedTypeOption.get))
            } else {
              val postTypeName = CmsReflectionUtils.getStrictAnnotationValue(parameterisedType, classOf[PostType], (pt: PostType) => pt.value())
              postToken.withField(ParameterisedListToken(postTypeName, injector, targetClass.asInstanceOf[Class[_ <: util.Collection[Object]]], parameterisedType.asInstanceOf[Class[CmsPostIdentifier]]))
            }
          }
        }
        else if (isPostType(targetClass)) {
          val postTypeName = CmsReflectionUtils.getStrictAnnotationValue(targetClass, classOf[PostType], (pt: PostType) => pt.value())
          postToken.withField(PostFieldToken[CmsPostIdentifier](postTypeName, injector,
            tokenizeRoot[CmsPostIdentifier](targetClass.asInstanceOf[Class[CmsPostIdentifier]])))
        } else {
          throw new IllegalStateException(s"Meta Data for [Field: ${field.getName}] on Class [$clazz] is incompatible with this compiler.  ")
        }
      }
      )
    val violations = semanticAnalysers.flatten(_.validate(token.fields))
    if (violations.nonEmpty) {
      val explanations = violations.foldLeft("")((acc, violation) => s"$acc${violation.explain} ")
      throw new IllegalArgumentException(s"Semantic error: $explanations")
    }

    token
  }
}
