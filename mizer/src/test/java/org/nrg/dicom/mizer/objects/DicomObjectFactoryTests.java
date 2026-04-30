package org.nrg.dicom.mizer.objects;

import org.dcm4che2.data.Tag;
import org.junit.Assert;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.tags.TagPublic;
import org.nrg.dicom.mizer.tags.TagSequence;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Period;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.nrg.dicom.mizer.TestUtilTags.AE_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.AE_VM2;
import static org.nrg.dicom.mizer.TestUtilTags.AS_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.AT_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.CS_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.DA_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.DT_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.FD_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.FL_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.IS_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.KNOWN_SEQ_TAG_ITEM;
import static org.nrg.dicom.mizer.TestUtilTags.KNOWN_SEQ_TAG_NO_ITEMS;
import static org.nrg.dicom.mizer.TestUtilTags.KNOWN_SEQ_TAG_WITH_POPULATED_ITEM;
import static org.nrg.dicom.mizer.TestUtilTags.LO_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.LT_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.NUMBER_OF_TAGS_IN_ALL_VRS_OBJECT;
import static org.nrg.dicom.mizer.TestUtilTags.OB_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.OD_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.OF_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.OL_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.OV_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.OW_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.PCID;
import static org.nrg.dicom.mizer.TestUtilTags.PCID_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.PN_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.PRIVATE_CREATOR_ID;
import static org.nrg.dicom.mizer.TestUtilTags.PRIVATE_TAG;
import static org.nrg.dicom.mizer.TestUtilTags.PT_WITHOUT_PCID_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.PT_WITHOUT_PCID_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.PT_WITH_ONE_ITEM_TAG_WITH_VALUE;
import static org.nrg.dicom.mizer.TestUtilTags.PT_WITH_PCID_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.PT_WITH_PCID_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.SH_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.SL_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.SPCID;
import static org.nrg.dicom.mizer.TestUtilTags.SPT_WITHOUT_PCID_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.SPT_WITHOUT_PCID_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.SPT_WITH_PCID_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.SPT_WITH_PCID_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.SS_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.STAG_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.STAG_PRESENT_NO_ITEMS;
import static org.nrg.dicom.mizer.TestUtilTags.STAG_PRESENT_ONE_ITEM_EMPTY;
import static org.nrg.dicom.mizer.TestUtilTags.STAG_PRESENT_ONE_ITEM_TAG_EMPTY_VALUE;
import static org.nrg.dicom.mizer.TestUtilTags.STAG_PRESENT_ONE_ITEM_TAG_WITH_VALUE;
import static org.nrg.dicom.mizer.TestUtilTags.STAG_PRESENT_ONE_ITEM_WITH_TAG_NOT_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.ST_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.SV_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_MATCHES_MULTIPLE_TAGS;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_PT_WITHOUT_PCID_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_PT_WITHOUT_PCID_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_PT_WITH_PCID_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_PT_WITH_PCID_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_SPT_WITHOUT_PCID_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_SPT_WITHOUT_PCID_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_SPT_WITH_PCID_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_SPT_WITH_PCID_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_STAG_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_STAG_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_STAG_PRESENT_EMPTY;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_TAG_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_TAG_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_TAG_PRESENT_EMPTY;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_UNKNOWN_STAG_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_UNKNOWN_STAG_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_UNKNOWN_STAG_PRESENT_EMPTY;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_UNKNOWN_TAG_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_UNKNOWN_TAG_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.TAGPATH_UNKNOWN_TAG_PRESENT_EMPTY;
import static org.nrg.dicom.mizer.TestUtilTags.TAG_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.TAG_MISSING_PCID;
import static org.nrg.dicom.mizer.TestUtilTags.TAG_PCID_IN_MISSING_PCID_GROUP;
import static org.nrg.dicom.mizer.TestUtilTags.TAG_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.TAG_PRESENT_EMPTY;
import static org.nrg.dicom.mizer.TestUtilTags.TAG_PRIVATE_CREATOR_ID;
import static org.nrg.dicom.mizer.TestUtilTags.TAG_PRIVATE_WITHOUT_ID;
import static org.nrg.dicom.mizer.TestUtilTags.TAG_PRIVATE_WITHOUT_ID_AFTER_CREATION;
import static org.nrg.dicom.mizer.TestUtilTags.TAG_PRIVATE_WITH_ID;
import static org.nrg.dicom.mizer.TestUtilTags.TAG_PRIVATE_WITH_WRONG_PVT_BLOCK;
import static org.nrg.dicom.mizer.TestUtilTags.TAG_PUBLIC_KNOWN;
import static org.nrg.dicom.mizer.TestUtilTags.TAG_PUBLIC_UNKNOWN;
import static org.nrg.dicom.mizer.TestUtilTags.TAG_SEQ_KNOWN;
import static org.nrg.dicom.mizer.TestUtilTags.TM_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.UC_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.UI_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.UL_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.UNKNOWN_SEQ_TAG_WITH_EMPTY_ITEM;
import static org.nrg.dicom.mizer.TestUtilTags.UNKNOWN_SEQ_TAG_WITH_POPULATED_ITEM;
import static org.nrg.dicom.mizer.TestUtilTags.UNKNOWN_STAG_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.UNKNOWN_STAG_ONE_ITEM_TAG_EMPTY_VALUE;
import static org.nrg.dicom.mizer.TestUtilTags.UNKNOWN_STAG_ONE_ITEM_TAG_WITH_VALUE;
import static org.nrg.dicom.mizer.TestUtilTags.UNKNOWN_STAG_ONE_ITEM_WITH_TAG_NOT_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.UNKNOWN_STAG_PRESENT_NO_ITEMS;
import static org.nrg.dicom.mizer.TestUtilTags.UNKNOWN_STAG_PRESENT_ONE_ITEM_EMPTY;
import static org.nrg.dicom.mizer.TestUtilTags.UNKNOWN_TAG_MISSING;
import static org.nrg.dicom.mizer.TestUtilTags.UNKNOWN_TAG_PRESENT;
import static org.nrg.dicom.mizer.TestUtilTags.UNKNOWN_TAG_PRESENT_EMPTY;
import static org.nrg.dicom.mizer.TestUtilTags.UN_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.UR_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.US_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.UT_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.UV_VM1;
import static org.nrg.dicom.mizer.TestUtilTags.createDicomObject_All_VRs;
import static org.nrg.dicom.mizer.TestUtilTags.createDicomObject_TagTypes;
import static org.nrg.dicom.mizer.TestUtils.TestSeqTag;
import static org.nrg.dicom.mizer.TestUtils.TestSeqTagVM;
import static org.nrg.dicom.mizer.TestUtils.TestTag;
import static org.nrg.dicom.mizer.TestUtils.encodeHex;
import static org.nrg.dicom.mizer.TestUtils.put;

/**
 * This class is expected to test 100% of the public methods in DicomObjectFactory. The goal here is basic coverage
 * and not in-depth coverage. In-depth coverage of various methods are implemented in other test classes.
 * DicomObjectFactory contains inner-class implementations of DicomObjectI and DicomElementI, so those implementations
 * are tested here.
 */
public class DicomObjectFactoryTests {

    @Test
    public void createFromFile() throws URISyntaxException, MizerException {
        ClassLoader loader = this.getClass().getClassLoader();
        DicomObjectI dobj = DicomObjectFactory.newInstance(new File(loader.getResource("dicom/vr.dcm").toURI()));
        assertFalse(dobj.isEmpty());
    }

    @Test
    public void createFromInputStream() throws URISyntaxException, MizerException {
        ClassLoader loader = this.getClass().getClassLoader();
        DicomObjectI dobj = DicomObjectFactory.newInstance(loader.getResourceAsStream("dicom/vr.dcm"));
        assertFalse(dobj.isEmpty());
    }

