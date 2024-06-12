/*
 * framework: org.nrg.framework.utilities.PropertiesLookup
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import org.apache.commons.lang3.text.StrLookup;

import java.util.Properties;

/**
 * Provides a lookup implementation that handles {@link OrderedProperties} objects for use with the Commons Text StringSubstitutor class.
 *
 * @deprecated Use {@link OrderedPropertiesLookup} and migrate to using classes from the commons-text library.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class PropertiesLookup extends StrLookup<String> {
    public PropertiesLookup(final Properties properties) {
        _properties = properties;
    }

    @Override
    public String lookup(final String key) {
        return _properties.getProperty(key);
    }

    private final Properties _properties;
}
