package com.suprnation.cms.tokens

import com.suprnation.cms.injector.Injector
import com.suprnation.cms.types.Taxonomy
import com.suprnation.cms.utils.CmsReflectionUtils

sealed trait CmsToken {
  val source: Class[_]
}

trait CmsFieldToken extends CmsToken {
  val injector: Injector

  def fieldName: String = CmsReflectionUtils.getAlias(injector.field)

  override def toString: String = s"$fieldName:${source.getSimpleName}"
}

trait PostType {
  val postType: String
}

trait CmsFieldTokenWithPostType[T] extends CmsFieldToken with PostType {
  val parameterisedType: Class[T]
}


object PostToken {
  def apply[T](postType: String, source: Class[T]) = new PostToken[T](postType, source, List.empty)
}

case class PostToken[T](postType: String, source: Class[T], fields: List[CmsFieldToken]) extends CmsToken with PostType {
  def withField(field: CmsFieldToken) = new PostToken(postType, source, field :: fields)

  override def toString: String = fields.foldLeft(s"$postType:${source.getSimpleName}::Post[$postType]")((acc, field) => s"$acc  $field")
}

case class FieldToken(injector: Injector, source: Class[_]) extends CmsFieldToken

case class ShallowListToken[T](injector: Injector, source: Class[_ <: java.util.Collection[_]], parameterisedType: Class[T]) extends CmsFieldToken {
  override def toString: String = s"$fieldName:${parameterisedType.getSimpleName}"
}

case class PostFieldToken[T](postType: String, injector: Injector, postToken: PostToken[T]) extends CmsFieldTokenWithPostType[T] {
  override val source: Class[_] = postToken.source
  override val parameterisedType: Class[T] = postToken.source

  override def toString: String = s"${super.toString}::Post[$postType]"
}

case class ParameterisedListToken[T](postType: String, injector: Injector, source: Class[_ <: java.util.Collection[_]], parameterisedType: Class[T]) extends CmsFieldTokenWithPostType[T] {
  override def toString: String = s"$fieldName:${source.getSimpleName}[${parameterisedType.getSimpleName}]::Post[$postType]"
}

trait RelationshipToken extends CmsFieldToken {
  def taxonomy: Taxonomy
}

case class ParameterisedRelationshipToken[T](override val taxonomy: Taxonomy, injector: Injector, source: Class[_ <: java.util.Collection[_]], parameterisedType: Class[T]) extends RelationshipToken {
  override def toString: String = s"$fieldName:${source.getSimpleName}[${parameterisedType.getSimpleName}]::Taxonomy[$taxonomy]"
}

