/**
 * $Id: AbstractAttrAdapter.java,v 1.7 2008/04/29 21:05:00 karchie Exp $
 * Copyright (c) 2006-2008 Washington University
 */
package org.nrg.attr;

import java.io.IOException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;


/**
 * Organizes conversion from native attributes (those found in
 * a data file format, such as DICOM or ECAT), with attribute index
 * type S and value type V, to external attributes with type
 * ExtAttrValue.
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 *
 */
public abstract class AbstractAttrAdapter<S,V> {
  private final AttrDefSet<S,V> attrDefs;
  
  public AbstractAttrAdapter(final AttrDefSet<S,V> ad,
      final ReadableAttrDefSet<S,V>...attrs) {
    this.attrDefs = ad;
    add(attrs);
  }
  
  protected abstract Collection<Map<S,V>> getUniqueCombinationsGivenValues(Map<S,V> given, Collection<S> attrs)
  throws IOException,ConversionFailureException;
  
  protected final AttrDefSet<S,V> getDefs() { return attrDefs; }
  
  /**
   * Adds sets of attributes to the adapter
   * @param attrs AttributeSets for conversion
   */
  public final void add(final ReadableAttrDefSet<S,V>...attrs) {
    attrDefs.add(attrs);
  }
  
  /**
   * Adds single attributes to the adapter
   * @param attrs external attributes for conversion
   */
  public final void add(final ExtAttrDef<S,V>...attrs) {
    for (final ExtAttrDef<S,V> a : attrs)
      attrDefs.add(a);
  }
  
  /**
   * Removes the named attributes
   * @param attrNames names of attributes to remove
   * @return number of external attributes removed
   */
  public final int remove(final String...attrNames) {
    int removed = 0;
    for (final String name : attrNames)
      removed += attrDefs.remove(name);
    return removed;
  }
  
  public final int remove(final S...nativeAttrs) {
    int removed = 0;
    for (final S attr : nativeAttrs)
      removed += attrDefs.remove(attr);
    return removed;
  }
  
  /**
   * Get the single value for each external attribute that has been defined
   * for this adapter
   * @return List of external attribute values, in the order they were added
   * @throws NoUniqueValueException if different datasets have different values for
   *   an attribute, or if no value was found for an attribute.
   */
  public final List<ExtAttrValue> getValues()
  throws IOException,NoUniqueValueException,ConversionFailureException {
    return getValuesGiven(new HashMap<S,V>());
  }
  
  public final List<ExtAttrValue> getValuesGiven(final Map<S,V> given)
  throws IOException,NoUniqueValueException,ConversionFailureException {
    return singFromMultiple(getMultipleValuesGiven(given));
  }
  
  private final List<ExtAttrValue> singFromMultiple(final List<Set<ExtAttrValue>> multVals)
  throws NoUniqueValueException {
    final List<ExtAttrValue> singValues = new LinkedList<ExtAttrValue>();
    
    final Iterator<Set<ExtAttrValue>> valsi = multVals.iterator();
    final Iterator<ExtAttrDef<S,V>> eai = attrDefs.iterator();
  
    while (valsi.hasNext()) {
      final Set<ExtAttrValue> vals = valsi.next();
      assert eai.hasNext();
      final ExtAttrDef<S,V> ead = eai.next();
      final Iterator<ExtAttrValue> vali = vals.iterator();
      if (!vali.hasNext()) {
	if (ead.isRequired()) {
	  throw new NoUniqueValueException(ead.getName());	  
	} else {
	  break;	// attribute isn't required, empty value is okay.
	}
      }
      final ExtAttrValue val = vali.next();
      assert val.getName().equals(ead.getName())
      		: "value " + val.getName() + " does not match definition " + ead.getName();
      singValues.add(val);
      if (vali.hasNext())	// even optional attributes shouldn't have multiple values
        throw new NoUniqueValueException(ead.getName(), vals.toArray(new ExtAttrValue[0]));
    }
    return singValues;
  }
  
  public final List<Set<ExtAttrValue>> getMultipleValuesGiven(Map<S,V> given)
  throws IOException,ConversionFailureException {
    final List<Set<ExtAttrValue>> values = new LinkedList<Set<ExtAttrValue>>();
    
    try {
      for (final ExtAttrDef<S,V> ea : attrDefs) {
        final Set<ExtAttrValue> attrVals = new HashSet<ExtAttrValue>();
        values.add(attrVals);
        
        // More than one combination of native values might map to a single value
        // of the external attribute.  In this case, we want to return the single
        // external attribute value once only.
        try {
	  VALUES: for (final Map<S,V> value: getUniqueCombinationsGivenValues(given, ea.getAttrs())) {
	    for (final S attr : ea.getAttrs())
	      if (!value.containsKey(attr) && ea.isRequired(attr))
	        continue VALUES;	// missing required value; move on to next attribute
	    attrVals.add(ea.convert(value));
	  }
	  
	  // Dummy attributes need special handling.
	  if (attrVals.isEmpty() && (ea instanceof ExtAttrDef.Constant))
	    attrVals.add(ea.convert(null));
	} catch (ConversionFailureException e) {
	  if (ea.isRequired()) {
	    throw e;
	  } // else ignore: no big deal if we can't convert an optional attribute
	}
      }
    } catch (ConversionFailureException e) {
      final Set<String> failedAttrs = new HashSet<String>();
      if (e.getExtAttrs() != null)
        failedAttrs.addAll(Arrays.asList(e.getExtAttrs()));
      
      for (final ExtAttrDef<S,V> ea : attrDefs) {
        if (ea.getAttrs().contains(e.getAttr()) && ea.isRequired())
          failedAttrs.add(ea.getName());
      }
      throw new ConversionFailureException(e, failedAttrs.toArray(new String[0]));
    }
    
    return values;
  }
}
