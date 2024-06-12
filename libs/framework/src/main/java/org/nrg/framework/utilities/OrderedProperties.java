package org.nrg.framework.utilities;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Provides a way to aggregate multiple instances of standard Java properties objects in order of precedence. A request
 * for a particular property will return the value set for that property in the instance with the highest precedence
 * that also contains the property. This lets you combine default property values and easily override those with other
 * sets of values that may contain only a few properties.
 */
public class OrderedProperties extends Properties {
    /**
     * Creates the ordered properties set from the submitted properties objects. The properties objects should in order
     * of precedence, with the highest precedence first in the list. You can not later reorder these properties objects
     * to change the values carried by precedence.
     *
     * @param properties The properties objects to be aggregated, highest precedence first.
     */
    public OrderedProperties(final Properties... properties) {
        final List<Properties> instances = Arrays.asList(properties);
        Collections.reverse(instances);
        final Properties aggregated = new Properties();
        for (final Properties instance : instances) {
            aggregated.putAll(instance);
        }
        _entries.put("default", aggregated);
        putAll(aggregated);
    }

    public void addProperties(final String key, final Properties properties) {
        _entries.put(key, properties);
        putAll(properties);
    }

    public Properties getProperties(final String key) {
        return _entries.containsKey(key) ? _entries.get(key) : EMPTY_PROPERTIES;
    }

    public Properties getPropertiesForNamespace(final String prefix) {
        final String prefixPeriod = StringUtils.appendIfMissing(prefix, ".");
        final Properties properties = new Properties();
        for (final String name : stringPropertyNames()) {
            if (name.startsWith(prefixPeriod)) {
                properties.setProperty(StringUtils.removeStart(name, prefixPeriod), getProperty(name));
            }
        }
        return properties;
    }

    private static final Properties EMPTY_PROPERTIES = new Properties();
    private final Map<String, Properties> _entries = Maps.newHashMap();
}
