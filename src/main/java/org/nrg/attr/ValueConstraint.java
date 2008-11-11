/**
 * Copyright (c) 2008 Washington University
 */
package org.nrg.attr;

import java.util.Map;

/**
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 *
 */
public class ValueConstraint<S,V> implements Map.Entry<S,V>{
  private final S s;
  private final V v;
  
  public ValueConstraint(final S attribute, final V value) {
    this.s = attribute;
    this.v = value;
  }
  
  public S getKey() { return s; }
  
  public V getValue() { return v; }
  
  public V setValue(V v) { throw new UnsupportedOperationException(); }
  
  public final boolean equals(final Object o) {
    if (!(o instanceof ValueConstraint)) { return false; }
    final ValueConstraint<?,?> vc = (ValueConstraint<?,?>)o;
    return s.equals(vc.s) && (v == vc.v || (null != v && v.equals(vc.v)));
  }
  
  public final int hashCode() {
    int result = 17;
    if (null != s) { result = 37 * result + s.hashCode(); }
    if (null != v) { result = 37 * result + v.hashCode(); }
    return result;
  }
  
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(s);
    sb.append("->");
    sb.append(v);
    return sb.toString();
  }
}
