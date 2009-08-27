/**
 * Copyright (c) 2006-2009 Washington University
 */
package org.nrg.attr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.nrg.attr.ExtAttrDef.MultiValue;
import org.nrg.attr.ExtAttrDef.Multiplex;
import org.nrg.attr.ExtAttrDef.Optional;


/**
 * Organizes conversion from native attributes (those found in
 * a data file format, such as DICOM or ECAT), with attribute index
 * type S and value type V, to external attributes with type
 * ExtAttrValue.
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 *
 */
public abstract class AbstractAttrAdapter<S,V> implements AttrAdapter<S,V> {
  private final AttrDefSet<S,V> attrDefs;

  public AbstractAttrAdapter(final AttrDefSet<S,V> ad,
      final ReadableAttrDefSet<S,V>...attrs) {
    this.attrDefs = ad;
    add(attrs);
  }

  protected abstract Collection<Map<S,V>>
  getUniqueCombinationsGivenValues(Map<S,V> given, Collection<S> attrs, Map<S,ConversionFailureException> failed)
  throws ExtAttrException;

  protected final AttrDefSet<S,V> getDefs() { return attrDefs; }

  /*
   * (non-Javadoc)
   * @see org.nrg.attr.AttrAdapter#add(org.nrg.attr.ReadableAttrDefSet<S,V>[])
   */
  public final void add(final ReadableAttrDefSet<S,V>...attrs) {
    attrDefs.add(attrs);
  }

  /*
   * (non-Javadoc)
   * @see org.nrg.attr.AttrAdapter#add(org.nrg.attr.ExtAttrDef<S,V>[])
   */
  public final void add(final ExtAttrDef<S,V>...attrs) {
    for (final ExtAttrDef<S,V> a : attrs)
      attrDefs.add(a);
  }

  /*
   * (non-Javadoc)
   * @see org.nrg.attr.AttrAdapter#remove(java.lang.String[])
   */
  public final int remove(final String...attrNames) {
    int removed = 0;
    for (final String name : attrNames)
      removed += attrDefs.remove(name);
    return removed;
  }

  /*
   * (non-Javadoc)
   * @see org.nrg.attr.AttrAdapter#remove(S[])
   */
  public final int remove(final S...nativeAttrs) {
    int removed = 0;
    for (final S attr : nativeAttrs)
      removed += attrDefs.remove(attr);
    return removed;
  }

  public final List<ExtAttrValue> getValues(final Map<ExtAttrDef<S,V>,Exception> failed)
  throws ExtAttrException {
    return getValuesGiven(new HashMap<S,V>(), failed);
  }

