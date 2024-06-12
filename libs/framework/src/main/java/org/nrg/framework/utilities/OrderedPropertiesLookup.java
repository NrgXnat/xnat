package org.nrg.framework.utilities;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.text.lookup.StringLookup;

/**
 * Provides a lookup implementation that handles {@link OrderedProperties} objects for use with the Commons Text StringSubstitutor class.
 *
 * Replaces the {@link PropertiesLookup} class.
 */
@SuppressWarnings("deprecation")
@Getter
@Accessors(prefix = "_")
public class OrderedPropertiesLookup implements StringLookup {
    public OrderedPropertiesLookup(final OrderedProperties orderedProperties) {
        _orderedProperties = orderedProperties;
    }

    @Override
    public String lookup(final String key) {
        return _orderedProperties.getProperty(key);
    }

    private final OrderedProperties _orderedProperties;
}
