/*
 * DicomEdit: TestAssignmentStatement
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.tags.TagPrivate;
import org.nrg.dicom.mizer.tags.TagPublic;
import org.nrg.dicom.mizer.tags.TagSequence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.nrg.dicom.dicomedit.TestUtils.bytes;

/**
 * Run tests of assignment/initialization statements.
 * <p>
 * Assignment/Initialization statements have syntax: lvalue := value
 * lvalue is a tagpath or a variable. Tagpaths are singular or plural-valued (have wildcards). Assigning to a
 * plural-valued tagpath is an error.
 * value is one of String, number, tagpath, function or variable. Tagpaths are singular or pluraled valued. Assigning
 * from a plural-valued tagpath is an error.
 * <p>
 * Created by drm on 8/8/16.
 */
public class TestAssignmentStatement {
    public static final String ASSIGN_TO_PUBLIC_TAG_FROM_STRING = "(0008,0080) := \"New Institute\" \n";
    public static final String ASSIGN_TO_PVT_TAG_FROM_STRING = "(0019,{SIEMENS}10) := \"Edited SIEMENS pvt tag\" \n";
    public static final String ASSIGN_TO_PUBLIC_TAG_IN_SEQ_FROM_STRING = "(0008,1032)[1]/(0010,0010) := \"Assigned string.\" \n";
    public static final String ASSIGN_TO_PVT_TAG_IN_SEQ_FROM_STRING = "(0021,{PVT ID}01)[1]/(0010,0010) := \"Assigned string.\" \n";
    public static final String ASSIGN_TO_PLURAL_TAG_FROM_STRING = "(002#,{PVT}01)[1]/(0010,0010) := \"Assigned string.\"\n";
    public static final String ASSIGN_TO_VARIABLE_FROM_PUBLIC_TAG = "foo := (0010,0010)";
    public static final String ASSIGN_FROM_STRING_TO_PUBLIC_TAG = "(0010,0010) := \"snafu\" \n";
    public static final String ASSIGN_FROM_NUMBER_TO_PUBLIC_TAG = "(0010,0010) := 666\n";
    public static final String ASSIGN_FROM_SINGULAR_TAGPATH_TO_PUBLIC_TAG = "(0010,0010) := (0008,0020)\n";
    public static final String ASSIGN_FROM_PLURAL_TAGPATH_TO_PUBLIC_TAG = "(0010,0010) := (0008,002X)\n";
    public static final String ASSIGN_FROM_FUNCTION_TO_PUBLIC_TAG = "(0010,0010) := concatenate[ \"situation normal \", (0008,0020)]\n";
    public static final String ASSIGN_TO_SEQUENCE_WILDCARDS = "+/(0010,0010) := \"anonymized\"\n";

    // 'To' tests.  Test storing the value in all permutations of the left side of the assignment operator.
    @Test
    public void testAssignToExistingPublicTagFromString() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        int[] s = {0x00080080};
        src_dobj.putString(s, "Some Institution");

