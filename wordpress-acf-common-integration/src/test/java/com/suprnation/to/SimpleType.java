package com.suprnation.to;

import com.suprnation.cms.annotations.PostType;
import com.suprnation.cms.marker.CmsPostIdentifier;
import lombok.Getter;

@Getter
@PostType("a")
public class SimpleType implements CmsPostIdentifier {
    int intPrimitive;
    long longPrimitive;

    String stringReference;
    Integer integerReference;
    Long longReference;

    public SimpleType(int intPrimitive, long longPrimitive, String stringReference, Integer integerReference, Long longReference){
        this.intPrimitive = intPrimitive;
        this.longPrimitive = longPrimitive;
        this.stringReference = stringReference;
        this.integerReference = integerReference;
        this.longReference = longReference;
    }


    @Override
    public Long getWordpressId() {
        return null;
    }
}
