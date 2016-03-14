package org.nrg.prefs.entities;

public class PreferenceInfo {
    private String _name;
    private String _defaultValue;
    private Class<?> _valueType;

    public PreferenceInfo() {
        // Need to provide default constructor for serialization.
    }

    public PreferenceInfo(final String name, final String defaultValue) {
        this(name, defaultValue, String.class);
    }

    public PreferenceInfo(final String name, final String defaultValue, final Class<?> valueType) {
        _name = name;
        _defaultValue = defaultValue;
        _valueType = valueType;
    }

    /**
     * The preference name.
     *
     * @return The preference name.
     */
    public String getName() {
        return _name;
    }

    /**
     * Sets the preference name.
     *
     * @param name    The preference name to set.
     */
    public void setName(final String name) {
        _name = name;
    }

    /**
     * The default value for newly created instances of the preference.
     *
     * @return The default value for the newly created instances of the preference.
     */
    public String getDefaultValue() {
        return _defaultValue;
    }

    /**
     * Sets the preference default value. If the {@link #getValueType() type of this preference} is not a string, the
     * value must be serialized to a string.
     *
     * @param defaultValue    The default value to set.
     */
    public void setDefaultValue(final String defaultValue) {
        _defaultValue = defaultValue;
    }

    /**
     * Indicates the type of the preference. If not specified, the default value is {@link String}.
     *
     * @return The class indicating the type of the property value.
     */
    public Class<?> getValueType() {
        return _valueType;
    }

    /**
     * Sets the type of the preference value.
     *
     * @param valueType    The type to set.
     */
    public void setValueType(final Class<?> valueType) {
        _valueType = valueType;
    }

}