    @Test
    public void getStringWithTag_All_VR() {
        DicomObjectI dobj = createDicomObject_All_VRs();
        assertEquals(AE_VM1.initialValue, dobj.getString(AE_VM1.tag));
        assertEquals(AS_VM1.initialValue, dobj.getString(AS_VM1.tag));
//         use the post value to get the value redirected from the initial value
        assertEquals(AT_VM1.initialValue, dobj.getString(AT_VM1.tag));
        assertEquals(CS_VM1.initialValue, dobj.getString(CS_VM1.tag));
        assertEquals(DA_VM1.initialValue, dobj.getString(DA_VM1.tag));
        assertEquals(DT_VM1.initialValue, dobj.getString(DT_VM1.tag));
        assertEquals(FL_VM1.initialValue, dobj.getString(FL_VM1.tag));
        assertEquals(FD_VM1.initialValue, dobj.getString(FD_VM1.tag));
        assertEquals(IS_VM1.initialValue, dobj.getString(IS_VM1.tag));
        assertEquals(LO_VM1.initialValue, dobj.getString(LO_VM1.tag));
        assertEquals(LT_VM1.initialValue, dobj.getString(LT_VM1.tag));
        assertEquals(OB_VM1.initialValue, dobj.getString(OB_VM1.tag));
        assertEquals(OD_VM1.initialValue, dobj.getString(OD_VM1.tag));
        assertEquals(OF_VM1.initialValue, dobj.getString(OF_VM1.tag));
        assertEquals(OL_VM1.initialValue, dobj.getString(OL_VM1.tag));
        assertEquals(OV_VM1.initialValue, dobj.getString(OV_VM1.tag));
        assertEquals(OW_VM1.initialValue, dobj.getString(OW_VM1.tag));
        assertEquals(PN_VM1.initialValue, dobj.getString(PN_VM1.tag));
        assertEquals(SH_VM1.initialValue, dobj.getString(SH_VM1.tag));
        assertEquals(SL_VM1.initialValue, dobj.getString(SL_VM1.tag));
        // SQ elements do not have values.
        // assertEquals(SQ_VM1.postValue, dobj.getString(SQ_VM1.tag));
        assertEquals(SS_VM1.initialValue, dobj.getString(SS_VM1.tag));
        assertEquals(ST_VM1.initialValue, dobj.getString(ST_VM1.tag));
        assertEquals(SV_VM1.initialValue, dobj.getString(SV_VM1.tag));
        assertEquals(TM_VM1.initialValue, dobj.getString(TM_VM1.tag));
        assertEquals(UC_VM1.initialValue, dobj.getString(UC_VM1.tag));
        assertEquals(UI_VM1.initialValue, dobj.getString(UI_VM1.tag));
        assertEquals(UL_VM1.initialValue, dobj.getString(UL_VM1.tag));
        assertEquals(UN_VM1.initialValue, dobj.getString(UN_VM1.tag));
        assertEquals(UI_VM1.initialValue, dobj.getString(UI_VM1.tag));
        assertEquals(UR_VM1.initialValue, dobj.getString(UR_VM1.tag));
        assertEquals(US_VM1.initialValue, dobj.getString(US_VM1.tag));
        assertEquals(UT_VM1.initialValue, dobj.getString(UT_VM1.tag));
        assertEquals(UV_VM1.initialValue, dobj.getString(UV_VM1.tag));
    }

    @Test
    public void getStringWithTagTypes() {
        DicomObjectI dobj = createDicomObject_TagTypes();
        assertTrue(dobj.contains(TAG_PRESENT.tag));
        assertTrue(dobj.contains(TAG_PRESENT_EMPTY.tag));
        assertFalse(dobj.contains(TAG_MISSING.tag));
        assertTrue(dobj.contains(UNKNOWN_TAG_PRESENT.tag));
        assertTrue(dobj.contains(UNKNOWN_TAG_PRESENT_EMPTY.tag));
        assertFalse(dobj.contains(UNKNOWN_TAG_MISSING.tag));
        assertTrue(dobj.contains(PCID.tag));
        assertTrue(dobj.contains(PT_WITH_PCID_PRESENT.tag));
        assertFalse(dobj.contains(PT_WITH_PCID_MISSING.tag));
        assertTrue(dobj.contains(PT_WITHOUT_PCID_PRESENT.tag));
        assertFalse(dobj.contains(PT_WITHOUT_PCID_MISSING.tag));
        assertTrue(dobj.contains(0x00209221));

        assertEquals(TAG_PRESENT.initialValue, dobj.getString(TAG_PRESENT.tag));
        assertEquals(TAG_PRESENT_EMPTY.initialValue, dobj.getString(TAG_PRESENT_EMPTY.tag));
        // value of missing tag is null.
        assertNull(dobj.getString(TAG_MISSING.tag));
        assertEquals(UNKNOWN_TAG_PRESENT.initialValue, dobj.getString(UNKNOWN_TAG_PRESENT.tag));
        assertEquals(UNKNOWN_TAG_PRESENT_EMPTY.initialValue, dobj.getString(UNKNOWN_TAG_PRESENT_EMPTY.tag));
        assertNull(dobj.getString(UNKNOWN_TAG_MISSING.tag));
        assertEquals(PCID.initialValue, dobj.getString(PCID.tag));
        assertEquals(PT_WITH_PCID_PRESENT.initialValue, dobj.getString(PT_WITH_PCID_PRESENT.tag));
        assertNull(dobj.getString(PT_WITH_PCID_MISSING.tag));
        // private tags with missing creator id are not a problem.
        assertEquals(PT_WITHOUT_PCID_PRESENT.initialValue, dobj.getString(PT_WITHOUT_PCID_PRESENT.tag));
        assertNull(dobj.getString(PT_WITHOUT_PCID_MISSING.tag));
        // getting value of sequence tag is unsupported.
        // Attributes doesn't throw exception.
//        assertThrows(UnsupportedOperationException.class, () -> dobj.getString(0x00209221));
    }