  /**
   * Demultiplexes the values into multiple attributes
   * @param ead Multiplex ExtAttrDef
   * @param given constraints that produce multiple values for the attribute/s
   * @return List of demultiplexed attributes; empty if a failure blocks demultiplexing
   * @throws ExtAttrException
   */
  @SuppressWarnings("unchecked")
  private final List<ExtAttrValue> demultiplex(final ExtAttrDef<S,V> ead, final Map<S,V> given)
      throws ExtAttrException {
    final Map<S,V> demultGiven = new HashMap<S,V>(given);
    final Collection<S> toGet = new LinkedHashSet<S>(ead.getAttrs());
    final Multiplex<S,V> mead = (Multiplex<S,V>)ead;
    toGet.add(mead.getIndexAttribute());
    final Map<S,ConversionFailureException> sfailed = new LinkedHashMap<S,ConversionFailureException>();
    final Collection<Map<S,V>> dmvs = getUniqueCombinationsGivenValues(demultGiven, toGet, sfailed);
    if (sfailed.isEmpty()) {
      final List<ExtAttrValue> values = new ArrayList<ExtAttrValue>();
      for (final Map<S,V> m : dmvs) {
        values.add(mead.demultiplex(m));
      }
      return values;
    } else {
      return Collections.emptyList();
    }
  }
  
  
  /*
   * (non-Javadoc)
   * @see org.nrg.attr.AttrAdapter#getValuesGiven(java.util.Map, java.util.Map)
   */
  @SuppressWarnings("unchecked")
  public final List<ExtAttrValue> getValuesGiven(final Map<S,V> given,
      final Map<ExtAttrDef<S,V>,Exception> failed)
      throws ExtAttrException {
    final List<ExtAttrValue> values = new ArrayList<ExtAttrValue>();
    final Iterator<Set<ExtAttrValue>> valsi = getMultipleValuesGiven(given, failed).iterator();
    final Iterator<? extends ExtAttrDef<S,V>> eai = attrDefs.iterator();

    while (valsi.hasNext()) {
      final Set<ExtAttrValue> vals = valsi.next();
      final ExtAttrDef<S,V> ead = eai.next();
      if (vals.isEmpty()) {
        if (!(ead instanceof Optional)) {
          failed.put(ead, new NoUniqueValueException(ead.getName()));
        }
      } else if (1 == vals.size()) {
        values.add(vals.iterator().next());
      } else {
        if (ead instanceof MultiValue) {
          // Merge the values together into one
          values.add(new BasicExtAttrValue(vals));
        } else if (ead instanceof Multiplex) {
          final List<ExtAttrValue> dmvals = demultiplex(ead, given);
          if (dmvals.isEmpty()) {
            failed.put(ead, new NoUniqueValueException(ead.getName(), vals));
          } else {
            values.addAll(dmvals);
          }
        } else {
          failed.put(ead, new NoUniqueValueException(ead.getName(), vals));
        }
      }
    }

    return values;
    //    return singFromMultiple(getMultipleValuesGiven(given));
  }

  
  /**
   * Implements {@link AttrAdapter#getMultipleValuesGiven(Map, Map)}
   * @param given
   * @param defs
   * @param failures
   * @return
   * @throws ExtAttrException
   */
  private final List<Set<ExtAttrValue>>
  getMultipleValuesGiven(final Map<S,V> given, final Iterable<ExtAttrDef<S,V>> defs,
      final Map<ExtAttrDef<S,V>,Exception> failures)
      throws ExtAttrException {
    final List<Set<ExtAttrValue>> values = new ArrayList<Set<ExtAttrValue>>();
    final Map<S,ConversionFailureException> failedS = new HashMap<S,ConversionFailureException>();

    for (final ExtAttrDef<S,V> ea : defs) {
      final Set<ExtAttrValue> attrVals = new HashSet<ExtAttrValue>();
      values.add(attrVals);

      // More than one combination of native values might map to a single value
      // of the external attribute.  In this case, we want to return the single
      // external attribute value once only.
      VALUES: for (final Map<S,V> value: getUniqueCombinationsGivenValues(given, ea.getAttrs(), failedS)) {
        for (final S attr : ea.getAttrs())
          if (!value.containsKey(attr) && !(ea instanceof Optional)) {
            continue VALUES;    // missing required value; move on to next attribute
          }
        try {
          attrVals.add(ea.convert(value));
        } catch (ConversionFailureException e) {
          failures.put(ea, e);
        }
      }

      // Dummy attributes need special handling.
      if (attrVals.isEmpty() && (ea instanceof ExtAttrDef.Constant<?,?>)) {
        try {
          attrVals.add(ea.convert(null));
        } catch (ConversionFailureException e) {
          throw new RuntimeException(e);    // can't happen
        }
      }
    }

    return values;
  }

  /*
   * (non-Javadoc)
   * @see org.nrg.attr.AttrAdapter#getMultipleValuesGiven(java.util.Map, java.util.Map)
   */
  public final List<Set<ExtAttrValue>>
  getMultipleValuesGiven(final Map<S,V> given, final Map<ExtAttrDef<S,V>,Exception> failures)
  throws ExtAttrException {
    return getMultipleValuesGiven(given, attrDefs, failures);
  }
}
