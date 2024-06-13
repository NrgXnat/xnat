/*
 * prefs: org.nrg.prefs.annotations.ValueDuple
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.annotations;

/**
 * The value duple provides a way to transport a value in a standard serialized format&mdash;that is, a string&mdash;along
 * with its representative type. The duple is just a container and has no clue how to convert the string value into an
 * object of the indicated type. This is something that the system using the duple has to figure out.
 */
@SuppressWarnings("unused")
public class ValueDuple {
    /**
     * Creates a new duple with the submitted value and the default value type of string.
     *
     * @param value    The duple's value.
     */
    public ValueDuple(final String value) {
        _value = value;
        _type = String.class;
    }

    /**
     * Creates a new duple with the submitted value and type.
     *
     * @param value    The duple's value.
     * @param type     The duple's type.
     */
    public ValueDuple(final String value, final Class<?> type) {
        _value = value;
        _type = type;
    }

    /**
     * Gets the serialized value.
     *
     * @return The serialized value.
     */
    public String getValue() {
        return _value;
    }

    /**
     * Gets the target object type for the serialized value.
     *
     * @return The target object type.
     */
    public Class<?> getType() {
        return _type;
    }

    private final String _value;
    private final Class<?> _type;
}
