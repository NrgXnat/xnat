/*
 * framework: org.nrg.framework.utilities.SortedPropertiesTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SortedPropertiesTest {
    private static final String[]   KEYS                = new String[]{"aa", "xx", "de", "tt", "dd", "rx"};
    private static final String[]   VALUES              = new String[]{"1", "2", "3", "4", "5", "6"};
    private static final Properties UNSORTED_PROPERTIES = new Properties();

    static {
        for (int index = 0; index < KEYS.length; index++) {
            UNSORTED_PROPERTIES.put(KEYS[index], VALUES[index]);
        }
    }

    @Test
    public void testBasicSortedProperties() {
        final SortedProperties properties = new SortedProperties();
        properties.putAll(UNSORTED_PROPERTIES);
        assertThat(properties).isNotEmpty()
                              .hasFieldOrPropertyWithValue("sortOnKeys", true)
                              .hasSize(UNSORTED_PROPERTIES.size());
        assertThat(isSortedByKey(properties)).isTrue();
    }

    @Test
    public void testValueSortedProperties() {
        final SortedProperties properties = new SortedProperties(false);
        properties.putAll(UNSORTED_PROPERTIES);

        assertThat(properties).isNotEmpty()
                              .hasFieldOrPropertyWithValue("sortOnKeys", false)
                              .hasSize(UNSORTED_PROPERTIES.size());
        assertThat(isSortedByKey(properties)).isFalse();
        assertThat(isSortedByValue(properties)).isTrue();
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
            assertThat(currentObject).isExactlyInstanceOf(String.class);
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
