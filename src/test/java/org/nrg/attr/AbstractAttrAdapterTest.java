/**
 * Copyright (c) 2007,2009 Washington University
 */
package org.nrg.attr;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 *
 */
public class AbstractAttrAdapterTest {
  public static final class AttrAdapter<S,V> extends AbstractAttrAdapter<S,V> {
    final Map<String,Map<S,V>> vals = new HashMap<String,Map<S,V>>();

    public AttrAdapter(final AttrDefSet<S,V> ad, final ReadableAttrDefSet<S,V>...attrs) {
      super(ad, attrs);
    }

    protected Collection<Map<S,V>> getUniqueCombinationsGivenValues(final Map<S,V> given,
        final Collection<S> attrs, final Map<S,ConversionFailureException> failures)
        throws ExtAttrException {
      final Set<Map<S,V>> matching = new HashSet<Map<S,V>>();
      FILES: for (final Map<S,V> data : vals.values()) {
        for (Map.Entry<S,V> e : given.entrySet()) {
          if (!e.getValue().equals(data.get(e.getKey()))) {
            continue FILES;
          }
        }
        matching.add(data);
      }

      final Set<Map<S,V>> combs = new HashSet<Map<S,V>>();
      for (final Map<S,V> match : matching) {
        final Map<S,V> vals = new HashMap<S,V>();
        for (final S s : attrs) {
          if (match.containsKey(s)) {
            vals.put(s, match.get(s));
          }
        }
        if (!vals.isEmpty()) {
          combs.add(vals);
        }
      }

      return combs;
    }

    public AttrAdapter<S,V> put(final String file, final S s, final V v) {
      if (!vals.containsKey(file)) {
        vals.put(file, new HashMap<S,V>());
      }
      if (vals.get(file).containsKey(s)) {
        throw new IllegalArgumentException("data " + file + " already contains value for " + s);
      }
      vals.get(file).put(s,v);
      return this;
    }
  }
  
  private final static class BasicMultiplexedAttrDef<S,V>
  extends ExtAttrDef.Text<S,V>
  implements ExtAttrDef.Multiplex {
    private final String root;
    private int index = 0;
    
    BasicMultiplexedAttrDef(final S attr, final String name, final String multiRoot) {
      super(name, attr);
      root = multiRoot;
    }
    
    public Collection<ExtAttrValue> extract(final Iterable<ExtAttrValue> vals) {
      final ArrayList<ExtAttrValue> out = new ArrayList<ExtAttrValue>();
      for (final ExtAttrValue val : vals) {
        out.add(new BasicExtAttrValue(root + (++index), val.getText()));
      }
      return out;
    }
  }

