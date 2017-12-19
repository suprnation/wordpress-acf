package com.suprnation.to;

import com.suprnation.cms.annotations.PostType;
import com.suprnation.cms.marker.CmsPostIdentifier;

@PostType("nt")
public class NestedType implements CmsPostIdentifier {

    private SimpleFlatList join;

    @Override
    public Long getWordpressId() {
        return null;
    }
}
