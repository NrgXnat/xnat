/*
 * DicomEdit: TestVRs
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.dcm4che2.util.TagUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultSuccess;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.exceptions.MizerException;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Run tests of assignments to all the different VRs.
 *
 * Write it out, read it in.  Any surprises?
 *
 */
public class TestVRs {
    public static final int TAG_AE = 0x21000140;   public static final String AE_VALUE = "seventeencharacte";  // Destination AE
    public static final int TAG_AS = 0x00101010;   public static final String AS_VALUE = "056Y"; // PatientAge
    public static final int TAG_AT = 0x00280808;   public static final String AT_VALUE = "00100010"; // ImageDataLocation
    public static final int TAG_CS = 0x00280040;   public static final String CS_VALUE = "CODE_STRING"; // ImageFormat
    public static final int TAG_DA = 0x00080012;   public static final String DA_VALUE = "19600519"; // InstanceCreationDate
    public static final int TAG_DS = 0x30060044;   public static final String DS_VALUE = "-3.14159"; // ContourSlabThickness
    public static final int TAG_DT = 0x0040A120;   public static final String DT_VALUE = "20160825225530.23+0600"; // DateTime
    public static final int TAG_FL = 0x00700262;   public static final String FL_VALUE = "3.333"; // DiameterOfVisibility
                                                   public static final float  FL_NUMBER = 3.333f;
    public static final int TAG_FD = 0x00189087;   public static final String FD_VALUE = "3.333333"; // Difusion b-value
                                                   public static final double FD_NUMBER = 3.333333;

    public static final int TAG_IS = 0x00183105;   public static final String IS_VALUE  = "-666"; // Lesion Number
                                                   public static final int    IS_NUMBER = -666;
                                                   public static final String IS_VALUE_AS_FLOAT = "-666.0";
    public static final int TAG_LO = 0x300A033A;   public static final String LO_VALUE = "Long string."; // LateralSpreadingDeviceDescription
    public static final int TAG_LT = 0x00184000;   public static final String LT_VALUE = "Looooong text."; // AcquistionComments
    public static final int TAG_OB = 0x04000520;   public static final String OB_VALUE = "New Institute"; // Encrypted content
                                                   public static final String OB_EXPECTED_VALUE = "4E\\65\\77\\20\\49\\6E\\73\\74\\69\\74\\75\\74\\65\\00";
//    public static final int TAG_OD = 0x?  ;        public static final String OD_VALUE = "New Institute"; // none?
    public static final int TAG_OF = 0x00660016;   public static final String OF_VALUE = "6.66"; // PointsCoordinateData
                                                   public static final String OF_EXPECTED_VALUE = "6.66";
    public static final int TAG_OW = 0x00660023;   public static final String OW_VALUE = "New Institute"; // TrianglePointIndexList
                                                   public static final String OW_EXPECTED_VALUE = "25934\\8311\\28233\\29811\\29801\\29813\\101";
    public static final int TAG_SH = 0x00181160;   public static final String SH_VALUE = "New Institute"; // FilterType
    public static final int TAG_SL = 0x00186020;   public static final String SL_VALUE = "-999"; // ReferencePixelX0
    public static final int TAG_SQ = 0x300A00B0;   public static final String SQ_VALUE = "New Institute"; // BeamSequence
    public static final int TAG_SS = 0x300A0306;   public static final String SS_VALUE = "-333"; // RadiationChargeState
    public static final int TAG_ST = 0x00080092;   public static final String ST_VALUE = "New Institute"; // ReferringPhysicianAddress
    public static final int TAG_TM = 0x00100032;   public static final String TM_VALUE = "234511.123456"; // PatientBirthTime
    public static final int TAG_UI = 0x0020000E;   public static final String UI_VALUE = "1.2.3.4.5.6.7.8.9"; // SeriesInstanceUID
    public static final int TAG_UL = 0x00081161;   public static final String UL_VALUE = "666"; // SimpleFrameList
    public static final int TAG_UN = 0x66666666;   public static final String UN_VALUE = "New Institute", UN_EXPECTED_VALUE = "New Institute\\00"; // Unknown Tag
    public static final int TAG_US = 0x00280106;   public static final String US_VALUE = "666"; // SmallestImagePixelValue
    public static final int TAG_UT = 0x00400602;   public static final String UT_VALUE = "New Institute"; // SpecimenDetailedDescription

