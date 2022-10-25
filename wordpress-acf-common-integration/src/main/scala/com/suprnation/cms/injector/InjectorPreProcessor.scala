package com.suprnation.cms.injector

import java.lang.reflect.Field

import com.suprnation.cms.deserialiser.{CmsDeserialiser, DeserialiseUsing}

object InjectorPreProcessor {

  def apply(clazz: Class[_ <: CmsDeserialiser[_]]): InjectorPreProcessor =
    new TypeTransformingInjectorPreProcessor(s => TypeTransformingInjectorPreProcessor(clazz.getDeclaredConstructor().newInstance()).transform(s))

  def getPreProcessorAndTargetType(field: Field): InjectorPreProcessor =
    Option(field.getAnnotation(classOf[DeserialiseUsing]))
      .map(_.value())
      .map(preProcessorClass => InjectorPreProcessor(preProcessorClass))
      .getOrElse(IdentityInjectorPreProcessor)
}

sealed trait InjectorPreProcessor {
  def transformerFunction: Object => Object

  def transform(s: Object): Object
}

object TypeTransformingInjectorPreProcessor{
  def apply(deserializer: CmsDeserialiser[_]): TypeTransformingInjectorPreProcessor =
    new TypeTransformingInjectorPreProcessor(s => deserializer.deserialise(s.asInstanceOf[String]).asInstanceOf[Object])
}

case class TypeTransformingInjectorPreProcessor(override val transformerFunction: Object => Object) extends InjectorPreProcessor {
  override def transform(s: Object): Object = transformerFunction.apply(s)
}

object IdentityInjectorPreProcessor extends InjectorPreProcessor {
  override def transformerFunction: Object => Object = identity

  override def transform(s: Object): Object = s
}
