/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ConditionalAttrDefContainsAssignmentRuleTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.FixedDicomAttributeIndex;
import org.nrg.dcm.xnat.AbstractConditionalAttrDef.Rule;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class ConditionalAttrDefContainsAssignmentRuleTest {
	private final DicomAttributeIndex I1 = new FixedDicomAttributeIndex(1);
	private final DicomAttributeIndex I2 = new FixedDicomAttributeIndex(1);
	private final DicomAttributeIndex I15 = new FixedDicomAttributeIndex(15);
	private final DicomAttributeIndex I19 = new FixedDicomAttributeIndex(19);
	private final DicomAttributeIndex I27 = new FixedDicomAttributeIndex(27);


	/**
	 * Test method for {@link org.nrg.dcm.xnat.AbstractConditionalAttrDef.ContainsAssignmentRule#ContainsAssignmentRule(int, java.lang.String, java.lang.String, java.lang.String, int)}.
	 */
	@Test
	public void testContainsAssignmentRuleIntStringStringStringInt() {
		final Rule rule = new AbstractConditionalAttrDef.ContainsAssignmentRule(I15, "lhs", "=", "\\d{4}", 0);
		assertEquals("3917", rule.getValue(Collections.singletonMap(I15, "lhs = 3917")));
		assertEquals("2112", rule.getValue(Collections.singletonMap(I15, "lhs=2112")));
		assertEquals("5309", rule.getValue(Collections.singletonMap(I15, "lhs=5309; other stuff")));
		assertEquals("1984", rule.getValue(Collections.singletonMap(I15, "lhs=1984\rand more")));
		assertNull(rule.getValue(Collections.singletonMap(I15, "rhs = 3917")));
		assertNull(rule.getValue(Collections.singletonMap(I15, "lhs : 3917")));
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.AbstractConditionalAttrDef.ContainsAssignmentRule#ContainsAssignmentRule(int, java.lang.String, java.lang.String, int)}.
	 */
	@Test
	public void testContainsAssignmentRuleIntStringStringInt() {
		final Rule rule = new AbstractConditionalAttrDef.ContainsAssignmentRule(I27, "lhs", "~", 0);
		assertEquals("foo", rule.getValue(Collections.singletonMap(I27, "lhs ~ foo; more")));
		assertNull(rule.getValue(Collections.singletonMap(I19, "lhs ~ bar^baz; yak")));
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.AbstractConditionalAttrDef.ContainsAssignmentRule#ContainsAssignmentRule(int, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testContainsAssignmentRuleIntStringString() {
		final Rule rule = new AbstractConditionalAttrDef.ContainsAssignmentRule(I19, "x", "==");
		assertEquals("bar", rule.getValue(Collections.singletonMap(I19, "x == bar;")));
		assertNull(rule.getValue(Collections.singletonMap(I19, "y == baz")));
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.AbstractConditionalAttrDef.ContainsAssignmentRule#ContainsAssignmentRule(int, java.lang.String, int)}.
	 */
	@Test
	public void testContainsAssignmentRuleIntStringInt() {
		final Rule rule = new AbstractConditionalAttrDef.ContainsAssignmentRule(I1, "y", 0);
		assertEquals("baz", rule.getValue(Collections.singletonMap(I1, "y: baz\nand more")));
		assertNull(rule.getValue(Collections.singletonMap(I1, "y = baz")));
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.AbstractConditionalAttrDef.ContainsAssignmentRule#ContainsAssignmentRule(int, java.lang.String)}.
	 */
	@Test
	public void testContainsAssignmentRuleIntString() {
		final Rule rule = new AbstractConditionalAttrDef.ContainsAssignmentRule(I2, "z");
		assertEquals("yak", rule.getValue(Collections.singletonMap(I2, "z:yak")));
		assertNull(rule.getValue(Collections.singletonMap(I2, "z:-bar")));
	}
}
