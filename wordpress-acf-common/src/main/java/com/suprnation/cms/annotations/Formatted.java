package com.suprnation.cms.annotations;


import com.suprnation.cms.enums.Formatting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Cast
public @interface Formatted {

    Formatting formatting() default Formatting.NoFormatting;

}
