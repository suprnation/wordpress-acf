package com.suprnation.cms

import scala.collection.mutable


package object context {

  type MutableCache[K, V] = mutable.Map[K, V]

  object MutableCache {
    def apply[K, V]() = Map.empty[K, V]

    def apply[K, V](value: (K, V)): MutableCache[K, V] = MutableCache[K, V](value)

    def apply[K, V](value: (K, V),
                    value2: (K, V)): MutableCache[K, V] = MutableCache[K, V](value, value2)

    def apply[K, V](value: (K, V),
                    value2: (K, V),
                    value3: (K, V)): MutableCache[K, V] = MutableCache[K, V](value, value2, value3)

    def apply[K, V](value: (K, V),
                    value2: (K, V),
                    value3: (K, V),
                    value4: (K, V)): MutableCache[K, V] = MutableCache[K, V](value, value2, value3, value4)

    def apply[K, V](value: (K, V),
                    value2: (K, V),
                    value3: (K, V),
                    value4: (K, V),
                    value5: (K, V)): MutableCache[K, V] = MutableCache[K, V](value, value2, value3, value4, value5)

    def empty = apply()
  }

  type Cache[K, V] = Map[K, V]

  object Cache {
    def apply[K, V]() = Map.empty[K, V]

    def apply[K, V](value: (K, V)): Cache[K, V] = Cache[K, V](value)

    def apply[K, V](value: (K, V),
                    value2: (K, V)): Cache[K, V] = Cache[K, V](value, value2)

    def apply[K, V](value: (K, V),
                    value2: (K, V),
                    value3: (K, V)): Cache[K, V] = Cache[K, V](value, value2, value3)

    def apply[K, V](value: (K, V),
                    value2: (K, V),
                    value3: (K, V),
                    value4: (K, V)): Cache[K, V] = Cache[K, V](value, value2, value3, value4)

    def apply[K, V](value: (K, V),
                    value2: (K, V),
                    value3: (K, V),
                    value4: (K, V),
                    value5: (K, V)): Cache[K, V] = Cache[K, V](value, value2, value3, value4, value5)

    def empty = apply()
  }


}
