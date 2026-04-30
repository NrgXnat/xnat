package org.nrg.dicom.mizer.objects;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.nrg.dicom.mizer.TestUtilTags.*;

/**
 * The file dicom/vr.dcm has random tags representing all the VRs.
 * dcm4ch2 does not know about VRs:  OD, OL, OV, SV, UC, UV
 * <p>
 * Note that (6666,0666) is a tag not in the dictionary but with a provided VR.
 * <p>
 * (0002,0000) UL #4 [44] FileMetaInformationGroupLength
 * (0002,0002) UI #0 [] MediaStorageSOPClassUID
 * (0002,0003) UI #0 [] MediaStorageSOPInstanceUID
 * (0002,0010) UI #20 [1.2.840.10008.1.2.1] TransferSyntaxUID
 * (0008,0005) CS #12 [codedString] SpecificCharacterSet
 * (0008,0013) TM #6 [132433] InstanceCreationTime
 * (0008,0020) DA #8 [19600519] StudyDate
 * (0008,002A) DT #14 [19600519132115] AcquisitionDateTime
 * (0008,0054) AE #8 [AETitle1] RetrieveAETitle
 * (0008,0117) UI #10 [1.2.3.4.5] ContextUID
 * (0008,0119) UC #20 [unlimitedCharacters] LongCodeValue
 * (0008,0120) UR #18 [https://org.snafu] URNCodeValue
 * (0008,1160) IS #6 [12345] ReferencedFrameNumber
 * (0008,1161) UL #4 [22222] SimpleFrameList
 * (0008,2130) DS #4 [-42] EventElapsedTimes
 * (0008,2132) LO #10 [longString] EventTimerNames
 * (0008,2134) FD #8 [6.28] EventTimeOffset
 * (0008,9459) FL #4 [3.14] RecommendedDisplayFrameRateInFloat
 * (0010,0010) PN #8 [Doe^John] PatientName
 * (0010,1010) AS #4 [064Y] PatientAge
 * (0010,2154) SH #12 [shortString] PatientTelephoneNumbers
 * (0010,21C0) US #2 [42] PregnancyStatus
 * (0014,0028) ST #10 [shortText] ComponentManufacturer
 * (0014,3080) OB #6 [-94\-40\94\-84\28\-83] BadPixelImage
 * (0020,4000) LT #8 [longText] ImageComments
 * (0020,5000) AT #4 [00204000] OriginalImageIdentification
 * (0028,1201) OW #0 [] RedPaletteColorLookupTableData
 * (0028,7FE0) UR #18 [https://org.snafu] PixelDataProviderURL
 * (0040,A160) UT #14 [unlimitedText] TextValue
 * (0064,0009) OF #4 [1.2] VectorGridData
 * (0072,0073) OD #4 [] SelectorODValue
 * (0072,0075) OL #4 [-1] SelectorOLValue
 * (0072,007C) SL #4 [-1] SelectorSLValue
 * (0072,007E) SS #2 [-134] SelectorSSValue
 * (0072,0081) OV #8 [1] SelectorOVValue
 * (0072,0082) SV #8 [123456] SelectorSVValue
 * (0072,0083) UV #8 [1111] SelectorUVValue
 * (6666,0666) LO #26 [unknown_tag_with_known_VR]
 * (6666,6666) UN #4 [-70\121\39\0]
 */
public class TestVR {

