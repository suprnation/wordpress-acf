package com.suprnation.to;

import com.suprnation.cms.annotations.PostType;
import com.suprnation.cms.marker.CmsPostIdentifier;
import lombok.Getter;

@Getter
@PostType("c")
public class InvalidSimpleType implements CmsPostIdentifier {

    float floatPrimitive;

    public InvalidSimpleType(float floatPrimitive){
        this.floatPrimitive = floatPrimitive;
    }

    @Override
    public Long getWordpressId() {
        return null;
    }
}
