/*
 * automation: org.nrg.automation.services.ScriptProperty
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum ScriptProperty {
    ScriptId("scriptId"),
    ScriptLabel("scriptLabel"),
    Description("description"),
    Script("script"),
    Language("language", "groovy"),
    LanguageVersion("languageVersion", "2.3.6");
    //ScriptVersion("scriptVersion","1");

    ScriptProperty(final String key) {
        this(key, null);
    }

    ScriptProperty(final String key, final String defaultValue) {
        _key = key;
        _defaultValue = defaultValue;
    }

    public String key() {
        return _key;
    }

    public String defaultValue() {
        return _defaultValue;
    }

    @Override
    public String toString() {
        return key();
    }

    public static ScriptProperty get(final String key) {
        if (_properties.isEmpty()) {
            synchronized (_properties) {
                for (ScriptProperty property : values()) {
                    _properties.put(property.key(), property);
                }
            }
        }
        return _properties.get(key);
    }

    public static Set<String> keys() {
        if (_properties.isEmpty()) {
            get("language");
        }
        return _properties.keySet();
    }

    public static Properties defaults() {
        if (_defaults.isEmpty()) {
            synchronized (_defaults) {
                for (final String key : keys()) {
                    final String value = get(key).defaultValue();
                    if (value != null && !value.trim().equals("")) {
                        _defaults.setProperty(key, value.trim());
                    }
                }
            }
        }
        return _defaults;
    }

    private static final Map<String, ScriptProperty> _properties = new ConcurrentHashMap<>();
    private static final Properties _defaults = new Properties();

    private final String _key;
    private final String _defaultValue;
}