    public String assignmentStatementString( int tag, String value) {
        return String.format("%s := \"%s\"", TagUtils.toString(tag), value);
    }

    public String assignmentStatementFloat( int tag, Number value) {
        return String.format("%s := %f", TagUtils.toString(tag), value.floatValue());
    }

    @Test
    public void testAssignToAEFromString() throws MizerException {
        assignFromString( TAG_AE, AE_VALUE);
    }

    @Test
    public void testAssignToASFromString() throws MizerException {
        assignFromString( TAG_AS, AS_VALUE);
   }

    /**
     * Test writing/reading VR AT (Attribute Tag) from String.
     *
     * @throws MizerException When an error occurs processing the DICOM object.
     */
    @Test
    public void testAssignToATFromString() throws MizerException {
        assignFromString( TAG_AT, AT_VALUE);
    }

    @Test
    public void testAssignToCSFromString() throws MizerException {
        assignFromString( TAG_CS, CS_VALUE);
    }

    @Test
    public void testAssignToDAFromString() throws MizerException {
        assignFromString( TAG_DA, DA_VALUE);
    }

    @Test
    public void testAssignToDSFromString() throws MizerException {
        assignFromString( TAG_DS, DS_VALUE);
    }

    @Test
    public void testAssignToDTFromString() throws MizerException {
        assignFromString( TAG_DT, DT_VALUE);
    }

    @Test
    public void testAssignToFLFromFLString() throws MizerException {
        assignFromString( TAG_FL, FL_VALUE);
    }
    @Test
    public void testAssignToFLFromFLNumber() throws MizerException {
        assignFromNumber( TAG_FL, FL_NUMBER, FL_VALUE);
    }

    @Test
    public void testAssignToFDFromFDString() throws MizerException {
        assignFromString( TAG_FD, FD_VALUE);
    }

    @Test
    public void testAssignToFDFromFDNumber() throws MizerException {
        assignFromNumber( TAG_FD, FD_NUMBER, FD_VALUE);
    }

    @Test
    public void testAssignToFDFromFLString() throws MizerException {
        assignFromString( TAG_FD, FL_VALUE);
    }
    @Test
    public void testAssignToFDFromFLString2() throws MizerException {
        assignFromString( 0x00189182, "30.0");
    }

    @Test
    public void testAssignToFDFromFLNumber() throws MizerException {
        assignFromNumber( TAG_FD, FL_NUMBER, FL_VALUE);
    }

    @Test
    public void testAssignToFDFromISString() throws MizerException {
        assignFromString( TAG_FD, IS_VALUE, IS_VALUE_AS_FLOAT);
    }

    @Test
    public void testAssignToFDFromISNumber() throws MizerException {
        assignFromNumber( TAG_FD, IS_NUMBER, IS_VALUE_AS_FLOAT);
    }

    @Test
    public void testAssignToISFromString() throws MizerException {
        assignFromString( TAG_IS, IS_VALUE);
    }

    @Test
    public void testAssignToLOFromString() throws MizerException {
        assignFromString( TAG_LO, LO_VALUE);
    }

    @Test
    public void testAssignToLTFromString() throws MizerException {
        assignFromString( TAG_LT, LT_VALUE);
    }

    @Test
    public void testAssignToOBFromString() throws MizerException {
        assignFromString(TAG_OB, OB_VALUE, OB_EXPECTED_VALUE);
    }

    @Test
    @Ignore
    public void testAssignToOFFromString() throws MizerException {
        assignFromString(TAG_OF, OF_VALUE, OF_EXPECTED_VALUE);
    }

