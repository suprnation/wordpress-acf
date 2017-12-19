package com.suprnation.cms.injector

import java.lang.annotation.Annotation
import java.lang.reflect.{Field, Method}
import java.util

import com.suprnation.cms.marker.CmsPostClonable
import com.suprnation.cms.utils.{CmsReflectionUtils, TypeUtils}

import scala.collection.JavaConverters._

object Injector {

  def getInjectorForField(field: Field, clazz: Class[_]): Injector = {
    val preProcessor = InjectorPreProcessor.getPreProcessorAndTargetType(field)
    clazz.getDeclaredMethods
      .filter(m => CmsReflectionUtils.isSetter(m, field))
      .toList match {
      case (Nil) => FieldInjector(field, preProcessor)
      case (m :: Nil) => SetterInjector(field, m, preProcessor)
      case (multiple) => throw new IllegalArgumentException(s"Expected one setter but found $multiple")
    }
  }

  object SetterInjector {
    def apply(field: Field, method: Method, preProcessor: InjectorPreProcessor = IdentityInjectorPreProcessor): SetterInjector = new SetterInjector(field, method, preProcessor)
  }

  object FieldInjector {
    def apply(field: Field, preProcessor: InjectorPreProcessor = IdentityInjectorPreProcessor): FieldInjector = new FieldInjector(field, preProcessor)
  }
}

sealed trait Injector {
  def field: Field

  def cloneOrReturn(target: Object): Object = {
    if (classOf[CmsPostClonable].isAssignableFrom(targetClass)) {
      target.asInstanceOf[CmsPostClonable].cloneObject()
    } else if (classOf[util.List[_]].isAssignableFrom(targetClass) && classOf[CmsPostClonable].isAssignableFrom(parameterisedType.get)) {
      target.asInstanceOf[util.List[CmsPostClonable]].asScala.foldLeft(new util.ArrayList[CmsPostClonable]())((z, a) => {
        z.add(a.cloneObject())
        z
      })
    } else {
      target
    }

  }

  def inject(target: Object, value: Object): Unit

  def preProcessor: InjectorPreProcessor

  def targetClass: Class[_]

  def parameterisedType: Option[Class[_]]

  def annotations: Option[List[_ >: Annotation]] = CmsReflectionUtils.getCastAnnotations(field)

  override def hashCode(): Int = field.hashCode()

  override def equals(obj: Any): Boolean = obj != null && obj.isInstanceOf[Injector] && field.equals(obj.asInstanceOf[Injector].field)
}

case class SetterInjector(override val field: Field,
                          method: Method,
                          override val preProcessor: InjectorPreProcessor)
  extends Injector {

  override val targetClass: Class[_] = TypeUtils.getType(method.getParameterTypes.head)

  override val parameterisedType: Option[Class[_]] = CmsReflectionUtils.getFirstGenericType(method.getGenericParameterTypes.head)

  override def inject(target: Object, value: Object): Unit = {
    method setAccessible true
    method invoke(target, preProcessor.transform(cloneOrReturn(value)))
  }

  override def equals(obj: Any) = super.equals(obj)
}


case class FieldInjector(override val field: Field,
                         override val preProcessor: InjectorPreProcessor)
  extends Injector {

  override val targetClass: Class[_] = TypeUtils.getType(field.getType)

  override val parameterisedType: Option[Class[_]] = CmsReflectionUtils.getFirstGenericType(field)

  override def inject(target: Object, value: Object): Unit = {
    field setAccessible true
    field set(target, preProcessor.transform(cloneOrReturn(value)))
  }

  override def equals(obj: Any) = super.equals(obj)
}

