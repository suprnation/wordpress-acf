package com.suprnation.cms.deserialiser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DeserialiseUsing {
    Class<? extends CmsDeserialiser> value();
}