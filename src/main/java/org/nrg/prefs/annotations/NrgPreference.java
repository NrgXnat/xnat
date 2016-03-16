package org.nrg.prefs.annotations;

import java.lang.annotation.*;

// TODO: I would like to make this work on fields and methods, but the right way to do it is to use the Spring bean framework and that's too involved at this point.
// @Target({ElementType.FIELD, ElementType.METHOD})
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NrgPreference {
    /**
     * The default value to set for the preference when bootstrapping the preferences settings.
     * @return The default value to set.
     */
    String defaultValue() default "";

    /**
     * Indicates the property on a complex preference object that serves as the key for locating an instance of the
     * preference in lists.
     * @return The key property if specified, nothing otherwise.
     */
    String key() default "";
}
