package org.nrg.prefs.annotations;

/**
 * Sets a default preference name and default value.
 */
public @interface NrgPrefValue {
    /**
     * The preference name.
     *
     * @return The preference name.
     */
    String name();

    /**
     * The default value for newly created instances of the preference.
     *
     * @return The default value for the newly created instances of the preference.
     */
    String defaultValue() default "";

    /**
     * Indicates the type of the preference. If not specified, the default value is {@link String}.
     *
     * @return The class indicating the type of the property value.
     */
    Class<?> valueType() default String.class;
}
