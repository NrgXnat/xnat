/*
 * org.nrg.prefs.annotations.NrgPreference
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NrgPreference {
    /**
     * The name to use for setting the property in the preferences store. By default, NRG preferences stores preferences
     * using the method name converted into property form. For example, the property <b>getFoo()</b> is stored with the
     * property name <b>foo</b>. Specifying the name attribute will use that property name instead. This is mainly provided for compatibility
     * with existing systems that may have properties stored with names that can't be converted to properties easily,
     * e.g. <b>UI.show-left-bar</b>.
     *
     * @return The property name to be used when storing the preference. If no property name is specified, the
     * propertized method name is used instead.
     */
    String property() default "";

    /**
     * The default value to set for the preference when bootstrapping the preferences settings.
     *
     * @return The default value to set.
     */
    String defaultValue() default "";

    /**
     * Indicates the property on a complex preference object that serves as the key for locating an instance of the
     * preference in lists.
     *
     * @return The key property if specified, nothing otherwise.
     */
    String key() default "";
}
