/*
 * mizer: org.nrg.dicom.mizer.exceptions.MizerException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.exceptions;

import org.nrg.dicom.mizer.service.MizerService;

/**
 * The primary class used to distinguish exceptions from operations in the {@link MizerService} and its components.
 *
 * <p>Also exposes static helpers for rendering exceptions and their cause chains for user-facing surfaces.
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
