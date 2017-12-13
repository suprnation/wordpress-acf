package com.suprnation.to;

import com.suprnation.cms.annotations.Alias;
import com.suprnation.cms.annotations.PostType;
import com.suprnation.cms.marker.CmsPostIdentifier;
import lombok.Getter;

@Getter
@PostType("b")
public class AliasedSimpleType implements CmsPostIdentifier {

    @Alias("ip")
    int intPrimitive;
    @Alias("lp")
    long longPrimitive;

    @Alias("sr")
    String stringReference;
    @Alias("ir")
    Integer integerReference;
    @Alias("lr")
    Long longReference;

    public AliasedSimpleType(int intPrimitive, long longPrimitive, String stringReference, Integer integerReference, Long longReference){
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