    @Test
    public void getStringWithIntTag_VM2() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        String[] aeTitles = new String[]{"AETitle1", "AETitle2"};
        dobj.putStrings(AE_VM2.tag, aeTitles);
        String s = String.join("\\", dobj.getStrings(AE_VM2.tag));
        assertEquals(AE_VM2.initialValue, dobj.getString(AE_VM2.tag));
    }

    @Test
    public void getStringWithTagPath() {
        DicomObjectI dobj = createDicomObject_TagTypes();
        assertEquals(TAG_PRESENT.initialValue, dobj.getString(TAGPATH_TAG_PRESENT));
        assertEquals(TAG_PRESENT_EMPTY.initialValue, dobj.getString(TAGPATH_TAG_PRESENT_EMPTY));
        assertNull(dobj.getString(TAGPATH_TAG_MISSING));
        assertEquals(UNKNOWN_TAG_PRESENT.initialValue, dobj.getString(TAGPATH_UNKNOWN_TAG_PRESENT));
        assertEquals(UNKNOWN_TAG_PRESENT_EMPTY.initialValue, dobj.getString(TAGPATH_UNKNOWN_TAG_PRESENT_EMPTY));
        assertNull(dobj.getString(TAGPATH_UNKNOWN_TAG_MISSING));
        // TODO problem with TagPrivateCreator
//        assertEquals(PCID.initialValue, dobj.getString(TAGPATH_PCID));
        assertEquals(PT_WITH_PCID_PRESENT.initialValue, dobj.getString(TAGPATH_PT_WITH_PCID_PRESENT));
        assertNull(dobj.getString(TAGPATH_PT_WITH_PCID_MISSING));
        assertEquals(PT_WITHOUT_PCID_PRESENT.initialValue, dobj.getString(TAGPATH_PT_WITHOUT_PCID_PRESENT));
        assertNull(dobj.getString(TAGPATH_PT_WITHOUT_PCID_MISSING));

        assertEquals(STAG_PRESENT_ONE_ITEM_TAG_WITH_VALUE.initialValue, dobj.getString(TAGPATH_STAG_PRESENT));
        assertEquals(STAG_PRESENT_ONE_ITEM_TAG_EMPTY_VALUE.initialValue, dobj.getString(TAGPATH_STAG_PRESENT_EMPTY));
        assertNull(dobj.getString(TAGPATH_STAG_MISSING));
        assertEquals(UNKNOWN_STAG_ONE_ITEM_TAG_WITH_VALUE.initialValue, dobj.getString(TAGPATH_UNKNOWN_STAG_PRESENT));
        assertEquals(UNKNOWN_STAG_ONE_ITEM_TAG_EMPTY_VALUE.initialValue, dobj.getString(TAGPATH_UNKNOWN_STAG_PRESENT_EMPTY));
        assertNull(dobj.getString(TAGPATH_UNKNOWN_STAG_MISSING));
        // TODO: resolving private tagpaths has problem.
//        assertEquals(SPCID.initialValue, dobj.getString(TAGPATH_SPCID));
        assertEquals(SPT_WITH_PCID_PRESENT.initialValue, dobj.getString(TAGPATH_SPT_WITH_PCID_PRESENT));
        assertNull(dobj.getString(TAGPATH_SPT_WITH_PCID_MISSING));
        assertEquals(SPT_WITHOUT_PCID_PRESENT.initialValue, dobj.getString(TAGPATH_SPT_WITHOUT_PCID_PRESENT));
        assertNull(dobj.getString(TAGPATH_SPT_WITHOUT_PCID_MISSING));
    }

    @Test
    public void getStringWithTagArray() {
        DicomObjectI dobj = createDicomObject_TagTypes();

        assertTrue(dobj.contains(STAG_PRESENT_ONE_ITEM_TAG_WITH_VALUE.tag));
        assertTrue(dobj.contains(STAG_PRESENT_ONE_ITEM_TAG_EMPTY_VALUE.tag));
        assertFalse(dobj.contains(STAG_PRESENT_ONE_ITEM_WITH_TAG_NOT_PRESENT.tag));
        assertTrue(dobj.contains(UNKNOWN_STAG_ONE_ITEM_TAG_WITH_VALUE.tag));
        assertTrue(dobj.contains(UNKNOWN_STAG_ONE_ITEM_TAG_EMPTY_VALUE.tag));
        assertFalse(dobj.contains(UNKNOWN_STAG_ONE_ITEM_WITH_TAG_NOT_PRESENT.tag));
        assertTrue(dobj.contains(SPCID.tag));
        assertTrue(dobj.contains(SPT_WITH_PCID_PRESENT.tag));
        assertFalse(dobj.contains(SPT_WITH_PCID_MISSING.tag));
        assertTrue(dobj.contains(SPT_WITHOUT_PCID_PRESENT.tag));
        assertFalse(dobj.contains(SPT_WITHOUT_PCID_MISSING.tag));

        assertEquals(STAG_PRESENT_ONE_ITEM_TAG_WITH_VALUE.initialValue, dobj.getString(STAG_PRESENT_ONE_ITEM_TAG_WITH_VALUE.tag));
        assertEquals(STAG_PRESENT_ONE_ITEM_TAG_EMPTY_VALUE.initialValue, dobj.getString(STAG_PRESENT_ONE_ITEM_TAG_EMPTY_VALUE.tag));
        // value of missing tag is null.
        assertNull(dobj.getString(STAG_PRESENT_ONE_ITEM_WITH_TAG_NOT_PRESENT.tag));
        assertEquals(UNKNOWN_STAG_ONE_ITEM_TAG_WITH_VALUE.initialValue, dobj.getString(UNKNOWN_STAG_ONE_ITEM_TAG_WITH_VALUE.tag));
        assertEquals(UNKNOWN_STAG_ONE_ITEM_TAG_EMPTY_VALUE.initialValue, dobj.getString(UNKNOWN_STAG_ONE_ITEM_TAG_EMPTY_VALUE.tag));
        // value of missing tag is null.
        assertNull(dobj.getString(UNKNOWN_STAG_ONE_ITEM_WITH_TAG_NOT_PRESENT.tag));
        assertEquals(SPCID.initialValue, dobj.getString(SPCID.tag));
        assertEquals(SPT_WITH_PCID_PRESENT.initialValue, dobj.getString(SPT_WITH_PCID_PRESENT.tag));
        assertNull(dobj.getString(SPT_WITH_PCID_MISSING.tag));
        // private tags with missing creator id are not a problem.
        assertEquals(SPT_WITHOUT_PCID_PRESENT.initialValue, dobj.getString(SPT_WITHOUT_PCID_PRESENT.tag));
        assertNull(dobj.getString(SPT_WITHOUT_PCID_MISSING.tag));
        // getting value of sequence tag is unsupported. (Dcm4che5 changed)L
//        assertThrows(UnsupportedOperationException.class, () -> dobj.getString(0x00209221));
    }

    @Test
    public void getStringsWithTag() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        String[] aeTitles = new String[]{"AETitle1", "AETitle2"};
        dobj.putStrings(AE_VM2.tag, aeTitles);
        Assert.assertArrayEquals(aeTitles, dobj.getStrings(AE_VM2.tag));
    }

    @Test
    public void getStringsWithTagArray() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        String[] values = new String[]{"value1", "value2"};
        TestSeqTagVM t2_0_t1 = new TestSeqTagVM(new int[]{0x00209221, 0, 0x00209164}, values);

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, t2_0_t1);

        assertTrue(dobj.contains(t1.tag));
        assertTrue(dobj.contains(t2_0_t1.tag));
        assertEquals(t2_0_t1.initialValues, dobj.getStrings(t2_0_t1.tag));
    }

    @Test
    public void getStringsWithTagPath() {
        String[] values = new String[]{"value1", "value2"};
        TestSeqTagVM seqTagVM = new TestSeqTagVM(new int[]{0x00209221, 0, 0x00209164}, values);

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, seqTagVM);
        TagPath tagPath = new TagPath(new TagSequence(new TagPublic(0x00209221), 0)).addTag(new TagPublic(0x00209164));
        assertArrayEquals(values, dobj.getStrings(tagPath));
    }

    @Test
    public void getBytes() {
        DicomObjectI dobj = createDicomObject_All_VRs();
        assertEquals(OB_VM1.encodedValue, encodeHex(dobj.getBytes(OB_VM1.tag)));
    }

    @Test
    // TODO Test putBytes better.
    public void putBytes() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.putBytes(OB_VM1.tag, "OB", OB_VM1.initialValue.getBytes(StandardCharsets.UTF_8));
        assertEquals(OB_VM1.initialValue, new String(dobj.getBytes(OB_VM1.tag),StandardCharsets.UTF_8));
    }

    @Test
    public void getVRWithTag() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AE_VM1);
        assertEquals("AE", dobj.getVR(AE_VM1.tag));
        dobj.putString(AE_VM1.tag, "SH", "different_VR");
        assertEquals("SH", dobj.getVR(AE_VM1.tag));
    }

    @Test
    public void getVRWithTagPath() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AE_VM1);
        TagPath tagPath = new TagPath(new TagPublic(AE_VM1.tag));
        assertEquals("AE", dobj.getVR(tagPath));
        assertThrows(IllegalArgumentException.class, () -> dobj.getVR(TAGPATH_MATCHES_MULTIPLE_TAGS));
    }

    @Test
    public void getAge() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        Optional<Period> age = dobj.getAge(AS_VM1.tag);
        assertEquals(Period.ofYears(64), age.get());
    }

    @Test
    // TODO changed
    public void resolvePrivateTag() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, TAG_PRIVATE_CREATOR_ID);
        put(dobj, TAG_PRIVATE_WITH_ID);
