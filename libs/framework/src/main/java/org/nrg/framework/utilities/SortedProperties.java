/*
 * framework: org.nrg.framework.utilities.SortedProperties
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class extends the standard Java Properties class to provide sorting based on the
 */
public class SortedProperties extends Properties {

    public SortedProperties() {
        this(true, null);
    }

    public SortedProperties(final boolean sortOnKeys) {
        this(sortOnKeys, null);
    }

    public SortedProperties(final Properties properties) {
        this(true, properties);
    }

    public SortedProperties(final boolean sortOnKeys, final Properties properties) {
        super(properties);
        _sortOnKeys = sortOnKeys;
    }

    public boolean isSortOnKeys() {
        return _sortOnKeys;
    }

    @Override
    public Enumeration<Object> keys() {
        if (!_sortOnKeys) {
            return super.keys();
        }

        final List<Object> properties  = new ArrayList<>(keySet());

        Collections.sort(properties, OBJECT_COMPARATOR);

        return Collections.enumeration(properties);
    }

    @Override
    @Nonnull
    public Collection<Object> values() {
        if (_sortOnKeys) {
            return super.values();
        }

        final List<Object> properties  = new ArrayList<>(super.values());

        Collections.sort(properties, OBJECT_COMPARATOR);

        return properties;
    }

    @Override
    public Set<String> stringPropertyNames() {
        if (!_sortOnKeys) {
            return super.stringPropertyNames();
        }
        final List<String> propertyNames = new ArrayList<>(super.stringPropertyNames());
        Collections.sort(propertyNames, OBJECT_COMPARATOR);
        return new LinkedHashSet<>(propertyNames);
    }

    private static final Comparator<Object> OBJECT_COMPARATOR = new Comparator<Object>() {
        @Override
        public int compare(final Object first, final Object second) {
            return first.toString().compareTo(second.toString());
        }
    };

    private final boolean _sortOnKeys;
}

