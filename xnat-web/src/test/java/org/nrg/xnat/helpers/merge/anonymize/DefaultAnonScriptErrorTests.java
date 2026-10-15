/*
 * web: org.nrg.xnat.helpers.merge.anonymize.DefaultAnonScriptErrorTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge.anonymize;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.nrg.dicom.dicomedit.BaseScriptApplicator;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultSeverity;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for XNAT-8499 against the **shipped default site-wide anonymization script**,
 * {@code META-INF/xnat/defaults/id.das} — the script that was actually failing in the report.
 *
 * <p>That script assigns PatientName from the injected subject label on line 11:
 * <pre>
 *  9  version "6.1"
 * 10  project != "Unassigned" ? (0008,1030) := project
 * 11  (0010,0010) := subject
 * 12  (0010,0020) := session
 * </pre>
 * When routing rules resolve no subject, {@code BaseScriptApplicator} leaves {@code subject}
 * defined-but-null (it seeds context values only {@code if (value != null)}) and line 11 fails.
 *
 * <p>Before the fix the admin saw only:
 * <pre>
 * Cannot invoke "org.nrg.dicom.mizer.values.Value.asString()" because "assignedValue" is null
 * </pre>
 * After the fix:
 * <pre>
 * Failed to apply script: line 11; stmt=`(0010,0010):=subject`: Variable 'subject' is null.
 * </pre>
 *
 * <p>The line number is the point of this class rather than an incidental detail. The default
 * script carries eight lines of licence comment before any statement, so "line 11" is only
 * correct if location tracking reports the position in the <em>file</em> rather than the index
 * of the statement — which a short synthetic script cannot demonstrate. See
 * {@code InjectedContextVariableErrorTests} in {@code dicom-edit6} for the mechanism tested in
 * isolation, including the absent-key variant.
 *
 * <p>{@link #defaultScriptStillHasSubjectOnLineEleven()} pins the fixture: if {@code id.das}
 * is ever edited, that test fails rather than letting the rest assert a stale line number.
 */
public class DefaultAnonScriptErrorTests {

    /** Matches DefaultAnonUtils.DEFAULT_ANON_SCRIPT, minus the classpath*: wildcard. */
    private static final String DEFAULT_ANON_SCRIPT = "META-INF/xnat/defaults/id.das";

    private static final int    SUBJECT_LINE   = 11;
    private static final String SUBJECT_STMT   = "(0010,0010) := subject";
    private static final String PROJECT_LINE   = "line 10";
    private static final String RAW_NPE        = "Cannot invoke";

    private InputStream defaultScript() {
        final InputStream in = getClass().getClassLoader().getResourceAsStream(DEFAULT_ANON_SCRIPT);
        assertNotNull("default anon script not on the test classpath: " + DEFAULT_ANON_SCRIPT, in);
        return in;
    }

    /**
     * Anonymize an empty object with the real default script. A null argument means that value
     * is left out of the context, as it is when routing rules do not resolve it.
     */
    private AnonymizationResult anonymizeWith(final String project, final String subject, final String session)
            throws Exception {
        try (InputStream script = defaultScript()) {
            final MizerContextWithScript context = new MizerContextWithScript();
            context.setScript(script);
            if (project != null) context.setElement("project", project);
            if (subject != null) context.setElement("subject", subject);
            if (session != null) context.setElement("session", session);
            return BaseScriptApplicator.getInstance(context).apply(DicomObjectFactory.newInstance());
        }
    }

    @Test
    public void defaultScriptStillHasSubjectOnLineEleven() throws Exception {
        try (InputStream in = defaultScript()) {
            final List<String> lines = IOUtils.readLines(in, StandardCharsets.UTF_8);
            assertTrue("default script is shorter than expected: " + lines.size(),
                    lines.size() >= SUBJECT_LINE);
            assertEquals("the subject assignment has moved; update SUBJECT_LINE and the assertions below",
                    SUBJECT_STMT, lines.get(SUBJECT_LINE - 1).trim());
        }
    }

    @Test
    public void defaultScriptDeclaresProjectSubjectAndSessionAsInjected() throws Exception {
        try (InputStream script = defaultScript()) {
            final MizerContextWithScript context = new MizerContextWithScript();
            context.setScript(script);
            assertTrue(BaseScriptApplicator.getInstance(context)
                    .getExternalVariableNames().containsAll(java.util.Arrays.asList("project", "subject", "session")));
        }
    }

    @Test
    public void allValuesSuppliedAnonymizesCleanly() throws Exception {
        // Positive control: the default script works when routing resolves everything.
        final AnonymizationResult result = anonymizeWith("PROJ01", "SUBJ01", "SESS01");
        assertEquals("expected clean anonymization, got: " + result.getMessage(),
                AnonymizationResultSeverity.SUCCESS, result.getSeverity());
    }

    @Test
    public void missingSubjectIsReportedAsAnError() throws Exception {
        assertEquals(AnonymizationResultSeverity.ERROR,
                anonymizeWith("PROJ01", null, "SESS01").getSeverity());
    }

    @Test
    public void missingSubjectErrorNamesLineElevenOfTheDefaultScript() throws Exception {
        final String message = anonymizeWith("PROJ01", null, "SESS01").getMessage();
        assertTrue("error should locate the failure at line " + SUBJECT_LINE
                   + " of the default script, but was: " + message,
                message.contains("line " + SUBJECT_LINE));
    }

    @Test
    public void missingSubjectErrorNamesTheVariableAndStatement() throws Exception {
        final String message = anonymizeWith("PROJ01", null, "SESS01").getMessage();
        assertTrue("error should name 'subject', but was: " + message, message.contains("'subject'"));
        assertTrue("error should quote the failing statement, but was: " + message,
                message.contains("(0010,0010):=subject"));
    }

    @Test
    public void missingSubjectErrorDoesNotLeakRawNullPointerText() throws Exception {
        final String message = anonymizeWith("PROJ01", null, "SESS01").getMessage();
        assertFalse("error should explain the null variable rather than surface raw NPE text,"
                    + " but was: " + message, message.contains(RAW_NPE));
    }

    @Test
    public void missingProjectFailsEarlierAtLineTen() throws Exception {
        // Proves the reported line tracks the actual failing statement rather than a fixed
        // location: with no project, the script dies one line earlier, on the conditional.
        final String message = anonymizeWith(null, "SUBJ01", "SESS01").getMessage();
        assertTrue("expected failure at line 10, but was: " + message, message.contains(PROJECT_LINE));
        assertTrue("expected 'project' to be named, but was: " + message, message.contains("'project'"));
    }
}
