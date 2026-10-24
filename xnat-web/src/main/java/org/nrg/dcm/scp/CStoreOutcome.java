/*
 * web: org.nrg.dcm.scp.CStoreOutcome
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
import org.dcm4che3.util.StringUtils;
import org.nrg.action.ServerException;
import org.nrg.dicom.mizer.exceptions.MizerContextException;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.exceptions.ScriptErrorContext;
import org.nrg.xnat.archive.ImportFailures;

/**
 * Turns an import failure into the fields of a C-STORE-RSP: a DICOM status and an Error Comment.
 *
 * <p>Offending Element (0000,0901) is not set. It wants the tag the failing statement acted on, and
 * nothing on the script-error path records one: {@link ScriptErrorContext} carries only the line and
 * the statement text.
 *
 * <h3>Why these status values</h3>
 *
 * PS3.4 Section B.2.3, Table B.2-1 gives the Storage Service Class its own failure statuses --
 * {@code A7xx} Refused: Out of Resources, {@code A9xx} Error: Data Set does not match SOP Class and
 * {@code Cxxx} Error: Cannot understand -- and defers the rest to PS3.7, whose C-STORE list in
 * Section 9.1.1.1.9 adds {@code 0122H}, {@code 0117H}, {@code 0210H}, {@code 0211H}, {@code 0212H}
 * and {@code 0124H}.
 *
 * <p>{@code 0110H} Processing Failure is in neither list. PS3.7 Annex C Section C.5.21 defines its
 * encoding, but Annex C states that "the applicability and semantics for each Status Type ... are
 * defined in the DIMSE Services", and C-STORE does not include it. It is therefore not a conformant
 * C-STORE response and is not used here, notwithstanding that dcm4che offers
 * {@link Status#ProcessingFailure} and that this class's predecessor set it.
 *
 * <p>The Storage Service Class has no status meaning "the server is misconfigured", which is what an
 * anonymization script failure actually is -- the object is fine and the script is broken. Of the two
 * imperfect fits, {@code Cxxx} is used rather than {@code A7xx} because Table B.2-1 permits both
 * Error Comment (0000,0902) and Offending Element (0000,0901) under {@code Cxxx} but only the former
 * under {@code A7xx}, and because B.2.3 requires that "an SCP implementation shall assign specific
 * Failure Status Codes by replacing each 'x' symbol with a hexadecimal digit in the range from 0 to
 * F" -- so XNAT can say in a sub-code what the family name cannot.
 *
 * <p>The sub-codes below are XNAT's and belong in its DICOM conformance statement.
 */
final class CStoreOutcome {

    /** {@code Cxxx} sub-code: the server could not process the instance, e.g. an anonymization script failed. */
    static final int PROCESSING_FAILED = 0xC001;

    /** {@code Cxxx} sub-code: another session already occupies the instance's prearchive destination. */
    static final int DESTINATION_CONFLICT = 0xC002;

    /** Error Comment is VR LO, capped at 64 characters. */
    private static final int ERROR_COMMENT_MAX = 64;

    private CStoreOutcome() {
    }

    /**
     * Choose the status for a failure.
     *
     * <p>Deliberately keyed on the <em>cause chain</em> rather than on the type of the outermost
     * throwable. The import path wraps freely -- an anonymization failure can surface as a
     * {@link org.nrg.action.ClientException} whose cause is a {@link ServerException} -- so the
     * outer type says more about which layer caught it than about what went wrong.
     */
    static int statusFor(final Throwable t) {
        if (t == null) {
            return PROCESSING_FAILED;
        }
        if (ImportFailures.isDestinationTaken(t)) {
            return DESTINATION_CONFLICT;
        }
        for (Throwable cause = t; cause != null; ) {
            if (cause instanceof MizerException || cause instanceof ServerException) {
                return PROCESSING_FAILED;
            }
            final Throwable next = cause.getCause();
            if (next == null || next == cause) {
                break;
            }
            cause = next;
        }
        // Nothing server-side in the chain, so treat the instance itself as the problem.
        return Status.CannotUnderstand;
    }

    /**
     * Compose the Error Comment: the terse reason, prefixed by the script location when one is known.
     *
     * <p>Front-loaded on purpose. 64 characters is the whole budget, and the rendered message that
     * travels down the cause chain spends its first sixty-odd on a constant prefix and the statement
     * text -- {@code "Failed to apply script: line 11; stmt=`(0010,0010):=subject`: Variable 'subject'
     * is null."} truncates to just before the part that matters. Taking the deepest message via
     * {@link MizerException#rootCauseMessage} and re-prefixing it with
     * {@link ScriptErrorContext#formatCompact()} yields {@code "line 11: Variable 'subject' is null."}
     * instead.
     *
     * @return the comment, never null, possibly empty
     */
    static String errorComment(final Throwable t) {
        if (t == null) {
            return "";
        }
        final String             reason  = MizerException.rootCauseMessage(t);
        final ScriptErrorContext ctx     = MizerContextException.findScriptErrorContext(t);
        final String             locator = ctx == null ? "" : ctx.formatCompact();
        final String             comment = locator.isEmpty() ? reason : locator + ": " + reason;
        return StringUtils.truncate(comment, ERROR_COMMENT_MAX);
    }

    /**
     * Populate a C-STORE-RSP with the failure. The response is the one dcm4che's
     * {@code BasicCStoreSCP} built and will write, so it keeps the UID fields and Data Set Type that
     * PS3.7 Section 9.3.1.2 requires.
     */
    static void applyTo(final Attributes rsp, final int status, final Throwable t) {
        rsp.setInt(Tag.Status, VR.US, status);
        final String comment = errorComment(t);
        if (!comment.isEmpty()) {
            rsp.setString(Tag.ErrorComment, VR.LO, comment);
        }
    }
}
