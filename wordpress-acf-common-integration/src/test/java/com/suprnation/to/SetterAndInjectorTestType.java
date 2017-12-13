package com.suprnation.to;

import com.suprnation.cms.annotations.PostType;
import com.suprnation.cms.deserialiser.DeserialiseUsing;
import com.suprnation.cms.deserialiser.TestDeserialiser;

@PostType("setterAndInjectorTestType")
public class SetterAndInjectorTestType {

    @DeserialiseUsing(TestDeserialiser.class)
    private int setterAndPreprocesser;

    private void setSetterAndPreprocesser(int setterAndPreprocesser) {
        this.setterAndPreprocesser = setterAndPreprocesser;
    }
}
