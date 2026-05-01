/*
 * DicomEdit: org.nrg.dcm.edit.ScriptApplicatorTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import com.google.common.base.Function;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.util.UIDUtils;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.dicomtools.exceptions.AttributeException;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * The test scripts here are based on the script language documentation at:
 * http://nrg.wustl.edu/projects/DICOM/AnonScript.jsp
 * since that's the closest thing to a spec that exists.
 * Note that this package uses MessageFormat instead of Formatter, so the
 * formatting string syntax has changed from the original DicomBrowser code.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class ScriptApplicatorTest {
    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File            f4               = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
    private static final File            f5               = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.5.1.20061214.091206.156000.0972418693.dcm.gz");
    private static final File            f6               = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.6.1.20061214.091206.156000.2130219399.dcm.gz");

    private static final String ORIG_INSTITUTION = "Hospital";
    private static final String NEW_INSTITUTION  = "My Test Institution";

    private static final String S_ASSIGN = "(0008,0080) := \"" + NEW_INSTITUTION + "\"\n";
    private static final String S_DELETE = "- (0010,0020)\n";

    private static final String S_COND_EQ = "(0020,0011) = \"5\" : (0008,103E) := \"Series Five\"\n"
                                            + "lowercase[(0020,0011)] = \"6\" : (0008,103E) := \"Series Six\"\n"
                                            + "(0020,0011) = \"4\" : - (0008,103E)\n";

    private static final String S_COND_RE = "(0020,0011) ~ \"[45]\" : (0008,103e) := \"Four or five: {0}\" (0020,0011)\n"
                                            + "(0020,0011) ~ \"[6-9]\" : (0008,103e) := \"Six through nine: {0}\" (0020,0011)\n";

    private static final String S_PRECEDENCE = "(0020,0011) = \"4\" : (0008,103e) := \"Series Four\"\n"
                                               + "(0008,103e) := \"Some other series\"\n";

    private static final String S_MULTILINE = "(0008,103e) :=\\\n\"foo\"\n";

    private static final String S_COMMENT = "// (0008,103e) := \"foo\"\n(0008,103e) := \"bar\"\n";

    private static final String S_USE_VAR = "(0008,103e) := studyDescription\n";

    private static final String S_USE_SUBSTRING = "(0010,0020) := \"{0}_{1}\" (0010,0020), substring[(0008,0020),2,8]\n";

    private static final String S_NEW_UID = "(0020,000E) := new UID";

    private static final String S_INIT_VAR = "studyDescription := \"var init\"\n";

    private static final String S_USE_FORMAT = "(0008,103e) := format[\"{0}_{1}\", (0008,103e), \"extended\"]\n";

    private static final String S_USE_REPLACE = "(0008,103e) := replace[(0008,103e), \"_\", \":\"]\n";

    private static final String S_USE_HASHUID = "(0020,000d) := hashUID[(0020,000d)]\n";

    private static final String S_DESCRIPTION = "describe foo \"Description\"\nbar := foo\n";

    private static final String S_EXPORT_FIELD = "export foo \"baz:/my/export/path\"\nbar := foo\n";

    private static final String S_HIDDEN = "foo := \"bar\"\ndescribe foo hidden\n";

    private static final String S_INIT_VAR_FROM_TAG = "patientID := (0010,0020)\n";

    private static final String S_USE_FORMAT_FROM_VAR = "(0008,1030) := format[\"{0}_{1}\", (0008,1030), patientID]\n";

    private static final String S_REMOVE_PRIVATE = "-(XXX#,XXXX)\n";

    private static final Function<File, DicomObject> loader = new Function<File, DicomObject>() {
        public DicomObject apply(final File f) {
            try {
                return DicomUtils.read(f);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    /**
     * Test method for {@link ScriptApplicator#ScriptApplicator(InputStream)}.
     *
     * @throws ScriptEvaluationException When an error occurs evaluating the requested script.
     * @throws IOException               When an error occurs reading or writing.
     */
    @Test
    public void testScriptApplicator() throws IOException, ScriptEvaluationException {
        final ByteArrayInputStream stream = bytes(S_ASSIGN);
        new ScriptApplicator(stream);
    }

    /**
     * Test method for {@link ScriptApplicator#apply(File)}.
     */
    @Test
    public void testApplyFile() throws Exception {
        final DicomObject do4 = loader.apply(f4);
        //	final DicomObject do5 = (DicomObject)loader.apply(f5);
        final DicomObject do6 = loader.apply(f6);

        assertNotNull(do4);
        assertNotNull(do6);

        final ScriptApplicator s_assign   = new ScriptApplicator(bytes(S_ASSIGN));
        final DicomObjectI     do6_assign = s_assign.apply(f6);

        assertEquals(ORIG_INSTITUTION, do6.getString(Tag.InstitutionName));
        assertEquals(NEW_INSTITUTION, do6_assign.getString(Tag.InstitutionName));

        final ScriptApplicator s_delete = new ScriptApplicator(bytes(S_DELETE));
        final DicomObjectI do1_delete = s_delete.apply(f6);

        assertTrue(do6.contains(0x00100020));
        assertTrue(do6_assign.contains(0x00100020));
        assertFalse(do1_delete.contains(0x00100020));

        final ScriptApplicator s_cond_eq = new ScriptApplicator(bytes(S_COND_EQ));
        final DicomObjectI do4_cond_eq = s_cond_eq.apply(f4);
        final DicomObjectI do5_cond_eq = s_cond_eq.apply(f5);
        final DicomObjectI do6_cond_eq = s_cond_eq.apply(f6);

        assertTrue(do4.contains(Tag.SeriesDescription));
        assertFalse(do4_cond_eq.contains(Tag.SeriesDescription));
        assertEquals("Series Five", do5_cond_eq.getString(Tag.SeriesDescription));
        assertEquals("Series Six", do6_cond_eq.getString(Tag.SeriesDescription));

        final ScriptApplicator s_cond_re = new ScriptApplicator(bytes(S_COND_RE));
        final DicomObjectI do4_cond_re = s_cond_re.apply(f4);
        final DicomObjectI do5_cond_re = s_cond_re.apply(f5);
        final DicomObjectI do6_cond_re = s_cond_re.apply(f6);

        assertEquals("Four or five: 4", do4_cond_re.getString(Tag.SeriesDescription));
        assertEquals("Four or five: 5", do5_cond_re.getString(Tag.SeriesDescription));
        assertEquals("Six through nine: 6", do6_cond_re.getString(Tag.SeriesDescription));

        final ScriptApplicator scriptPrecedence = new ScriptApplicator(bytes(S_PRECEDENCE));
        final DicomObjectI object4Precedence = scriptPrecedence.apply(f4);
        final DicomObjectI object6Precedence = scriptPrecedence.apply(f6);

        assertEquals("Series Four", object4Precedence.getString(Tag.SeriesDescription));
        assertEquals("Some other series", object6Precedence.getString(Tag.SeriesDescription));

        final ScriptApplicator s_multiline   = new ScriptApplicator(bytes(S_MULTILINE));
        final DicomObjectI     do4_multiline = s_multiline.apply(f4);
        assertEquals("foo", do4_multiline.getString(Tag.SeriesDescription));

        final ScriptApplicator s_comment   = new ScriptApplicator(bytes(S_COMMENT));
        final DicomObjectI     do4_comment = s_comment.apply(f4);
        assertEquals("bar", do4_comment.getString(Tag.SeriesDescription));

        final ScriptApplicator s_use_variable = new ScriptApplicator(bytes(S_USE_VAR));
        final Variable         studyDescVar   = s_use_variable.getVariable("studyDescription");
        assertNotNull(studyDescVar);
        studyDescVar.setValue("my study");
        final DicomObjectI do5_use_variable = s_use_variable.apply(f5);
        assertEquals("my study", do5_use_variable.getString(Tag.SeriesDescription));

        final ScriptApplicator scriptUseSubstring  = new ScriptApplicator(bytes(S_USE_SUBSTRING));
        final DicomObjectI     object4UseSubstring = scriptUseSubstring.apply(f4);
        assertEquals(do4.getString(Tag.PatientID) + "_" + do4.getString(Tag.StudyDate).substring(2, 8),
                     object4UseSubstring.getString(Tag.PatientID));

        final ScriptApplicator s_new_uid   = new ScriptApplicator(bytes(S_NEW_UID));
        final DicomObjectI     do4_new_uid = s_new_uid.apply(f4);
        assertEquals(do4.getString(Tag.StudyInstanceUID), do4_new_uid.getString(Tag.StudyInstanceUID));
        assertFalse(do4.getString(Tag.SeriesInstanceUID).equals(do4_new_uid.getString(Tag.SeriesInstanceUID)));

        final ScriptApplicator s_init_var   = new ScriptApplicator(bytes(S_INIT_VAR + S_USE_VAR));
        final DicomObjectI     do6_init_var = s_init_var.apply(f6);
        assertEquals("var init", do6_init_var.getString(Tag.SeriesDescription));

        final ScriptApplicator s_use_format   = new ScriptApplicator(bytes(S_USE_FORMAT));
        final DicomObjectI     do4_use_format = s_use_format.apply(f4);
        assertEquals("t1_mpr_1mm_p2_pos50_extended", do4_use_format.getString(Tag.SeriesDescription));

        final ScriptApplicator s_use_replace   = new ScriptApplicator(bytes(S_USE_REPLACE));
        final DicomObjectI     do4_use_replace = s_use_replace.apply(f4);
        assertEquals("t1:mpr:1mm:p2:pos50", do4_use_replace.getString(Tag.SeriesDescription));

        final ScriptApplicator s_use_hashUID   = new ScriptApplicator(bytes(S_USE_HASHUID));
        final DicomObjectI     do4_use_hashUID = s_use_hashUID.apply(f4);
        assertTrue(UIDUtils.isValidUID(do4_use_hashUID.getString(Tag.StudyInstanceUID)));
        assertFalse(do4.getString(Tag.StudyInstanceUID).equals(do4_use_hashUID.getString(Tag.StudyInstanceUID)));
    }

    @Test
    public void testScriptsWithTerminatingComments() throws AttributeException, MizerException, IOException {
        final DicomObject do7 = loader.apply(f4);

        assertNotNull(do7);

        // This tests the fix for an error that occurred when scripts ended with comments, both inline and on their own
        // line. This required adding {$channel=HIDDEN;} to the rule defining DicomEdit COMMENT.
        final ScriptApplicator inline = new ScriptApplicator(bytes("-(0008,0081) // test"));
        final ScriptApplicator newLine = new ScriptApplicator(bytes("-(0008,0081)\n// test"));
        final DicomObjectI deletedWithInLine = inline.apply(f4);
        final DicomObjectI deletedWithNewLine = newLine.apply(f4);

        assertTrue(do7.contains(0x00080081));
        assertFalse(deletedWithNewLine.contains(0x00080081));
        assertFalse(deletedWithInLine.contains(0x00080081));
    }

    @Test
    public void testGetSortedVariables() throws Exception {
        final String script = "a := \"foo\"\nb := format[\"{}_{}\", a, c]\nc := \"baz\"\n";
        final ScriptApplicator applicator = new ScriptApplicator(bytes(script));
        assertEquals(3, applicator.getSortedVariables().size());
        assertEquals("b", applicator.getSortedVariables().get(2).getName());
        assertEquals(2, applicator.getSortedVariables(new String[]{"b"}).size());
        assertEquals(1, applicator.getSortedVariables(Arrays.asList(new String[]{"a", "b"})).size());
        assertEquals("c", applicator.getSortedVariables(Arrays.asList(new String[]{"a", "b"})).get(0).getName());
    }

    @Test
    public void testDescription() throws Exception {
        final ScriptApplicator applicator = new ScriptApplicator(bytes(S_DESCRIPTION));
        final Variable foo = applicator.getVariable("foo");
        assertNotNull(foo);
        assertEquals("Description", foo.getDescription());
        final Variable bar = applicator.getVariable("bar");
        assertNotNull(bar);
        assertNull(bar.getDescription());
    }

    @Test
    public void testExportField() throws Exception {
        final ScriptApplicator applicator = new ScriptApplicator(bytes(S_EXPORT_FIELD));
        final Variable foo = applicator.getVariable("foo");
        assertNotNull(foo);
        assertEquals("baz:/my/export/path", foo.getExportField());
        final Variable bar = applicator.getVariable("bar");
        assertNotNull(bar);
        assertNull(bar.getExportField());
    }

    @Test
    public void testHidden() throws Exception {
        final ScriptApplicator applicator = new ScriptApplicator(bytes(S_HIDDEN));
        final Variable foo = applicator.getVariable("foo");
        assertNotNull(foo);
        assertTrue(foo.isHidden());
        final ScriptApplicator a2 = new ScriptApplicator(bytes(S_DESCRIPTION));
        final Variable f2 = a2.getVariable("foo");
        assertNotNull(f2);
        assertFalse(f2.isHidden());
    }

    @Test
    public void testUnify() throws Exception {
        final ScriptApplicator a1 = new ScriptApplicator(bytes(S_INIT_VAR));
        final ScriptApplicator a2 = new ScriptApplicator(bytes(S_USE_VAR));
        final Variable a1StudyDesc = a1.getVariable("studyDescription");
        final Variable a2StudyDesc = a2.getVariable("studyDescription");
        assertNotNull(a2StudyDesc);
        assertNotSame(a2StudyDesc, a1StudyDesc);
        assertNull(a2StudyDesc.getInitialValue());
        a2.unify(a1StudyDesc);
        assertSame(a1.getVariable("studyDescription"), a2.getVariable("studyDescription"));
    }

    @Test
    public void testUnifyNestedValues() throws Exception {
        final ScriptApplicator a1 = new ScriptApplicator(bytes(S_INIT_VAR_FROM_TAG));
        final ScriptApplicator a2 = new ScriptApplicator(bytes(S_USE_FORMAT_FROM_VAR));

        final DicomObject do4 = loader.apply(f4);
        assertNotNull(do4);
        assertEquals("head^DHead", do4.getString(Tag.StudyDescription));
        assertEquals("head^DHead_null", a2.apply(f4).getString(Tag.StudyDescription));

        for (final Variable v : a1.getVariables().values()) {
            a2.unify(v);
        }
        assertEquals("head^DHead_Sample ID", a2.apply(f4).getString(Tag.StudyDescription));
    }

    @Test
    public void testRemovePrivate() throws Exception {
        final ScriptApplicator a1 = new ScriptApplicator(bytes(S_REMOVE_PRIVATE));
        final DicomObject do4 = loader.apply(f4);
        assertNotNull(do4);
        assertTrue(do4.contains(0x00185100));
        assertTrue(do4.contains(0x00190010));
        assertTrue(do4.contains(0x00191008));
        assertTrue(do4.contains(0x00191009));
        assertTrue(do4.contains(0x00290010));
        final DicomObjectI do4a = a1.apply(f4);
        assertTrue(do4a.contains(0x00185100));
        assertFalse(do4a.contains(0x00190010));
        assertFalse(do4a.contains(0x00191008));
        assertFalse(do4a.contains(0x00191009));
        assertFalse(do4a.contains(0x00290010));
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
}
