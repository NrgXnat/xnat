/*
 * DicomEdit: TestFunctions
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Run tests of mapReferencedUIDs method.
 *
 */
public class TestMapReferencedUIDsMethod {

//    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
//    private static final File FILE4 = _resourceManager.getTestResourceFile("dicom/1.2.840.113717.2.21733635.1-3-2-7b04bw.dcm");
//    private static final File FILE4 = _resourceManager.getTestResourceFile("short.dcm");

    /**
     * Test 1 level sequence with multiple items.
     *
     * @throws MizerException
     */
    @Test
    public void testStructureSetROISequence() throws MizerException {

        String script = "uidprefix := \"1.23.4\" \n" +
                "mapReferencedUIDs[ uidprefix, \"(3006, 0020)/(3006, 0024)\"]";

        final DicomObjectI src_dobj = createSeqTestObject();

        assertEquals( "PatientName", src_dobj.getString(s));

        assertEquals( "", src_dobj.getString(s0_1));
        assertEquals( "1", src_dobj.getString(s0_2));
        assertEquals( "1.3.12.2.1107.5.2.6.22750.20111117084936000.0.0.0", src_dobj.getString(s0_3));
        assertEquals( "Tum1_LFront", src_dobj.getString(s0_4));
        assertEquals( "MANUAL", src_dobj.getString(s0_5));

        assertEquals( "", src_dobj.getString(s1_1));
        assertEquals( "2", src_dobj.getString(s1_2));
        assertEquals( "1.3.12.2.1107.5.2.6.22750.20111117084936000.0.0.0", src_dobj.getString(s1_3));
        assertEquals( "Tum2_Vermis", src_dobj.getString(s1_4));
        assertEquals( "MANUAL", src_dobj.getString(s1_5));

        assertEquals( "", src_dobj.getString(s2_1));
        assertEquals( "3", src_dobj.getString(s2_2));
        assertEquals( "1.3.12.2.1107.5.2.6.22750.20111117084936000.0.0.0", src_dobj.getString(s2_3));
        assertEquals( "Tum3_RFront", src_dobj.getString(s2_4));
        assertEquals( "MANUAL", src_dobj.getString(s2_5));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "PatientName", src_dobj.getString(s));

        String expectedUID = result_dobj.getString(s0_3);
        assertEquals( "", result_dobj.getString(s0_1));
        assertEquals( "1", result_dobj.getString(s0_2));
        assertEquals( expectedUID, result_dobj.getString(s0_3));
        assertEquals( "Tum1_LFront", result_dobj.getString(s0_4));
        assertEquals( "MANUAL", result_dobj.getString(s0_5));

        assertEquals( "", result_dobj.getString(s1_1));
        assertEquals( "2", result_dobj.getString(s1_2));
        assertEquals( expectedUID, result_dobj.getString(s1_3));
        assertEquals( "Tum2_Vermis", result_dobj.getString(s1_4));
        assertEquals( "MANUAL", result_dobj.getString(s1_5));

