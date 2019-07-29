package org.nrg.framework.utilities;

import com.google.common.base.Predicate;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtils {
    private PropertiesUtils() {
        // no op
    }

    /**
     * Combines all of the specified properties objects, starting with the first object. This means that
     * properties that have the same key in different objects will end up with the value specified in
     * the last occurrence.
     *
     * @param properties The properties objects to be combined.
     *
     * @return A single properties object containing all properties from all of the objects.
     */
    public static Properties combine(final Properties... properties) {
        final Properties aggregated = new Properties();
        for (final Properties single : properties) {
            if (single != null && !single.isEmpty()) {
                aggregated.putAll(single);
            }
        }
        return aggregated;
    }

    /**
     * Creates a properties object from the array of strings, with each pair of strings in the array
     * used as a key-value pair. The total number of strings must be even.
     *
     * @param strings Strings to be used as key-value pairs.
     *
     * @return A properties object containing the submitted values.
     */
    public static Properties of(final String... strings) {
        if (strings.length % 2 == 1) {
            throw new RuntimeException("You must specify properties in pairs, first property name then value. You specified " + strings.length + " values: " + StringUtils.join(strings, ", "));
        }
        final Properties properties = new Properties();
        for (int index = 0; index < strings.length / 2; index++) {
            properties.setProperty(strings[index], strings[index + 1]);
        }
        return properties;
    }

    public static Map<String, Object> of(final Object... items) {
        if (items.length % 2 == 1) {
            throw new RuntimeException("You must specify properties in pairs, first property name then value. You specified " + items.length + " values: " + StringUtils.join(items, ", "));
        }
        final Map<String, Object> map = new HashMap<>();
        for (int index = 0; index < items.length / 2; index++) {
            map.put(items[index].toString(), items[index + 1]);
        }
        return map;
    }

    public static <T extends Properties> Predicate<T> empty() {
        return new Predicate<T>() {
            @Override
            public boolean apply(@Nullable final T map) {
                return map == null || map.isEmpty();
            }
        };
    }

    @SuppressWarnings("unused")
    public static <T extends Properties> Predicate<T> notEmpty() {
        return new Predicate<T>() {
            @Override
            public boolean apply(@Nullable final T map) {
                return map != null && !map.isEmpty();
            }
        };
    }
}
