package com.suprnation.to;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.suprnation.cms.annotations.PostType;
import com.suprnation.cms.marker.CmsPostIdentifier;

import java.util.List;

@PostType("simpleFlatListWithJackson")
public class SimpleFlatListWithJackson implements CmsPostIdentifier {

    @JsonProperty("pIds")
    List<Integer> postIds;

    public SimpleFlatListWithJackson(List<Integer> postIds){
        this.postIds = postIds;
    }

    @Override
    public Long getWordpressId() {
        return null;
    }
}
