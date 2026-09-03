/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.BasicCodedEntryAttributeIndexTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;


import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Attributes;
import org.junit.Test;
import org.nrg.attr.ConversionFailureException;
import org.nrg.dcm.DicomAttributeIndex;

import static org.junit.Assert.*;

public class BasicCodedEntryAttributeIndexTest {

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex(int[], java.lang.String, java.lang.String, java.lang.String, int)}.
	 */
	@Test
	public void testCodeSequenceAttributeIndexIntArrayStringStringStringInt() {
		new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0", "CZM_1_0", 0x00080104);
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex(int[], java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testCodeSequenceAttributeIndexIntArrayStringStringString() {
		new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0", "CZM_1_0");
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex(int[], java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testCodeSequenceAttributeIndexIntArrayStringString() {
		new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0");
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex#getAttributeName(org.dcm4che3.data.Attributes)}.
	 */
	@Test
	public void testGetAttributeName() {
		final Attributes dummy = new Attributes();
		final DicomAttributeIndex dai = new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0");
		//There are no spaces in Attribute name in Dcm4che3
		assertEquals("PerformedProtocolCodeSequence[99CZM:v1.0]", dai.getAttributeName(dummy));
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex#getColumnName()}.
	 */
	@Test
	public void testGetColumnName() {
		final DicomAttributeIndex dai0 = new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0", "CZM_1_0");
		assertEquals("CZM_1_0", dai0.getColumnName());
		
		final DicomAttributeIndex dai1 = new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0");
		assertEquals("cosq00400260_99CZM_1_0", dai1.getColumnName());
	}


	@Test
	public void testGetPath() {
		final Attributes dummy = new Attributes();
		final DicomAttributeIndex dai = new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0");
		assertArrayEquals(new Integer[]{0x00400260}, dai.getPath(dummy));
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex#getString(org.dcm4che3.data.Attributes)}.
	 */
	@Test
	public void testGetString() throws ConversionFailureException {
        final Attributes o = new Attributes();
        final Attributes se1 = new Attributes();
        o.ensureSequence(0x00400260, 1).add(se1);

        se1.setString(Tag.CodeValue, VR.SH, "SD-S2");
        se1.setString(Tag.CodingSchemeDesignator, VR.SH, "99CZM");
        se1.setString(Tag.CodingSchemeVersion, VR.SH, "1.0");
        se1.setString(Tag.CodeMeaning, VR.LO, "Macular Cube 512x128");

		final DicomAttributeIndex dai0 = new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0");
		assertEquals("Macular Cube 512x128", dai0.getString(o));
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex#getStrings(org.dcm4che3.data.Attributes)}.
	 */
	@Test
	public void testGetStrings() {
		final Attributes attributes = new Attributes();
        final Attributes sq0 = new Attributes();
        attributes.ensureSequence(0x00400260, 1).add(sq0);

        sq0.setString(Tag.CodeValue, VR.SH, "SD-S2");
		sq0.setString(Tag.CodingSchemeDesignator, VR.SH, "99CZM");
		sq0.setString(Tag.CodingSchemeVersion, VR.SH, "1.0");
		sq0.setString(Tag.CodeMeaning, VR.LO, "Macular Cube 512x128");

		final DicomAttributeIndex dai0 = new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0");
		assertArrayEquals(new String[]{"Macular Cube 512x128"}, dai0.getStrings(attributes));
	}

	/**
	 * A stop tag only gates top-level elements, so the bound is the sequence this enters. The value tag is read
	 * out of a nested context -- getStrings resolves it against getNestedContexts -- so it is reachable once the
	 * sequence is read and must not widen the window. Here it is numerically lower anyway; the second case is the
	 * one that would have been wrong.
	 */
	@Test
	public void boundsAtTheSequenceRatherThanTheValueTag() {
		assertEquals(0x00400260,
		             new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0", "CZM_1_0",
		                                               0x00080104).getMaxTag());
	}

	/** A value tag above its own sequence must still not widen the window. */
	@Test
	public void boundsAtTheSequenceEvenWhenTheValueTagSortsHigher() {
		assertEquals(0x00080100,
		             new BasicCodedEntryAttributeIndex(new Integer[]{0x00080100}, "99CZM", "1.0", "CZM_1_0",
		                                               0xF2151050).getMaxTag());
	}
}
