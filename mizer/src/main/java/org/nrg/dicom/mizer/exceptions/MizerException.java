/*
 * mizer: org.nrg.dicom.mizer.exceptions.MizerException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.exceptions;

import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.MizerService;

/**
 * The primary class used to distinguish exceptions from operations in the {@link MizerService} and its components.
 *
 * <p>Also exposes static helpers for decorating in-flight exceptions with {@link ScriptErrorContext}
 * information without losing the original cause or stack trace, and for rendering cause chains for
 * user-facing surfaces.
 *
 * <p>Intended use of {@link #rewrap}: wrap a try/catch at a well-defined boundary (script-applicator,
 * file-applicator, CLI entry point) and call {@link #rewrap(Throwable, ScriptErrorContext)} to attach
 * whatever context that boundary knows about. The resulting exception's message reads outside-in
 * (boundary added latest is leftmost), and the original cause chain is preserved.
 */
public class MizerException extends Exception {
    /**
     * Creates a new exception with the specified message.
     *
     * @param message The detailed exception message.
     */
    public MizerException(final String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified message and root cause.
     *
     * @param cause The root cause of the exception.
     */
    public MizerException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new exception with the specified root cause.
     *
     * @param message The detailed exception message.
     * @param cause   The root cause of the exception.
     */
    public MizerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Decorate a thrown exception with the supplied {@link ScriptErrorContext}.
     *
     * <p>If {@code cause} is already a {@link MizerContextException}, the returned exception
     * preserves the original {@link MizerContext}. Otherwise the returned exception's mizer
     * context is {@code null}.
     *
     * <p>The returned exception's message is {@code "<context-prefix>: <cause-message>"}.
     * If the supplied context is empty, the cause message is used as-is.
     *
     * @param cause the in-flight exception (must not be {@code null})
     * @param ctx   the context to attach (may be {@code null} or {@link ScriptErrorContext#EMPTY})
     * @return a new {@link MizerContextException} wrapping {@code cause}
     */
    public static MizerContextException rewrap(final Throwable cause, final ScriptErrorContext ctx) {
        if (cause == null) {
            throw new IllegalArgumentException("cause must not be null");
        }
        final String prefix = (ctx == null) ? "" : ctx.format();
        final String causeMsg = describeMessage(cause);
        final String message = prefix.isEmpty() ? causeMsg : prefix + ": " + causeMsg;

        final MizerContext mizerContext = (cause instanceof MizerContextException)
                ? ((MizerContextException) cause).getContext()
                : null;
        return new MizerContextException(mizerContext, message, cause);
    }

    /**
     * Decorate a thrown exception with both a {@link MizerContext} and a {@link ScriptErrorContext}.
     * Use this overload when the boundary has access to a {@link MizerContext} that the cause
     * may not already carry (e.g., the top-level service layer).
     */
    public static MizerContextException rewrap(final Throwable cause, final MizerContext mizerContext, final ScriptErrorContext ctx) {
        if (cause == null) {
            throw new IllegalArgumentException("cause must not be null");
        }
        final String prefix = (ctx == null) ? "" : ctx.format();
        final String causeMsg = describeMessage(cause);
        final String message = prefix.isEmpty() ? causeMsg : prefix + ": " + causeMsg;

        final MizerContext effective;
        if (mizerContext != null) {
            effective = mizerContext;
        } else if (cause instanceof MizerContextException) {
            effective = ((MizerContextException) cause).getContext();
        } else {
            effective = null;
        }
        return new MizerContextException(effective, message, cause);
    }

    /**
     * Render the most useful single-line description of a throwable: its message if non-empty,
     * otherwise the simple class name. Never returns null or the empty string.
     */
    public static String describeMessage(final Throwable t) {
        final String m = t.getMessage();
        return (m != null && !m.isEmpty()) ? m : t.getClass().getSimpleName();
    }

    /**
     * Walk the cause chain and return the deepest {@link Throwable#getMessage()} that is non-null
     * and non-empty; falls back to {@link #describeMessage(Throwable)} on the original.
     *
     * <p>Useful at user-facing boundaries that want to surface "what actually went wrong"
     * even when the top-level message is generic.
     */
    public static String rootCauseMessage(final Throwable t) {
        Throwable current = t;
        Throwable deepest = t;
        while (current != null) {
            final String m = current.getMessage();
            if (m != null && !m.isEmpty()) {
                deepest = current;
            }
            final Throwable next = current.getCause();
            if (next == null || next == current) {
                break;
            }
            current = next;
        }
        return describeMessage(deepest);
    }

    /**
     * Render the cause chain as a multi-line string of {@code "\ncaused by: <message>"} entries.
     * Skips any cause whose message is already a substring of {@code alreadyShown} or of a previously
     * emitted line, so the result does not duplicate information already visible at the top of the
     * exception's own message.
     *
     * <p>Returns the empty string if the throwable has no cause, or every cause is redundant.
     *
     * <p>Intended for user-facing surfaces like {@code AnonymizationResultError.getMessage()} that
     * want the full diagnostic chain without forcing the user to read a stack trace.
     *
     * @param t           the top-level throwable (may be {@code null})
     * @param alreadyShown text already visible to the user; causes whose messages are substrings of
     *                    this are not re-emitted (may be {@code null} or empty)
     */
    public static String formatCauseChain(final Throwable t, final String alreadyShown) {
        if (t == null) {
            return "";
        }
        final StringBuilder shown = new StringBuilder(alreadyShown == null ? "" : alreadyShown);
        final StringBuilder out = new StringBuilder();
        Throwable current = t.getCause();
        while (current != null) {
            final String msg = describeMessage(current);
            if (!shown.toString().contains(msg)) {
                out.append("\ncaused by: ").append(msg);
                shown.append('\n').append(msg);
            }
            final Throwable next = current.getCause();
            if (next == null || next == current) {
                break;
            }
            current = next;
        }
        return out.toString();
    }
}
