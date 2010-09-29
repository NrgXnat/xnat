/**
 * Copyright (c) 2007,2010 Washington University
 */
package org.nrg.attr;

import java.util.Iterator;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public class MutableAttrDefsTest {
	/**
	 * Test method for {@link org.nrg.attr.MutableAttrDefs#add(org.nrg.attr.ExtAttrDef)}.
	 */
	@Test
	public void testAddExtAttrDefOfSV() {
		final MutableAttrDefs<NativeAttr,Float> ads = new MutableAttrDefs<NativeAttr,Float>();
		Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
		assertFalse(i.hasNext());

		final ExtAttrDef<NativeAttr,Float> extA = 
			new ExtAttrDef.Text<NativeAttr,Float>("ext-A", NativeAttr.A);
		ads.add(extA);
		i = ads.iterator();
		assertEquals(extA, i.next());
		assertFalse(i.hasNext());
	}

	/**
	 * Test method for {@link org.nrg.attr.MutableAttrDefs#add(java.lang.String)}.
	 */
	@Test
	public void testAddString() {
		final MutableAttrDefs<NativeAttr,Float> ads = new MutableAttrDefs<NativeAttr,Float>();
		Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
		assertFalse(i.hasNext());

		final ExtAttrDef<NativeAttr,Float> empty = new ExtAttrDef.Empty<NativeAttr,Float>("empty");
		ads.add("empty");
		i = ads.iterator();
		assertEquals(empty, i.next());
		assertFalse(i.hasNext());
	}

	/**
	 * Test method for {@link org.nrg.attr.MutableAttrDefs#add(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAddStringString() {
		final MutableAttrDefs<NativeAttr,Float> ads = new MutableAttrDefs<NativeAttr,Float>();
		Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
		assertFalse(i.hasNext());

		final ExtAttrDef<NativeAttr,Float> foo = new ExtAttrDef.Constant<NativeAttr,Float>("foo", "bar");
		ads.add("foo", "bar");
		i = ads.iterator();
		assertEquals(foo, i.next());
		assertFalse(i.hasNext());
	}

	/**
	 * Test method for {@link org.nrg.attr.MutableAttrDefs#add(java.lang.String, java.lang.Object)}.
	 */
	@Test
	public void testAddStringS() {
		final MutableAttrDefs<NativeAttr,Float> ads = new MutableAttrDefs<NativeAttr,Float>();
		Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
		assertFalse(i.hasNext());

		final ExtAttrDef<NativeAttr,Float> extA = new ExtAttrDef.Text<NativeAttr,Float>("extA", NativeAttr.A);
		ads.add("extA", NativeAttr.A);
		i = ads.iterator();
		assertEquals(extA, i.next());
		assertFalse(i.hasNext());
	}

	/**
	 * Test method for {@link org.nrg.attr.MutableAttrDefs#add(org.nrg.attr.ReadableAttrDefSet<S,V>[])}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testAddReadableAttrDefSetOfSVArray() {
		final MutableAttrDefs<NativeAttr,Float> ads = new MutableAttrDefs<NativeAttr,Float>();
		Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
		assertFalse(i.hasNext());

		ads.add(NativeAttr.frads);
		i = ads.iterator();
		assertEquals(NativeAttr.fextA, i.next());
		assertEquals(NativeAttr.fextC_BA, i.next());
		assertFalse(i.hasNext());
	}


	/**
	 * Test method for {@link org.nrg.attr.MutableAttrDefs#iterator()}.
	 */
	@Test
	public void testIterator() {
		final MutableAttrDefs<NativeAttr,Float> ads = new MutableAttrDefs<NativeAttr,Float>();
		Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
		assertFalse(i.hasNext());

		ads.add("empty");
		i = ads.iterator();
		assertEquals(new ExtAttrDef.Empty<NativeAttr,Float>("empty"), i.next());
		assertFalse(i.hasNext());
	}

	/**
	 * Test method for {@link org.nrg.attr.MutableAttrDefs#getNativeAttrs()}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetNativeAttrs() {
		final MutableAttrDefs<NativeAttr,Float> ads = new MutableAttrDefs<NativeAttr,Float>();
		Iterator<NativeAttr> i = ads.getNativeAttrs().iterator();
		assertFalse(i.hasNext());

		ads.add(NativeAttr.frads);

		i = ads.getNativeAttrs().iterator();
		assertEquals(NativeAttr.A, i.next());
		assertEquals(NativeAttr.B, i.next());
		assertEquals(NativeAttr.C, i.next());
		assertFalse(i.hasNext());
	}
}
