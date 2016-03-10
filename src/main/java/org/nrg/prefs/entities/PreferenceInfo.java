package org.nrg.prefs.entities;

public class PreferenceInfo {
    private final String _name;
    private final String _defaultValue;
    private final Class<?> _valueType;

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
     * The default value for newly created instances of the preference.
     *
     * @return The default value for the newly created instances of the preference.
     */
    public String getDefaultValue() {
        return _defaultValue;
    }

    /**
     * Indicates the type of the preference. If not specified, the default value is {@link String}.
     *
     * @return The class indicating the type of the property value.
     */
    public Class<?> getValueType() {
        return _valueType;
    }
}
