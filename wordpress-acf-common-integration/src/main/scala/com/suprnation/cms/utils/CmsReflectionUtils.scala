package com.suprnation.cms.utils

import java.lang.reflect._

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty}
import com.suprnation.cms.annotations.{Alias, Cast}

import scala.annotation.{Annotation, tailrec}
import scala.collection.mutable

object CmsReflectionUtils {
  def getAllFieldsIncludedInherited(postClass: Class[_]): List[Field] = {

    @tailrec
    def getAllFieldsWorker(clazz: Class[_], fields: mutable.Buffer[Field]): Unit = {
      fields ++= clazz.getDeclaredFields.filter(f => f.getAnnotation(classOf[JsonIgnore]) == null)
      if (clazz.getSuperclass != classOf[Object]) {
        getAllFieldsWorker(clazz.getSuperclass, fields)
      }
    }

    val buffer = mutable.Buffer[Field]()
    getAllFieldsWorker(postClass, buffer)
    buffer.toList
  }


  val cache: mutable.Map[(Class[_], String), String] = mutable.Map[(Class[_], String), String]()

  def isSetter(m: Method, forField: Field): Boolean =
    (m.getName == "set" + capitalize(forField.getName)) &&
      m.getParameterCount == 1

  def getCastAnnotations(field: Field) : Option[List[_ >: java.lang.annotation.Annotation]] = {
    val annotations : List[_ >: Annotation] = field.getDeclaredAnnotations.toList.filter(a => a.annotationType().isAnnotationPresent(classOf[Cast]))
    if(annotations.isEmpty) {
      Option.empty
    } else {
      Option(annotations)
    }
  }

  def getAnnotationValues[T <: java.lang.annotation.Annotation](field: Field, annotation: Class[T], extractValue: T => String): Option[List[String]] = {
    val annotations: List[T] = field.getDeclaredAnnotationsByType(annotation).toList
    if (annotations.isEmpty) {
      Option.empty
    } else {
      Option(annotations.map(a => extractValue.apply(a)))
    }
  }

  def getAnnotationValue[T <: java.lang.annotation.Annotation](sourceClass: Class[_], annotation: Class[T], extractValue: T => String): Option[String] = {
    val annotations: List[T] = sourceClass.getDeclaredAnnotationsByType(annotation).toList
    if (annotations.length != 1) {
      Option.empty
    } else {
      val a: T = annotations.head
      Option(extractValue.apply(a))
    }
  }

  def getStrictAnnotationValue[T <: java.lang.annotation.Annotation](sourceClass: Class[_], annotation: Class[T], extractValue: T => String): String = {
    this.getAnnotationValue(sourceClass, annotation, extractValue) match {
      case Some(value) => value
      case None => throw new IllegalStateException(s"[Class: ${sourceClass.getName}] is only expected to have one annotation of type [Annotation: ${annotation.getName}]")
    }
  }

  private[this] def getAliases(field: Field): List[String] =
    getAnnotationValues(field, classOf[Alias], (pt: Alias) => pt.value())
      .orElse(getAnnotationValues(field, classOf[JsonProperty], (pt: JsonProperty) => pt.value()))
      .orElse(Option(List(StringUtils.convertToLowerDashCaseFromCamelCase(field.getName))))
      .get

  def getAlias(field: Field): String = {
    val aliases = this.getAliases(field)
    if (aliases.size > 1) {
      throw new IllegalStateException(s"Multiple aliases found on [Field: $field]")
    } else
      aliases.head
  }


  def getField[T](possibleKeys: String, obj: T): Field = {
    val fields: List[Field] = obj.getClass.getDeclaredFields.toList
    fields.filter((f: Field) => {
      val alias = getAlias(f)
      possibleKeys == alias
    }).head
  }

  def getFieldWithAliases[T](obj: T): Map[String, Field] = {
    val fields: List[Field] = obj.getClass.getDeclaredFields.toList
    fields.map((f: Field) => getAlias(f) -> f).toMap
  }

  def getFirstGenericType(field: Field): Option[Class[_]] = getFirstGenericType(field.getGenericType)

  def getFirstGenericType(clazz: Type): Option[Class[_]] =
    Option(clazz)
      .filter(_.isInstanceOf[ParameterizedType])
      .flatMap(_.asInstanceOf[ParameterizedType]
        .getActualTypeArguments
        .headOption)
      .map(_.asInstanceOf[Class[_]])


  def construct[T](clazz: Class[_]): T = {
    if (clazz.eq(classOf[java.util.Collection[_]])) {
      throw new UnsupportedOperationException()
    } else if (clazz.eq(classOf[java.util.List[_]])) {
      java.util.Arrays.asList().asInstanceOf[T]
    } else {
      val noArgsConstructor = (clazz.getDeclaredConstructors ++ clazz.getConstructors)
        .filter(c => c.getParameterCount == 0)
        .head
      noArgsConstructor.setAccessible(true)
      clazz.getFields.foreach(_.setAccessible(true))
      noArgsConstructor.newInstance().asInstanceOf[T]
    }
  }

  def instantiateEnum(enumClass: Class[_], value: String): AnyRef = {
    this.hasStaticMethod(enumClass, "getEnum") match {
      case Some(method) => method.invoke(null, value)
      case None => enumClass.getMethod("valueOf", classOf[String]).invoke(null, value)
    }
  }

  def capitalize(str: String): String = str.charAt(0).toUpper + str.substring(1)

  def hasStaticMethod(clazz: Class[_], methodName: String): Option[Method] = {
    clazz.getMethods.toList.find(method => method.getName == methodName && Modifier.isStatic(method.getModifiers))
  }
}
