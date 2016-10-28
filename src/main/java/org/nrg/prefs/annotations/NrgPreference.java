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
     * property name <b>foo</b>. Specifying the <b>property</b> attribute will use that property name instead.
     *
     * <b>Note:</b> This is mainly provided for compatibility with existing systems that may have properties stored with
     * names that can't be converted to properties easily, such as <b>UI.show-left-bar</b>. You can also set those
     * original names as values for the {@link #aliases()} attribute, in which case the alias names will be converted to
     * the standard preference property name.
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

    /**
     * Indicates alternative property names under which the preference might be stored. If a value isn't found for the
     * preference under the standard name,
     *
     * @return The key property if specified, nothing otherwise.
     */
    String[] aliases() default {};
}
