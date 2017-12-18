package com.suprnation.cms

import com.suprnation.cms.cache.GlobalPostCache

import scala.collection.mutable

package object store {
  type GlobalPostCacheStore = Store[String, GlobalPostCache]

  trait Store[K, V] {
    def +=(keyValue: (K, V)): this.type

    def get(key: K): Option[V]

    def getOrElse(key: K, defaultValue: => V): V = get(key).getOrElse(defaultValue)
  }

  class InMemoryStore[K, V]() extends Store[K, V] {
    private val underlyingStore: mutable.Map[K, V] = mutable.Map()

    override def +=(keyValue: (K, V)): this.type = {
      underlyingStore += keyValue
      this
    }

    override def get(key: K): Option[V] = underlyingStore get key
  }

  object InMemoryStore {
    def newStore[K,V] = new InMemoryStore[K,V]
  }

}
