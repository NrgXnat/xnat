/*
 * DicomEdit: TestAssignToNumericVRs
 * XNAT http://www.xnat.org
 * Copyright (c) 2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.test.workers.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.junit.Assert.*;

/**
 * Test assignment to numeric VRs.
 *
 */
public class TestAssignToNumericVRs {

    private static final Logger logger = LoggerFactory.getLogger(TestAssignToNumericVRs.class);

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();

    private static final File de11 = _resourceManager.getTestResourceFile("dicom/IM_0001");

    private static final String S_ASSIGN_FLOAT = "(0018,9182) := \"30.0\"\n";
    private static final String S_ASSIGN_FLOAT_FROM_NONFLOAT = "(0018,9182) := \"fubar\"\n";

    // (0010,21C0) US #2 [4] PregnancyStatus
    private static final String S_ASSIGN_US = "(0010,21C0) := \"20\"\n";
    private static final String S_ASSIGN_US_FROM_NON_NUMERIC = "(0010,21C0) := \"fubar\"\n";
    private static final String S_ASSIGN_US_FROM_OUT_OF_RANGE = "(0010,21C0) := \"1234567\"\n";

    @Test
    public void testAssignVRFloat() throws Exception {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance( de11);
        assertTrue(src_dobj.contains(0x00189182));
        assertEquals("26.0802056567773", src_dobj.getString( 0x00189182));
        final BaseScriptApplicator applicator    = BaseScriptApplicator.getInstance( bytes( S_ASSIGN_FLOAT));
        final DicomObjectI result_dobj = applicator.apply(src_dobj).getDicomObject();
        assertEquals("30", result_dobj.getString( 0x00189182));
    }

    @Test
    public void testAssignVRFloatFromNonFloatString() throws Exception {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(de11);
        assertTrue(src_dobj.contains(0x00189182));
        assertEquals("26.0802056567773", src_dobj.getString(0x00189182));
        final BaseScriptApplicator applicator = BaseScriptApplicator.getInstance( bytes(S_ASSIGN_FLOAT_FROM_NONFLOAT));
        final AnonymizationResult result = applicator.apply(src_dobj);

        assertTrue( result instanceof AnonymizationResultError);
    }

    @Test
    public void testAssignVRInteger() throws Exception {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance( de11);
        assertTrue(src_dobj.contains(0x001021C0));
        assertEquals("4", src_dobj.getString( 0x001021C0));
        final BaseScriptApplicator applicator    = BaseScriptApplicator.getInstance( bytes( S_ASSIGN_US));
        final DicomObjectI result_dobj = applicator.apply(src_dobj).getDicomObject();
        assertEquals("20", result_dobj.getString( 0x001021C0));
    }

    @Test
    public void testAssignVR_USFromNonIntegerString() throws Exception {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(de11);
        assertTrue(src_dobj.contains(0x001021C0));
        assertEquals("4", src_dobj.getString(0x001021C0));
        final BaseScriptApplicator applicator = BaseScriptApplicator.getInstance( bytes(S_ASSIGN_US_FROM_NON_NUMERIC));
        final AnonymizationResult result = applicator.apply(src_dobj);

        assertTrue( result instanceof AnonymizationResultError);
    }

    // TODO: This silently truncates the supplied value - is this an issue?
    @Test
    public void testAssignVR_USFromOutOfRangeIntegerString() throws Exception {
        try {
            final DicomObjectI src_dobj = DicomObjectFactory.newInstance(de11);
            assertTrue(src_dobj.contains(0x001021C0));
            assertEquals("4", src_dobj.getString(0x001021C0));
            final BaseScriptApplicator applicator = BaseScriptApplicator.getInstance( bytes(S_ASSIGN_US_FROM_OUT_OF_RANGE));
            final DicomObjectI result_dobj = applicator.apply(src_dobj).getDicomObject();
//            assertEquals("1234567", result_dobj.getString( 0x001021C0));
            assertEquals("54919", result_dobj.getString( 0x001021C0));
//            fail("Expected failure to parse int from out of range string.");
        }
        catch (Exception e) {
            assertTrue( e instanceof NumberFormatException);
        }
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
