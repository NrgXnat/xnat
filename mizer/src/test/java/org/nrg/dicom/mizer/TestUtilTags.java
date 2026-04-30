package org.nrg.dicom.mizer;

import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.*;

import static org.nrg.dicom.mizer.TestUtils.*;

/**
 * Create a set of arbitrary tags that cover all known VRs.
 * The tags should be in the data dictionary unless noted otherwise.
 * These tags must match the contents of the DICOM file vr.dcm
 * <p>
 * These are the known VRs as of 2023d
 * Application Entity AE
 * Age String AS
 * Attribute Tag AT
 * Code String CS
 * Date DA
 * Decimal String DS
 * DateTime DT
 * Floating Point Single FL
 * Floating Point Double FD
 * Integer String IS
 * Long String LO
 * Long Text LT
 * Other Byte OB
 * Other Double OD
 * Other Float OF
 * Other Long OL
 * Other Very Long OV
 * Other Word OW
 * Person Name PN
 * Short String SH
 * Signed Long SL
 * Sequence Items SQ
 * Signed Short SS
 * Short Text ST
 * Signed 64-bit very Long SV
 * Time TM
 * Unlimited Characters UC
 * Unique Identifier UI
 * Unsigned Long UL
 * Unknown UN
 * URL or URI UR
 * Unsigned Short US
 * Unlimited Text UT
 * Unsigned 64-bit Very Long UV
 */
public class TestUtilTags {

    // Application Entity AE
    // Retrieve Ae Title, VM 1-n
    public static TestTag AE_VM1 = new TestTag(0x00080054, "AETitle1", "Replaced");
    public static TestTag AE_VM2 = new TestTag(0x00080054, "AETitle1\\AETitle2");

    // Age String AS
    // PatientAge vm = 1
    public static TestTag AS_VM1 = new TestTag(0x00101010, "064Y", "077Y");

    // Attribute Tag AT
    // OriginalImageIdentification    VM 1-n
//    (0020,5000)	AT	Original Image Identification	Retired
    public static TestTag AT_VM1 = new TestTag(0x00205000, "00205000", "00205000");
    public static TestTag AT_VM1_REPLACED = new TestTag(0x00205000, "StudyInstanceUID", "0020000D");

    // Code String CS
    // SpecificCharacterSet VM 1-n
    public static TestTag CS_VM1 = new TestTag(0x00080005, "codedString", "cs_replaced");

    // Date DA
    // StudyDate VM 1
    public static TestTag DA_VM1 = new TestTag(0x00080020, "19600519", "19620706");

    // Decimal String DS
    // EventElapsedTimes VM 1-n
    public static TestTag DS_VM1 = new TestTag(0x00082130, "-42", "66");

    // DateTime DT
    // AcquisitionDateTime VM 1
    public static TestTag DT_VM1 = new TestTag(0x0008002A, "19600519132115", "19620706223344");

    // Floating Point Single FL
    // RecommendedDisplayFrameRateInFloat VM 1
    public static TestTag FL_VM1 = new TestTag(0x00089459, "3.14", "2.03");

    // Floating Point Double FD
    // EventTimeOffset VM 1
    public static TestTag FD_VM1 = new TestTag(0x00082134, "6.28", "12.56");

    // Integer String IS
    // ReferencedFrameNumber VM 1-n
    public static TestTag IS_VM1 = new TestTag(0x00081160, "12345", "23456");

    // Long String LO
    // EventTimerNames VM 1-n
    public static TestTag LO_VM1 = new TestTag(0x00082132, "longString", "ls_replaced");

    // Long Text LT
    // ImageComments VM 1
    public static TestTag LT_VM1 = new TestTag(0x00204000, "longText", "lt_replaced");

    // Other Byte OB
    // BadPixelImage VM 1
    public static TestTag OB_VM1 = new TestTag(0x00143080, "otherByte", "ob_replaced", "6F\\74\\68\\65\\72\\42\\79\\74\\65");

    // Other Double OD
    // SelectorODValue VM 1
    public static TestTag OD_VM1 = new TestTag(0x00720073, "6.6");

    // Other Float OF
    public static TestTag OF_VM1 = new TestTag(0x00640009, "-1.2", "-36.9");

    // Other Long OL
    // SelectorOLValue VM 1
    public static TestTag OL_VM1 = new TestTag(0x00720075, "-1", "-2");

    // Other Very Long OV
    // SelectorOVValue VM 1
    public static TestTag OV_VM1 = new TestTag(0x00720081, "1", "2");

