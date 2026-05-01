/*
 * dicom-xnat-sop: org.nrg.dcm.SOPModelTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.dcm4che2.data.UID;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class SOPModelTest {

	/**
	 * Test method for {@link org.nrg.dcm.SOPModel#getScanType(java.lang.String)}.
	 */
	@Test
	public void testGetScanType() {
		assertEquals("mrScanData", SOPModel.getScanType(UID.MRImageStorage));
		assertEquals("mrScanData", SOPModel.getScanType(UID.EnhancedMRImageStorage));
		
		assertEquals("petScanData", SOPModel.getScanType(UID.PositronEmissionTomographyImageStorage));
		assertEquals("petScanData", SOPModel.getScanType(UID.EnhancedPETImageStorage));
		
		assertEquals("ctScanData", SOPModel.getScanType(UID.CTImageStorage));
		assertEquals("ctScanData", SOPModel.getScanType(UID.EnhancedCTImageStorage));
		
		assertEquals("xaScanData", SOPModel.getScanType(UID.XRayAngiographicImageStorage));
		
		assertEquals("usScanData", SOPModel.getScanType(UID.UltrasoundImageStorage));
		assertEquals("usScanData", SOPModel.getScanType(UID.UltrasoundMultiFrameImageStorage));
		
		assertEquals("rtImageScanData", SOPModel.getScanType(UID.RTImageStorage));
		assertEquals("rtImageScanData", SOPModel.getScanType(UID.RTDoseStorage));

		assertEquals("crScanData", SOPModel.getScanType(UID.ComputedRadiographyImageStorage));
		
		assertEquals("optScanData", SOPModel.getScanType(UID.OphthalmicTomographyImageStorage));

                assertEquals("smScanData", SOPModel.getScanType(UID.VLWholeSlideMicroscopyImageStorage));
		
		assertEquals("scScanData", SOPModel.getScanType(UID.SecondaryCaptureImageStorage));
		
		assertEquals("nmScanData", SOPModel.getScanType(UID.NuclearMedicineImageStorage));
		assertEquals("segScanData", SOPModel.getScanType(UID.SegmentationStorage));
		assertEquals("srScanData", SOPModel.getScanType(UID.BasicTextSRStorage));
		assertEquals("srScanData", SOPModel.getScanType(UID.EnhancedSRStorage));
		assertEquals("otherDicomScanData", SOPModel.getScanType(UID.SiemensCSANonImageStorage));

		assertEquals(null, SOPModel.getScanType("NoSuchSOPClass"));
		
		assertEquals("ctScanData", SOPModel.getScanType(UID.CTImageStorage, UID.SecondaryCaptureImageStorage));
		assertEquals("petScanData", SOPModel.getScanType(UID.PositronEmissionTomographyImageStorage, UID.CTImageStorage));
	}
	

	@Test
	public void testIsPrimaryImagingType() {
		assertTrue(SOPModel.isPrimaryImagingSOP(UID.MRImageStorage));
		assertTrue(SOPModel.isPrimaryImagingSOP(UID.CTImageStorage));
		assertFalse(SOPModel.isPrimaryImagingSOP(UID.SecondaryCaptureImageStorage));
	}
	
	@Test
	public void testGetLeadModality() {
		assertEquals("MR", SOPModel.getLeadModality(ImmutableSet.of("MR", "SC", "OT")));
		assertEquals("PT", SOPModel.getLeadModality(ImmutableSet.of("MR", "PT")));
		assertNull(SOPModel.getLeadModality(Collections.<String>emptySet()));
	}
		
	@Test
	public void testGetModalityToSessionTypes() {
	    final Map<String,String> m = SOPModel.getModalityToSessionTypes();
	    assertEquals("mrSessionData", m.get("MR"));
	    assertEquals("petSessionData", m.get("PT"));
	    assertEquals("smSessionData", m.get("SM"));
	    assertEquals("gmSessionData", m.get("GM"));
	    assertNull(m.get("PET"));
	}
	
	@Test
	public void testGetSOPClassToSessionTypes() {
	    final Map<String,String> m = SOPModel.getSOPClassToSessionTypes();
        assertEquals("mrSessionData", m.get(UID.EnhancedMRImageStorage));
        assertEquals("petSessionData", m.get(UID.PositronEmissionTomographyImageStorage));
        assertEquals("smSessionData", m.get(UID.VLWholeSlideMicroscopyImageStorage));
        assertEquals("gmSessionData", m.get(UID.VLMicroscopicImageStorage));
        assertNull(m.get(UID.ExplicitVRBigEndian));
	}
	
	@Test
	public void testGetSessionType() {
	    // assertEquals("petSessionData", OldSOPModel.getSessionType(Arrays.asList(UID.MRImageStorage, UID.PositronEmissionTomographyImageStorage)));
	    assertEquals("petSessionData", SOPModel.getSessionType(Arrays.asList(UID.MRImageStorage, UID.PositronEmissionTomographyImageStorage)));
	    assertEquals("mrSessionData", SOPModel.getSessionType(Collections.singleton(UID.EnhancedMRColorImageStorage)));
	    assertEquals("smSessionData", SOPModel.getSessionType(Collections.singleton(UID.VLWholeSlideMicroscopyImageStorage)));
	    assertNull(SOPModel.getSessionType(Collections.singleton(UID.ImplicitVRLittleEndian)));
	    assertNull(SOPModel.getSessionType(Collections.<String>emptySet()));
	}
}

