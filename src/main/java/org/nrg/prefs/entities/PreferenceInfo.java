package org.nrg.prefs.entities;

import org.apache.commons.lang3.StringUtils;
import org.nrg.prefs.annotations.NrgPreference;

public class PreferenceInfo {
    private String   _name;
    private String   _property;
    private String   _defaultValue;
    private Class<?> _valueType;
    private Class<?> _itemType;
    private String   _key;

    public PreferenceInfo() {
        this(null, null, null, null, null);
    }

    public PreferenceInfo(final String name, final String defaultValue) {
        this(name, null, defaultValue, null, String.class);
    }

    public PreferenceInfo(final String name, final String property, final String defaultValue, final String key, final Class<?> valueType) {
        setName(name);
        setProperty(property);
        setDefaultValue(defaultValue);
        setKey(key);
        setValueType(valueType);
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
     * Gets the property name to use for storing the preference in the service store. If the property name hasn't been
     * set by calling {@link #setProperty(String)}, the {@link #getName() preference name} is used. See {@link
     * NrgPreference#property()} for more information on the purpose of this setting.
     *
     * @return The property name to use for storing the preference.
     */
    public String getProperty() {
        return StringUtils.defaultIfBlank(_property, _name);
    }

    /**
     * Sets the property name to use for storing the preference in the service store. If the property name isn't set,
     * the {@link #getName() preference name} is used.
     *
     * @param property    The property name to use for storing the preference.
     */
    public void setProperty(final String property) {
        _property = property;
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

    /**
     * When the {@link #getValueType() preference value type} is a list, this indicates the type of the item stored in
     * the list. When the {@link #getValueType() preference value type} is a map, this indicates the type of the value
     * stored in the list (the key is always presumed to be a string).
     * @return The type of item stored in a list or map value.
     */
    public Class<?> getItemType() {
        return _itemType;
    }

    /**
     * When the {@link #getValueType() preference value type} is a list, this indicates the type of the item stored in
     * the list. When the {@link #getValueType() preference value type} is a map, this indicates the type of the value
     * stored in the list (the key is always presumed to be a string).
     * @param itemType    The type of item stored in a list or map value.
     */
    public void setItemType(final Class<?> itemType) {
        _itemType = itemType;
    }

    /**
     * When the {@link #getValueType() preference value type} is a map, this indicates the property of the item stored
     * in the list to be used as a key.
     * @return The key to use for items in the map.
     */
    public String getKey() {
        return _key;
    }

    /**
     * When the {@link #getValueType() preference value type} is a map, this indicates the property of the item stored
     * in the list to be used as a key.
     * @param key    The key to use for items in the map.
     */
    public void setKey(final String key) {
        _key = key;
    }

    @Override
    public String toString() {
        return _name + (StringUtils.isNotBlank(_property) ? " (stored as " + _property + ")" : "");
    }
}
