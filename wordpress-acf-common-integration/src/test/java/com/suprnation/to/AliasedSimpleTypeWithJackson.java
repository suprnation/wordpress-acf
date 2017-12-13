package com.suprnation.to;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.suprnation.cms.annotations.Alias;
import com.suprnation.cms.annotations.PostType;
import com.suprnation.cms.marker.CmsPostIdentifier;
import lombok.Getter;

@Getter
@PostType("c")
public class AliasedSimpleTypeWithJackson implements CmsPostIdentifier {
    @Alias("lr")
    @JsonProperty("lr-jackson")
    Long longReference;

    public AliasedSimpleTypeWithJackson(Long longReference) {
        this.longReference = longReference;
    }

    @Override
    public Long getWordpressId() {
        return null;
    }
}