    @Test
    @Ignore
    public void testAssignToOWFromString() throws MizerException {
        assignFromString(TAG_OW, OW_VALUE, OW_EXPECTED_VALUE);
    }

    @Test
    @Ignore
    public void testAssignToSHFromString() throws MizerException {
        assignFromString( TAG_SH, SH_VALUE);
    }

    @Test
    public void testAssignToSLFromString() throws MizerException {
        assignFromString( TAG_SL, SL_VALUE);
    }

    /**
     * tags with VR = SQ are added but do not have values.
     * @throws MizerException
     */
    @Test
    public void testAssignToSQFromString() throws MizerException {
        final DicomObjectI dobj = DicomObjectFactory.newInstance();

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(assignmentStatementString(TAG_SQ, SQ_VALUE)));
        AnonymizationResult result = sa.apply(dobj);
        assertTrue(result instanceof AnonymizationResultSuccess);
        assertTrue(dobj.contains(TAG_SQ));
    }

    @Test
    public void testAssignToSSFromString() throws MizerException {
        assignFromString( TAG_SS, SS_VALUE);
    }

    @Test
    public void testAssignToSTFromString() throws MizerException {
        assignFromString( TAG_ST, ST_VALUE);
    }

    @Test
    public void testAssignToTMFromString() throws MizerException {
        assignFromString( TAG_TM, TM_VALUE);
    }

    @Test
    public void testAssignToUIFromString() throws MizerException {
        assignFromString( TAG_UI, UI_VALUE);
    }

    @Test
    public void testAssignToULFromString() throws MizerException {
        assignFromString( TAG_UL, UL_VALUE);
    }

    /**
     * Test writing/reading to UNKNOWN VR from String.
     *
     *  Expected :New Institute\00
     *  Actual   :New Institute
     *  The length is padded to be of even length when written and reading doesn't know the real intended length.
     */
    @Test
    public void testAssignToUNFromString() throws MizerException {
        assignFromString( TAG_UN, UN_VALUE, UN_EXPECTED_VALUE);
    }

    @Test
    public void testAssignToUSFromString() throws MizerException {
        assignFromString( TAG_US, US_VALUE);
    }

    @Test
    public void testAssignToUTFromString() throws MizerException {
        assignFromString( TAG_UT, UT_VALUE);
    }


    public void assignFromString(int tag, String value) throws MizerException {
        assignFromString( tag, value, value);
    }

    public void assignFromString(int tag, String value, String expectedValue) throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        final BaseScriptApplicator sa          = BaseScriptApplicator.getInstance( bytes(assignmentStatementString(tag, value)));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        result_dobj.addMetaHeader();
        try {
            final File file = File.createTempFile("dcmTest", "dcm");
            try (final OutputStream os = new FileOutputStream(file)) {
                result_dobj.write(os);
            }

            try ( FileInputStream is = new FileInputStream( file)) {
                final DicomObjectI dicomObject = DicomObjectFactory.newInstance(is);
                assertEquals( expectedValue, dicomObject.getString(tag));
            }
        } catch (IOException e) {
            throw new MizerException(e);
        }
    }

    public void assignFromNumber(int tag, Number value, String expectedValueAsString) throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        final BaseScriptApplicator sa          = BaseScriptApplicator.getInstance( bytes(assignmentStatementFloat(tag, value)));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        result_dobj.addMetaHeader();
        try {
            final File file = File.createTempFile("dcmTest", "dcm");
            try (final OutputStream os = new FileOutputStream(file)) {
                result_dobj.write(os);
            }

            try ( FileInputStream is = new FileInputStream( file)) {
                final DicomObjectI dicomObject = DicomObjectFactory.newInstance(is);
                assertEquals( expectedValueAsString, dicomObject.getString(tag));
            }
        } catch (IOException e) {
            throw new MizerException(e);
        }
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
