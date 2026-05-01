/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ConditionalAttrDefMapRuleTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import java.util.Collections;
import java.util.Map;

import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.FixedDicomAttributeIndex;
import org.nrg.dcm.xnat.AbstractConditionalAttrDef.MapRule;

import com.google.common.collect.ImmutableMap;

import junit.framework.TestCase;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class ConditionalAttrDefMapRuleTest extends TestCase {
	private static final DicomAttributeIndex I7 = new FixedDicomAttributeIndex(7);
	private static final DicomAttributeIndex I8 = new FixedDicomAttributeIndex(8);
	private static final DicomAttributeIndex I42 = new FixedDicomAttributeIndex(42);

	/**
	 * Test method for {@link org.nrg.dcm.xnat.AbstractConditionalAttrDef.SimpleValueRule#getTags()}.
	 */
	public void testGetTag() {
		final Map<String,String> map = Collections.emptyMap();
		final MapRule rule = new MapRule(I42, map);
		assertEquals(I42, rule.getTags().get(0));
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.AbstractConditionalAttrDef.SimpleValueRule#getValue(java.util.Map)}.
	 */
	public void testGetValue() {
		final Map<String,String> map = ImmutableMap.of("a", "A", "b", "B", "c", "C");
		final MapRule rule = new MapRule(I7, map);
		assertEquals("A", rule.getValue(Collections.singletonMap(I7, "a")));
		assertEquals("B", rule.getValue(Collections.singletonMap(I7, "b")));
		assertEquals("d", rule.getValue(Collections.singletonMap(I7, "d")));
		assertEquals(null, rule.getValue(Collections.singletonMap(I8, "a")));
	}
}
