package com.suprnation.cms

import com.suprnation.cms.cache.GlobalPostCache
import com.suprnation.cms.context.MutableCache

package object store {
  type GlobalPostCacheStore = MutableCache[String, GlobalPostCache]
}