        assertTrue(src_dobj.contains(0x00080080));
        assertEquals(src_dobj.getString(s), "Some Institution");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_TO_PUBLIC_TAG_FROM_STRING));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(0x00080080), "New Institute");
    }

    @Test
    public void testAssignToNotExistingPublicTagFromString() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        assertFalse(src_dobj.contains(0x00080080));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_TO_PUBLIC_TAG_FROM_STRING));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(0x00080080), "New Institute");
    }

    @Test
    public void testAssignToExistingPrivateTagFromString() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        int tge = src_dobj.putCreatorIDString(0x00191010, "GE", "GE private string");
        int tsm = src_dobj.putCreatorIDString(0x00191010, "SIEMENS", "SIEMENS private string");
        assertEquals(src_dobj.getString(tge), "GE private string");
        assertEquals(src_dobj.getString(tsm), "SIEMENS private string");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_TO_PVT_TAG_FROM_STRING));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(tge), "GE private string");
        assertEquals(result_dobj.getString(tsm), "Edited SIEMENS pvt tag");
    }

    @Test
    public void testAssignToNotExistingPrivateTagFromString() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        int tge = src_dobj.putCreatorIDString(0x00191010, "GE", "GE private string");
        assertEquals(src_dobj.getString(tge), "GE private string");
        TagPath tp = new TagPath();
        tp.addTag(new TagPrivate(0x00191010, "SIEMENS"));

        assertNull(src_dobj.getString(tp));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_TO_PVT_TAG_FROM_STRING));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(tge), "GE private string");
        assertEquals(result_dobj.getString(tp), "Edited SIEMENS pvt tag");
    }

    @Test
    public void testAssignToExistingPublicTagInSequenceFromString() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        int[] s0 = {0x00081032, 0, 0x00100010};
        int[] s1 = {0x00081032, 1, 0x00100010};
        src_dobj.putString(s0, "s0");
        src_dobj.putString(s1, "s1");

        assertEquals(src_dobj.getString(s0), "s0");
        assertEquals(src_dobj.getString(s1), "s1");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_TO_PUBLIC_TAG_IN_SEQ_FROM_STRING));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(s0), "s0");
        assertEquals(result_dobj.getString(s1), "Assigned string.");
    }

    @Test
    public void testAssignToNotExistingPublicTagInSequenceFromString() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        int[] s0 = {0x00081032, 0, 0x00100010};
        int[] s1 = {0x00081032, 1, 0x00100010};
        src_dobj.putString(s0, "s0");

        assertEquals(src_dobj.getString(s0), "s0");
        assertTrue(StringUtils.isEmpty(src_dobj.getString(s1)));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_TO_PUBLIC_TAG_IN_SEQ_FROM_STRING));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(s0), "s0");
        assertEquals(result_dobj.getString(s1), "Assigned string.");
    }

    @Test
    public void testAssignToExistingPrivateTagInSequenceFromString() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        TagPath tp1 = new TagPath();
        tp1.addTag(new TagSequence(new TagPrivate(0x00211001, "PVT ID"), 0)).addTag(new TagPublic(0x00100010));
        TagPath tp2 = new TagPath();
        tp2.addTag(new TagSequence(new TagPrivate(0x00211001, "PVT ID"), 1)).addTag(new TagPublic(0x00100010));
        src_dobj.assign(tp1, "s0");
        src_dobj.assign(tp2, "s1");

        int[] s = {0x00210010};
        int[] s0 = {0x00211001, 0, 0x00100010};
        int[] s1 = {0x00211001, 1, 0x00100010};
        assertEquals("PVT ID", src_dobj.getString(s));
        assertEquals("s0", src_dobj.getString(s0));
        assertEquals("s1", src_dobj.getString(s1));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_TO_PVT_TAG_IN_SEQ_FROM_STRING));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals("PVT ID", result_dobj.getString(s));
        assertEquals("s0", result_dobj.getString(s0));
        assertEquals("Assigned string.", result_dobj.getString(s1));
    }

    @Test
    public void testAssignToNotExistingPrivateTagInSequenceFromString() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

