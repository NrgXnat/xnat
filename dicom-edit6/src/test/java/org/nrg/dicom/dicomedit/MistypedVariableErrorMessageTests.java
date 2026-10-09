/*
 * DicomEdit: MistypedVariableErrorMessageTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.dicomedit;

import org.dcm4che3.data.Tag;
import org.junit.Before;
import org.junit.Test;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultSeverity;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for XNAT-8499: a mistyped variable reference in an anonymization script
 * produced an error the user could not act on.
 *
 * <p>The reported case was an assignment to PatientName {@code (0010,0010)} whose right-hand
 * side misspelled the variable — {@code patientName} where the script had defined
 * {@code PatientName}. Note that DicomEdit's variable discovery registers identifiers it sees
 * on the right-hand side, so the mistyped name is <em>present</em> in scope with a null value
 * rather than absent from it; this is the null-variable path, not the "unknown variable" path.
 *
 * <p>Before the fix, the applicator surfaced the raw NPE text from the failed assignment:
 * <pre>
 * Cannot invoke "org.nrg.dicom.mizer.values.Value.asString()" because "assignedValue" is null
 * </pre>
 * which names neither the offending variable nor where in the script it appeared. After the fix:
 * <pre>
 * Failed to apply script: line 2; stmt=`(0010,0010):=patientName`: Variable 'patientName' is null.
 * </pre>
 *
 * <p>{@link #errorNamesTheMistypedVariable()}, {@link #errorNamesTheSourceLine()},
 * {@link #errorQuotesTheFailingStatement()} and {@link #errorDoesNotLeakRawNullPointerText()}
 * all fail against the pre-fix code; they are the red/green core of this ticket.
 * {@link #failureIsReportedAsAnError()} and {@link #failedScriptDoesNotModifyTheTag()} held
 * before the fix too and are here to pin invariants the fix must not break.
 */
public class MistypedVariableErrorMessageTests {

    private static final ResourceManager RESOURCES = ResourceManager.getInstance();

    /** Has PatientName "Sample Patient" and SeriesDescription "t1_mpr_1mm_p2_pos50". */
    private static final File DICOM_FILE = RESOURCES.getTestResourceFile(
            "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    /**
     * Defines {@code PatientName} but assigns from {@code patientName} — a one-character
     * case typo, which is how the original report arose.
     */
    private static final String SCRIPT_WITH_TYPO =
            "PatientName := \"Anonymized\"\n"
            + "(0010,0010) := patientName\n";

    /** Raw text the pre-fix code surfaced; must not come back. */
    private static final String RAW_NPE_FRAGMENT = "Cannot invoke";

    private AnonymizationResult result;

    @Before
    public void applyScriptWithTypo() throws Exception {
        final BaseScriptApplicator applicator =
                BaseScriptApplicator.getInstance(new ByteArrayInputStream(SCRIPT_WITH_TYPO.getBytes()));
        result = applicator.apply(DICOM_FILE);
        assertNotNull("applicator returned no result", result);
    }

    @Test
    public void failureIsReportedAsAnError() {
        assertEquals(AnonymizationResultSeverity.ERROR, result.getSeverity());
    }

    @Test
    public void errorNamesTheMistypedVariable() {
        final String message = result.getMessage();
        assertTrue("error message should name the offending variable 'patientName', but was: " + message,
                message.contains("patientName"));
    }

    @Test
    public void errorNamesTheSourceLine() {
        final String message = result.getMessage();
        assertTrue("error message should locate the failure at script line 2, but was: " + message,
                message.contains("line 2"));
    }

    @Test
    public void errorQuotesTheFailingStatement() {
        final String message = result.getMessage();
        assertTrue("error message should quote the failing statement, but was: " + message,
                message.contains("(0010,0010):=patientName"));
    }

    @Test
    public void errorDoesNotLeakRawNullPointerText() {
        final String message = result.getMessage();
        assertFalse("error message should explain the null variable rather than surface raw NPE text,"
                    + " but was: " + message,
                message.contains(RAW_NPE_FRAGMENT));
    }

    @Test
    public void failedScriptDoesNotModifyTheTag() throws Exception {
        // A script that dies partway must not leave the object half-anonymized: PatientName
        // should still hold its original value, not the assignment's intended one and not empty.
        final DicomObjectI original = DicomObjectFactory.newInstance(DICOM_FILE);
        final String before = original.getString(Tag.PatientName);
        final String after = result.getDicomObject().getString(Tag.PatientName);
        assertEquals("failed script should leave PatientName untouched", before, after);
        assertFalse("PatientName should not have taken the intended anonymized value",
                "Anonymized".equals(after));
    }
}
