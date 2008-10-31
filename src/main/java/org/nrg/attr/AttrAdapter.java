/**
 * Copyright (c) 2008 Washington University
 */
package org.nrg.attr;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 *
 */
public interface AttrAdapter<S,V> {
  /**
   * Adds sets of attributes to the adapter
   * @param attrs AttributeSets for conversion
   */
  public void add(ReadableAttrDefSet<S,V>...attrs);
  
  /**
   * Adds single attributes to the adapter
   * @param attrs external attributes for conversion
   */
  public void add(ExtAttrDef<S,V>...attrs);
  
  /**
   * Removes the named attributes
   * @param attrNames names of attributes to remove
   * @return number of external attributes removed
   */
  public int remove(String...attrNames);
  
  public int remove(S...nativeAttrs);
  
  /**
   * Get the single value for each external attribute that has been defined
   * for this adapter
   * @return List of external attribute values, in the order they were added
   * @throws NoUniqueValueException if different datasets have different values for
   *   an attribute, or if no value was found for an attribute.
   */
  public List<ExtAttrValue> getValues() throws ExtAttrException;
  
  public List<ExtAttrValue> getValuesGiven(final Map<S,V> given) throws ExtAttrException;
  
  public List<Set<ExtAttrValue>> getMultipleValuesGiven(Map<S,V> given) throws ExtAttrException;
}
