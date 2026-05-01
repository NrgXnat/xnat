/*
 * DicomDB: org.nrg.dcm.MutableAttrDefsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import static org.junit.Assert.*;

import org.junit.Test;

import org.nrg.dcm.TestAttrDef;
import org.nrg.attr.ExtAttrDef;

/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
@SuppressWarnings("unchecked")
public class MutableAttrDefsTest {
	@Test
	public final void testAddExtAttrDef() {
		MutableAttrDefs a = new MutableAttrDefs();
		int count = 0;
		for (@SuppressWarnings("unused") ExtAttrDef<DicomAttributeIndex> ea : a) {
			count++;
		}
		assertEquals(0, count);
		assertEquals(0, a.getNativeAttrs().size());

		a.add(new TestAttrDef.Text("foo", new FixedDicomAttributeIndex(1)));
		assertEquals(1, a.getNativeAttrs().size());
		count = 0;
		for (ExtAttrDef<DicomAttributeIndex> ea : a) {
			assertEquals("foo", ea.getName());
		}

		a.add(new TestAttrDef.Text("bar", new FixedDicomAttributeIndex(16)));
		a.add(new TestAttrDef.Text("baz", new FixedDicomAttributeIndex(8)));

		Iterator<ExtAttrDef<DicomAttributeIndex>> eai = a.iterator();
		assertTrue(eai.hasNext());
		assertEquals("foo", eai.next().getName());
		assertEquals("bar", eai.next().getName());
		assertEquals("baz", eai.next().getName());

	}

	@Test
	public final void testAddStringInt() {
		MutableAttrDefs a = new MutableAttrDefs();
		int count = 0;
		for (@SuppressWarnings("unused") ExtAttrDef<DicomAttributeIndex> ea : a) {
			count++;
		}
		assertEquals(0, count);
		assertEquals(0, a.getNativeAttrs().size());

		a.add("foo", new FixedDicomAttributeIndex(1));
		assertEquals(1, a.getNativeAttrs().size());
		count = 0;
		for (ExtAttrDef<DicomAttributeIndex> ea : a) {
			assertEquals("foo", ea.getName());
		}
	}

	@Test
	public final void testAddReadableAttrDefSetArray() {
		MutableAttrDefs a1 = new MutableAttrDefs();
		MutableAttrDefs a2 = new MutableAttrDefs();
		a2.add("foo", new FixedDicomAttributeIndex(1));
		a2.add("bar", new FixedDicomAttributeIndex(2));
		a2.add("baz", new FixedDicomAttributeIndex(3));

		a1.add(a2);
		assertEquals(3, a1.getNativeAttrs().size());

		MutableAttrDefs a3 = new MutableAttrDefs();
		a3.add("ack", new FixedDicomAttributeIndex(4));
		a3.add(a2);
		assertEquals(4, a3.getNativeAttrs().size());
	}


	@Test
	public final void testIterator() {
		String[] namea = {"foo", "bar", "baz", "yak"};
		assert namea.length > 0;

		Set<String> names = new HashSet<String>(Arrays.asList(namea));
		assert namea.length == names.size();

		MutableAttrDefs a = new MutableAttrDefs();
		for (int i = 0; i < namea.length; i++) {
			a.add(namea[i], new FixedDicomAttributeIndex(i));
		}
		assertEquals(namea.length, a.getNativeAttrs().size());

		Iterator<ExtAttrDef<DicomAttributeIndex>> eai = a.iterator();
		assertEquals("foo", eai.next().getName());
		assertEquals("bar", eai.next().getName());
		assertEquals("baz", eai.next().getName());
		assertEquals("yak", eai.next().getName());

		for (ExtAttrDef<DicomAttributeIndex> ea : a) {
			assertTrue(names.contains(ea.getName()));
			names.remove(ea.getName());
		}
		assertEquals(0, names.size());
	}


	@Test
	public final void testgetNativeAttrs() {
		String[] namea = {"foo", "bar", "baz", "yak"};
		assert namea.length > 0;

		MutableAttrDefs a = new MutableAttrDefs();
		for (int i = 0; i < namea.length; i++) {
			a.add(namea[i], new FixedDicomAttributeIndex(i));
		}
		assertEquals(namea.length, a.getNativeAttrs().size());

		for (int i = 0; i < namea.length; i++) {
			assertTrue(a.getNativeAttrs().contains(new FixedDicomAttributeIndex(i)));
		}
	}

}