//        put(dobj, TAG_PRIVATE_WITHOUT_ID);
        put(dobj, TAG_PCID_IN_MISSING_PCID_GROUP);
        // not meant to find private creator id tags. won't resolve correctly.
        assertNotEquals(TAG_PRIVATE_CREATOR_ID.tag, dobj.resolvePrivateTag(TAG_PRIVATE_CREATOR_ID.tag, TAG_PRIVATE_CREATOR_ID.initialValue, false));
        assertEquals(TAG_PRIVATE_WITH_ID.tag, dobj.resolvePrivateTag(TAG_PRIVATE_WITH_ID.tag, TAG_PRIVATE_CREATOR_ID.initialValue, false));
        assertEquals(TAG_PRIVATE_WITH_ID.tag, dobj.resolvePrivateTag(TAG_PRIVATE_WITH_WRONG_PVT_BLOCK.tag, TAG_PRIVATE_CREATOR_ID.initialValue, false));
        assertEquals(-1, dobj.resolvePrivateTag(TAG_PRIVATE_WITHOUT_ID.tag, TAG_PRIVATE_CREATOR_ID.initialValue, false));
        assertFalse(dobj.contains(TAG_MISSING_PCID.tag));
        assertFalse(dobj.contains(TAG_PRIVATE_WITHOUT_ID.tag));
        assertTrue(dobj.contains(TAG_PCID_IN_MISSING_PCID_GROUP.tag));
        // create the missing pcid, resolve the pvt tag but do not create the tag.
        assertEquals(TAG_PRIVATE_WITHOUT_ID_AFTER_CREATION.tag, dobj.resolvePrivateTag(TAG_PRIVATE_WITHOUT_ID.tag, TAG_MISSING_PCID.initialValue, true));
        assertTrue(dobj.contains(TAG_MISSING_PCID.tag));
        assertFalse(dobj.contains(TAG_PRIVATE_WITHOUT_ID_AFTER_CREATION.tag));
    }

    @Test
    // TODO changed
    public void resolveTagPath() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag p1c = new TestTag(0x20230010, "p1c");
        TestTag p1t1 = new TestTag(0x202310EF, "p1t1");
        TestSeqTag s1 = new TestSeqTag(new int[]{0x00209221, 0, 0x00209164}, "s1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, p1c);
        put(dobj, p1t1);
        put(dobj, s1);

        assertTrue(dobj.contains(t1.tag));
        assertTrue(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1t1.tag));

        TagPath tp = new TagPath(new TagPublic(t1.tag));
        assertArrayEquals(new int[]{t1.tag}, dobj.resolveTagPath(tp).get());

        TagPath tp2 = new TagPath(new TagSequence(new TagPublic(0x00209221), 0)).addTag(new TagPublic(0x00209164));
        assertArrayEquals(new int[]{0x00209221, 0, 0x00209164}, dobj.resolveTagPath(tp2).get());

        assertThrows(IllegalArgumentException.class, () -> dobj.resolveTagPath(TAGPATH_MATCHES_MULTIPLE_TAGS));
    }

    @Test
    public void assignStringWithTagPath() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        assertEquals(AS_VM1.initialValue, dobj.getString(AS_VM1.tag));
        TagPath tagPath1 = new TagPath(new TagPublic(AS_VM1.tag));
        dobj.assign(tagPath1, "019Y");
        assertEquals("019Y", dobj.getString(AS_VM1.tag));

        TagPath tagPath2 = new TagPath(new TagPublic(FD_VM1.tag));
        dobj.assign(tagPath2, FD_VM1.initialValue);
        assertEquals(FD_VM1.initialValue, dobj.getString(FD_VM1.tag));
    }

    @Test
    public void assignIfExists() {
        TestTag t_present = new TestTag(0x00100010, "present");
        TestTag t_absent = new TestTag(0x00100020, "absent");
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t_present);

        assertTrue(dobj.contains(t_present.tag));
        assertFalse(dobj.contains(t_absent.tag));
        TagPath tagPath_present = new TagPath(new TagPublic(t_present.tag));
        TagPath tagPath_absent = new TagPath(new TagPublic(t_absent.tag));
        Value value = new ConstantValue("exists");
        dobj.assignIfExists(tagPath_present, value);
        dobj.assignIfExists(tagPath_absent, value);
        assertEquals("exists", dobj.getString(t_present.tag));
        assertFalse(dobj.contains(t_absent.tag));
    }

    @Test
    public void assignValueWithInt() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, SH_VM1);
        assertEquals(SH_VM1.initialValue, dobj.getString(SH_VM1.tag));
        Value value = new ConstantValue("new");
        dobj.assign(SH_VM1.tag, value);
        assertEquals("new", dobj.getString(SH_VM1.tag));
    }

    @Test
    public void assignValueWithIntArray() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        Value value = new ConstantValue("new");
        int[] tags = new int[]{AE_VM1.tag};
        dobj.assign(tags, value);
        assertEquals("new", dobj.getString(AE_VM1.tag));
    }

    @Test
    public void deleteTag() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        assertTrue(dobj.contains(AS_VM1.tag));
        dobj.delete(AS_VM1.tag);
        assertFalse(dobj.contains(AS_VM1.tag));
    }

    @Test
    // TODO changed
    public void deleteTagArray() {
        DicomObjectI dobj = createDicomObject_All_VRs();
        assertTrue(dobj.contains(AS_VM1.tag));
        dobj.delete(new int[]{AS_VM1.tag});
        assertFalse(dobj.contains(AS_VM1.tag));
    }

    @Test
    public void deleteTagPath() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AE_VM1);
        assertTrue(dobj.contains(AE_VM1.tag));
        TagPath tagPath = new TagPath(new TagPublic(AE_VM1.tag));
        dobj.delete(tagPath);
        assertFalse(dobj.contains(AE_VM1.tag));
    }

    @Test
    public void deleteAllTags() {
        DicomObjectI dobj = createDicomObject_All_VRs();
        assertEquals(NUMBER_OF_TAGS_IN_ALL_VRS_OBJECT, dobj.size());
        dobj.deleteAllTags();
        assertEquals(0, dobj.size());
    }

    @Test
    public void readFile() throws MizerException, URISyntaxException {
        File file = new File("nosuchfile");
        DicomObjectI noDobj = DicomObjectFactory.newInstance();
        assertThrows(MizerException.class, () -> noDobj.read(file));
        ClassLoader loader = this.getClass().getClassLoader();
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.read(new File(loader.getResource("dicom/vr.dcm").toURI()));
        assertFalse(dobj.isEmpty());
        DicomObjectI notDobj = DicomObjectFactory.newInstance();
        File notDicom = new File(loader.getResource("dicom/seq/000.xml").toURI());
        assertThrows(MizerException.class, () -> notDobj.read(notDicom));
    }

    @Test()
    public void readInputStream() throws MizerException {
        ClassLoader loader = this.getClass().getClassLoader();
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.read(loader.getResourceAsStream("dicom/vr.dcm"));
        assertFalse(dobj.isEmpty());
        DicomObjectI notDobj = DicomObjectFactory.newInstance();
        InputStream notDicomStream = loader.getResourceAsStream("dicom/seq/000.xml");
        assertThrows(MizerException.class, () -> notDobj.read(notDicomStream));
    }

    @Test(expected = NullPointerException.class)
    // TODO changed
    public void write() throws MizerException {
        OutputStream outputStream = null;
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.write(outputStream);
    }

    @Test
    public void containsIntTag() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        assertTrue(dobj.contains(AS_VM1.tag));
    }

    @Test
    public void containsTagWithPCID() {
        TestTag t1 = new TestTag(0x00100010, "present");
        TestTag pcid = new TestTag(0x00170010, "pcid");
        TestTag pc1 = new TestTag(0x00171001, "pc1");
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, pcid);
        put(dobj, pc1);
        assertTrue(dobj.contains(pc1.tag, "pcid"));
        assertFalse(dobj.contains(pc1.tag, "foo"));
    }

    @Test
    public void containsTagArray() throws MizerException {
        ClassLoader loader = this.getClass().getClassLoader();
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.read(loader.getResourceAsStream("dicom/seq/seq_types.dcm"));
        dobj.putString(new int[]{0x00120064, 0, 0x00080104}, "test");
        assertTrue(dobj.contains(new int[]{0x00120064, 1, 0x00080100}));
        assertTrue(dobj.contains(new int[]{0x00120064, 0, 0x00080104}));
        assertTrue(dobj.contains(new int[]{0x00120064, 1}));
        assertTrue(dobj.contains(new int[]{0x00120064}));
        assertFalse(dobj.contains(new int[]{0x00081032, 0, 0x00080100}));
        assertTrue(dobj.contains(new int[]{0x00081032, 0}));
        assertTrue(dobj.contains(new int[]{0x00081032}));
        assertFalse(dobj.contains(new int[]{0x0008103F, 0, 0x00080100}));
        assertFalse(dobj.contains(new int[]{0x0008103F, 0}));
        assertTrue(dobj.contains(new int[]{0x00081032}));
    }

    @Test
    public void containsTag() {
        DicomObjectI dobj = createDicomObject_TagTypes();
        assertTrue(dobj.contains(TAGPATH_TAG_PRESENT.getTag(0)));
        assertFalse(dobj.contains(TAGPATH_TAG_MISSING.getTag(0)));
        // TODO: does not resolve private creator ids correctly.
//        assertTrue(dobj.contains(TAGPATH_PCID.getTag(0)));
//        assertFalse(dobj.contains(TAGPATH_PCID_MISSING.getTag(0)));
        assertTrue(dobj.contains(TAGPATH_PT_WITH_PCID_PRESENT.getTag(0)));
        assertFalse(dobj.contains(TAGPATH_PT_WITH_PCID_MISSING.getTag(0)));
        assertTrue(dobj.contains(TAGPATH_PT_WITHOUT_PCID_PRESENT.getTag(0)));
        // finds private tags with missing pcid
        assertTrue(dobj.contains(TAGPATH_PT_WITHOUT_PCID_PRESENT.getTag(0)));
        assertTrue(dobj.contains(TAGPATH_STAG_PRESENT.getTag(0)));
        assertTrue(dobj.contains(TAGPATH_STAG_PRESENT_EMPTY.getTag(0)));
    }

    @Test
    public void containsTagPath() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        assertTrue(dobj.contains(new TagPath(new TagPublic(AS_VM1.tag))));
    }

    @Test
    public void getPrivateCreator() {
        TestTag pcid = new TestTag(0x00170010, "pcid");
        TestTag pc1 = new TestTag(0x00171001, "pc1");
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, pcid);
        put(dobj, pc1);

        // tag does not have to exist in the dataset.
        assertEquals("pcid", dobj.getPrivateCreator(0x00171000));
        assertEquals("pcid", dobj.getPrivateCreator(0x00171001));
        // Null if the block does not have an ID.
        assertNull(dobj.getPrivateCreator(0x00171100));
        assertNull(dobj.getPrivateCreator(0x00170010));
        assertNull(dobj.getPrivateCreator(0x00100010));
    }

    @Test
    public void isSequenceElement() {
        DicomObjectI dobj = createDicomObject_TagTypes();
        assertFalse(dobj.isSequenceElement(TAG_PRESENT.tag));
        assertFalse(dobj.isSequenceElement(TAG_PRESENT_EMPTY.tag));
        assertFalse(dobj.isSequenceElement(TAG_MISSING.tag));
        assertFalse(dobj.isSequenceElement(UNKNOWN_TAG_PRESENT.tag));
        assertFalse(dobj.isSequenceElement(UNKNOWN_TAG_PRESENT_EMPTY.tag));
        assertFalse(dobj.isSequenceElement(UNKNOWN_TAG_MISSING.tag));
        assertFalse(dobj.isSequenceElement(PCID.tag));
        assertFalse(dobj.isSequenceElement(PCID_MISSING.tag));
        assertFalse(dobj.isSequenceElement(PT_WITH_PCID_PRESENT.tag));
        assertFalse(dobj.isSequenceElement(PT_WITH_PCID_MISSING.tag));
        assertFalse(dobj.isSequenceElement(PT_WITHOUT_PCID_PRESENT.tag));
        assertFalse(dobj.isSequenceElement(PT_WITHOUT_PCID_MISSING.tag));
        // SQ element must be present
        assertFalse(dobj.isSequenceElement(STAG_MISSING.tag));
        // true if VR=SQ but no items present.
        assertTrue(dobj.isSequenceElement(STAG_PRESENT_NO_ITEMS.tag));
        assertTrue(dobj.isSequenceElement(STAG_PRESENT_ONE_ITEM_EMPTY.tag[0]));
        assertTrue(dobj.isSequenceElement(STAG_PRESENT_ONE_ITEM_TAG_WITH_VALUE.tag[0]));
        assertTrue(dobj.isSequenceElement(STAG_PRESENT_ONE_ITEM_TAG_EMPTY_VALUE.tag[0]));
        assertTrue(dobj.isSequenceElement(STAG_PRESENT_ONE_ITEM_WITH_TAG_NOT_PRESENT.tag[0]));

        assertFalse(dobj.isSequenceElement(UNKNOWN_STAG_MISSING.tag));
        // false if VR=UN but no items present. how would it know.
        assertFalse(dobj.isSequenceElement(UNKNOWN_STAG_PRESENT_NO_ITEMS.tag));
        assertTrue(dobj.isSequenceElement(UNKNOWN_STAG_PRESENT_ONE_ITEM_EMPTY.tag[0]));
        assertTrue(dobj.isSequenceElement(UNKNOWN_STAG_ONE_ITEM_TAG_WITH_VALUE.tag[0]));
        assertTrue(dobj.isSequenceElement(UNKNOWN_STAG_ONE_ITEM_TAG_EMPTY_VALUE.tag[0]));
        assertTrue(dobj.isSequenceElement(UNKNOWN_STAG_ONE_ITEM_WITH_TAG_NOT_PRESENT.tag[0]));
        assertTrue(dobj.isSequenceElement(PT_WITH_ONE_ITEM_TAG_WITH_VALUE.tag[0]));
    }

    @Test
    public void addMetaHeader() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.addMetaHeader();
        assertTrue(dobj.contains(0x00020010));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void iterator() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        Iterator<DicomElementI> iterator = dobj.iterator();
        assertTrue(iterator instanceof Iterator);
        // this should throw UnsupportedOperationException and is included for coverage.
        iterator.remove();
    }

    @Test
    public void isEmpty() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        assertTrue(dobj.isEmpty());
        put(dobj, AS_VM1);
        assertFalse(dobj.isEmpty());
    }

    @Test
    public void isEmptyTag() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag tag_empty = new TestTag(0x00100010, "");
        TestTag tag_valued = new TestTag(0x00100020, "value");

        put(dobj, tag_empty);
        put(dobj, tag_valued);

        assertTrue(dobj.isEmpty(tag_empty.tag));
        assertFalse(dobj.isEmpty(tag_valued.tag));
        // non-existing tag is an illegal argument
        assertThrows(IllegalArgumentException.class, () -> dobj.isEmpty(0x0020000D));
    }

    @Test
    public void isEmptyTagArray() {
        TestSeqTag t_populated = new TestSeqTag(new int[]{0x00209221, 0, 0x00209164}, "value");
        TestSeqTag t_empty = new TestSeqTag(new int[]{0x00209221, 0, 0x00209165}, "");
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t_populated);
        put(dobj, t_empty);
        assertTrue(dobj.isEmpty(t_empty.tag));
        assertFalse(dobj.isEmpty(t_populated.tag));
        // NPE! dobj.isEmpty(new int[]{AE_VM1.tag});
    }

    @Test
    public void size() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        assertEquals(0, dobj.size());
        put(dobj, AS_VM1);
        assertEquals(1, dobj.size());
        put(dobj, new TestSeqTag(new int[]{0x00209221, 0, 0x00209164}, "value1"));
        put(dobj, new TestSeqTag(new int[]{0x00209221, 1, 0x00209164}, "value2"));
        // does not count contents of seq tags.
        assertEquals(2, dobj.size());
    }

    @Test
    public void getDicomObject() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        // TODO: This is dead code. the implenetaion only returns null.
        assertNull(dobj.getDicomObject(new TagPublic(AE_VM1.tag)));
    }

    @Test
    public void getItemIntArray() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestSeqTag t2_0_t1 = new TestSeqTag(new int[]{0x00209221, 0, 0x00209164}, "t2_0_t1");
        TestSeqTag t2_1_t1_missing = new TestSeqTag(new int[]{0x00209221, 1, 0x00209164}, "t2_0_t2");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, t2_0_t1);
        assertNull(dobj.getItem(t2_1_t1_missing.tag));
        DicomObjectI item1 = dobj.getItem(new int[]{0x00209221, 0});
        assertEquals(t2_0_t1.initialValue, item1.getString(0x00209164));
        DicomObjectI item2 = dobj.getItem(new int[]{0x00209221, 1});
        assertNull(item2);

        // The item just references the tags in the orignal object. Thus ops on the item effect the original object.
        assertEquals(t2_0_t1.initialValue, dobj.getString(t2_0_t1.tag));
        item1.putString(0x00209164, "changed");
        assertEquals("changed", dobj.getString(t2_0_t1.tag));
    }

    @Test
    public void getItemTagPath() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestSeqTag t2_0_t1 = new TestSeqTag(new int[]{0x00209221, 0, 0x00209164}, "t2_0_t1");
        TestSeqTag t2_1_t1_missing = new TestSeqTag(new int[]{0x00209221, 1, 0x00209164}, "t2_0_t2");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, t2_0_t1);
        TagPath tp_t1 = new TagPath(new TagPublic(t1.tag));
        TagPath tp_item1 = new TagPath(new TagSequence(new TagPublic(0x00209221), 0));
        TagPath tp_item2 = new TagPath(new TagSequence(new TagPublic(0x00209221), 1));
        TagPath tp_item_missing = new TagPath(new TagSequence(new TagPublic(0x00209221), 2));

        assertFalse(dobj.getItem(tp_t1).isPresent());
        DicomObjectI item1 = dobj.getItem(tp_item1).get();
        assertEquals(t2_0_t1.initialValue, item1.getString(0x00209164));
        assertFalse(dobj.getItem(tp_item_missing).isPresent());

        // The item just references the tags in the orignal object. Thus ops on the item effect the original object.
        assertEquals(t2_0_t1.initialValue, dobj.getString(t2_0_t1.tag));
        item1.putString(0x00209164, "changed");
        assertEquals("changed", dobj.getString(t2_0_t1.tag));
    }

    @Test
    public void getWithIntTag() {
        DicomObjectI dobj = createDicomObject_TagTypes();

        assertEquals(Integer.toHexString(TAG_PRESENT.tag), dobj.get(TAG_PRESENT.tag).tagString());
        assertNull(dobj.get(TAG_MISSING.tag));
        assertEquals(Integer.toHexString(TAG_PRESENT_EMPTY.tag), dobj.get(TAG_PRESENT_EMPTY.tag).tagString());
        assertEquals(Integer.toHexString(UNKNOWN_TAG_PRESENT.tag), dobj.get(UNKNOWN_TAG_PRESENT.tag).tagString());
        assertEquals(Integer.toHexString(UNKNOWN_TAG_PRESENT_EMPTY.tag), dobj.get(UNKNOWN_TAG_PRESENT_EMPTY.tag).tagString());
        assertNull(dobj.get(UNKNOWN_TAG_MISSING.tag));
        assertEquals(Integer.toHexString(PCID.tag), dobj.get(PCID.tag).tagString());
        assertNull(dobj.get(PCID_MISSING.tag));
        assertEquals(Integer.toHexString(PT_WITH_PCID_PRESENT.tag), dobj.get(PT_WITH_PCID_PRESENT.tag).tagString());
        assertNull(dobj.get(PT_WITH_PCID_MISSING.tag));
        assertEquals(Integer.toHexString(PT_WITHOUT_PCID_PRESENT.tag), dobj.get(PT_WITHOUT_PCID_PRESENT.tag).tagString());
        assertNull(dobj.get(PT_WITHOUT_PCID_MISSING.tag));
    }

    @Test
    public void getWithTag() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        assertEquals(AS_VM1.tag, dobj.get(new TagPublic(AS_VM1.tag)).tag());
    }

    @Test
    public void getElement() {
        DicomObjectI dobj = createDicomObject_TagTypes();

        assertEquals(Integer.toHexString(TAG_PRESENT.tag), dobj.getElement(TAG_PRESENT.tag).tagString());
        assertNull(dobj.getElement(TAG_MISSING.tag));
        assertEquals(Integer.toHexString(TAG_PRESENT_EMPTY.tag), dobj.getElement(TAG_PRESENT_EMPTY.tag).tagString());
        assertEquals(Integer.toHexString(UNKNOWN_TAG_PRESENT.tag), dobj.getElement(UNKNOWN_TAG_PRESENT.tag).tagString());
        assertEquals(Integer.toHexString(UNKNOWN_TAG_PRESENT_EMPTY.tag), dobj.getElement(UNKNOWN_TAG_PRESENT_EMPTY.tag).tagString());
        assertNull(dobj.getElement(UNKNOWN_TAG_MISSING.tag));
        assertEquals(Integer.toHexString(PCID.tag), dobj.getElement(PCID.tag).tagString());
        assertNull(dobj.getElement(PCID_MISSING.tag));
        assertEquals(Integer.toHexString(PT_WITH_PCID_PRESENT.tag), dobj.getElement(PT_WITH_PCID_PRESENT.tag).tagString());
        assertNull(dobj.getElement(PT_WITH_PCID_MISSING.tag));
        assertEquals(Integer.toHexString(PT_WITHOUT_PCID_PRESENT.tag), dobj.getElement(PT_WITHOUT_PCID_PRESENT.tag).tagString());
        assertNull(dobj.getElement(PT_WITHOUT_PCID_MISSING.tag));
    }

    @Test
    public void putString() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        // create known public tag
        dobj.putString(TAG_PUBLIC_KNOWN.tag, TAG_PUBLIC_KNOWN.initialValue);
        assertEquals(TAG_PUBLIC_KNOWN.initialValue, dobj.getString(TAG_PUBLIC_KNOWN.tag));
        // update known public tag
        dobj.putString(TAG_PUBLIC_KNOWN.tag, TAG_PUBLIC_KNOWN.postValue);
        assertEquals(TAG_PUBLIC_KNOWN.postValue, dobj.getString(TAG_PUBLIC_KNOWN.tag));
        // create unknown public tag
        dobj.putString(TAG_PUBLIC_UNKNOWN.tag, TAG_PUBLIC_UNKNOWN.initialValue);
        assertEquals(TAG_PUBLIC_UNKNOWN.initialValue, dobj.getString(TAG_PUBLIC_UNKNOWN.tag));
        // update unknown public tag
        dobj.putString(TAG_PUBLIC_UNKNOWN.tag, TAG_PUBLIC_UNKNOWN.postValue);
        assertEquals(TAG_PUBLIC_UNKNOWN.postValue, dobj.getString(TAG_PUBLIC_UNKNOWN.tag));
        // create private creator id
        dobj.putString(TAG_PRIVATE_CREATOR_ID.tag, TAG_PRIVATE_CREATOR_ID.initialValue);
        assertEquals(TAG_PRIVATE_CREATOR_ID.initialValue, dobj.getString(TAG_PRIVATE_CREATOR_ID.tag));
        // update private creator id
        dobj.putString(TAG_PRIVATE_CREATOR_ID.tag, TAG_PRIVATE_CREATOR_ID.postValue);
        assertEquals(TAG_PRIVATE_CREATOR_ID.postValue, dobj.getString(TAG_PRIVATE_CREATOR_ID.tag));
        // create private with id
        dobj.putString(TAG_PRIVATE_WITH_ID.tag, TAG_PRIVATE_WITH_ID.initialValue);
        assertEquals(TAG_PRIVATE_WITH_ID.initialValue, dobj.getString(TAG_PRIVATE_WITH_ID.tag));
        // update private with id
        dobj.putString(TAG_PRIVATE_WITH_ID.tag, TAG_PRIVATE_WITH_ID.postValue);
        assertEquals(TAG_PRIVATE_WITH_ID.postValue, dobj.getString(TAG_PRIVATE_WITH_ID.tag));
        // create private without id
        dobj.putString(TAG_PRIVATE_WITHOUT_ID.tag, TAG_PRIVATE_WITHOUT_ID.initialValue);
        assertEquals(TAG_PRIVATE_WITHOUT_ID.initialValue, dobj.getString(TAG_PRIVATE_WITHOUT_ID.tag));
        // update private without id
        dobj.putString(TAG_PRIVATE_WITHOUT_ID.tag, TAG_PRIVATE_WITHOUT_ID.postValue);
        assertEquals(TAG_PRIVATE_WITHOUT_ID.postValue, dobj.getString(TAG_PRIVATE_WITHOUT_ID.tag));
        // adding a private tag does not create a private creator tag.
        assertFalse(dobj.contains(TAG_MISSING_PCID.tag));
        // create seq tag
        dobj.putString(TAG_SEQ_KNOWN.tag, TAG_SEQ_KNOWN.initialValue);
        assertTrue(dobj.contains(TAG_SEQ_KNOWN.tag));
        // update private without id
        dobj.putString(TAG_SEQ_KNOWN.tag, TAG_SEQ_KNOWN.postValue);
        assertTrue(dobj.contains(TAG_SEQ_KNOWN.tag));
    }

    @Test
    public void putStringIntArray() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        // create
        dobj.putString(KNOWN_SEQ_TAG_NO_ITEMS.tag, KNOWN_SEQ_TAG_NO_ITEMS.initialValue);
        assertTrue(dobj.contains(KNOWN_SEQ_TAG_NO_ITEMS.tag));
        // update
        dobj.putString(KNOWN_SEQ_TAG_NO_ITEMS.tag, KNOWN_SEQ_TAG_NO_ITEMS.postValue);
        assertTrue(dobj.contains(KNOWN_SEQ_TAG_NO_ITEMS.tag));
        // create
        assertThrows(IllegalArgumentException.class, () -> dobj.putString(KNOWN_SEQ_TAG_ITEM.tag, KNOWN_SEQ_TAG_ITEM.initialValue));
        // update
        assertThrows(IllegalArgumentException.class, () -> dobj.putString(KNOWN_SEQ_TAG_ITEM.tag, KNOWN_SEQ_TAG_ITEM.initialValue));
        // create
        dobj.putString(KNOWN_SEQ_TAG_WITH_POPULATED_ITEM.tag, KNOWN_SEQ_TAG_WITH_POPULATED_ITEM.initialValue);
        assertEquals(KNOWN_SEQ_TAG_WITH_POPULATED_ITEM.initialValue, dobj.getString(KNOWN_SEQ_TAG_WITH_POPULATED_ITEM.tag));
        // update
        dobj.putString(KNOWN_SEQ_TAG_WITH_POPULATED_ITEM.tag, KNOWN_SEQ_TAG_WITH_POPULATED_ITEM.postValue);
        assertEquals(KNOWN_SEQ_TAG_WITH_POPULATED_ITEM.postValue, dobj.getString(KNOWN_SEQ_TAG_WITH_POPULATED_ITEM.tag));
        // create
        assertThrows(IllegalArgumentException.class, () -> dobj.putString(UNKNOWN_SEQ_TAG_WITH_EMPTY_ITEM.tag, UNKNOWN_SEQ_TAG_WITH_EMPTY_ITEM.initialValue));
        // update
        assertThrows(IllegalArgumentException.class, () -> dobj.putString(UNKNOWN_SEQ_TAG_WITH_EMPTY_ITEM.tag, UNKNOWN_SEQ_TAG_WITH_EMPTY_ITEM.initialValue));
        // create
        dobj.putString(UNKNOWN_SEQ_TAG_WITH_POPULATED_ITEM.tag, UNKNOWN_SEQ_TAG_WITH_POPULATED_ITEM.initialValue);
        assertEquals(UNKNOWN_SEQ_TAG_WITH_POPULATED_ITEM.initialValue, dobj.getString(UNKNOWN_SEQ_TAG_WITH_POPULATED_ITEM.tag));
        // update
        dobj.putString(UNKNOWN_SEQ_TAG_WITH_POPULATED_ITEM.tag, UNKNOWN_SEQ_TAG_WITH_POPULATED_ITEM.postValue);
        assertEquals(UNKNOWN_SEQ_TAG_WITH_POPULATED_ITEM.postValue, dobj.getString(UNKNOWN_SEQ_TAG_WITH_POPULATED_ITEM.tag));
    }

    @Test
    public void putStringWithIntTagAndVR() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        String desiredVR = "SH";
        String desiredVR2 = "LO";
        String definedVR = "AS";
        dobj.putString(AS_VM1.tag, desiredVR, AS_VM1.initialValue);
        // The specified VR is used despite the dictionary-defined value when element is created.
        assertEquals(desiredVR, dobj.getVR(AS_VM1.tag));
        dobj.putString(AS_VM1.tag, desiredVR2, AS_VM1.initialValue);
        // The specified VR is used despite the dictionary-defined value when element is updated.
        assertEquals(desiredVR2, dobj.getVR(AS_VM1.tag));
        dobj.putString(UN_VM1.tag, desiredVR, UN_VM1.initialValue);
        // The unknown element is given the specified VR when created.
        assertEquals(desiredVR, dobj.getVR(UN_VM1.tag));
        // The unknown element is given the specified VR when updated.
        dobj.putString(UN_VM1.tag, desiredVR2, UN_VM1.initialValue);
        assertEquals(desiredVR2, dobj.getVR(UN_VM1.tag));
    }

    @Test
    public void putStringWithIntArrayAndVR() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        String desiredVR = "SH";
        String desiredVR2 = "LO";
        dobj.putString(new int[]{AS_VM1.tag}, desiredVR, AS_VM1.initialValue);
        // The specified VR is used despite the dictionary-defined value when element is created.
        assertEquals(desiredVR, dobj.getVR(AS_VM1.tag));
        dobj.putString(new int[]{AS_VM1.tag}, desiredVR2, AS_VM1.initialValue);
        // The specified VR is used despite the dictionary-defined value when element is updated.
        assertEquals(desiredVR2, dobj.getVR(AS_VM1.tag));
        dobj.putString(new int[]{UN_VM1.tag}, desiredVR, UN_VM1.initialValue);
        // The unknown element is given the specified VR when created.
        assertEquals(desiredVR, dobj.getVR(UN_VM1.tag));
        // The unknown element is given the specified VR when updated.
        dobj.putString(new int[]{UN_VM1.tag}, desiredVR, UN_VM1.initialValue);
        assertEquals(desiredVR, dobj.getVR(UN_VM1.tag));
    }

    @Test
    public void putStrings() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.putStrings(AS_VM1.tag, new String[]{"test1", "test2"});
        assertEquals("test1\\test2", dobj.getString(AS_VM1.tag));
    }

    @Test
    public void putStringsIntArray() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.putStrings(new int[]{AE_VM1.tag}, new String[]{"test1", "test2"});
        assertEquals("test1\\test2", dobj.getString(AE_VM1.tag));
    }

    @Test
    public void putStringsWithIntTagAndVR() {
        // TODO: The behavior of dobj.putStrings( int tag, String vr, String[] value) is broken.
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        String desiredVR = "SH";
        dobj.putStrings(AS_VM1.tag, desiredVR, new String[]{"test1", "test2"});
        assertEquals("test1\\test2", dobj.getString(AS_VM1.tag));
        // The specified VR is used despite the dictionary-defined value when element is created.
        assertEquals(desiredVR, dobj.getVR(AS_VM1.tag));
    }

    @Test
    public void putStringsWithIntArrayAndVR() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        String desiredVR = "SH";
        String desiredVR2 = "LO";
        dobj.putStrings(new int[]{AS_VM1.tag}, desiredVR, new String[]{"test1", "test2"});
        // The specified VR overrides the dictionary-defined value when element is created.
        assertEquals(desiredVR, dobj.getVR(AS_VM1.tag));
        dobj.putStrings(new int[]{AS_VM1.tag}, desiredVR2, new String[]{"test1", "test2"});
        // The specified VR is used despite the dictionary-defined value when element is updated.
        assertEquals(desiredVR2, dobj.getVR(AS_VM1.tag));
        dobj.putStrings(new int[]{UN_VM1.tag}, desiredVR, new String[]{"test1", "test2"});
        // The unknown element is given the specified VR when created.
        assertEquals(desiredVR, dobj.getVR(UN_VM1.tag));
        // The unknown element is given the specified VR when updated.
        dobj.putStrings(new int[]{UN_VM1.tag}, desiredVR, new String[]{"test1", "test2"});
        assertEquals(desiredVR, dobj.getVR(UN_VM1.tag));
    }

    @Test
    // TODO Changed
    public void putCreatorIDString() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        assertFalse(dobj.contains(PRIVATE_CREATOR_ID.tag));
        assertFalse(dobj.contains(PRIVATE_TAG.tag));
        dobj.putCreatorIDString(PRIVATE_TAG.tag, PRIVATE_CREATOR_ID.initialValue, PRIVATE_TAG.initialValue);
        assertTrue(dobj.contains(PRIVATE_CREATOR_ID.tag));
        assertTrue(dobj.contains(PRIVATE_TAG.tag));
    }

    @Test
    public void removeTag() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        assertTrue(dobj.contains(AS_VM1.tag));
        dobj.removeTag(AS_VM1.tag);
        assertFalse(dobj.contains(AS_VM1.tag));
    }

    @Test
    // TODO changed
    public void removePrivateTag() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.putCreatorIDString(PRIVATE_TAG.tag, PRIVATE_CREATOR_ID.initialValue, PRIVATE_TAG.initialValue);
        assertTrue(dobj.contains(PRIVATE_CREATOR_ID.tag));
        assertTrue(dobj.contains(PRIVATE_TAG.tag));
        dobj.removePrivateTag(PRIVATE_TAG.tag, PRIVATE_CREATOR_ID.initialValue);
        assertTrue(dobj.contains(PRIVATE_CREATOR_ID.tag));
        assertFalse(dobj.contains(PRIVATE_TAG.tag));
    }

    @Test
    public void deleteAllPrivateTags() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.putCreatorIDString(0x00170000, "PC17_1", "value0");
        dobj.putCreatorIDString(0x00170001, "PC17_2", "value1");
        assertTrue(dobj.contains(0x00170010));
        assertTrue(dobj.contains(0x00170011));
        assertTrue(dobj.contains(0x00171000));
        assertTrue(dobj.contains(0x00171101));
        dobj.deleteAllPrivateTags();
        assertFalse(dobj.contains(0x00170010));
        assertFalse(dobj.contains(0x00170011));
        assertFalse(dobj.contains(0x00171000));
        assertFalse(dobj.contains(0x00171101));
    }

    @Test
    public void deleteEmptyPrivateBlocks() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.putCreatorIDString(PRIVATE_TAG.tag, PRIVATE_CREATOR_ID.initialValue, PRIVATE_TAG.initialValue);
        assertTrue(dobj.contains(PRIVATE_CREATOR_ID.tag));
        assertTrue(dobj.contains(PRIVATE_TAG.tag));
        dobj.removePrivateTag(PRIVATE_TAG.tag, PRIVATE_CREATOR_ID.initialValue);
        assertTrue(dobj.contains(PRIVATE_CREATOR_ID.tag));
        assertFalse(dobj.contains(PRIVATE_TAG.tag));
        dobj.deleteEmptyPrivateBlocks();
        assertFalse(dobj.contains(PRIVATE_CREATOR_ID.tag));
    }

    @Test
    public void isEmptyPrivateBlock() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.putCreatorIDString(0x00170000, "PC17_1", "value0");
        dobj.putCreatorIDString(0x00170001, "PC17_2", "value1");
        assertTrue(dobj.contains(0x00170010));
        assertTrue(dobj.contains(0x00170011));
        assertTrue(dobj.contains(0x00171000));
        assertTrue(dobj.contains(0x00171101));
        dobj.removeTag(0x00171000);
        assertTrue(dobj.contains(0x00170010));
        assertTrue(dobj.contains(0x00170011));
        assertFalse(dobj.contains(0x00171000));
        assertTrue(dobj.contains(0x00171101));

        assertTrue(dobj.isEmptyPrivateBlock(0x00170010));
        assertFalse(dobj.isEmptyPrivateBlock(0x00170011));
        // Careful. Empty block but tag is not a private-creator-id tag.
        assertFalse(dobj.isEmptyPrivateBlock(0x00171010));
        // Private-creator-id tag does not have to be present.
        assertTrue(dobj.isEmptyPrivateBlock(0x00130011));
    }

    @Test
    public void toCompleteString() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AE_VM1);
        put(dobj, AS_VM1);
        dobj.toCompleteString();
        assertEquals("(0008,0054) AE [AETitle1] RetrieveAETitle\n" +
                "(0010,1010) AS [064Y] PatientAge\n", dobj.toCompleteString());
    }

    @Test
    public void toStringTest() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AE_VM1);
        put(dobj, AS_VM1);
        dobj.toString();
        assertEquals("(0008,0054) AE [AETitle1] RetrieveAETitle\n" +
                "(0010,1010) AS [064Y] PatientAge\n", dobj.toCompleteString());
    }

    @Test
    public void dump() throws IOException {
        File file = Files.createTempFile(null, null).toFile();
        file.deleteOnExit();
        try (PrintStream ps = new PrintStream(file)) {
            DicomObjectI dobj = DicomObjectFactory.newInstance();
            put(dobj, AS_VM1);
            dobj.dump(ps);
            assertEquals(22, file.length());
        }
    }

    // DicomElementI methods
    @Test
    public void elementTag() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        DicomElementI element = dobj.getElement(AS_VM1.tag);
        assertEquals(AS_VM1.tag, element.tag());
    }

    @Test
    public void elementTagString() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        DicomElementI element = dobj.getElement(AS_VM1.tag);
        assertEquals(Integer.toString(AS_VM1.tag, 16), element.tagString());
    }

    @Test
    public void elementHasItems() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        DicomElementI element = dobj.getElement(AS_VM1.tag);
        assertFalse(element.hasItems());
        TestSeqTag seqTag = new TestSeqTag(new int[]{0x00209221, 0, 0x00209164}, "value");
        put(dobj, seqTag);
        element = dobj.getElement(0x00209221);
        assertTrue(element.hasItems());
    }

    @Test
    public void elementCountItems() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestSeqTag seqTag = new TestSeqTag(new int[]{0x00209221, 0, 0x00209164}, "value");
        put(dobj, seqTag);
        DicomElementI element = dobj.getElement(0x00209221);
        assertEquals(1, element.countItems());
    }

    @Test
    public void elementGetDicomObject() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestSeqTag seqTag = new TestSeqTag(new int[]{0x00209221, 0, 0x00209164}, "value");
        put(dobj, seqTag);
        DicomElementI element = dobj.getElement(0x00209221);
        assertEquals("value", element.getDicomObject(0).getString(0x00209164));
    }

    @Test
    public void elementRemoveItem() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestSeqTag seqTag = new TestSeqTag(new int[]{0x00209221, 0, 0x00209164}, "value");
        put(dobj, seqTag);
        DicomElementI element = dobj.getElement(0x00209221);
        assertTrue(element.hasItems());
        element.removeItem(0);
        assertFalse(element.hasItems());
    }

    @Test
    public void elementIsUID() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        put(dobj, UI_VM1);
        assertFalse(dobj.getElement(AS_VM1.tag).isUID());
        assertTrue(dobj.getElement(UI_VM1.tag).isUID());
    }

    @Test
    public void elementGetVRasString() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AS_VM1);
        put(dobj, UI_VM1);
        assertEquals("AS", dobj.getElement(AS_VM1.tag).getVRAsString());
        assertEquals("UI", dobj.getElement(UI_VM1.tag).getVRAsString());
    }

    private final static double epsilon = 0.001;

    @Test
    public void putAndGetStringFLMultiple() {
        final float[] vals = new float[]{1.1f, 2.2f};
        final String[] strvals = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            strvals[i] = Float.toString(vals[i]);
        }
        final DicomObjectI o = DicomObjectFactory.newInstance();
        final int[] tp = new int[]{Tag.ObjectPixelSpacingInCenterOfBeam};
        assertEquals("FL", o.getVR(tp[tp.length-1]));
        o.putString(tp, String.join("\\", strvals));
        final String[] out = o.getString(tp).split("\\\\");
        IntStream.range(0, vals.length).forEach(i -> assertEquals(vals[i], Float.parseFloat(out[i]), epsilon));
    }

    @Test
    public void putAndGetStringFLSingle() {
        final float val = 10.0f;
        final DicomObjectI o = DicomObjectFactory.newInstance();
        final int[] tp = new int[]{Tag.ExaminedBodyThickness};
        assertEquals("FL", o.getVR(tp[tp.length-1]));
        o.putString(tp, Float.toString(val));
        assertEquals(val, Float.parseFloat(o.getString(tp)), epsilon);
    }

    @Test
    public void putAndGetStringFDMultiple() {
        final double[] vals = new double[]{0.0, 1.1, 2.2, 3.3, 4.4, 5.5, 6.6};
        final String[] strvals = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            strvals[i] = Double.toString(vals[i]);
        }
        final DicomObjectI o = DicomObjectFactory.newInstance();
        final int[] tp = new int[]{Tag.ImageOrientationVolume};
        assertEquals("FD", o.getVR(tp[tp.length-1]));
        o.putString(tp, String.join("\\", strvals));
        final String[] out = o.getString(tp).split("\\\\");
        IntStream.range(0, vals.length).forEach(i -> assertEquals(vals[i], Double.parseDouble(out[i]), epsilon));
    }

    @Test
    public void putAndGetStringFDSingle() {
        final double val = 1.2;
        final DicomObjectI o = DicomObjectFactory.newInstance();
        final int[] tp = new int[]{Tag.EventTimeOffset};
        assertEquals("FD", o.getVR(tp[tp.length-1]));
        o.putString(tp, Double.toString(val));
        assertEquals(val, Double.parseDouble(o.getString(tp)), epsilon);
    }

    @Test
    public void putAndGetStringSLMultiple() {
        final int[] vals = new int[]{1, 2, 3, 4, 5, 6};
        final String[] strvals = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            strvals[i] = Integer.toString(vals[i]);
        }
        final DicomObjectI o = DicomObjectFactory.newInstance();
        final int[] tp = new int[]{Tag.PixelCoordinatesSetTrial};
        assertEquals("SL", o.getVR(tp[tp.length-1]));
        o.putString(tp, String.join("\\", strvals));
        final String[] out = o.getString(tp).split("\\\\");
        IntStream.range(0, vals.length).forEach(i -> assertEquals(vals[i], Double.parseDouble(out[i]), epsilon));
    }

    @Test
    public void putAndGetStringSLSingle() {
        final int val = 42;
        final DicomObjectI o = DicomObjectFactory.newInstance();
        final int[] tp = new int[]{Tag.SelectorSLValue};
        assertEquals("SL", o.getVR(tp[tp.length-1]));
        o.putString(tp, Integer.toString(val));
        assertEquals(val, Integer.parseInt(o.getString(tp)));
    }

    @Test
    public void putAndGetStringSSMultiple() {
        final short[] vals = new short[]{9,10};
        final String[] strvals = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            strvals[i] = Short.toString(vals[i]);
        }
        final DicomObjectI o = DicomObjectFactory.newInstance();
        final int[] tp = new int[]{Tag.CenterOfCircularExposureControlSensingRegion};
        assertEquals("SS", o.getVR(tp[tp.length-1]));
        o.putString(tp, String.join("\\", strvals));
        final String[] out = o.getString(tp).split("\\\\");
        IntStream.range(0, vals.length).forEach(i -> assertEquals(vals[i], Short.parseShort(out[i]), epsilon));
    }

    @Test
    public void putAndGetStringSSSingle() {
        final short val = 19;
        final DicomObjectI o = DicomObjectFactory.newInstance();
        final int[] tp = new int[]{Tag.TagAngleSecondAxis};
        assertEquals("SS", o.getVR(tp[tp.length-1]));
        o.putString(tp, Short.toString(val));
        assertEquals(val, Short.parseShort(o.getString(tp)));
    }

    @Test
    public void putAndGetStringSVMultiple() {
        final long[] vals = new long[]{10,12};
        final String[] strvals = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            strvals[i] = Long.toString(vals[i]);
        }
        final DicomObjectI o = DicomObjectFactory.newInstance();
        final int[] tp = new int[]{0x00720082}; // Selector SV Value
        assertEquals("SV", o.getVR(tp[tp.length-1]));
        o.putString(tp, String.join("\\", strvals));
        final String[] out = o.getString(tp).split("\\\\");
        IntStream.range(0, vals.length).forEach(i -> assertEquals(vals[i], Long.parseLong(out[i]), epsilon));
    }

    @Test
    public void putAndGetStringSVSingle() {
        final long val = 23;
        final DicomObjectI o = DicomObjectFactory.newInstance();
        final int[] tp = new int[]{0x00720082}; // Selector SV Value
        assertEquals("SV", o.getVR(tp[tp.length-1]));
        o.putString(tp, Long.toString(val));
        assertEquals(val, Long.parseLong(o.getString(tp)));
    }
}