        assertEquals( "", result_dobj.getString(s2_1));
        assertEquals( "3", result_dobj.getString(s2_2));
        assertEquals( expectedUID, result_dobj.getString(s2_3));
        assertEquals( "Tum3_RFront", result_dobj.getString(s2_4));
        assertEquals( "MANUAL", result_dobj.getString(s2_5));

    }


    @Test
    public void testReferencedFrameOfReferenceSequence() throws MizerException {

        String script = "uidprefix := \"1.23.4\" \n" +
                "mapReferencedUIDs[ uidprefix, \"(0008,0018)\"] \n" +
                "mapReferencedUIDs[ uidprefix, \"(0020,000E)\"] \n" +
                "mapReferencedUIDs[ uidprefix, \"(0020,000D)\"] \n" +
                "mapReferencedUIDs[ uidprefix, \"(0020,0052)\"] \n" +
                "mapReferencedUIDs[ uidprefix, \"(3006,0010)/(0020,0052)\"] \n" +
                "mapReferencedUIDs[ uidprefix, \"(3006,0010)/(3006,0012)/(0008,1155)\"] \n" +
                "mapReferencedUIDs[ uidprefix, \"(3006,0010)/(3006,0012)/(3006,0014)/(0020,000E)\"] \n" +
                "mapReferencedUIDs[ uidprefix, \"(3006,0010)/(3006,0012)/(3006,0014)/(3006,0016)/(0008,1155)\"]";

        DicomObjectI img1 = createImageObject1();
        DicomObjectI img2 = createImageObject2();
        DicomObjectI rtstruct = createSeqTestObject();

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        img1 = sa.apply( img1).getDicomObject();
        img2 = sa.apply( img2).getDicomObject();
        rtstruct = sa.apply( rtstruct).getDicomObject();

        assertEquals("PatientName", img1.getString(s));
        assertEquals("PatientName", img2.getString(s));
        assertEquals("PatientName", rtstruct.getString(s));

        // studyInstance UID remapped consistently
        assertNotEquals( "2.25.286464958606692196444730198198599124223", img1.getString( studyInstanceUID) );
        assertEquals( img1.getString(studyInstanceUID), img2.getString(studyInstanceUID));

        // seriesInstance UID remapped consistently
        assertNotEquals( "2.25.298537985984438881272704370944910195420", img1.getString( seriesInstanceUID) );
        assertEquals( img1.getString(seriesInstanceUID), img2.getString(seriesInstanceUID));

        assertEquals( img1.getString( studyInstanceUID), rtstruct.getString(rfors0_3));
        assertEquals( img1.getString( seriesInstanceUID), rtstruct.getString(rfors0_4));
        assertEquals( img1.getString( frameOfReferenceUID), rtstruct.getString(rfors0_1));
        assertEquals( img1.getString( sopClassUID), rtstruct.getString(rfors0_5));
        assertEquals( img1.getString( sopInstanceUID), rtstruct.getString(rfors0_6));
        assertEquals( img2.getString( sopClassUID), rtstruct.getString(rfors0_7));
        assertEquals( img2.getString( sopInstanceUID), rtstruct.getString(rfors0_8));

    }


    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private DicomObjectI createImageObject1() {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(s, "PatientName");

        src_dobj.putString(sopClassUID, "1.2.840.10008.5.1.4.1.1.4");
        src_dobj.putString(sopInstanceUID, "2.25.286464958606692196444730198198599124223");
        src_dobj.putString(studyInstanceUID, "2.25.276185840903524351125970126541016142013");
        src_dobj.putString(seriesInstanceUID, "2.25.298537985984438881272704370944910195420");
        src_dobj.putString(frameOfReferenceUID, "2.25.112897175093649388296539326564566382179");

        return src_dobj;
    }

    private DicomObjectI createImageObject2() {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(s, "PatientName");

        src_dobj.putString(sopClassUID, "1.2.840.10008.5.1.4.1.1.4");
        src_dobj.putString(sopInstanceUID, "2.25.154043429377232606927681981680972422032");
        src_dobj.putString(studyInstanceUID, "2.25.276185840903524351125970126541016142013");
        src_dobj.putString(seriesInstanceUID, "2.25.298537985984438881272704370944910195420");
        src_dobj.putString(frameOfReferenceUID, "2.25.112897175093649388296539326564566382179");

        return src_dobj;
    }

    private DicomObjectI createSeqTestObject() {
        final DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString(s, "PatientName");

        dobj.putString(s0_1, null);
        dobj.putString(s0_2, "1");
        dobj.putString(s0_3, "1.3.12.2.1107.5.2.6.22750.20111117084936000.0.0.0");
        dobj.putString(s0_4, "Tum1_LFront");
        dobj.putString(s0_5, "MANUAL");

        dobj.putString(s1_1, null);
        dobj.putString(s1_2, "2");
        dobj.putString(s1_3, "1.3.12.2.1107.5.2.6.22750.20111117084936000.0.0.0");
        dobj.putString(s1_4, "Tum2_Vermis");
        dobj.putString(s1_5, "MANUAL");

        dobj.putString(s2_1, null);
        dobj.putString(s2_2, "3");
        dobj.putString(s2_3, "1.3.12.2.1107.5.2.6.22750.20111117084936000.0.0.0");
        dobj.putString(s2_4, "Tum3_RFront");
        dobj.putString(s2_5, "MANUAL");

        dobj.putString( rfors0_1, "2.25.112897175093649388296539326564566382179");
        dobj.putString( rfors0_2, "1.2.840.10008.5.1.4.1.1.4");
        dobj.putString( rfors0_3, "2.25.276185840903524351125970126541016142013");
        dobj.putString( rfors0_4, "2.25.298537985984438881272704370944910195420");
        dobj.putString( rfors0_5, "1.2.840.10008.5.1.4.1.1.4");
        dobj.putString( rfors0_6, "2.25.286464958606692196444730198198599124223");
        dobj.putString( rfors0_7, "1.2.840.10008.5.1.4.1.1.4");
        dobj.putString( rfors0_8, "2.25.154043429377232606927681981680972422032");

        return dobj;
    }

    private static int studyInstanceUID = 0x0020000D;
    private static int seriesInstanceUID = 0x0020000E;
    private static int sopInstanceUID = 0x00080018;
    private static int sopClassUID = 0x00080016;
    private static int frameOfReferenceUID = 0x00200052;
    private static int[] s = {0x00100010};

    private static int[] s0_1 = {0x30060020, 0, 0x00201040};  // StructureSetROISequence / PositionReferenceIndicator
    private static int[] s0_2 = {0x30060020, 0, 0x30060022};  // StructureSetROISequence / ROINumber
    private static int[] s0_3 = {0x30060020, 0, 0x30060024};  // StructureSetROISequence / ReferencedFrameOfReferenceUID
    private static int[] s0_4 = {0x30060020, 0, 0x30060026};  // StructureSetROISequence / ROIName
    private static int[] s0_5 = {0x30060020, 0, 0x30060036};  // StructureSetROISequence / ROIGenerationAlgorithm

    private static int[] s1_1 = {0x30060020, 1, 0x00201040};  // StructureSetROISequence / PositionReferenceIndicator
    private static int[] s1_2 = {0x30060020, 1, 0x30060022};  // StructureSetROISequence / ROINumber
    private static int[] s1_3 = {0x30060020, 1, 0x30060024};  // StructureSetROISequence / ReferencedFrameOfReferenceUID
    private static int[] s1_4 = {0x30060020, 1, 0x30060026};  // StructureSetROISequence / ROIName
    private static int[] s1_5 = {0x30060020, 1, 0x30060036};  // StructureSetROISequence / ROIGenerationAlgorithm

    private static int[] s2_1 = {0x30060020, 2, 0x00201040};  // StructureSetROISequence / PositionReferenceIndicator
    private static int[] s2_2 = {0x30060020, 2, 0x30060022};  // StructureSetROISequence / ROINumber
    private static int[] s2_3 = {0x30060020, 2, 0x30060024};  // StructureSetROISequence / ReferencedFrameOfReferenceUID
    private static int[] s2_4 = {0x30060020, 2, 0x30060026};  // StructureSetROISequence / ROIName
    private static int[] s2_5 = {0x30060020, 2, 0x30060036};  // StructureSetROISequence / ROIGenerationAlgorithm

    //                               ReferencedFrameOfReferenceSequence
    private static int[] rfors0_1 = {0x30060010, 0, 0x00200052};   //  FrameOfReferenceUID
    //                                              RTReferencedStudySequence
    private static int[] rfors0_2 = {0x30060010, 0, 0x30060012, 0, 0x00081150};   //   ReferencedSOPClassUID
    private static int[] rfors0_3 = {0x30060010, 0, 0x30060012, 0, 0x00081155};   //   ReferencedSOPInstanceUID
    //                                                             RTReferencedSeriesSequence
    private static int[] rfors0_4 = {0x30060010, 0, 0x30060012, 0, 0x30060014, 0, 0x0020000E};   //  SeriesInstanceUID
    //                                                                            ContourImageSequence
    private static int[] rfors0_5 = {0x30060010, 0, 0x30060012, 0, 0x30060014, 0, 0x30060016, 0, 0x00081150};   //  ReferencedSOPClassUID
    private static int[] rfors0_6 = {0x30060010, 0, 0x30060012, 0, 0x30060014, 0, 0x30060016, 0, 0x00081155};   //  ReferencedSOPInstanceUID
    private static int[] rfors0_7 = {0x30060010, 0, 0x30060012, 0, 0x30060014, 0, 0x30060016, 1, 0x00081150};   //  ReferencedSOPClassUID
    private static int[] rfors0_8 = {0x30060010, 0, 0x30060012, 0, 0x30060014, 0, 0x30060016, 1, 0x00081155};   //  ReferencedSOPInstanceUID
}