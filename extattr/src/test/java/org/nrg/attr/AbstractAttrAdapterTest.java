/*
 * ExtAttr: org.nrg.attr.AbstractAttrAdapterTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class AbstractAttrAdapterTest {
    public static final class MapAttrAdapter<S,V> extends AbstractAttrAdapter<S,V> {
        final Map<String,Map<S,V>> vals = Maps.newHashMap();

        public MapAttrAdapter(final MutableAttrDefs<S> ad, final AttrDefs<S>...attrs) {
            super(ad, attrs);
        }

        protected Collection<Map<S,V>> getUniqueCombinationsGivenValues(final Map<S,V> given,
                final Collection<S> attrs, final Map<S,ConversionFailureException> failures) {
            final Set<Map<S,V>> matching = Sets.newHashSet();
            FILES: for (final Map<S,V> data : vals.values()) {
                for (Map.Entry<? extends S,? extends V> e : given.entrySet()) {
                    if (!e.getValue().equals(data.get(e.getKey()))) {
                        continue FILES;
                    }
                }
                matching.add(data);
            }

            final Set<Map<S,V>> combs = Sets.newHashSet();
            for (final Map<S,V> match : matching) {
                final Map<S,V> vals = Maps.newHashMap();
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

        public MapAttrAdapter<S,V> put(final String file, final S s, final V v) {
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


    /**
     * Test method for {@link org.nrg.attr.AbstractAttrAdapter#AbstractAttrAdapter(org.nrg.attr.AttrDefSet, org.nrg.attr.ReadableAttrDefSet<S,V>[])}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testAbstractAttrAdapter() {
        final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>(), NativeAttr.frads);
        assertEquals(NativeAttr.frads.getNativeAttrs(), aa.getDefs().getNativeAttrs());
    }

    /**
     * Test method for {@link org.nrg.attr.AbstractAttrAdapter#getDefs()}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testGetDefs() {
        final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>(), NativeAttr.frads);
        assertEquals(NativeAttr.frads.getNativeAttrs(), aa.getDefs().getNativeAttrs());
    }

    /**
     * Test method for {@link org.nrg.attr.AbstractAttrAdapter#add(org.nrg.attr.ReadableAttrDefSet<S,V>[])}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testAddReadableAttrDefSetOfSVArray() {
        final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>(), NativeAttr.emptyFrads);
        aa.add(NativeAttr.frads);
        assertEquals(NativeAttr.frads.getNativeAttrs(), aa.getDefs().getNativeAttrs());
    }

    /**
     * Test method for {@link org.nrg.attr.AbstractAttrAdapter#add(org.nrg.attr.ExtAttrDef<S,V>[])}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testAddExtAttrDefOfSVArray() {
        final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>(), NativeAttr.emptyFrads);
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
        final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>(), NativeAttr.frads);
        assertNotNull(aa.getDefs());
        assertTrue(Sets.newHashSet(aa.getDefs()).contains(NativeAttr.fextA));
        aa.remove(new String[]{"ext-A"});
        assertFalse(Sets.newHashSet(aa.getDefs()).contains(NativeAttr.fextA));
        assertTrue(Sets.newHashSet(aa.getDefs()).contains(NativeAttr.fextC_BA));
    }

    /**
     * Test method for {@link org.nrg.attr.AbstractAttrAdapter#remove(S[])}.
     *
     **/
    @SuppressWarnings("unchecked")
    @Test
    public final void testRemoveSArray() {
        final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>(), NativeAttr.frads);
        assertNotNull(aa.getDefs());
        assertTrue(Sets.newHashSet(aa.getDefs()).contains(NativeAttr.fextC_BA));
        aa.remove(new NativeAttr[]{NativeAttr.C});
        assertFalse(Sets.newHashSet(aa.getDefs()).contains(NativeAttr.fextC_BA));
        assertTrue(Sets.newHashSet(aa.getDefs()).contains(NativeAttr.fextA));
        aa.remove(new NativeAttr[]{NativeAttr.A});
        assertFalse(Sets.newHashSet(aa.getDefs()).contains(NativeAttr.fextA));
        assertTrue(aa.getDefs().getNativeAttrs().isEmpty());

        aa.add(NativeAttr.frads);
        assertTrue(Sets.newHashSet(aa.getDefs()).contains(NativeAttr.fextA));
        assertTrue(Sets.newHashSet(aa.getDefs()).contains(NativeAttr.fextC_BA));
        aa.remove(new NativeAttr[]{NativeAttr.A});	// both ExtAttrDefs use A, so both will be removed.
        assertTrue(Sets.newHashSet(aa.getDefs()).isEmpty());
        assertTrue(aa.getDefs().getNativeAttrs().isEmpty());
    }

    /**
     * Test method for {@link org.nrg.attr.AbstractAttrAdapter#getValues()}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testGetValues() throws ExtAttrException {
        final String f1 = "file1";
        final String f2 = "file2";

        final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>(), NativeAttr.frads);
        aa.put(f1, NativeAttr.A, 0.0f);
        aa.put(f2, NativeAttr.A, 0.0f);
        aa.put(f1, NativeAttr.B, 1.0f);
        aa.put(f2, NativeAttr.B, 1.0f);
        aa.put(f1, NativeAttr.C, 2.0f);
        aa.put(f2, NativeAttr.C, 2.0f);

        final Map<ExtAttrDef<NativeAttr>,Throwable> failures = Maps.newLinkedHashMap();
        final Collection<ExtAttrValue> vals = aa.getValues(failures);
        assertTrue(failures.isEmpty());

        final ExtAttrValue a0 = new BasicExtAttrValue("ext-A", Float.toString(0.0f));
        final ExtAttrValue c2_b1a0 = new BasicExtAttrValue("ext-C", Float.toString(2.0f),
                Utils.zipmap(new LinkedHashMap<String,String>(), new String[]{"B", "A"},
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
            final Iterator<Map.Entry<ExtAttrDef<NativeAttr>,Throwable>> mei = failures.entrySet().iterator();
            final ExtAttrDef<NativeAttr> failed = mei.next().getKey();
            assertEquals("ext-C", failed.getName());
            assertFalse(mei.hasNext());
        } catch (NoUniqueValueException e) {
            // expected: 2 values for attribute ext-C
            assertEquals("ext-C", e.getAttribute());
            assertEquals(2, e.getValues().length);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        final MapAttrAdapter<NativeAttr,Float> aam = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>());
        aam.put(f1, NativeAttr.A, 0.0f);
        aam.put(f2, NativeAttr.A, 0.0f);
        aam.put(f1, NativeAttr.B, 1.0f);
        aam.put(f2, NativeAttr.B, 1.5f);
        aam.put(f1, NativeAttr.C, 1.0f);
        aam.put(f2, NativeAttr.C, 2.0f);

        // This definition actually gives us funny-looking attribute names with floating point
        // detritus in.  The definition of multiplexed attr def for a particular native attribute
        // type will need to be made carefully.
        final ExtAttrDef<NativeAttr> extB = new MultiplexTextAttrDef<NativeAttr,Float>("extB_mp", "MultiB_%f", NativeAttr.B, NativeAttr.C);
        final Pattern pattern = Pattern.compile("MultiB_(.*)");
        aam.add(extB);

        failures.clear();
        try {
            final List<ExtAttrValue> mvals = aam.getValues(failures);
            assertEquals(2, mvals.size());
            for (final ExtAttrValue val : mvals) {
                final Matcher m = pattern.matcher(val.getName());
                assertTrue(m.matches());
                final Float f = Float.valueOf(m.group(1));
                boolean foundIndex = false;
                for (final Map<NativeAttr,Float> fm : aam.vals.values()) {
                    if (fm.get(NativeAttr.C).equals(f)) {
                        assertEquals(Float.valueOf(val.getText()), fm.get(NativeAttr.B));
                        foundIndex = true;
                    }
                }
                assertTrue(foundIndex);
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
    public final void testGetValuesGiven() throws ExtAttrException {
        final String f1 = "file1";
        final String f2 = "file2";
        final String f3 = "file3";

        final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>(), NativeAttr.frads);
        aa.put(f1, NativeAttr.A, 0.0f);
        aa.put(f2, NativeAttr.A, 0.0f);
        aa.put(f3, NativeAttr.A, 0.0f);
        aa.put(f1, NativeAttr.B, 1.0f);
        aa.put(f2, NativeAttr.B, 1.0f);
        aa.put(f3, NativeAttr.B, 1.0f);
        aa.put(f1, NativeAttr.C, 2.0f);
        aa.put(f2, NativeAttr.C, 2.0f);
        aa.put(f3, NativeAttr.C, 2.1f);

        final Map<ExtAttrDef<NativeAttr>,Throwable> failures = Maps.newLinkedHashMap();
        final Map<NativeAttr,Float> given = Maps.newHashMap();
        given.put(NativeAttr.C, 2.0f);
        final Collection<ExtAttrValue> vals = aa.getValuesGiven(given, failures);
        assertTrue(failures.isEmpty());

        final ExtAttrValue a0 = new BasicExtAttrValue("ext-A", Float.toString(0.0f));
        final ExtAttrValue c2_b1a0 = new BasicExtAttrValue("ext-C", Float.toString(2.0f),
                Utils.zipmap(new LinkedHashMap<String,String>(), new String[]{"B", "A"},
                        new String[]{Float.toString(1.0f), Float.toString(0.0f)}));

        final Iterator<ExtAttrValue> i = vals.iterator();
        assertEquals(a0, i.next());
        assertEquals(c2_b1a0, i.next());
        assertFalse(i.hasNext());
    }

    /**
     * Test method for {@link org.nrg.attr.AbstractAttrAdapter#getMultipleValuesGiven(java.util.Map)}.
     */
    /*
	@SuppressWarnings("unchecked")
	@Test
	public final void testGetMultipleValuesGiven() throws ExtAttrException {
		final String f1 = "file1";
		final String f2 = "file2";
		final String f3 = "file3";

		final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>(), NativeAttr.frads);
		aa.put(f1, NativeAttr.A, 0.0f);
		aa.put(f2, NativeAttr.A, 0.0f);
		aa.put(f3, NativeAttr.A, 0.0f);
		aa.put(f1, NativeAttr.B, 1.0f);
		aa.put(f2, NativeAttr.B, 1.0f);
		aa.put(f3, NativeAttr.B, 1.0f);
		aa.put(f1, NativeAttr.C, 2.0f);
		aa.put(f2, NativeAttr.C, 2.0f);
		aa.put(f3, NativeAttr.C, 2.1f);

		final Map<ExtAttrDef<NativeAttr>,Exception> failures = Maps.newLinkedHashMap();
		final Map<NativeAttr,Float> given = Maps.newLinkedHashMap();
		given.put(NativeAttr.C, 2.0f);
		final List<Set<ExtAttrValue>> vals = aa.getMultipleValuesGiven(new HashMap<NativeAttr,Float>(), failures);
		assertTrue(failures.isEmpty());
		final ExtAttrValue a0 = new BasicExtAttrValue("ext-A", Float.toString(0.0f));

		final Iterator<Set<ExtAttrValue>> i = vals.iterator();
		final Set<ExtAttrValue> a0_vals = i.next();
		assertEquals(1, a0_vals.size());
		assertEquals(a0, a0_vals.toArray(new ExtAttrValue[0])[0]);
		final Set<ExtAttrValue> c2_b1a0_vals = i.next();
		assertEquals(2, c2_b1a0_vals.size());
		assertFalse(i.hasNext());
	}

	@SuppressWarnings("unchecked")
	@Test
	public final void testMissingSingleAttribute() throws ExtAttrException {
		final String f1 = "file1";

		final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>(), NativeAttr.frads);
		aa.put(f1, NativeAttr.A, 0.0f);
		aa.put(f1, NativeAttr.C, 2.0f);

		final ExtAttrDef<NativeAttr> extB = new SingleValueTextAttr<NativeAttr>("ext-B", NativeAttr.B);
		aa.add(extB);

		final Map<ExtAttrDef<NativeAttr>,Exception> failures = Maps.newLinkedHashMap();
		final Map<NativeAttr,Float> given = Maps.newLinkedHashMap();
		final List<Set<ExtAttrValue>> vals = aa.getMultipleValuesGiven(given, failures);
		final Set<String> names = Sets.newHashSet();
		for (final Set<ExtAttrValue> vs : vals) {
			for (final ExtAttrValue v : vs) {
				assertFalse(v.equals(extB));
				names.add(v.getName());
			}
		}
		assertTrue(names.contains("ext-A"));
		assertFalse(names.contains(extB.getName()));
		assertFalse(names.contains("ext-C"));   // uses B in attributes
		assertTrue(failures.containsKey(extB));
	}

	@SuppressWarnings("unchecked")
	@Test
	public final void testMissingComponent() throws ExtAttrException {
		final String f1 = "file1";

		final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>());
		aa.put(f1, NativeAttr.A, 0.0f);
		aa.put(f1, NativeAttr.C, 2.0f);

		final ExtAttrDef<NativeAttr> concat = new ConcatAttrDef<NativeAttr,Float>("concat", NativeAttr.A, NativeAttr.B, NativeAttr.C);
		aa.add(concat);

		final Map<ExtAttrDef<NativeAttr>,Exception> failures = Maps.newLinkedHashMap();
		final Map<NativeAttr,Float> given = Maps.newLinkedHashMap();
		final List<Set<ExtAttrValue>> vals = aa.getMultipleValuesGiven(given, failures);
		for (final Set<ExtAttrValue> vs : vals) {
			assertTrue(vs.isEmpty());
		}
		assertTrue(failures.containsKey(concat));
	}

	@SuppressWarnings("unchecked")
	@Test
	public final void testMissingComponentOptionalAttr() throws ExtAttrException {
		final String f1 = "file1";

		final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>());
		aa.put(f1, NativeAttr.A, 0.0f);
		aa.put(f1, NativeAttr.C, 2.0f);

		final ExtAttrDef<NativeAttr> concat = new OptionalConcatAttrDef<NativeAttr,Float>("concat", NativeAttr.A, NativeAttr.B, NativeAttr.C);
		aa.add(concat);

		final Map<ExtAttrDef<NativeAttr>,Exception> failures = Maps.newLinkedHashMap();
		final Map<NativeAttr,Float> given = Maps.newLinkedHashMap();
		final List<Set<ExtAttrValue>> vals = aa.getMultipleValuesGiven(given, failures);
		for (final Set<ExtAttrValue> vs : vals) {
			assertTrue(vs.isEmpty());
		}
		assertFalse(failures.containsKey(concat));
	}


	@SuppressWarnings("unchecked")
	@Test
	public final void testMissingComponentNotRequired() throws ExtAttrException {
		final String f1 = "file1";

		final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>());
		aa.put(f1, NativeAttr.A, 0.0f);
		aa.put(f1, NativeAttr.C, 2.0f);

		final AbstractExtAttrDef<NativeAttr,Float,Map<NativeAttr,Float>> concat = new ConcatAttrDef<NativeAttr,Float>("concat", NativeAttr.A, NativeAttr.B, NativeAttr.C);
		aa.add(concat);
		concat.makeOptional(NativeAttr.B);

		final Map<ExtAttrDef<NativeAttr>,Exception> failures = Maps.newLinkedHashMap();
		final Map<NativeAttr,Float> given = Maps.newLinkedHashMap();
		final List<Set<ExtAttrValue>> vals = aa.getMultipleValuesGiven(given, failures);
		for (final Set<ExtAttrValue> vs : vals) {
			assertFalse(vs.isEmpty());

		}
		assertTrue(failures.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test
	public final void testNullConflict() throws ExtAttrException {
		final String f1 = "file1", f2 = "file2";
		final MapAttrAdapter<NativeAttr,Float> aa = new MapAttrAdapter<NativeAttr,Float>(new MutableAttrDefs<NativeAttr>());
		aa.put(f1, NativeAttr.A, 1.0f);
		aa.put(f1, NativeAttr.C, 3.0f);
		aa.put(f2, NativeAttr.A, 2.0f);

		final ExtAttrDef<NativeAttr> concat = new ConcatAttrDef<NativeAttr,Float>("concat", NativeAttr.A, NativeAttr.C);
		aa.add(concat);

		final Map<ExtAttrDef<NativeAttr>,Exception> failures = Maps.newLinkedHashMap();
		final Map<NativeAttr,Float> given = Maps.newLinkedHashMap();
		final List<Set<ExtAttrValue>> vals = aa.getMultipleValuesGiven(given, failures);
		for (final Set<ExtAttrValue> vs : vals) {
			assertFalse(vs.isEmpty());

		}
		assertTrue(failures.isEmpty());
	}
     */
}
