package com.suprnation.to;

import com.suprnation.cms.annotations.PostType;
import com.suprnation.cms.marker.CmsPostIdentifier;

import java.util.List;

@PostType("s")
public class SimpleFlatList implements CmsPostIdentifier {

    List<Integer> postIds;

    public SimpleFlatList (List<Integer> postIds){
        this.postIds = postIds;
    }

    @Override
    public Long getWordpressId() {
        return null;
    }
}
