/*
 * DicomEdit: TestScriptApplicator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Tag;
import org.dcm4che3.util.UIDUtils;
import org.junit.Test;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultSeverity;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.junit.Assert.*;

/**
 * Unit test for simple ScriptApplicator.
 */
public class TestScriptApplicator {

    private static final String ORIG_INSITUTION = "Hospital";
    private static final String NEW_INSTITUTION = "My Test Institution";

    private static final String S_ASSIGN_STRING_TO_PUBLIC_TAG = "(0008,0080) := \"" + NEW_INSTITUTION + "\"\n";

    private static final String S_COND_EQ = "(0020,0011) = \"5\" ? (0008,103E) := \"Series Five\"\n"
                                            + "lowercase[(0020,0011)] = \"6\" ? (0008,103E) := \"Series Six\"\n"
                                            + "(0020,0011) = \"4\" ? - (0008,103E)\n";

    private static final String S_COND_RE = "(0020,0011) ~ \"[45]\" ? (0008,103e) := format[\"Four or five: {0}\", (0020,0011)]\n"
                                            + "(0020,0011) ~ \"[6-9]\" ? (0008,103e) := format[\"Six through nine: {0}\", (0020,0011)]\n";

    private static final String S_PRECEDENCE = "(0020,0011) = \"4\" ? (0008,103e) := \"Series Four\"\n"
                                               + "(0008,103e) := \"Some other series\"\n";

    private static final String S_COMMENT = "// (0008,103e) := \"foo\"\n(0008,103e) := \"bar\"\n";

    private static final String S_USE_VAR = "(0008,103e) := studyDescription\n";

    private static final String S_USE_SUBSTR = "(0010,0020) := format[\"{0}_{1}\", (0010,0020), substring[(0008,0020),2,8]]\n";

    private static final String S_NEW_UID = "(0020,000E) := newUID[]\n";

    private static final String S_INIT_VAR = "studyDescription := \"var init\"\n";

    private static final String S_USE_FORMAT = "(0008,103e) := format[\"{0}_{1}\", (0008,103e), \"extended\"]\n";

    private static final String S_USE_REPLACE = "(0008,103e) := replace[(0008,103e), \"_\", \":\"]\n";

    private static final String S_USE_HASHUID = "(0020,000d) := hashUID[(0020,000d)]\n";

    private static final String S_DESCRIPTION = "describe foo \"Description\"\nbar := foo\n";

    private static final String S_EXPORT_FIELD = "export foo \"baz:/my/export/path\"\nbar := foo\n";

    private static final String S_HIDDEN = "foo := \"bar\"\ndescribe foo hidden\n";

    private static final String S_REMOVE_PRIVATE = "removeAllPrivateTags\n";

    private static final String S_REMOVE_SINGIDX_NESTED = "-(0008,1140)[1]/(0008,1155)\n";

    private static final String S_REMOVE_MULTIDX_NESTED = "-(0008,1140)[%]/(0008,1155)\n";

    private static final String S_REMOVE_NESTED = "-*/(0008,1155)\n"; // "-*(0008,1155)\n";

    private static final String S_LOWERCASE_NULL_VALUE = "-(0008,0070)\nfoo := lowercase[(0008,0070)]";

    private static final String ERROR_EVALUATING_LOWERCASE_EXPECTED_MESSAGE = "Error evaluating lowercase";

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    public TestScriptApplicator() throws Exception {
    }