    /**
     * Read the vr.dcm file and check the vrs
     */
    @Test
    public void testInitialVRs() throws Exception {
        ClassLoader loader = TestVR.class.getClassLoader();
        File file = new File(loader.getResource("dicom/vr.dcm").toURI());
        DicomObjectI dobj = DicomObjectFactory.newInstance(file);

        assertEquals("AE", dobj.get(AE_VM1.tag).getVRAsString());
        assertEquals("AS", dobj.get(AS_VM1.tag).getVRAsString());
        assertEquals("AT", dobj.get(AT_VM1.tag).getVRAsString());
        assertEquals("CS", dobj.get(CS_VM1.tag).getVRAsString());
        assertEquals("DA", dobj.get(DA_VM1.tag).getVRAsString());
        assertEquals("DS", dobj.get(DS_VM1.tag).getVRAsString());
        assertEquals("DT", dobj.get(DT_VM1.tag).getVRAsString());
        assertEquals("FD", dobj.get(FD_VM1.tag).getVRAsString());
        assertEquals("FL", dobj.get(FL_VM1.tag).getVRAsString());
        assertEquals("IS", dobj.get(IS_VM1.tag).getVRAsString());
        assertEquals("LO", dobj.get(LO_VM1.tag).getVRAsString());
        assertEquals("LT", dobj.get(LT_VM1.tag).getVRAsString());
        assertEquals("OB", dobj.get(OB_VM1.tag).getVRAsString());
        assertEquals("OD", dobj.get(OD_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("OF", dobj.get(OF_VM1.tag).getVRAsString());
        assertEquals("OL", dobj.get(OL_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("OV", dobj.get(OV_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("OW", dobj.get(OW_VM1.tag).getVRAsString());
        assertEquals("PN", dobj.get(PN_VM1.tag).getVRAsString());
        assertEquals("SH", dobj.get(SH_VM1.tag).getVRAsString());
        assertEquals("SL", dobj.get(SL_VM1.tag).getVRAsString());
        assertEquals("SS", dobj.get(SS_VM1.tag).getVRAsString());
//        assertEquals( "SQ", dobj.get(sqTag.tag).getVRAsString());
        assertEquals("ST", dobj.get(ST_VM1.tag).getVRAsString());
        assertEquals("SV", dobj.get(SV_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("TM", dobj.get(TM_VM1.tag).getVRAsString());
        assertEquals("UC", dobj.get(UC_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("UI", dobj.get(UI_VM1.tag).getVRAsString());
        assertEquals("UL", dobj.get(UL_VM1.tag).getVRAsString());
        assertEquals("UN", dobj.get(UN_VM1.tag).getVRAsString());
        assertEquals("UR", dobj.get(UR_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("US", dobj.get(US_VM1.tag).getVRAsString());
        assertEquals("UT", dobj.get(UT_VM1.tag).getVRAsString());
        assertEquals("UV", dobj.get(UV_VM1.tag).getVRAsString());  // retrieved as UN

        assertEquals("LO", dobj.get(MYSTERY_TAG_KNOWN_VR.tag).getVRAsString());

    }

    /**
     * Replace values in existing tags. The VR should not change.
     *
     * @throws Exception
     */
    @Test
    public void testDontChangeExistingVR() throws Exception {
        ClassLoader loader = TestVR.class.getClassLoader();
        File file = new File(loader.getResource("dicom/vr.dcm").toURI());
        DicomObjectI dobj = DicomObjectFactory.newInstance(file);

        dobj.putString(AE_VM1.tag, AE_VM1.postValue);
        dobj.putString(AS_VM1.tag, AS_VM1.postValue);
        dobj.putString(AT_VM1.tag, AT_VM1.postValue);
        dobj.putString(CS_VM1.tag, CS_VM1.postValue);
        dobj.putString(DA_VM1.tag, DA_VM1.postValue);
        dobj.putString(DS_VM1.tag, DS_VM1.postValue);
        dobj.putString(DT_VM1.tag, DT_VM1.postValue);
        dobj.putString(FD_VM1.tag, FD_VM1.postValue);
        dobj.putString(FL_VM1.tag, FL_VM1.postValue);
        dobj.putString(IS_VM1.tag, IS_VM1.postValue);
        dobj.putString(LO_VM1.tag, LO_VM1.postValue);
        dobj.putString(LT_VM1.tag, LT_VM1.postValue);
        dobj.putString(OB_VM1.tag, OB_VM1.postValue);
        dobj.putString(OD_VM1.tag, OD_VM1.postValue);
        dobj.putString(OF_VM1.tag, OF_VM1.postValue);
        dobj.putString(OL_VM1.tag, OL_VM1.postValue);
        dobj.putString(OV_VM1.tag, OV_VM1.postValue);
        dobj.putString(OW_VM1.tag, OW_VM1.postValue);
        dobj.putString(PN_VM1.tag, PN_VM1.postValue);
        dobj.putString(SH_VM1.tag, SH_VM1.postValue);
        dobj.putString(SL_VM1.tag, SL_VM1.postValue);
//        dobj.putString( sqTag.tag, sqTag.postValue);
        dobj.putString(SS_VM1.tag, SS_VM1.postValue);
        dobj.putString(ST_VM1.tag, ST_VM1.postValue);
        dobj.putString(SV_VM1.tag, SV_VM1.postValue);
        dobj.putString(TM_VM1.tag, TM_VM1.postValue);
        dobj.putString(UC_VM1.tag, UC_VM1.postValue);
        dobj.putString(UI_VM1.tag, UI_VM1.postValue);
        dobj.putString(UL_VM1.tag, UL_VM1.postValue);
        dobj.putString(UN_VM1.tag, UN_VM1.postValue);
        dobj.putString(UR_VM1.tag, UR_VM1.postValue);
        dobj.putString(US_VM1.tag, US_VM1.postValue);
        dobj.putString(UT_VM1.tag, UT_VM1.postValue);
        dobj.putString(UV_VM1.tag, UV_VM1.postValue);
        dobj.putString(MYSTERY_TAG_KNOWN_VR.tag, MYSTERY_TAG_KNOWN_VR.postValue);

        assertEquals("AE", dobj.get(AE_VM1.tag).getVRAsString());
        assertEquals("AS", dobj.get(AS_VM1.tag).getVRAsString());
        assertEquals("AT", dobj.get(AT_VM1.tag).getVRAsString());
        assertEquals("CS", dobj.get(CS_VM1.tag).getVRAsString());
        assertEquals("DA", dobj.get(DA_VM1.tag).getVRAsString());
        assertEquals("DS", dobj.get(DS_VM1.tag).getVRAsString());
        assertEquals("DT", dobj.get(DT_VM1.tag).getVRAsString());
        assertEquals("FD", dobj.get(FD_VM1.tag).getVRAsString());
        assertEquals("FL", dobj.get(FL_VM1.tag).getVRAsString());
        assertEquals("IS", dobj.get(IS_VM1.tag).getVRAsString());
        assertEquals("LO", dobj.get(LO_VM1.tag).getVRAsString());
        assertEquals("LT", dobj.get(LT_VM1.tag).getVRAsString());
        assertEquals("OB", dobj.get(OB_VM1.tag).getVRAsString());
        assertEquals("OD", dobj.get(OD_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("OF", dobj.get(OF_VM1.tag).getVRAsString());
        assertEquals("OL", dobj.get(OL_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("OV", dobj.get(OV_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("OW", dobj.get(OW_VM1.tag).getVRAsString());
        assertEquals("PN", dobj.get(PN_VM1.tag).getVRAsString());
        assertEquals("SH", dobj.get(SH_VM1.tag).getVRAsString());
        assertEquals("SL", dobj.get(SL_VM1.tag).getVRAsString());
        assertEquals("SS", dobj.get(SS_VM1.tag).getVRAsString());
//        assertEquals( "SQ", dobj.get(sqTag.tag).getVRAsString());
        assertEquals("ST", dobj.get(ST_VM1.tag).getVRAsString());
        assertEquals("SV", dobj.get(SV_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("TM", dobj.get(TM_VM1.tag).getVRAsString());
        assertEquals("UC", dobj.get(UC_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("UI", dobj.get(UI_VM1.tag).getVRAsString());
        assertEquals("UL", dobj.get(UL_VM1.tag).getVRAsString());
        assertEquals("UN", dobj.get(UN_VM1.tag).getVRAsString());
        assertEquals("UR", dobj.get(UR_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("US", dobj.get(US_VM1.tag).getVRAsString());
        assertEquals("UT", dobj.get(UT_VM1.tag).getVRAsString());
        assertEquals("UV", dobj.get(UV_VM1.tag).getVRAsString());  // retrieved as UN

        assertEquals("LO", dobj.get(MYSTERY_TAG_KNOWN_VR.tag).getVRAsString());
    }

    @Test
    public void testVRofNewTags() throws Exception {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString(AE_VM1.tag, AE_VM1.initialValue);
        dobj.putString(AS_VM1.tag, AS_VM1.initialValue);
        dobj.putString(AT_VM1.tag, AT_VM1.postValue);
        dobj.putString(CS_VM1.tag, CS_VM1.initialValue);
        dobj.putString(DA_VM1.tag, DA_VM1.initialValue);
        dobj.putString(DS_VM1.tag, DS_VM1.initialValue);
        dobj.putString(DT_VM1.tag, DT_VM1.initialValue);
        dobj.putString(FD_VM1.tag, FD_VM1.initialValue);
        dobj.putString(FL_VM1.tag, FL_VM1.initialValue);
        dobj.putString(IS_VM1.tag, IS_VM1.initialValue);
        dobj.putString(LO_VM1.tag, LO_VM1.initialValue);
        dobj.putString(LT_VM1.tag, LT_VM1.initialValue);
        dobj.putString(OB_VM1.tag, OB_VM1.initialValue);
        dobj.putString(OD_VM1.tag, OD_VM1.initialValue);
        dobj.putString(OF_VM1.tag, OF_VM1.initialValue);
        dobj.putString(OL_VM1.tag, OL_VM1.initialValue);
        dobj.putString(OV_VM1.tag, OV_VM1.initialValue);
        dobj.putString(OW_VM1.tag, OW_VM1.initialValue);
        dobj.putString(PN_VM1.tag, PN_VM1.initialValue);
        dobj.putString(SH_VM1.tag, SH_VM1.initialValue);
        dobj.putString(SL_VM1.tag, SL_VM1.initialValue);
//        dobj.putString( sqTag.tag, sqTag.initialValue);
        dobj.putString(SS_VM1.tag, SS_VM1.initialValue);
        dobj.putString(ST_VM1.tag, ST_VM1.initialValue);
        dobj.putString(SV_VM1.tag, SV_VM1.initialValue);
        dobj.putString(TM_VM1.tag, TM_VM1.initialValue);
        dobj.putString(UC_VM1.tag, UC_VM1.initialValue);
        dobj.putString(UI_VM1.tag, UI_VM1.initialValue);
        dobj.putString(UL_VM1.tag, UL_VM1.initialValue);
        dobj.putString(UN_VM1.tag, UN_VM1.initialValue);
        dobj.putString(UR_VM1.tag, UR_VM1.initialValue);
        dobj.putString(US_VM1.tag, US_VM1.initialValue);
        dobj.putString(UT_VM1.tag, UT_VM1.initialValue);
        dobj.putString(UV_VM1.tag, UV_VM1.initialValue);
        dobj.putString(MYSTERY_TAG_KNOWN_VR.tag, MYSTERY_TAG_KNOWN_VR.initialValue);

        assertEquals("AE", dobj.get(AE_VM1.tag).getVRAsString());
        assertEquals("AS", dobj.get(AS_VM1.tag).getVRAsString());
        assertEquals("AT", dobj.get(AT_VM1.tag).getVRAsString());
        assertEquals("CS", dobj.get(CS_VM1.tag).getVRAsString());
        assertEquals("DA", dobj.get(DA_VM1.tag).getVRAsString());
        assertEquals("DS", dobj.get(DS_VM1.tag).getVRAsString());
        assertEquals("DT", dobj.get(DT_VM1.tag).getVRAsString());
        assertEquals("FD", dobj.get(FD_VM1.tag).getVRAsString());
        assertEquals("FL", dobj.get(FL_VM1.tag).getVRAsString());
        assertEquals("IS", dobj.get(IS_VM1.tag).getVRAsString());
        assertEquals("LO", dobj.get(LO_VM1.tag).getVRAsString());
        assertEquals("LT", dobj.get(LT_VM1.tag).getVRAsString());
        assertEquals("OB", dobj.get(OB_VM1.tag).getVRAsString());
        assertEquals("OD", dobj.get(OD_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("OF", dobj.get(OF_VM1.tag).getVRAsString());
        assertEquals("OL", dobj.get(OL_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("OV", dobj.get(OV_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("OW", dobj.get(OW_VM1.tag).getVRAsString());
        assertEquals("PN", dobj.get(PN_VM1.tag).getVRAsString());
        assertEquals("SH", dobj.get(SH_VM1.tag).getVRAsString());
        assertEquals("SL", dobj.get(SL_VM1.tag).getVRAsString());
        assertEquals("SS", dobj.get(SS_VM1.tag).getVRAsString());
//        assertEquals( "SQ", dobj.get(sqTag.tag).getVRAsString());
        assertEquals("ST", dobj.get(ST_VM1.tag).getVRAsString());
        assertEquals("SV", dobj.get(SV_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("TM", dobj.get(TM_VM1.tag).getVRAsString());
        assertEquals("UC", dobj.get(UC_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("UI", dobj.get(UI_VM1.tag).getVRAsString());
        assertEquals("UL", dobj.get(UL_VM1.tag).getVRAsString());
        assertEquals("UN", dobj.get(UN_VM1.tag).getVRAsString());
        assertEquals("UR", dobj.get(UR_VM1.tag).getVRAsString());  // retrieved as UN
        assertEquals("US", dobj.get(US_VM1.tag).getVRAsString());
        assertEquals("UT", dobj.get(UT_VM1.tag).getVRAsString());
        assertEquals("UV", dobj.get(UV_VM1.tag).getVRAsString());  // retrieved as UN

        assertEquals("UN", dobj.get(MYSTERY_TAG_KNOWN_VR.tag).getVRAsString());
    }
}
