/**
 * Copyright (c) 2007,2009,2010 Washington University
 */
package org.nrg.attr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mock native attribute type used for unit tests
 * @author Kevin A. Archie <karchie@wustl.edu>
 */
@SuppressWarnings("unchecked")
public class NativeAttr implements Comparable<NativeAttr> {
  private static final Map<NativeAttr,Map<NativeAttr,Integer>> comparisons =
    new HashMap<NativeAttr,Map<NativeAttr,Integer>>();

  private final String name;

  private NativeAttr(final String name) {
    this.name = name;
  }

  @Override
  public final String toString() {
    return name;
  }

  public int compareTo(NativeAttr other) {
    return comparisons.get(this).get(other);
  }
  
  @Override
  public boolean equals(final Object o) {
    return o instanceof NativeAttr && 0 == comparisons.get(this).get(o);
  }

  public static final NativeAttr A = new NativeAttr("NativeAttr A");
  public static final NativeAttr B = new NativeAttr("NativeAttr B");
  public static final NativeAttr C = new NativeAttr("NativeAttr C");
  public static final NativeAttr D = new NativeAttr("NativeAttr D");

  public interface RWAttrDefSet<S,V> extends AttrDefs<S,V> {
    public RWAttrDefSet<S,V> addExtAttrDef(ExtAttrDef<S,V> ead);
  }

  public static final class SampleAttrDefSet<S,V> implements AttrDefs<S,V> {
    private Set<S> nas = new HashSet<S>();
    private Map<String,ExtAttrDef<S,V>> defs =
      new LinkedHashMap<String,ExtAttrDef<S,V>>();

    public SampleAttrDefSet(ExtAttrDef<S,V>...eads) {
      for (final ExtAttrDef<S,V> ead : eads)
	addExtAttrDef(ead);
    }
    
    public Set<S> getNativeAttrs() {
      return new HashSet<S>(nas);
    }

    public ExtAttrDef<S,V> getExtAttrDef(final String name) {
      return defs.get(name);
    }

    public Iterator<ExtAttrDef<S,V>> iterator() {
      return new LinkedHashSet<ExtAttrDef<S,V>>(defs.values()).iterator();
    }
    
    public SampleAttrDefSet<S,V> addExtAttrDef(final ExtAttrDef<S,V> ead) {
      defs.put(ead.getName(), ead);
      nas.addAll(ead.getAttrs());
      return this;
    }
  }

  public static final ExtAttrDef<NativeAttr,Float> fextA = new ExtAttrDef.Text<NativeAttr,Float>("ext-A", NativeAttr.A);
  public static final ExtAttrDef<NativeAttr,Float> fextC_BA =
    new ExtAttrDef.TextWithAttributes<NativeAttr,Float>("ext-C", NativeAttr.C,
	new String[]{"B", "A"}, new NativeAttr[]{NativeAttr.B, NativeAttr.A});
  
  public static final SampleAttrDefSet<NativeAttr,Float> frads = new SampleAttrDefSet<NativeAttr,Float>(fextA, fextC_BA);

  public static final SampleAttrDefSet<NativeAttr,Float> emptyFrads = new SampleAttrDefSet<NativeAttr,Float>();
  
  static {
    final Map<NativeAttr,Integer> compareA = new HashMap<NativeAttr,Integer>();
    compareA.put(A, 0);
    compareA.put(B, -1);
    compareA.put(C, -2);
    compareA.put(D, -3);
    comparisons.put(A, compareA);
    final Map<NativeAttr,Integer> compareB = new HashMap<NativeAttr,Integer>();
    compareB.put(A, 1);
    compareB.put(B, 0);
    compareB.put(C, -1);
    compareB.put(D, -2);
    comparisons.put(B, compareB);
    final Map<NativeAttr,Integer> compareC = new HashMap<NativeAttr,Integer>();
    compareC.put(A, 2);
    compareC.put(B, 1);
    compareC.put(C, 0);
    compareC.put(D, 1);
    comparisons.put(C, compareC);
    final Map<NativeAttr,Integer> compareD = new HashMap<NativeAttr,Integer>();
    compareD.put(A, 3);
    compareD.put(B, 2);
    compareD.put(C, 1);
    compareD.put(D, 0);
    comparisons.put(D, compareD);
  }
}