    @Test
    public void testApplyFile() throws Exception {
        final DicomObjectI do4 = DicomObjectFactory.newInstance(FILE4);
        //	final DicomObject do5 = (DicomObject)loader.apply(f5);
        final DicomObjectI do6 = DicomObjectFactory.newInstance(FILE6);

        final BaseScriptApplicator s_assign   = BaseScriptApplicator.getInstance( bytes(S_ASSIGN_STRING_TO_PUBLIC_TAG));
        final DicomObjectI do6_assign = s_assign.apply(FILE6).getDicomObject();

        assertEquals(ORIG_INSITUTION, do6.getString(Tag.InstitutionName));
        assertEquals(NEW_INSTITUTION, do6_assign.getString(Tag.InstitutionName));
        assertTrue(do6_assign.contains(0x00100020));

        final BaseScriptApplicator s_cond_eq   = BaseScriptApplicator.getInstance( bytes(S_COND_EQ));
        final DicomObjectI      do4_cond_eq = s_cond_eq.apply(FILE4).getDicomObject();
        final DicomObjectI      do5_cond_eq = s_cond_eq.apply(FILE5).getDicomObject();
        final DicomObjectI      do6_cond_eq = s_cond_eq.apply(FILE6).getDicomObject();

        assertTrue(do4.contains(Tag.SeriesDescription));
        assertFalse(do4_cond_eq.contains(Tag.SeriesDescription));
        assertEquals("Series Five", do5_cond_eq.getString(Tag.SeriesDescription));
        assertEquals("Series Six", do6_cond_eq.getString(Tag.SeriesDescription));

        final BaseScriptApplicator s_cond_re   = BaseScriptApplicator.getInstance( bytes(S_COND_RE));
        final DicomObjectI      do4_cond_re = s_cond_re.apply(FILE4).getDicomObject();
        final DicomObjectI      do5_cond_re = s_cond_re.apply(FILE5).getDicomObject();
        final DicomObjectI      do6_cond_re = s_cond_re.apply(FILE6).getDicomObject();

        assertEquals("Four or five: 4", do4_cond_re.getString(Tag.SeriesDescription));
        assertEquals("Four or five: 5", do5_cond_re.getString(Tag.SeriesDescription));
        assertEquals("Six through nine: 6", do6_cond_re.getString(Tag.SeriesDescription));

        final BaseScriptApplicator s_precedence = BaseScriptApplicator.getInstance( bytes(S_PRECEDENCE));
        final DicomObjectI      do4_prec     = s_precedence.apply(FILE4).getDicomObject();
        final DicomObjectI      do6_prec     = s_precedence.apply(FILE6).getDicomObject();

        // fail here.  Why?
//        assertEquals("Series Four", do4_prec.getString(Tag.SeriesDescription));
        assertEquals("Some other series", do6_prec.getString(Tag.SeriesDescription));

        // fail to parse the multiline assignment statement.
//        final ScriptApplicator s_multiline = new ScriptApplicator(bytes(S_MULTILINE));
//        final DicomObject do4_multiline = s_multiline.apply(f4);
//        assertEquals("foo", do4_multiline.getString(Tag.SeriesDescription));

        final BaseScriptApplicator s_comment   = BaseScriptApplicator.getInstance( bytes(S_COMMENT));
        final DicomObjectI      do4_comment = s_comment.apply(FILE4).getDicomObject();
        assertEquals("bar", do4_comment.getString(Tag.SeriesDescription));

        // using variables is odd...
//        final ScriptApplicator s_use_variable = new ScriptApplicator(bytes(S_USE_VAR));
//        final Variable studyDescVar = s_use_variable.getVariable("studyDescription");
//        studyDescVar.setValue("my study");
//        final DicomObject do5_use_variable = s_use_variable.apply(f5);
//        assertEquals("my study", do5_use_variable.getString(Tag.SeriesDescription));

        final BaseScriptApplicator s_use_substr   = BaseScriptApplicator.getInstance( bytes(S_USE_SUBSTR));
        final DicomObjectI      do4_use_substr = s_use_substr.apply(FILE4).getDicomObject();
        assertEquals(do4.getString(Tag.PatientID) + "_" + do4.getString(Tag.StudyDate).substring(2, 8),
                do4_use_substr.getString(Tag.PatientID));


//        final ScriptApplicator s_new_uid = new ScriptApplicator(bytes(S_NEW_UID_DEPRECATED));
//        final DicomObject do4_new_uid = s_new_uid.apply(f4);
//        assertEquals(do4.getString(Tag.StudyInstanceUID), do4_new_uid.getString(Tag.StudyInstanceUID));
//        assertFalse(do4.getString(Tag.SeriesInstanceUID).equals(do4_new_uid.getString(Tag.SeriesInstanceUID)));

        final BaseScriptApplicator s_new_uid   = BaseScriptApplicator.getInstance( bytes(S_NEW_UID));
        final DicomObjectI      do4_new_uid = s_new_uid.apply(FILE4).getDicomObject();
        assertEquals(do4.getString(Tag.StudyInstanceUID), do4_new_uid.getString(Tag.StudyInstanceUID));
        assertFalse(do4.getString(Tag.SeriesInstanceUID).equals(do4_new_uid.getString(Tag.SeriesInstanceUID)));

        final BaseScriptApplicator s_init_var   = BaseScriptApplicator.getInstance( bytes(S_INIT_VAR + S_USE_VAR));
        final DicomObjectI      do6_init_var = s_init_var.apply(FILE6).getDicomObject();
        assertEquals("var init", do6_init_var.getString(Tag.SeriesDescription));

        final BaseScriptApplicator s_use_format   = BaseScriptApplicator.getInstance( bytes(S_USE_FORMAT));
        final DicomObjectI      do4_use_format = s_use_format.apply(FILE4).getDicomObject();
        assertEquals("t1_mpr_1mm_p2_pos50_extended", do4_use_format.getString(Tag.SeriesDescription));

        final BaseScriptApplicator s_use_replace   = BaseScriptApplicator.getInstance( bytes(S_USE_REPLACE));
        final DicomObjectI      do4_use_replace = s_use_replace.apply(FILE4).getDicomObject();
        assertEquals("t1:mpr:1mm:p2:pos50", do4_use_replace.getString(Tag.SeriesDescription));

        final BaseScriptApplicator s_use_hashUID   = BaseScriptApplicator.getInstance( bytes(S_USE_HASHUID));
        final DicomObjectI      do4_use_hashUID = s_use_hashUID.apply(FILE4).getDicomObject();
        assertTrue(UIDUtils.isValid(do4_use_hashUID.getString(Tag.StudyInstanceUID)));
        assertFalse(do4.getString(Tag.StudyInstanceUID).equals(do4_use_hashUID.getString(Tag.StudyInstanceUID)));

    }

