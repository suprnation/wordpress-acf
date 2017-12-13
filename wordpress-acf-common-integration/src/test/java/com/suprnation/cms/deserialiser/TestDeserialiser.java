package com.suprnation.cms.deserialiser;

public class TestDeserialiser implements CmsDeserialiser<Object> {

    @Override
    public Object deserialise(String content) {
        return content;
    }
}