    // Other Word OW
    // RedPaletteColorLookupTableData VM 1
    // The string must be even in length.
    public static TestTag OW_VM1 = new TestTag(0x00281201, "otherWords", "-1", "29807\\25960\\22386\\29295\\29540");

    // Person Name PN
    // PatientName VM 1
    public static TestTag PN_VM1 = new TestTag(0x00100010, "Doe^John", "Replaced^Jane");

    // Short String SH
    // PatientTelephoneNumbers VM 1-n
    public static TestTag SH_VM1 = new TestTag(0x00102154, "shortString", "sh_replaced");
    public static TestTag SH_VM2 = new TestTag(0x00102154, "shortString1/shortString2");

    // Signed Long SL
    // SelectorSLValue VM 1-n
    public static TestTag SL_VM1 = new TestTag(0x0072007C, "-1", "-22");

    // Sequence Items SQ
    // FailedSOPSequence VM 1
    public static TestTag SQ_VM1 = new TestTag(0x00081198, "");

    // Signed Short SS
    // SelectorSSValue VM 1-n
    public static TestTag SS_VM1 = new TestTag(0x0072007E, "-134", "-999");

    // Short Text ST
    // ComponentManufacturer
    public static TestTag ST_VM1 = new TestTag(0x00140028, "shortText", "st_replaced");

    // Signed 64-bit very Long SV
    // SelectorSVValue VM 1-n
    public static TestTag SV_VM1 = new TestTag(0x00720082, "123456", "-123456");

    // Time TM
    // InstanceCreationTime VM 1
    public static TestTag TM_VM1 = new TestTag(0x00080013, "132433", "141516");

    // Unlimited Characters UC
    // LongCodeValue VM 1
    public static TestTag UC_VM1 = new TestTag(0x00080119, "unlimitedCharacters", "uc_replaced");

    // Unique Identifier UI
    // ContextUID VM 1
    public static TestTag UI_VM1 = new TestTag(0x00080117, "1.2.3.4.5", "2.2.3.3.11");

    // Unsigned Long UL
    // SimpleFrameList VM 1-n
    public static TestTag UL_VM1 = new TestTag(0x00081161, "1", "2");

    // Unknown UN
    public static TestTag UN_VM1 = new TestTag(0x66666666, "unknown", "un_replaced");

    // URL or URI UR
    // URNCodeValue VM 1
    // This is in dcm4che2 but not as UR?
    public static TestTag UR_VM1 = new TestTag(0x00080120, "https://org.snafu", "https://org.replaced");

    // Unsigned Short US
    // PregnancyStatus VM 1
    public static TestTag US_VM1 = new TestTag(0x001021C0, "42", "24");

    // Unlimited Text UT
    // TextValue VM 1
    public static TestTag UT_VM1 = new TestTag(0x0040A160, "unlimitedText", "ut_replaced");

    // Unsigned 64-bit Very Long UV
    // SelectorUVValue VM 1-n
    public static TestTag UV_VM1 = new TestTag(0x00720083, "1111", "2222");

    // Tag not in dictionary but with provided VR in vr.dcm
    public static TestTag MYSTERY_TAG_KNOWN_VR = new TestTag(0x66660666, "unknown_tag_with_known_VR", "replaced");

    public static int NUMBER_OF_TAGS_IN_ALL_VRS_OBJECT = 34;

    public static TestTag PRIVATE_CREATOR_ID = new TestTag(0x00170010, "private creator id");
    public static TestTag PRIVATE_TAG = new TestTag(0x0017101A, "private value");