    @Test
    public void testDescription() throws Exception {
        final BaseScriptApplicator applicator = BaseScriptApplicator.getInstance( bytes(S_DESCRIPTION));
        // Apply the script so the exported variable is created.
        final DicomObjectI dobj = applicator.apply(FILE4).getDicomObject();
        final Variable foo  = applicator.getVariable("foo");
        assertEquals("Description", foo.getDescription());
        final Variable bar = applicator.getVariable("bar");
        assertNull(bar.getDescription());
    }

    @Test
    public void testExportField() throws Exception {
        final BaseScriptApplicator applicator = BaseScriptApplicator.getInstance( bytes(S_EXPORT_FIELD));
        // Apply the script so the exported variable is created.
        final DicomObjectI dobj = applicator.apply(FILE4).getDicomObject();
        final Variable    foo  = applicator.getVariable("foo");
        assertEquals("baz:/my/export/path", foo.getExportField());
        final Variable bar = applicator.getVariable("bar");
        assertNull(bar.getExportField());
    }

    @Test
    public void testHidden() throws Exception {
        final BaseScriptApplicator applicator = BaseScriptApplicator.getInstance( bytes(S_HIDDEN));
        // Apply the script so the variable is created.
        final DicomObjectI dobj = applicator.apply(FILE4).getDicomObject();
        final Variable    foo  = applicator.getVariable("foo");
        assertTrue(foo.isHidden());
        final BaseScriptApplicator a2 = BaseScriptApplicator.getInstance( bytes(S_DESCRIPTION));
        // Apply the script so the variable is created.
        final DicomObjectI dobj2 = a2.apply(FILE4).getDicomObject();
        final Variable    f2    = a2.getVariable("foo");
        assertFalse(f2.isHidden());
    }

    @Test
    public void testRemovePrivate() throws Exception {
        final BaseScriptApplicator a1  = BaseScriptApplicator.getInstance( bytes(S_REMOVE_PRIVATE));
        final DicomObjectI do4 = DicomObjectFactory.newInstance(FILE4);
        assertTrue(do4.contains(0x00185100));
        assertTrue(do4.contains(0x00190010));
        assertTrue(do4.contains(0x00191008));
        assertTrue(do4.contains(0x00191009));
        assertTrue(do4.contains(0x00290010));
        final DicomObjectI do4a = a1.apply(FILE4).getDicomObject();
        assertTrue(do4a.contains(0x00185100));
        assertFalse(do4a.contains(0x00190010));
        assertFalse(do4a.contains(0x00191008));
        assertFalse(do4a.contains(0x00191009));
        assertFalse(do4a.contains(0x00290010));
    }

