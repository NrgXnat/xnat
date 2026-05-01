package org.nrg.automation.services;

import java.util.List;
import java.util.Map;

public class Filtering {
    /**
     * This iterates through the entries in a filter map and tests whether the submitted set of properties contains any
     * of the keys in the filter map and, if so, if the value for that property matches any of the values contained in
     * the filter map's list of matching values.
     *
     * @param properties The properties to be evaluated.
     * @param filterMap  The filter map to use for evaluation.
     *
     * @return Returns true if any of the keys and values in the properties set match the filter map <i>or</i> if the
     *         filter map is null or empty.
     */
    public static boolean propertiesMatchFilterMap(final Map<String, String> properties, final Map<String, List<String>> filterMap) {
        if (filterMap == null || filterMap.isEmpty()) {
            return true;
        }
        for (final Map.Entry<String, List<String>> entry : filterMap.entrySet()) {
            final String key = entry.getKey();
            if (!properties.containsKey(key)) {
                continue;
            }
            final List<String> value = entry.getValue();
            if (value != null && !value.isEmpty() && value.contains(properties.get(key))) {
                return true;
            }
        }
        return false;
    }
}
