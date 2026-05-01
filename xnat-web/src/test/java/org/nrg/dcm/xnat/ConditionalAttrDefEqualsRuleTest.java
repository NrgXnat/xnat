/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ConditionalAttrDefEqualsRuleTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import java.util.Collections;

import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.FixedDicomAttributeIndex;
import org.nrg.dcm.xnat.AbstractConditionalAttrDef.EqualsRule;

import junit.framework.TestCase;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class ConditionalAttrDefEqualsRuleTest extends TestCase {
	private static final DicomAttributeIndex I13 = new FixedDicomAttributeIndex(13);
	private static final DicomAttributeIndex I14 = new FixedDicomAttributeIndex(14);
	private static final DicomAttributeIndex I42 = new FixedDicomAttributeIndex(42);

	/**
	 * Test method for {@link org.nrg.dcm.xnat.AbstractConditionalAttrDef.SimpleValueRule#getTags()}.
	 */
	public void testGetTag() {
		final EqualsRule rule = new EqualsRule(I42);
		assertEquals(I42, rule.getTags().get(0));
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.AbstractConditionalAttrDef.SimpleValueRule#getValue(java.util.Map)}.
	 */
	public void testGetValue() {
		final EqualsRule rule = new EqualsRule(I13);
		assertEquals("foo", rule.getValue(Collections.singletonMap(I13, "foo")));
		assertEquals(null, rule.getValue(Collections.singletonMap(I14, "foo")));
	}
}