    @Test
    public void testRemoveNestedByIndex() throws Exception {
        final BaseScriptApplicator a1  = BaseScriptApplicator.getInstance( bytes(S_REMOVE_SINGIDX_NESTED));
        final DicomObjectI do4 = DicomObjectFactory.newInstance(FILE4);
        assertNotNull(do4.getString(new int[]{0x00081140, 0, 0x00081155}));
        assertNotNull(do4.getString(new int[]{0x00081140, 1, 0x00081155}));
        assertNotNull(do4.getString(new int[]{0x00081140, 2, 0x00081155}));
        assertTrue(StringUtils.isEmpty(do4.getString(new int[]{0x00081140, 3, 0x00081155})));

        final DicomObjectI do4a = a1.apply(FILE4).getDicomObject();
        assertNotNull(do4a.getString(new int[]{0x00081140, 0, 0x00081155})); // SQ item 0 untouched
        assertNotNull(do4a.getString(new int[]{0x00081140, 0, 0x00081150})); // SQ item 1 still present
        assertNull(do4a.getString(new int[]{0x00081140, 1, 0x00081155}));   // but matching elements removed
        assertNotNull(do4a.getString(new int[]{0x00081140, 2, 0x00081155})); // SQ item 2 untouched
        assertTrue(StringUtils.isEmpty(do4a.getString(new int[]{0x00081140, 3, 0x00081155})));
    }

    @Test
    public void testRemoveMultIndex() throws Exception {
        final BaseScriptApplicator a1 = BaseScriptApplicator.getInstance(bytes(S_REMOVE_MULTIDX_NESTED));
        final DicomObjectI do4 = DicomObjectFactory.newInstance(FILE4);
        assertNotNull(do4.getString(new int[]{0x00081140, 0, 0x00081155}));
        assertNotNull(do4.getString(new int[]{0x00081140, 1, 0x00081155}));
        assertNotNull(do4.getString(new int[]{0x00081140, 2, 0x00081155}));
        assertTrue(StringUtils.isEmpty(do4.getString(new int[]{0x00081140, 3, 0x00081155})));
        final DicomObjectI do4a = a1.apply(FILE4).getDicomObject();
        assertNotNull(do4a.getString(new int[]{0x00081140, 0, 0x00081150})); // SQ item 0 still present
        assertTrue(StringUtils.isEmpty(do4a.getString(new int[]{0x00081140, 0, 0x00081155})));   // but matching elements removed
        assertTrue(StringUtils.isEmpty(do4a.getString(new int[]{0x00081140, 1, 0x00081155})));
        assertTrue(StringUtils.isEmpty(do4a.getString(new int[]{0x00081140, 2, 0x00081155})));
        assertTrue(StringUtils.isEmpty(do4a.getString(new int[]{0x00081140, 3, 0x00081155})));
    }

    @Test
    public void testRemoveNested() throws Exception {
        final BaseScriptApplicator a1  = BaseScriptApplicator.getInstance( bytes(S_REMOVE_NESTED));
        final DicomObjectI do4 = DicomObjectFactory.newInstance(FILE4);
        assertNotNull(do4.getString(new int[]{0x00081140, 0, 0x00081155}));
        assertNotNull(do4.getString(new int[]{0x00081140, 1, 0x00081155}));
        assertNotNull(do4.getString(new int[]{0x00081140, 2, 0x00081155}));
        assertTrue(StringUtils.isEmpty(do4.getString(new int[]{0x00081140, 3, 0x00081155})));
        final DicomObjectI do4a = a1.apply(FILE4).getDicomObject();
        assertNotNull(do4a.getString(new int[]{0x00081140, 0, 0x00081150})); // SQ item 0 still present
        assertTrue(StringUtils.isEmpty(do4a.getString(new int[]{0x00081140, 0, 0x00081155})));   // but matching elements removed
        assertTrue(StringUtils.isEmpty(do4a.getString(new int[]{0x00081140, 1, 0x00081155})));
        assertTrue(StringUtils.isEmpty(do4a.getString(new int[]{0x00081140, 2, 0x00081155})));
        assertTrue(StringUtils.isEmpty(do4a.getString(new int[]{0x00081140, 3, 0x00081155})));
    }

    @Test
    public void testLowercaseNullValue() throws Exception {
        final BaseScriptApplicator a1 = BaseScriptApplicator.getInstance( bytes(S_LOWERCASE_NULL_VALUE));
        final AnonymizationResult result = a1.apply(FILE4);
        assertNotNull(result);
        assertEquals(AnonymizationResultSeverity.ERROR, result.getSeverity());
        assertEquals(ERROR_EVALUATING_LOWERCASE_EXPECTED_MESSAGE, result.getMessage());
    }

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();

    private static final File FILE4 = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
    private static final File FILE5 = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.5.1.20061214.091206.156000.0972418693.dcm.gz");
    private static final File FILE6 = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.6.1.20061214.091206.156000.2130219399.dcm.gz");
}
