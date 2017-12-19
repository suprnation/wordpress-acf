package com.suprnation.to;

import com.suprnation.cms.annotations.PostType;
import com.suprnation.cms.marker.CmsPostIdentifier;

import java.util.List;

@PostType("nlt")
public class NestedListType implements CmsPostIdentifier {

    private List<SimpleFlatList> list;

    @Override
    public Long getWordpressId() {
        return null;
    }
}
