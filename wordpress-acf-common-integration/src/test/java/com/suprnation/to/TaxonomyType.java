package com.suprnation.to;

import com.suprnation.cms.annotations.PostType;
import com.suprnation.cms.annotations.Taxonomy;
import com.suprnation.cms.marker.CmsPostIdentifier;

import java.util.List;

@PostType("tt")
public class TaxonomyType implements CmsPostIdentifier {

    private List<Tax> terms;

    @Taxonomy("taxonomy")
    public enum Tax {
    }

    @Override
    public Long getWordpressId() {
        return null;
    }

}
