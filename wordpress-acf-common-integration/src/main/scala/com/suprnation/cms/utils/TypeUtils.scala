package com.suprnation.cms.utils

import java.lang.annotation.Annotation
import java.util

import com.github.ooxi.phparser.SerializedPhpParser
import com.suprnation.cms.converter.PrimitiveTypeConverter
import com.suprnation.cms.tokens._

import scala.collection.JavaConverters._

object TypeUtils {

  def getType(clazz: Class[_]): Class[_] = {
    clazz match {
      case java.lang.Long.TYPE =>
        classOf[java.lang.Long]
      case java.lang.Integer.TYPE =>
        classOf[java.lang.Integer]
      case java.lang.Boolean.TYPE =>
        classOf[java.lang.Boolean]
      case java.lang.Double.TYPE =>
        classOf[java.lang.Double]
      case _ =>
        clazz
    }
  }

  private[this] def isAllDigits(x: String) = {
    x forall Character.isDigit
  }

  def convert(value: String, cmsToken: CmsToken): Any = cmsToken match {
    case FieldToken(injector, _) => convertPrimitiveOrEnum(value, cmsToken.source, injector.annotations)
    case ParameterisedListToken(_, injector, collectionClass, _) =>
      convert(value, ShallowListToken(injector, collectionClass, classOf[java.lang.Long]))
    case PostFieldToken(_, _, _) =>
      if (value.startsWith("null") || org.springframework.util.StringUtils.isEmpty(value)) {
        null
      } else {
        java.lang.Long.valueOf(value)
      }
    case ShallowListToken(injector, collectionType, parameterisedType) =>
      val result: java.util.Collection[Object] = instantiateCollection(collectionType.asInstanceOf[Class[util.Collection[Object]]])
      if (value.startsWith("a")) {
        val obj = new SerializedPhpParser(value).parse()
        (obj match {
          case map: java.util.LinkedHashMap[String, String] =>
            map.values()
        }).asScala.foreach(value => {
          val obj = this.convertPrimitiveOrEnum(value, parameterisedType, injector.annotations)
          result.add(obj)
        })
      } else if (isAllDigits(value) && value.length > 0) {
        // TODO: Add commentary that this is needed since we are treating List[PostType] and PostType the same.
        // so that the compiler is more lean.
        result.add(java.lang.Long.valueOf(value))
      }
      result
  }

  def convertPrimitiveOrEnum(value: String, targetClass: Class[_], annotations: Option[List[_ >: Annotation]]): Object = {
    PrimitiveTypeConverter.convert(value, targetClass, annotations.getOrElse(List.empty))
  }

  def convertToEnum[T](collectionClass: Class[_ <: util.Collection[_]], values: util.List[T], parameterisedType: Class[Enum[_]]): util.Collection[T] = {
    val objects = instantiateCollection(collectionClass.asInstanceOf[Class[util.Collection[Object]]])
    values.asScala.foreach(value => objects.add(CmsReflectionUtils.instantiateEnum(
      parameterisedType, value.toString)))
    objects.asInstanceOf[util.Collection[T]]
  }

  def instantiateCollection[T](collectionClass: Class[_ <: util.Collection[T]]): util.Collection[T] = {
    if (collectionClass == classOf[util.List[_]]) {
      new util.ArrayList[T]()
    } else {
      if (collectionClass == classOf[util.Set[_]]) {
        new util.LinkedHashSet[T]()
      } else {
        throw new UnsupportedOperationException(s"$collectionClass is not one of the supported collection types")
      }
    }
  }

}
