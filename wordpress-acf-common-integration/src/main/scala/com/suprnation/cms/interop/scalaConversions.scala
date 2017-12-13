package com.suprnation.cms

import java.util
import java.util.Optional
import java.util.function._

import scala.collection.JavaConverters._
import scala.compat.java8.FunctionConverters._
import scala.compat.java8.OptionConverters
import scala.compat.java8.StreamConverters._

package object interop {

  implicit def optionalToOption[T](optional: Optional[T]): Option[T] = OptionConverters.toScala(optional)

  implicit def streamToStream[T](stream: java.util.stream.Stream[T]): Stream[T] = stream.toScala[Stream]

  implicit def toScala[T](list: java.util.List[T]): List[T] = list.asScala.toList

  implicit def scalaListToList[T](list: List[T]): java.util.List[T] = list.asJava

  implicit def scalaSetToSet[T](set: Set[T]): java.util.Set[T] = set.asJava

  implicit def mapToMap[K, V](map: util.Map[K, V]): Map[K, V] = map.asScala.toMap

  implicit def voidFunctionToSupplier[T](x: () => T): Supplier[T] = () => x()

  implicit def tToSupplierT[T](t: T): Supplier[T] = () => t

  implicit def functionToFunction[T, U](f: U => T): util.function.Function[U, T] = asJavaFunction(f)

  implicit def functionToPredicate[T](f: (T) => scala.Boolean): util.function.Predicate[T] = asJavaPredicate(f)

  implicit def listToStream[T](list: java.util.List[T]): Stream[T] = this.streamToStream(list.stream())

  implicit def unitFunctionToConsumer[T](x: T => Unit): Consumer[T] = (t: T) => x.apply(t)

  implicit def voidFunctionToConsumer[T](x: T => Void): Consumer[T] = (t: T) => x.apply(t)

  implicit def tToOptionT[T](t: T): Option[T] = Option(t)
}