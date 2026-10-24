/*
 * web: org.nrg.dcm.scp.CStoreOutcomeTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Status;
import org.junit.Test;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.dicom.dicomedit.BaseScriptApplicator;
import org.nrg.dicom.mizer.exceptions.MizerContextException;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.exceptions.ScriptErrorContext;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.xnat.helpers.prearchive.SessionException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The C-STORE-RSP fields for a failed import.
 *
 * <p>The statuses are checked against the standard rather than against dcm4che's convenience
 * constants: PS3.4 Table B.2-1 gives Storage only {@code A7xx}, {@code A9xx} and {@code Cxxx}, and
 * {@code 0110H} Processing Failure -- which this class's predecessor set -- is in neither that table
 * nor PS3.7 9.1.1.1.9's list for C-STORE.
 */
public class CStoreOutcomeTest {

    /** The reported failure: the shipped default script's line 11 with no subject resolved. */
    private static Throwable anonymizationFailureAsItArrives() {
        // Mirrors the real chain: the applicator wraps the failure with its location, applyScripts turns
        // the result into a ServerException, and the importer's outer catch wraps that in a ClientException.
        final ScriptErrorContext ctx = ScriptErrorContext.empty()
                .withScriptLine(11)
                .withStatementText("(0010,0010):=subject");
        final Throwable applicator = new MizerContextException(null, ctx,
                "line 11; stmt=`(0010,0010):=subject`: Variable 'subject' is null.",
                new RuntimeException("Variable 'subject' is null."));
        final ServerException server = new ServerException("Failed to apply script", (Exception) applicator);
        return new ClientException("unable to read DICOM object 000.dcm", server);
    }

    @Test
    public void anonymizationFailureIsAnXnatAssignedCannotUnderstandSubCode() {
        assertEquals(0xC001, CStoreOutcome.statusFor(anonymizationFailureAsItArrives()));
    }

    @Test
    public void processingFailureIsNeverUsedBecauseCStoreDoesNotDefineIt() {
        // PS3.7 Annex C C.5.21 defines 0110H's encoding, but Annex C defers applicability to each
        // DIMSE service and C-STORE's list omits it. Guards against a well-meaning reinstatement.
        for (final Throwable t : new Throwable[]{
                anonymizationFailureAsItArrives(),
                new ServerException("boom"),
                new ClientException("boom"),
                new IllegalArgumentException("No DICOM SCP receiver for AE/port"),
                new IOException("socket closed"),
                null}) {
            assertNotEquals("0110H is not a conformant C-STORE status", Status.ProcessingFailure, CStoreOutcome.statusFor(t));
        }
    }

    @Test
    public void everyStatusUsedIsOneCStorePermits() {
        for (final Throwable t : new Throwable[]{
                anonymizationFailureAsItArrives(),
                new ServerException("boom"),
                new ClientException("boom"),
                new ClientException("x", new SessionException(SessionException.Error.AlreadyExists, "exists")),
                new IllegalArgumentException("unknown AE"),
                null}) {
            final int status = CStoreOutcome.statusFor(t);
            // Cxxx is the only family this class assigns from; B.2.3 requires the SCP to pick the digits.
            assertEquals("status " + String.format("%04X", status) + " is outside the Cxxx family",
                         0xC000, status & 0xF000);
        }
    }

    @Test
    public void aDestinationConflictIsDistinguishedFromAProcessingFailure() {
        final Throwable taken = new ClientException("unable to import DICOM object 000.dcm",
                new SQLException("duplicate key value violates unique constraint", "23505"));
        assertEquals(0xC002, CStoreOutcome.statusFor(taken));
        assertNotEquals(CStoreOutcome.statusFor(taken), CStoreOutcome.statusFor(anonymizationFailureAsItArrives()));
    }

    @Test
    public void aServerSideFailureAnywhereInTheChainCountsAsProcessingFailed() {
        // The outer type says which layer caught it, not what went wrong, so the chain decides.
        assertEquals(0xC001, CStoreOutcome.statusFor(new ClientException("wrapped", new ServerException("real cause"))));
        assertEquals(0xC001, CStoreOutcome.statusFor(new ServerException("direct")));
        assertEquals(0xC001, CStoreOutcome.statusFor(new IllegalStateException("x", new MizerException("script broke"))));
    }

    @Test
    public void aBadObjectWithNothingServerSideIsCannotUnderstand() {
        assertEquals(Status.CannotUnderstand, CStoreOutcome.statusFor(new ClientException("object 000.dcm contains no SOP Class UID")));
    }