    public static DicomObjectI createDicomObject_All_VRs() {

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, AE_VM1);
        put(dobj, AS_VM1);
        put(dobj, AT_VM1);
        put(dobj, CS_VM1);
        put(dobj, DA_VM1);
        put(dobj, DS_VM1);
        put(dobj, DT_VM1);
        put(dobj, FL_VM1);
        put(dobj, FD_VM1);
        put(dobj, IS_VM1);
        put(dobj, LO_VM1);
        put(dobj, LT_VM1);
        put(dobj, OB_VM1);
        put(dobj, OD_VM1);
        put(dobj, OF_VM1);
        put(dobj, OL_VM1);
        put(dobj, OV_VM1);
        put(dobj, OW_VM1);
        put(dobj, PN_VM1);
        put(dobj, SH_VM1);
        put(dobj, SL_VM1);
        // SQ elements do not have values. Unsupported op.
        // put(dobj, SQ_VM1);
        put(dobj, SS_VM1);
        put(dobj, ST_VM1);
        put(dobj, SV_VM1);
        put(dobj, TM_VM1);
        put(dobj, UC_VM1);
        put(dobj, UI_VM1);
        put(dobj, UL_VM1);
        put(dobj, UN_VM1);
        put(dobj, UR_VM1);
        put(dobj, US_VM1);
        put(dobj, UT_VM1);
        put(dobj, UV_VM1);
        put(dobj, MYSTERY_TAG_KNOWN_VR);
        return dobj;
    }


    // Types of tags
    public static TestTag TAG_PRESENT = new TestTag(0x00100010, "tag_present");
    public static TestTag TAG_PRESENT_EMPTY = new TestTag(0x0020000D, "");
    public static TestTag TAG_MISSING = new TestTag(0x00100020, "tag_missing");
    public static TestTag UNKNOWN_TAG_PRESENT = new TestTag(0x66666660, "unknown_tag_present");
    public static TestTag UNKNOWN_TAG_PRESENT_EMPTY = new TestTag(0x66666661, "");
    public static TestTag UNKNOWN_TAG_MISSING = new TestTag(0x66666662, "unknown_tag_missing");
    public static TestTag PCID = new TestTag(0x00190010, "pcid");
    public static TestTag PCID_MISSING = new TestTag(0x00210010, "pcid_missing");
    public static TestTag PT_WITH_PCID_PRESENT = new TestTag(0x00191001, "pt_with_pcid_present");
    public static TestTag PT_WITH_PCID_MISSING = new TestTag(0x00191002, "pt_with_pcid_missing");
    public static TestTag PT_WITHOUT_PCID_PRESENT = new TestTag(0x00211011, "pt_without_pcid_present");
    public static TestTag PT_WITHOUT_PCID_MISSING = new TestTag(0x00211012, "pt_without_pcid_missing");

    public static TestTag STAG_MISSING = new TestTag(0x00080220, "ignored");
    public static TestTag STAG_PRESENT_NO_ITEMS = new TestTag(0x00080110, "ignored");
    public static TestSeqTag STAG_PRESENT_ONE_ITEM_EMPTY = new TestSeqTag(new int[]{0x00080121, 0}, "ignored");
    public static TestSeqTag STAG_PRESENT_ONE_ITEM_TAG_WITH_VALUE = new TestSeqTag(new int[]{0x00209221, 0, 0x00209164}, "stag_present");
    public static TestSeqTag STAG_PRESENT_ONE_ITEM_TAG_EMPTY_VALUE = new TestSeqTag(new int[]{0x00209221, 0, 0x0020000D}, "");
    public static TestSeqTag STAG_PRESENT_ONE_ITEM_WITH_TAG_NOT_PRESENT = new TestSeqTag(new int[]{0x00209221, 0, 0x00100010}, "stag_missing");
    public static TestTag UNKNOWN_STAG_MISSING = new TestTag(0x66606660, "ignored");
    public static TestTag UNKNOWN_STAG_PRESENT_NO_ITEMS = new TestTag(0x66606661, "ignored");
    public static TestSeqTag UNKNOWN_STAG_PRESENT_ONE_ITEM_EMPTY = new TestSeqTag(new int[]{0x66606662, 0}, "ignored");
    public static TestSeqTag UNKNOWN_STAG_ONE_ITEM_TAG_WITH_VALUE = new TestSeqTag(new int[]{0x00209221, 0, 0x66666660}, "unknown_stag_present");
    public static TestSeqTag UNKNOWN_STAG_ONE_ITEM_TAG_EMPTY_VALUE = new TestSeqTag(new int[]{0x00209221, 0, 0x66666661}, "");
    public static TestSeqTag UNKNOWN_STAG_ONE_ITEM_WITH_TAG_NOT_PRESENT = new TestSeqTag(new int[]{0x00209221, 0, 0x66666662}, "unknown_stag_missing");
    public static TestSeqTag SPCID = new TestSeqTag(new int[]{0x00209221, 1, 0x00150010}, "spcid");
    public static TestSeqTag SPT_WITH_PCID_PRESENT = new TestSeqTag(new int[]{0x00209221, 1, 0x00151001}, "spt_with_pcid_present");
    public static TestSeqTag SPT_WITH_PCID_MISSING = new TestSeqTag(new int[]{0x00209221, 1, 0x00151002}, "spt_with_pcid_missing");
    public static TestSeqTag SPT_WITHOUT_PCID_PRESENT = new TestSeqTag(new int[]{0x00209221, 1, 0x00171001}, "spt_without_pcid_present");
    public static TestSeqTag SPT_WITHOUT_PCID_MISSING = new TestSeqTag(new int[]{0x00209221, 1, 0x00171002}, "spt_without_pcid_missing");
    public static TestSeqTag PT_WITH_ONE_ITEM_TAG_WITH_VALUE = new TestSeqTag(new int[]{0x55551000, 1, 0x00171002}, "pt-seq-item-value");

    public static TagPath TAGPATH_TAG_PRESENT = new TagPath(new TagPublic(TAG_PRESENT.tag));
    public static TagPath TAGPATH_TAG_PRESENT_EMPTY = new TagPath(new TagPublic(TAG_PRESENT_EMPTY.tag));
    public static TagPath TAGPATH_TAG_MISSING = new TagPath(new TagPublic(TAG_MISSING.tag));
    public static TagPath TAGPATH_UNKNOWN_TAG_PRESENT = new TagPath(new TagPublic(UNKNOWN_TAG_PRESENT.tag));
    public static TagPath TAGPATH_UNKNOWN_TAG_PRESENT_EMPTY = new TagPath(new TagPublic(UNKNOWN_TAG_PRESENT_EMPTY.tag));
    public static TagPath TAGPATH_UNKNOWN_TAG_MISSING = new TagPath(new TagPublic(UNKNOWN_TAG_MISSING.tag));
    public static TagPath TAGPATH_PCID = new TagPath(new TagPrivateCreator(PCID.tag, PCID.initialValue));
    public static TagPath TAGPATH_PCID_MISSING = new TagPath(new TagPrivateCreator(PCID_MISSING.tag, PCID_MISSING.initialValue));
    public static TagPath TAGPATH_PT_WITH_PCID_PRESENT = new TagPath(new TagPrivate(PT_WITH_PCID_PRESENT.tag, PCID.initialValue));
    public static TagPath TAGPATH_PT_WITH_PCID_MISSING = new TagPath(new TagPublic(PT_WITH_PCID_MISSING.tag));
    public static TagPath TAGPATH_PT_WITHOUT_PCID_PRESENT = new TagPath(new TagPublic(PT_WITHOUT_PCID_PRESENT.tag));
    public static TagPath TAGPATH_PT_WITHOUT_PCID_MISSING = new TagPath(new TagPublic(PT_WITHOUT_PCID_MISSING.tag));

    public static TagPath TAGPATH_STAG_PRESENT = new TagPath(new TagSequence(new TagPublic(0x00209221), 0)).addTag(new TagPublic(0x00209164));
    public static TagPath TAGPATH_STAG_PRESENT_EMPTY = new TagPath(new TagSequence(new TagPublic(0x00209221), 0)).addTag(new TagPublic(0x0020000D));
    public static TagPath TAGPATH_STAG_MISSING = new TagPath(new TagSequence(new TagPublic(0x00209221), 0)).addTag(new TagPublic(0x00100010));
    public static TagPath TAGPATH_UNKNOWN_STAG_PRESENT = new TagPath(new TagSequence(new TagPublic(0x00209221), 0)).addTag(new TagPublic(0x66666660));
    public static TagPath TAGPATH_UNKNOWN_STAG_PRESENT_EMPTY = new TagPath(new TagSequence(new TagPublic(0x00209221), 0)).addTag(new TagPublic(0x66666661));
    public static TagPath TAGPATH_UNKNOWN_STAG_MISSING = new TagPath(new TagSequence(new TagPublic(0x00209221), 0)).addTag(new TagPublic(0x66666662));
    //    public static TagPath TAGPATH_SPCID= new TagPath(new TagSequence(new TagPublic(0x00209221),1)).addTag(new TagPrivateCreator(0x00150010, "spcid"));
    public static TagPath TAGPATH_SPCID = new TagPath(new TagSequence(new TagPublic(0x00209221), 1)).addTag(new TagPrivate(0x00150010, "spcid"));
    public static TagPath TAGPATH_SPT_WITH_PCID_PRESENT = new TagPath(new TagSequence(new TagPublic(0x00209221), 1)).addTag(new TagPrivate(0x00151001, "spcid"));
    public static TagPath TAGPATH_SPT_WITH_PCID_MISSING = new TagPath(new TagSequence(new TagPublic(0x00209221), 1)).addTag(new TagPrivate(0x00151002, "spcid"));
    public static TagPath TAGPATH_SPT_WITHOUT_PCID_PRESENT = new TagPath(new TagSequence(new TagPublic(0x00209221), 1)).addTag(new TagPrivate(0x00171001, ""));
    public static TagPath TAGPATH_SPT_WITHOUT_PCID_MISSING = new TagPath(new TagSequence(new TagPublic(0x00209221), 1)).addTag(new TagPrivate(0x00171002, ""));
    public static TagPath TAGPATH_MATCHES_MULTIPLE_TAGS = new TagPath(new TagPublic("0010", "00X0"));

    public static DicomObjectI createDicomObject_TagTypes() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, TAG_PRESENT);
        put(dobj, TAG_PRESENT_EMPTY);
        put(dobj, UNKNOWN_TAG_PRESENT);
        put(dobj, UNKNOWN_TAG_PRESENT_EMPTY);
        put(dobj, PCID);
        put(dobj, PT_WITH_PCID_PRESENT);
        put(dobj, PT_WITHOUT_PCID_PRESENT);
        put(dobj, STAG_PRESENT_NO_ITEMS);
        put(dobj, STAG_PRESENT_ONE_ITEM_EMPTY);
        put(dobj, STAG_PRESENT_ONE_ITEM_TAG_WITH_VALUE);
        put(dobj, STAG_PRESENT_ONE_ITEM_TAG_EMPTY_VALUE);
        put(dobj, UNKNOWN_STAG_PRESENT_NO_ITEMS);
        put(dobj, UNKNOWN_STAG_PRESENT_ONE_ITEM_EMPTY);
        put(dobj, UNKNOWN_STAG_ONE_ITEM_TAG_WITH_VALUE);
        put(dobj, UNKNOWN_STAG_ONE_ITEM_TAG_EMPTY_VALUE);
        put(dobj, SPCID);
        put(dobj, SPT_WITH_PCID_PRESENT);
        put(dobj, SPT_WITHOUT_PCID_PRESENT);
        put(dobj, PT_WITH_ONE_ITEM_TAG_WITH_VALUE);
        return dobj;
    }

    public static TestTag TAG_PUBLIC_KNOWN = new TestTag(0x00100010, "public_known_created", "public_known_updated");
    public static TestTag TAG_PUBLIC_UNKNOWN = new TestTag(0x06660010, "public_unknown_created", "public_unknown_updated");
    public static TestTag TAG_PRIVATE_CREATOR_ID = new TestTag(0x00210010, "pcid_created", "pcid_updated");
    public static TestTag TAG_PRIVATE_WITH_ID = new TestTag(0x002110cc, "private_with_id_created", "private_with_id_updated");
    public static TestTag TAG_PRIVATE_WITH_WRONG_PVT_BLOCK = new TestTag(0x002113cc, "private_with_bad_block_created", "private_with_bad_block_updated");
    public static TestTag TAG_PRIVATE_WITHOUT_ID = new TestTag(0x00311011, "private_without_id_created", "private_without_id_updated");
    public static TestTag TAG_MISSING_PCID = new TestTag(0x00310011, "missing_pcid_created", "missing_pcid_updated");
    public static TestTag TAG_PCID_IN_MISSING_PCID_GROUP = new TestTag(0x00310010, "known-pcid-in-block");
    public static TestTag TAG_PRIVATE_WITHOUT_ID_AFTER_CREATION = new TestTag(0x00311111, "created", "updated");
    public static TestTag TAG_SEQ_KNOWN = new TestTag(0x00080110, "known");
    public static TestSeqTag KNOWN_SEQ_TAG_NO_ITEMS = new TestSeqTag(new int[]{0x00081032}, "created", "updated");
    public static TestSeqTag KNOWN_SEQ_TAG_ITEM = new TestSeqTag(new int[]{0x00081032, 0}, "known");
    public static TestSeqTag KNOWN_SEQ_TAG_WITH_POPULATED_ITEM = new TestSeqTag(new int[]{0x00081032, 0, 0x00100010}, "created", "updated");
    public static TestSeqTag UNKNOWN_SEQ_TAG_WITH_EMPTY_ITEM = new TestSeqTag(new int[]{0x00660066, 0}, "created", "updated");
    public static TestSeqTag UNKNOWN_SEQ_TAG_WITH_POPULATED_ITEM = new TestSeqTag(new int[]{0x00660066, 0, 0x00100010}, "created", "updated");

}
