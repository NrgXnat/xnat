/*
 * dicom-xnat-util: org.nrg.xnat.LabelsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xnat;

import junit.framework.TestCase;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class LabelsTest extends TestCase {

	/**
	 * Test method for {@link org.nrg.xnat.Labels#isValidLabel(java.lang.CharSequence)}.
	 */
	public void testIsValidLabel() {
		assertEquals("4", Labels.toLabelChars("4"));
		assertEquals("_", Labels.toLabelChars("_"));
		assertEquals("-", Labels.toLabelChars("-"));
		assertEquals("_", Labels.toLabelChars("."));
		assertEquals("_", Labels.toLabelChars("%"));
		assertEquals("_f_o_o_", Labels.toLabelChars(",f:o^o)"));
	}

	/**
	 * Test method for {@link org.nrg.xnat.Labels#toLabelChars(java.lang.CharSequence)}.
	 */
	public void testToLabelChars() {
		assertTrue(Labels.isValidLabel("4"));
		assertTrue(Labels.isValidLabel("_"));
		assertFalse(Labels.isValidLabel("foo.bar"));
		assertFalse(Labels.isValidLabel(""));
		assertFalse(Labels.isValidLabel("*w*"));
	}

}
