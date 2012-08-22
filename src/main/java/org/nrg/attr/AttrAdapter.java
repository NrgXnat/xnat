/**
 * Copyright (c) 2009 Washington University
 */
package org.nrg.attr;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public interface AttrAdapter<S,V> {
    /**
     * Adds sets of attributes to the adapter
     * @param attrs AttributeSets for conversion
     */
    void add(AttrDefs<S>...attrs);

    /**
     * Adds attributes to the adapter
     * @param attrs external attributes for conversion
     */
    void add(ExtAttrDef<S>...attrs);

    /**
     * Adds attributes to the adapter
     * @param attrs external attributes for conversion
     */
    void add(Iterable<? extends ExtAttrDef<S>> attrs);

    /**
     * Removes the named attributes
     * @param attrNames names of attributes to remove
     * @return number of external attributes removed
     */
    int remove(String...attrNames);

    int remove(S...nativeAttrs);

    /**
     * Get the single value for each external attribute that has been defined
     * for this adapter
     * @return List of external attribute values, in the order they were added
     * @throws NoUniqueValueException if different datasets have different values for
     *   an attribute, or if no value was found for an attribute.
     */
    List<ExtAttrValue> getValues(Map<ExtAttrDef<S>,Throwable> failed) throws ExtAttrException;

    List<ExtAttrValue> getValuesGiven(Map<S,V> given, Map<ExtAttrDef<S>,Throwable> failed) throws ExtAttrException;

    /*
    List<Set<ExtAttrValue>> getMultipleValuesGiven(Map<S,V> given, Map<ExtAttrDef<S>,Exception> failed) throws ExtAttrException;
     */
}
