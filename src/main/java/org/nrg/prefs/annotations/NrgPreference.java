package org.nrg.prefs.annotations;

import java.lang.annotation.*;

// TODO: I would like to make this work on fields and methods, but the right way to do it is to use the Spring bean framework and that's too involved at this point.
// @Target({ElementType.FIELD, ElementType.METHOD})
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NrgPreference {
    String defaultValue() default "";
}