    @Test
    public void errorCommentLeadsWithTheLineAndTheReasonNotTheBoilerplate() {
        final String comment = CStoreOutcome.errorComment(anonymizationFailureAsItArrives());
        assertEquals("line 11: Variable 'subject' is null.", comment);
        assertTrue("must fit VR LO", comment.length() <= 64);
    }

    @Test
    public void errorCommentIsTruncatedToTheSixtyFourCharacterBudget() {
        final StringBuilder reason = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            reason.append("verbose ");
        }
        final String comment = CStoreOutcome.errorComment(new ServerException(reason.toString().trim()));
        assertEquals(64, comment.length());
    }

    @Test
    public void errorCommentKeepsTheReasonWhenNoLocationIsKnown() {
        assertEquals("No room at the inn", CStoreOutcome.errorComment(new ServerException("No room at the inn")));
    }

    @Test
    public void errorCommentDegradesToAClassNameRatherThanNullOrEmpty() {
        final String comment = CStoreOutcome.errorComment(new NullPointerException());
        assertFalse(comment.isEmpty());
        assertTrue(comment, comment.contains("NullPointerException"));
        assertEquals("", CStoreOutcome.errorComment(null));
    }

    @Test
    public void applyToPopulatesStatusAndComment() {
        final Attributes rsp = new Attributes();
        rsp.setString(Tag.AffectedSOPInstanceUID, VR.UI, "1.2.3.4");
        final Throwable t = anonymizationFailureAsItArrives();

        CStoreOutcome.applyTo(rsp, CStoreOutcome.statusFor(t), t);

        assertEquals(0xC001, rsp.getInt(Tag.Status, -1));
        assertEquals("line 11: Variable 'subject' is null.", rsp.getString(Tag.ErrorComment));
        // No tag reaches here, so (0000,0901) stays unset rather than holding a guess.
        assertFalse(rsp.contains(Tag.OffendingElement));
        // PS3.7 9.3.1.2 wants the UID fields in the response; applyTo must not clear them.
        assertEquals("1.2.3.4", rsp.getString(Tag.AffectedSOPInstanceUID));
    }

    @Test
    public void applyToUsesTheMessageWhenNoLocationIsKnown() {
        final Attributes rsp = new Attributes();
        final Throwable t = new ServerException("no location");
        CStoreOutcome.applyTo(rsp, CStoreOutcome.statusFor(t), t);

        assertEquals(0xC001, rsp.getInt(Tag.Status, -1));
        assertEquals("no location", rsp.getString(Tag.ErrorComment));
    }

    /**
     * End to end through the real plumbing, which the hand-built chains above cannot prove: the shipped
     * {@code id.das} with no subject resolved, the real applicator, the real {@link AnonymizationResult},
     * wrapped the way {@code GradualDicomImporter} wraps it, rendered by this class.
     *
     * <p>This is the test that would have caught the gap the fix had to close first. Until
     * {@code applyScripts} carried the result's own throwable into its {@code ServerException}, the chain
     * reaching here was a flattened string and the line was gone.
     */
    @Test
    public void theRealDefaultScriptFailureYieldsALocatedComment() throws Exception {
        final AnonymizationResult result;
        try (InputStream script = CStoreOutcomeTest.class.getClassLoader()
                .getResourceAsStream("META-INF/xnat/defaults/id.das")) {
            assertNotNull("shipped default anon script not on the test classpath", script);
            final MizerContextWithScript context = new MizerContextWithScript();
            context.setScript(script);
            context.setElement("project", "PROJ01");
            context.setElement("session", "SESS01");
            // subject deliberately absent, as when routing rules resolve none.
            result = BaseScriptApplicator.getInstance(context).apply(DicomObjectFactory.newInstance());
        }
        assertTrue("expected the line-11 failure, got: " + result.getMessage(),
                   result instanceof AnonymizationResultError);

        // Exactly how GradualDicomImporter.applyScripts and its outer catch now build the chain.
        final Throwable arrives = new ClientException("unable to read DICOM object 000.dcm",
                new ServerException(org.restlet.data.Status.SERVER_ERROR_INTERNAL,
                                    String.join("\n", result.getMessages()),
                                    ((AnonymizationResultError) result).getCause()));

        final String comment = CStoreOutcome.errorComment(arrives);
        assertEquals(0xC001, CStoreOutcome.statusFor(arrives));
        assertTrue("comment should lead with the location, was: " + comment, comment.startsWith("line 11: "));
        assertTrue("comment should name the variable, was: " + comment, comment.contains("subject"));
        assertTrue("comment must fit VR LO, was " + comment.length() + ": " + comment, comment.length() <= 64);
        // The thing the sender could not previously be told at all.
        System.out.println("[CStoreOutcomeTest] real chain -> status C001, (0000,0902)=\"" + comment + "\"");
    }
}