//        TagPath tp1 = new TagPath();
//        tp1.addTag( new TagSequence( new TagPrivate( 0x00211001, "PVT"), 0)).addTag( new TagPublic( 0x00100010));
//        TagPath tp2 = new TagPath();
//        tp2.addTag( new TagSequence( new TagPrivate( 0x00211001, "PVT"), 1)).addTag( new TagPublic( 0x00100010));
//        src_dobj.assign( tp1, "s0");
//        src_dobj.assign( tp2, "s1");

        int[] s = {0x00210010};
        int[] s0 = {0x00211001, 0, 0x00100010};
        int[] s1 = {0x00211001, 1, 0x00100010};
        assertTrue(StringUtils.isEmpty(src_dobj.getString(s)));
        assertTrue(StringUtils.isEmpty(src_dobj.getString(s0)));
        assertTrue(StringUtils.isEmpty(src_dobj.getString(s1)));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_TO_PVT_TAG_IN_SEQ_FROM_STRING));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(s), "PVT ID");
        assertTrue(StringUtils.isEmpty(src_dobj.getString(s0)));
        assertEquals(result_dobj.getString(s1), "Assigned string.");
    }

    @Test
    public void testAssignToNonSingularPublicTagPathFromString() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_TO_PLURAL_TAG_FROM_STRING));
        final AnonymizationResult result = sa.apply(src_dobj);
        assertTrue(result instanceof AnonymizationResultError);
    }

    @Test
    public void testAssignToVariableFromPublicTag() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        int[] s = {0x00100010};
        src_dobj.putString(s, "Patient^Name");

        assertTrue(src_dobj.contains(0x00100010));
        assertEquals(src_dobj.getString(s), "Patient^Name");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_TO_VARIABLE_FROM_PUBLIC_TAG));

        assertNull(sa.getValue("foo"));

        sa.apply(src_dobj);

        assertEquals(sa.getValue("foo").asString(), "Patient^Name");
    }


    // 'From' tests. Test getting the correct values from the right side of the assignment op.
    @Test
    public void testAssignFromStringToPublicTag() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        int[] s = {0x00100010};
        src_dobj.putString(s, "Patient^Name");

        assertTrue(src_dobj.contains(0x00100010));
        assertEquals(src_dobj.getString(s), "Patient^Name");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_FROM_STRING_TO_PUBLIC_TAG));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(0x00100010), "snafu");
    }

    @Test
    public void testAssignFromNumberToPublicTag() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        int[] s = {0x00100010};
        src_dobj.putString(s, "Patient^Name");

        assertTrue(src_dobj.contains(0x00100010));
        assertEquals(src_dobj.getString(s), "Patient^Name");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_FROM_NUMBER_TO_PUBLIC_TAG));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(0x00100010), Integer.toString(666));
    }

    @Test
    public void testAssignFromSingularTagPathToPublicTag() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        int[] s1 = {0x00100010};
        src_dobj.putString(s1, "Patient^Name");
        int[] s2 = {0x00080020};
        src_dobj.putString(s2, "fubar");

        assertTrue(src_dobj.contains(0x00100010));
        assertEquals(src_dobj.getString(s1), "Patient^Name");
        assertEquals(src_dobj.getString(s2), "fubar");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_FROM_SINGULAR_TAGPATH_TO_PUBLIC_TAG));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(0x00100010), "fubar");
        assertEquals(result_dobj.getString(0x00080020), "fubar");
    }

    @Test
    public void testAssignFromPluralTagPathToPublicTag() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        int[] s1 = {0x00100010};
        src_dobj.putString(s1, "Patient^Name");

        assertTrue(src_dobj.contains(0x00100010));
        assertEquals(src_dobj.getString(s1), "Patient^Name");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_FROM_PLURAL_TAGPATH_TO_PUBLIC_TAG));
        final AnonymizationResult result = sa.apply(src_dobj);
        assertTrue(result instanceof AnonymizationResultError);
    }

    @Test
    public void testAssignFromFunctionToPublicTag() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        int[] s1 = {0x00100010};
        src_dobj.putString(s1, "Patient^Name");
        int[] s2 = {0x00080020};
        src_dobj.putString(s2, "all f*ed up.");

        assertTrue(src_dobj.contains(0x00100010));
        assertEquals(src_dobj.getString(s1), "Patient^Name");
        assertEquals(src_dobj.getString(s2), "all f*ed up.");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_FROM_FUNCTION_TO_PUBLIC_TAG));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(0x00100010), "situation normal all f*ed up.");
        assertEquals(result_dobj.getString(0x00080020), "all f*ed up.");
    }

    @Test
    public void testAssignToSequenceWildcard() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        int[] s1 = {0x00100010};
        dobj.putString(s1, "Patient^Name");

        assertTrue(dobj.contains(0x00100010));
        assertEquals(dobj.getString(s1), "Patient^Name");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(ASSIGN_TO_SEQUENCE_WILDCARDS));
        final AnonymizationResult result = sa.apply(dobj);
        assertTrue(result instanceof AnonymizationResultError);
    }
}
