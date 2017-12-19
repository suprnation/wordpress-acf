package com.suprnation.cms.ast

import com.suprnation.cms.deserialiser.TestDeserialiser
import com.suprnation.cms.injector.Injector.{FieldInjector, SetterInjector}
import com.suprnation.cms.injector.InjectorPreProcessor
import com.suprnation.cms.tokens._
import com.suprnation.to.TaxonomyType.Tax
import com.suprnation.to._
import org.scalatest.{FunSuite, _}

class AstBuilderTest extends FunSuite with Matchers {

  val astBuilder = new AstBuilder()

  test("shouldTokenizeSimpleObjectTypes") {
    val clazz = classOf[SimpleType]
    val postToken = astBuilder.tokenizeRoot(clazz)
    postToken.postType shouldEqual "a"
    postToken.source shouldEqual clazz
    postToken.fields shouldEqual List(
      FieldToken(FieldInjector(clazz.getDeclaredField("longReference")), classOf[java.lang.Long]),
      FieldToken(FieldInjector(clazz.getDeclaredField("integerReference")), classOf[java.lang.Integer]),
      FieldToken(FieldInjector(clazz.getDeclaredField("stringReference")), classOf[java.lang.String]),
      FieldToken(FieldInjector(clazz.getDeclaredField("longPrimitive")), classOf[java.lang.Long]),
      FieldToken(FieldInjector(clazz.getDeclaredField("intPrimitive")), classOf[java.lang.Integer]),
    )
  }

  test("shouldTokenizeAliasedSimpleType") {
    val clazz = classOf[AliasedSimpleType]
    val postToken = astBuilder.tokenizeRoot(clazz)
    postToken.postType shouldEqual "b"
    postToken.source shouldEqual clazz
    postToken.fields shouldEqual List(
      FieldToken(FieldInjector(clazz.getDeclaredField("longReference")), classOf[java.lang.Long]),
      FieldToken(FieldInjector(clazz.getDeclaredField("integerReference")), classOf[java.lang.Integer]),
      FieldToken(FieldInjector(clazz.getDeclaredField("stringReference")), classOf[java.lang.String]),
      FieldToken(FieldInjector(clazz.getDeclaredField("longPrimitive")), classOf[java.lang.Long]),
      FieldToken(FieldInjector(clazz.getDeclaredField("intPrimitive")), classOf[java.lang.Integer])
    )
  }

  test("shouldTokenizeAliasedSimpleTypeWithJacksonMappings") {
    val clazz = classOf[AliasedSimpleTypeWithJackson]
    val postToken = astBuilder.tokenizeRoot(clazz)
    postToken.postType shouldEqual "c"
    postToken.source shouldEqual clazz
    postToken.fields shouldEqual List(
      FieldToken(FieldInjector(clazz.getDeclaredField("longReference")), classOf[java.lang.Long])
    )
  }

  test("shouldNotAcceptNonSupportedPrimitiveTypes") {
    assertThrows[IllegalStateException] {
      astBuilder.tokenize(classOf[InvalidSimpleType])
    }
  }

  test("shouldSupportFlatLists") {
    val clazz = classOf[SimpleFlatList]
    val postToken = astBuilder.tokenizeRoot(clazz)
    postToken.postType shouldEqual "s"
    postToken.source shouldEqual classOf[SimpleFlatList]
    postToken.fields shouldEqual List(
      ShallowListToken(FieldInjector(clazz.getDeclaredField("postIds")), classOf[java.util.List[_]], classOf[java.lang.Integer])
    )
  }

  test("shouldSupportFlatListsWithJacksonMappins") {
    val clazz = classOf[SimpleFlatListWithJackson]
    val postToken = astBuilder.tokenizeRoot(clazz)
    postToken.postType shouldEqual "simpleFlatListWithJackson"
    postToken.source shouldEqual clazz
    postToken.fields shouldEqual List(
      ShallowListToken(FieldInjector(clazz.getDeclaredField("postIds")), classOf[java.util.List[_]], classOf[java.lang.Integer])
    )
  }

  test("shouldSupportTaxonomies") {
    val clazz = classOf[TaxonomyType]
    val postToken = astBuilder.tokenizeRoot(clazz)
    postToken.postType shouldEqual "tt"
    postToken.source shouldEqual clazz
    postToken.fields shouldEqual List(
      ParameterisedRelationshipToken("taxonomy", FieldInjector(clazz.getDeclaredField("terms")), classOf[java.util.List[Object]], classOf[Tax])
    )
  }

  test("shouldSupportNestedObjects") {
    val clazz = classOf[NestedType]
    val postToken = astBuilder.tokenizeRoot(clazz)
    postToken.postType shouldEqual "nt"
    postToken.source shouldEqual clazz
    postToken.fields shouldEqual List(
      PostFieldToken("s", FieldInjector(clazz.getDeclaredField("join")), PostToken("s", classOf[SimpleFlatList], List(
        ShallowListToken(FieldInjector(classOf[SimpleFlatList].getDeclaredField("postIds")), classOf[java.util.List[_]], classOf[java.lang.Integer])
      )))
    )
  }

  test("shouldSupportNestedListObjects") {
    val clazz = classOf[NestedListType]
    val postToken = astBuilder.tokenizeRoot(clazz)
    postToken.postType shouldEqual "nlt"
    postToken.source shouldEqual clazz
    postToken.fields shouldEqual List(
      ParameterisedListToken("s", FieldInjector(classOf[NestedListType].getDeclaredField("list")), classOf[java.util.List[Object]], classOf[SimpleFlatList])
    )
  }

  test("shouldSupportCustomerDeserialisersAndSetters") {
    val clazz = classOf[SetterAndInjectorTestType]
    val postToken = astBuilder.tokenizeRoot(clazz)
    val setter = classOf[SetterAndInjectorTestType].getDeclaredMethod("setSetterAndPreprocesser", classOf[java.lang.Integer])
    postToken.postType shouldEqual "setterAndInjectorTestType"
    postToken.source shouldEqual clazz
    postToken.fields shouldEqual List(
      FieldToken(SetterInjector(clazz.getDeclaredField("setterAndPreprocesser"), setter, InjectorPreProcessor(classOf[TestDeserialiser])), classOf[java.lang.Integer])
    )
  }

}
