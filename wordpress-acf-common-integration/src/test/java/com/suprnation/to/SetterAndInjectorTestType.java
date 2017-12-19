package com.suprnation.to;

import com.suprnation.cms.annotations.PostType;
import com.suprnation.cms.deserialiser.DeserialiseUsing;
import com.suprnation.cms.deserialiser.TestDeserialiser;
import com.suprnation.cms.marker.CmsPostIdentifier;

@PostType("setterAndInjectorTestType")
public class SetterAndInjectorTestType implements CmsPostIdentifier {

    @DeserialiseUsing(TestDeserialiser.class)
    private int setterAndPreprocesser;

    private void setSetterAndPreprocesser(Integer setterAndPreprocesser) {
        this.setterAndPreprocesser = setterAndPreprocesser;
    }

    @Override
    public Long getWordpressId() {
        return null;
    }
}
