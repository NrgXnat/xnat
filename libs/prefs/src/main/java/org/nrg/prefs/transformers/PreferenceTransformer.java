/*
 * prefs: org.nrg.prefs.transformers.PreferenceTransformer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.transformers;

import org.nrg.prefs.entities.PreferenceInfo;

import java.util.List;
import java.util.Map;

/**
 * Defines the interface for a transformer that can converted a serialized instance of an object into an object of the
 * specified generic type.
 *
 * @param <T> The type that this transformer can create from a serialized instance.
 */
public interface PreferenceTransformer<T> {
    /**
     * Returns the value type this transformer handles.
     *
     * @return The value type handled by this transformer.
     */
    Class<?> getValueType();

    /**
     * Indicates whether this transformer can handle instances of the submitted value type.
     *
     * @param valueType The value type to test.
     *
     * @return Returns true if this transformer can convert a serialized instance into the specified value type.
     *
     * @throws CheckItemTypeException When the value type is a container and the type of the items in the container can't be determined.
     */
    boolean handles(final Class<?> valueType) throws CheckItemTypeException;

    /**
     * Indicates whether this transformer can handle instances of the submitted preference.
     *
     * @param info The preference info to test.
     *
     * @return Returns true if this transformer can convert a serialized instance into the specified preference.
     */
    boolean handles(final PreferenceInfo info);

    /**
     * Transforms the serialized instance into an instance of the supported transform value type.
     *
     * @param serialized A serialized instance of the transform value type.
     *
     * @return The instantiated instance.
     */
    T transform(final String serialized);

    /**
     * Transforms the serialized instance into an array of the supported transform value type.
     *
     * @param serialized A serialized instance of an array of the transform value type.
     *
     * @return An array of instantiated instances of the transform value type.
     */
    T[] arrayOf(final String serialized);

    /**
     * Transforms the serialized instance into a list of the supported transform value type.
     *
     * @param serialized A serialized instance of a list of the transform value type.
     *
     * @return A list of instantiated instances of the transform value type.
     */
    List<T> listOf(final String serialized);

    /**
     * Transforms the serialized instance into a map of string keys and values of the supported transform value type.
     * Note that many preference transformers may not support this operation because there may be no way to know how the
     * key value should be derived. These instances will throw the <b>UnsupportedOperationException</b> exception.
     *
     * @param serialized A serialized instance of a map of string keys and values of the supported transform value type.
     *
     * @return A map of string keys and values of the supported transform value type.
     */
    Map<String, T> mapOf(final String serialized) throws UnsupportedOperationException;
}
