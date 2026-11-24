/*
 * DicomEdit: InjectedContextVariableErrorTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.dicomedit;

import org.dcm4che3.data.Tag;
import org.junit.Test;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultSeverity;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for XNAT-8499, reproducing the reported cause.
 *
 * <p>XNAT injects {@code project}, {@code subject} and {@code session} into the anonymization
 * context ({@code BaseMizerService.createContext}), and site scripts assign from them — most
 * commonly {@code (0010,0010) := subject}, which is what replaces PatientName with the subject
 * label. {@code BaseScriptApplicator} seeds those as script variables only
 * <em>{@code if (value != null)}</em>, so when routing rules do not resolve a subject the
 * variable is left defined-but-null and the assignment blows up.
 *
 * <p>Before the fix, that surfaced as:
 * <pre>
 * Cannot invoke "org.nrg.dicom.mizer.values.Value.asString()" because "assignedValue" is null
 * </pre>
 * which says nothing about {@code subject}, nothing about routing rules, and nothing about where
 * in the script it happened — leaving an admin no way to find the misconfiguration. After the fix:
 * <pre>
 * Failed to apply script: line 2; stmt=`(0010,0010):=subject`: Variable 'subject' is null.
 * </pre>
 *
 * <p>The line number here is 2 only because this class uses a minimal script to isolate the
 * mechanism. In the report the failing script was the shipped site-wide default, where the
 * assignment is on line 11; see
 * {@code org.nrg.xnat.helpers.merge.anonymize.DefaultAnonScriptErrorTests} in {@code xnat-web},
 * which reproduces the reported message against the real {@code id.das}.
 *
 * <p>The failure is silent-ish in a dangerous way, which {@link #failedAssignmentLeavesOriginalPatientNameIntact()}
 * pins: because the assignment never runs, PatientName retains its <em>original</em> value. The
 * result is reported as ERROR, so a caller that checks severity is safe — but a caller that
 * ignores it would be handling un-anonymized data.
 *
 * <p>Tests asserting on message content fail against the pre-fix code; they are the red/green core.
 * {@link #subjectSuppliedAnonymizesCleanly()} is the positive control proving the harness works.
 *
 * @see MistypedVariableErrorMessageTests for a mistyped script variable, which reaches the same
 *      null-variable check by a different route
 */
public class InjectedContextVariableErrorTests {

    private static final ResourceManager RESOURCES = ResourceManager.getInstance();

    private static final File DICOM_FILE = RESOURCES.getTestResourceFile(
            "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    /** PatientName carried by the unmodified test object. */
    private static final String ORIGINAL_PATIENT_NAME = "Sample Patient";

    /** The shape of a real site script: PatientName comes from the injected subject label. */
    private static final List<String> SCRIPT = Arrays.asList(
            "version \"6.1\"",
            "(0010,0010) := subject",
            "(0008,103e) := \"anonymized\"");

    /** Raw text the pre-fix code surfaced; must not come back. */
    private static final String RAW_NPE_FRAGMENT = "Cannot invoke";

    /**
     * @param subject     the subject label routing rules resolved, or null if they did not
     * @param setSubject  false to leave the key out of the context entirely
     */
    private AnonymizationResult anonymizeWith(final String subject, final boolean setSubject) throws Exception {
        final MizerContextWithScript context = new MizerContextWithScript();
        context.setScript(SCRIPT);
        context.setElement("project", "PROJ01");
        context.setElement("session", "SESS01");
        if (setSubject) {
            context.setElement("subject", subject);
        }
        final DicomObjectI object = DicomObjectFactory.newInstance(DICOM_FILE);
        assertEquals("test fixture changed", ORIGINAL_PATIENT_NAME, object.getString(Tag.PatientName));
        return BaseScriptApplicator.getInstance(context).apply(object);
    }

    @Test
    public void subjectIsDiscoveredAsAnExternalVariable() throws Exception {
        // Precondition: the script's `subject` reference is what makes it an injected variable.
        final MizerContextWithScript context = new MizerContextWithScript();
        context.setScript(SCRIPT);
        assertTrue(BaseScriptApplicator.getInstance(context).getExternalVariableNames().contains("subject"));
    }

    @Test
    public void subjectSuppliedAnonymizesCleanly() throws Exception {
        // Positive control: with a subject resolved, the script does what it is meant to.
        final AnonymizationResult result = anonymizeWith("SUBJ01", true);
        assertEquals("expected clean anonymization, got: " + result.getMessage(),
                AnonymizationResultSeverity.SUCCESS, result.getSeverity());
        assertEquals("SUBJ01", result.getDicomObject().getString(Tag.PatientName));
    }

    @Test
    public void nullSubjectIsReportedAsAnError() throws Exception {
        assertEquals(AnonymizationResultSeverity.ERROR, anonymizeWith(null, true).getSeverity());
    }

    @Test
    public void nullSubjectErrorNamesTheVariable() throws Exception {
        final String message = anonymizeWith(null, true).getMessage();
        assertTrue("error should name the injected variable 'subject', but was: " + message,
                message.contains("'subject'"));
    }

    @Test
    public void nullSubjectErrorNamesTheSourceLineAndStatement() throws Exception {
        final String message = anonymizeWith(null, true).getMessage();
        assertTrue("error should locate the failure at script line 2, but was: " + message,
                message.contains("line 2"));
        assertTrue("error should quote the failing statement, but was: " + message,
                message.contains("(0010,0010):=subject"));
    }

    @Test
    public void nullSubjectErrorDoesNotLeakRawNullPointerText() throws Exception {
        final String message = anonymizeWith(null, true).getMessage();
        assertFalse("error should explain the null variable rather than surface raw NPE text,"
                    + " but was: " + message,
                message.contains(RAW_NPE_FRAGMENT));
    }

    @Test
    public void absentSubjectKeyBehavesLikeANullSubject() throws Exception {
        // Routing rules that never set the key at all must report the same actionable error.
        final AnonymizationResult result = anonymizeWith(null, false);
        assertEquals(AnonymizationResultSeverity.ERROR, result.getSeverity());
        final String message = result.getMessage();
        assertTrue("error should name 'subject', but was: " + message, message.contains("'subject'"));
        assertFalse("error should not surface raw NPE text, but was: " + message,
                message.contains(RAW_NPE_FRAGMENT));
    }

    @Test
    public void failedAssignmentLeavesOriginalPatientNameIntact() throws Exception {
        // The dangerous part: the assignment never runs, so PatientName keeps its original
        // value. Severity must be ERROR so a caller checking it cannot archive this silently.
        final AnonymizationResult result = anonymizeWith(null, true);
        assertEquals(AnonymizationResultSeverity.ERROR, result.getSeverity());
        assertEquals("PatientName should be unchanged after the failed assignment",
                ORIGINAL_PATIENT_NAME, result.getDicomObject().getString(Tag.PatientName));
    }
}
