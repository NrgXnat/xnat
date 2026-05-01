/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.BasicCodedEntryAttributeIndexTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.DicomObjectSequenceElement;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.junit.Test;
import org.nrg.attr.ConversionFailureException;
import org.nrg.dcm.DicomAttributeIndex;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class BasicCodedEntryAttributeIndexTest {

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex#CodeSequenceAttributeIndex(int[], java.lang.String, java.lang.String, java.lang.String, int)}.
	 */
	@Test
	public void testCodeSequenceAttributeIndexIntArrayStringStringStringInt() {
		new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0", "CZM_1_0", 0x00080104);
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex#CodeSequenceAttributeIndex(int[], java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testCodeSequenceAttributeIndexIntArrayStringStringString() {
		new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0", "CZM_1_0");
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex#CodeSequenceAttributeIndex(int[], java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testCodeSequenceAttributeIndexIntArrayStringString() {
		new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0");
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex#getAttributeName(org.dcm4che2.data.DicomObject)}.
	 */
	@Test
	public void testGetAttributeName() {
		final DicomObject dummy = new BasicDicomObject();
		final DicomAttributeIndex dai = new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0");
		assertEquals("Performed Protocol Code Sequence[99CZM:v1.0]", dai.getAttributeName(dummy));
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

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex#getElement(org.dcm4che2.data.DicomObject)}.
	 */
	@Test
	public void testGetElement() {
		final DicomObject o = new BasicDicomObject();
		final DicomObject se1 = new BasicDicomObject();
		se1.putString(Tag.CodeValue, VR.SH, "SD-S2");
		se1.putString(Tag.CodingSchemeDesignator, VR.SH, "99CZM");
		se1.putString(Tag.CodingSchemeVersion, VR.SH, "1.0");
		se1.putString(Tag.CodeMeaning, VR.LO, "Macular Cube 512x128");
		final DicomElement sqe = new DicomObjectSequenceElement(0x00400260, o,
				Collections.singleton(se1));
		o.add(sqe);

		final DicomAttributeIndex dai0 = new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0");
		final DicomElement ve = dai0.getElement(o);
		
		assertEquals(VR.LO, ve.vr());
		assertEquals(Tag.CodeMeaning, ve.tag());
		assertEquals("Macular Cube 512x128", ve.getString(o.getSpecificCharacterSet(), o.cacheGet()));
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex#getPath(org.dcm4che2.data.DicomObject)}.
	 */
	@Test
	public void testGetPath() {
		final DicomObject dummy = new BasicDicomObject();
		final DicomAttributeIndex dai = new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0");
		assertTrue(Arrays.equals(new Integer[]{0x00400260}, dai.getPath(dummy)));
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex#getString(org.dcm4che2.data.DicomObject)}.
	 */
	@Test
	public void testGetString() throws ConversionFailureException {
		final DicomObject o = new BasicDicomObject();
		final DicomObject se1 = new BasicDicomObject();
		se1.putString(Tag.CodeValue, VR.SH, "SD-S2");
		se1.putString(Tag.CodingSchemeDesignator, VR.SH, "99CZM");
		se1.putString(Tag.CodingSchemeVersion, VR.SH, "1.0");
		se1.putString(Tag.CodeMeaning, VR.LO, "Macular Cube 512x128");
		final DicomElement sqe = new DicomObjectSequenceElement(0x00400260, o,
				Collections.singleton(se1));
		o.add(sqe);

		final DicomAttributeIndex dai0 = new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0");
		assertEquals("Macular Cube 512x128", dai0.getString(o));
	}

	/**
	 * Test method for {@link org.nrg.dcm.xnat.BasicCodedEntryAttributeIndex#getStrings(org.dcm4che2.data.DicomObject)}.
	 */
	@Test
	public void testGetStrings() {
		final DicomObject o = new BasicDicomObject();
		final DicomObject se1 = new BasicDicomObject();
		se1.putString(Tag.CodeValue, VR.SH, "SD-S2");
		se1.putString(Tag.CodingSchemeDesignator, VR.SH, "99CZM");
		se1.putString(Tag.CodingSchemeVersion, VR.SH, "1.0");
		se1.putString(Tag.CodeMeaning, VR.LO, "Macular Cube 512x128");
		final DicomElement sqe = new DicomObjectSequenceElement(0x00400260, o,
				Collections.singleton(se1));
		o.add(sqe);

		final DicomAttributeIndex dai0 = new BasicCodedEntryAttributeIndex(new Integer[]{0x00400260}, "99CZM", "1.0");
		assertTrue(Arrays.equals(new String[]{"Macular Cube 512x128"}, dai0.getStrings(o)));
	}
}
