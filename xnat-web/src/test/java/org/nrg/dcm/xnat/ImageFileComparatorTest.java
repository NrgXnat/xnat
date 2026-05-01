/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ImageFileComparatorTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Comparator;

import org.junit.Test;
import org.nrg.attr.BasicExtAttrValue;
import org.nrg.attr.ExtAttrValue;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class ImageFileComparatorTest {
	private static ExtAttrValue instanceNumberOf(final String n) {
		return new BasicExtAttrValue("instanceNumber", n);
	}
	
	@Test
	public void testCompare() {
		final File f1 = new File("/foo/bar/f1");
		final File f2 = new File("/foo/bar/f2");
		
		;
		final Comparator<File> c1 = new ImageFileComparator(ImmutableMultimap.of(
				f1, instanceNumberOf("1"), f2, instanceNumberOf("2")));
		assertTrue(0 > c1.compare(f1, f2));
		assertTrue(0 < c1.compare(f2, f1));
		assertEquals(0, c1.compare(f1, f1));
		
		final Comparator<File> c2 = new ImageFileComparator(ImmutableMultimap.of(
				f1, instanceNumberOf("2"), f2, instanceNumberOf("1")));
		assertTrue(0 < c2.compare(f1, f2));
		assertTrue(0 > c2.compare(f2, f1));
		assertEquals(0, c2.compare(f2, f2));
		
		final Multimap<File,ExtAttrValue> empty = ImmutableMultimap.of();
		final Comparator<File> c3 = new ImageFileComparator(empty);
		assertTrue(0 > c3.compare(f1, f2));
		assertTrue(0 < c3.compare(f2, f1));
		assertEquals(0, c3.compare(f1, f1));
		
		final Comparator<File> c4 = new ImageFileComparator(ImmutableMultimap.of(
				f2, instanceNumberOf("2")));
		assertTrue(0 < c4.compare(f1, f2));
		assertTrue(0 > c4.compare(f2, f1));
		assertEquals(0, c4.compare(f1, f1));
		assertEquals(0, c4.compare(f2, f2));
	}
}
