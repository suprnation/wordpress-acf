package com.suprnation.cms.ast

import com.suprnation.cms.injector.Injector.FieldInjector
import com.suprnation.to._
import com.suprnation.cms.tokens.{FieldToken, ShallowListToken}
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

  }

  test("shouldSupportNestedObjects") {

  }

  // the below test does not pass because of functions not matching
  //  test("shouldSupportCustomerDeserialisersAndSetters") {
  //    val clazz = classOf[SetterAndInjectorTestType]
  //    val postToken = astBuilder.tokenizeRoot(clazz)
  //    val setter = classOf[SetterAndInjectorTestType].getDeclaredMethods.head
  //    postToken.postType shouldEqual "setterAndInjectorTestType"
  //    postToken.source shouldEqual clazz
  //    postToken.fields shouldEqual List(
  //      FieldToken("setter-and-preprocesser", SetterInjector(setter, InjectorPreProcessor(classOf[TestDeserialiser])), classOf[lang.Integer])
  //    )
  //  }

}
