package com.suprnation.cms.service

import com.suprnation.cms.cache.GlobalRelationshipCache
import com.suprnation.cms.types.{PostId, Taxonomy}

trait CmsRelationshipService {

  def getRelationshipsForPostsAndTaxonomies(postIds: Set[PostId], taxonomies: Set[Taxonomy]): GlobalRelationshipCache
}