  /**
   * Test method for {@link org.nrg.attr.AbstractAttrAdapter#AbstractAttrAdapter(org.nrg.attr.AttrDefSet, org.nrg.attr.ReadableAttrDefSet<S,V>[])}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public final void testAbstractAttrAdapter() {
    final AttrAdapter<NativeAttr,Float> aa = new AttrAdapter<NativeAttr,Float>(new AttrDefSet<NativeAttr,Float>(), NativeAttr.frads);
    assertEquals(NativeAttr.frads.getNativeAttrs(), aa.getDefs().getNativeAttrs());
  }

  /**
   * Test method for {@link org.nrg.attr.AbstractAttrAdapter#getDefs()}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public final void testGetDefs() {
    final AttrAdapter<NativeAttr,Float> aa = new AttrAdapter<NativeAttr,Float>(new AttrDefSet<NativeAttr,Float>(), NativeAttr.frads);
    assertEquals(NativeAttr.frads.getNativeAttrs(), aa.getDefs().getNativeAttrs());
  }

  /**
   * Test method for {@link org.nrg.attr.AbstractAttrAdapter#add(org.nrg.attr.ReadableAttrDefSet<S,V>[])}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public final void testAddReadableAttrDefSetOfSVArray() {
    final AttrAdapter<NativeAttr,Float> aa = new AttrAdapter<NativeAttr,Float>(new AttrDefSet<NativeAttr,Float>(), NativeAttr.emptyFrads);
    aa.add(NativeAttr.frads);
    assertEquals(NativeAttr.frads.getNativeAttrs(), aa.getDefs().getNativeAttrs());
  }

  /**
   * Test method for {@link org.nrg.attr.AbstractAttrAdapter#add(org.nrg.attr.ExtAttrDef<S,V>[])}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public final void testAddExtAttrDefOfSVArray() {
    final AttrAdapter<NativeAttr,Float> aa = new AttrAdapter<NativeAttr,Float>(new AttrDefSet<NativeAttr,Float>(), NativeAttr.emptyFrads);
    aa.add(NativeAttr.fextA);
    aa.add(NativeAttr.fextC_BA);
    assertEquals(NativeAttr.frads.getNativeAttrs(), aa.getDefs().getNativeAttrs());
  }

  /**
   * Test method for {@link org.nrg.attr.AbstractAttrAdapter#remove(java.lang.String[])}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public final void testRemoveStringArray() {
    final AttrAdapter<NativeAttr,Float> aa = new AttrAdapter<NativeAttr,Float>(new AttrDefSet<NativeAttr,Float>(), NativeAttr.frads);
    assertNotNull(aa.getDefs());
    assertEquals(NativeAttr.fextA, aa.getDefs().getExtAttrDef("ext-A"));
    aa.remove(new String[]{"ext-A"});
    assertNull(aa.getDefs().getExtAttrDef("ext-A"));
    assertEquals(NativeAttr.fextC_BA, aa.getDefs().getExtAttrDef("ext-C"));
  }

  /**
   * Test method for {@link org.nrg.attr.AbstractAttrAdapter#remove(S[])}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public final void testRemoveSArray() {
    final AttrAdapter<NativeAttr,Float> aa = new AttrAdapter<NativeAttr,Float>(new AttrDefSet<NativeAttr,Float>(), NativeAttr.frads);
    assertNotNull(aa.getDefs());
    assertEquals(NativeAttr.fextC_BA, aa.getDefs().getExtAttrDef("ext-C"));
    aa.remove(new NativeAttr[]{NativeAttr.C});
    assertNull(aa.getDefs().getExtAttrDef("ext-C"));
    assertEquals(NativeAttr.fextA, aa.getDefs().getExtAttrDef("ext-A"));
    aa.remove(new NativeAttr[]{NativeAttr.A});
    assertNull(aa.getDefs().getExtAttrDef("ext-A"));
    assertTrue(aa.getDefs().getNativeAttrs().isEmpty());

    aa.add(NativeAttr.frads);
    assertEquals(NativeAttr.fextA, aa.getDefs().getExtAttrDef("ext-A"));
    assertEquals(NativeAttr.fextC_BA, aa.getDefs().getExtAttrDef("ext-C"));
    aa.remove(new NativeAttr[]{NativeAttr.A});	// both ExtAttrDefs use A, so both will be removed.
    assertTrue(aa.getDefs().getNativeAttrs().isEmpty());
  }

  /**
   * Test method for {@link org.nrg.attr.AbstractAttrAdapter#getValues()}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public final void testGetValues() {
    final String f1 = "file1";
    final String f2 = "file2";

    final AttrAdapter<NativeAttr,Float> aa = new AttrAdapter<NativeAttr,Float>(new AttrDefSet<NativeAttr,Float>(), NativeAttr.frads);
    aa.put(f1, NativeAttr.A, 0.0f);
    aa.put(f2, NativeAttr.A, 0.0f);
    aa.put(f1, NativeAttr.B, 1.0f);
    aa.put(f2, NativeAttr.B, 1.0f);
    aa.put(f1, NativeAttr.C, 2.0f);
    aa.put(f2, NativeAttr.C, 2.0f);

    final Map<ExtAttrDef<NativeAttr,Float>,Exception> failures =
      new HashMap<ExtAttrDef<NativeAttr,Float>,Exception>();
    final Collection<ExtAttrValue> vals;
    try {
      vals = aa.getValues(failures);
      assertTrue(failures.isEmpty());
    } catch (Exception e) {
      fail(e.getMessage());
      return;
    }
    final ExtAttrValue a0 = new BasicExtAttrValue("ext-A", Float.toString(0.0f));
    final ExtAttrValue c2_b1a0 = new BasicExtAttrValue("ext-C", Float.toString(2.0f),
        Utils.put(new LinkedHashMap<String,String>(), new String[]{"B", "A"},
            new String[]{Float.toString(1.0f), Float.toString(0.0f)}));

    final Iterator<ExtAttrValue> i = vals.iterator();
    assertEquals(a0, i.next());
    assertEquals(c2_b1a0, i.next());
    assertFalse(i.hasNext());

    final String f3 = "file3";
    aa.put(f3, NativeAttr.A, 0.0f);
    aa.put(f3, NativeAttr.B, 1.0f);
    aa.put(f3, NativeAttr.C, 2.1f);

    try {
      aa.getValues(failures);
      assertEquals(1, failures.size());
      final Iterator<Map.Entry<ExtAttrDef<NativeAttr,Float>,Exception>> mei = failures.entrySet().iterator();
      final ExtAttrDef<NativeAttr,Float> failed = mei.next().getKey();
      assertEquals("ext-C", failed.getName());
      assertFalse(mei.hasNext());
    } catch (NoUniqueValueException e) {
      // expected: 2 values for attribute ext-C
      assertEquals("ext-C", e.getAttribute());
      assertEquals(2, e.getValues().length);
    } catch (Exception e) {
      fail(e.getMessage());
    }
 
    final AttrAdapter<NativeAttr,Float> aam = new AttrAdapter<NativeAttr,Float>(new AttrDefSet<NativeAttr,Float>());
    aam.put(f1, NativeAttr.A, 0.0f);
    aam.put(f2, NativeAttr.A, 0.0f);
    aam.put(f1, NativeAttr.B, 1.0f);
    aam.put(f2, NativeAttr.B, 1.5f);
    aam.put(f1, NativeAttr.C, 2.0f);
    aam.put(f2, NativeAttr.C, 2.0f);

    final ExtAttrDef<NativeAttr,Float> extB = new BasicMultiplexedAttrDef(NativeAttr.B, "extB_mp", "MultiB-");
    aam.add(extB);
    
    failures.clear();
    try {
      final List<ExtAttrValue> mvals = aam.getValues(failures);
      assertEquals(2, mvals.size());
      for (final ExtAttrValue val : mvals) {
        assertTrue(val.getName().startsWith("MultiB"));
      }
    } catch (ExtAttrException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link org.nrg.attr.AbstractAttrAdapter#getValuesGiven(java.util.Map)}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public final void testGetValuesGiven() {
    final String f1 = "file1";
    final String f2 = "file2";
    final String f3 = "file3";

    final AttrAdapter<NativeAttr,Float> aa = new AttrAdapter<NativeAttr,Float>(new AttrDefSet<NativeAttr,Float>(), NativeAttr.frads);
    aa.put(f1, NativeAttr.A, 0.0f);
    aa.put(f2, NativeAttr.A, 0.0f);
    aa.put(f3, NativeAttr.A, 0.0f);
    aa.put(f1, NativeAttr.B, 1.0f);
    aa.put(f2, NativeAttr.B, 1.0f);
    aa.put(f3, NativeAttr.B, 1.0f);
    aa.put(f1, NativeAttr.C, 2.0f);
    aa.put(f2, NativeAttr.C, 2.0f);
    aa.put(f3, NativeAttr.C, 2.1f);

    final Map<ExtAttrDef<NativeAttr,Float>,Exception> failures =
      new HashMap<ExtAttrDef<NativeAttr,Float>,Exception>();
    final Map<NativeAttr,Float> given = new HashMap<NativeAttr,Float>();
    given.put(NativeAttr.C, 2.0f);
    final Collection<ExtAttrValue> vals;
    try {
      vals = aa.getValuesGiven(given, failures);
      assertTrue(failures.isEmpty());
    } catch (Exception e) {
      fail(e.getMessage());
      return;
    }
    final ExtAttrValue a0 = new BasicExtAttrValue("ext-A", Float.toString(0.0f));
    final ExtAttrValue c2_b1a0 = new BasicExtAttrValue("ext-C", Float.toString(2.0f),
        Utils.put(new LinkedHashMap<String,String>(), new String[]{"B", "A"},
            new String[]{Float.toString(1.0f), Float.toString(0.0f)}));
 
    final Iterator<ExtAttrValue> i = vals.iterator();
    assertEquals(a0, i.next());
    assertEquals(c2_b1a0, i.next());
    assertFalse(i.hasNext());
  }

  /**
   * Test method for {@link org.nrg.attr.AbstractAttrAdapter#getMultipleValuesGiven(java.util.Map)}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public final void testGetMultipleValuesGiven() {
    final String f1 = "file1";
    final String f2 = "file2";
    final String f3 = "file3";

    final AttrAdapter<NativeAttr,Float> aa = new AttrAdapter<NativeAttr,Float>(new AttrDefSet<NativeAttr,Float>(), NativeAttr.frads);
    aa.put(f1, NativeAttr.A, 0.0f);
    aa.put(f2, NativeAttr.A, 0.0f);
    aa.put(f3, NativeAttr.A, 0.0f);
    aa.put(f1, NativeAttr.B, 1.0f);
    aa.put(f2, NativeAttr.B, 1.0f);
    aa.put(f3, NativeAttr.B, 1.0f);
    aa.put(f1, NativeAttr.C, 2.0f);
    aa.put(f2, NativeAttr.C, 2.0f);
    aa.put(f3, NativeAttr.C, 2.1f);

    final Map<ExtAttrDef<NativeAttr,Float>,Exception> failures =
      new HashMap<ExtAttrDef<NativeAttr,Float>,Exception>();
    final Map<NativeAttr,Float> given = new HashMap<NativeAttr,Float>();
    given.put(NativeAttr.C, 2.0f);
    final List<Set<ExtAttrValue>> vals;
    try {
      vals = aa.getMultipleValuesGiven(new HashMap<NativeAttr,Float>(), failures);
      assertTrue(failures.isEmpty());
    } catch (Exception e) {
      fail(e.getMessage());
      return;
    }
    final ExtAttrValue a0 = new BasicExtAttrValue("ext-A", Float.toString(0.0f));
 
    final Iterator<Set<ExtAttrValue>> i = vals.iterator();
    final Set<ExtAttrValue> a0_vals = i.next();
    assertEquals(1, a0_vals.size());
    assertEquals(a0, a0_vals.toArray(new ExtAttrValue[0])[0]);
    final Set<ExtAttrValue> c2_b1a0_vals = i.next();
    assertEquals(2, c2_b1a0_vals.size());
    assertFalse(i.hasNext());
  }
}
