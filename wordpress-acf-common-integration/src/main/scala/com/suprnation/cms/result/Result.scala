package com.suprnation.cms.result

object Result {

  import scala.language.implicitConversions

  val MAX_TO_STRING_CONTENT_LENGTH = 50

  /** An Option factory which creates Some(x) if the argument is not null,
    * and None if it is null.
    *
    * @param  x the value
    * @return Some(value) if value != null, None if value == null
    */
  def apply[A](x: A): Result[A] = if (x == null) NotFoundInDb else CachedValue(x)

  /** An Option factory which returns `None` in a manner consistent with
    * the collections hierarchy.
    */
  def searchInDatabase[A]: Result[A] = SearchInDatabase

  /** An Option factory which returns `None` in a manner consistent with
    * the collections hierarchy.
    */
  def notFoundInDatabase[A]: Result[A] = NotFoundInDb
}


sealed abstract class Result[+A] {

  /** Returns true if the option is $none, false otherwise.
    */
  def isEmpty: Boolean

  /** Returns true if the option is an instance of $some, false otherwise.
    */
  def isDefined: Boolean = !isEmpty

  /** Returns the option's value.
    *
    * @note The option must be nonempty.
    * @throws java.util.NoSuchElementException if the option is empty.
    */
  def get: A

  /** Returns the option's value if the option is nonempty, otherwise
    * return the result of evaluating `default`.
    *
    * @param default the default expression.
    */
  @inline final def getOrElse[B >: A](default: => B): B =
    if (isEmpty) default else this.get
}


final case class CachedValue[+A](value: A) extends Result[A] {
  def isEmpty = false

  def get: A = value

  override def toString: String = {
    val stringValue = x.toString
    if (stringValue.length > Result.MAX_TO_STRING_CONTENT_LENGTH) stringValue.substring(0, Result.MAX_TO_STRING_CONTENT_LENGTH) + "..." else stringValue
  }

  def x: A = value
}


case object SearchInDatabase extends Result[Nothing] {
  def isEmpty = true

  def get = throw new NoSuchElementException("None.get")
}

case object NotFoundInDb extends Result[Nothing] {
  def isEmpty = true

  def get = throw new NoSuchElementException("None.get")

  override def toString: String = "empty"
}

