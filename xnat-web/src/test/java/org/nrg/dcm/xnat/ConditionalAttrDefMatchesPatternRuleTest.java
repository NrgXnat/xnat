/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ConditionalAttrDefMatchesPatternRuleTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.FixedDicomAttributeIndex;
import org.nrg.dcm.xnat.AbstractConditionalAttrDef.Rule;
import org.nrg.dcm.xnat.AbstractConditionalAttrDef.MatchesPatternRule;

import com.google.common.collect.Maps;

import junit.framework.TestCase;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class ConditionalAttrDefMatchesPatternRuleTest extends TestCase {
	private static final DicomAttributeIndex I13 = new FixedDicomAttributeIndex(13);
	private static final DicomAttributeIndex I42 = new FixedDicomAttributeIndex(42);
	private static final DicomAttributeIndex I43 = new FixedDicomAttributeIndex(43);
	private static final DicomAttributeIndex I44 = new FixedDicomAttributeIndex(44);

	/**
	 * Test method for {@link org.nrg.dcm.xnat.AbstractConditionalAttrDef.MatchesPatternRule#getTags()}.
	 */
	public void testGetTag() {
		final Rule rule = new MatchesPatternRule(I42, "(my-(?:\\d{4})).*", 1);
		assertEquals(I42, rule.getTags().getFirst());
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.AbstractConditionalAttrDef.MatchesPatternRule#getValue(java.util.Map)}.
	 */
	public void testGetValue() {
		final Rule rule = new MatchesPatternRule(I42, "(my-(?:\\d{4})).*", 1);
		assertEquals("my-1234", rule.getValue(Collections.singletonMap(I42, "my-1234 should match")));
		assertEquals(null, rule.getValue(Collections.singletonMap(I42, "my-abcd shouldn't")));
		assertEquals(null, rule.getValue(Collections.singletonMap(I43, "my-1234 has the wrong tag")));

		final Map<DicomAttributeIndex,String> map = Maps.newHashMap();
		map.put(I42, "my-3456 is a match");
		map.put(I44, "my-7890 would be if it had the right tag");
		assertEquals("my-3456", rule.getValue(map));

		final Rule ciRule = new MatchesPatternRule(I13, "Foo:\\s*(\\w+)", Pattern.CASE_INSENSITIVE, 1);
		assertEquals("bar", ciRule.getValue(Collections.singletonMap(I13, "Foo:bar")));
		assertEquals("baz", ciRule.getValue(Collections.singletonMap(I13, "fOo:baz")));
		assertEquals("yak", ciRule.getValue(Collections.singletonMap(I13, "foO: yak")));
		assertNull(ciRule.getValue(Collections.singletonMap(I13, "fool:bar")));
	}
}
