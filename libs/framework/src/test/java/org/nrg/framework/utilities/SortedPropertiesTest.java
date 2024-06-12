/*
 * framework: org.nrg.framework.utilities.SortedPropertiesTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SortedPropertiesTest {
    private static final String[]   KEYS                = new String[] {"aa", "xx", "de", "tt", "dd", "rx"};
    private static final String[]   VALUES              = new String[] {"1", "2", "3", "4", "5", "6"};
    private static final String[]   SORTED_KEYS         = Arrays.copyOf(KEYS, KEYS.length);
    private static final String[]   SORTED_VALUES       = Arrays.copyOf(VALUES, VALUES.length);
    private static final Properties UNSORTED_PROPERTIES = new Properties();
    static {
        Arrays.sort(SORTED_KEYS);
        Arrays.sort(SORTED_VALUES);
        for (int index = 0; index < KEYS.length; index++) {
            UNSORTED_PROPERTIES.put(KEYS[index], VALUES[index]);
        }
    }

    @Test
    public void testBasicSortedProperties() {
        final SortedProperties properties = new SortedProperties();
        properties.putAll(UNSORTED_PROPERTIES);

        assertTrue(properties.isSortOnKeys());
        assertTrue(isSortedByKey(properties));
        assertEquals(UNSORTED_PROPERTIES.size(), properties.size());
    }

    @Test
    public void testValueSortedProperties() {
        final SortedProperties properties = new SortedProperties(false);
        properties.putAll(UNSORTED_PROPERTIES);

        assertFalse(properties.isSortOnKeys());
        assertTrue(isSortedByValue(properties));
        assertEquals(UNSORTED_PROPERTIES.size(), properties.size());
    }

    private boolean isSortedByKey(final SortedProperties properties) {
        String previous = null;
        for (final String current : properties.stringPropertyNames()) {
            if (previous != null) {
                if (current.compareTo(previous) < 0) {
                    return false;
                }
            }
            previous = current;
        }
        return true;
    }

    private boolean isSortedByValue(final SortedProperties properties) {
        String previous = null;
        for (final Object currentObject : properties.values()) {
            assertTrue(currentObject instanceof String);
            final String current = (String) currentObject;
            if (previous != null) {
                if (current.compareTo(previous) < 0) {
                    return false;
                }
            }
            previous = current;
        }
        return true;
    }
}
