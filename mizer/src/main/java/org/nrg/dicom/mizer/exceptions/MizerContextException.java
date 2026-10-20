/*
 * mizer: org.nrg.dicom.mizer.exceptions.MizerContextException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.exceptions;

import org.nrg.dicom.mizer.service.MizerContext;

/**
 * Indicates issues with creating, executing, or handling a {@link MizerContext Mizer anonymization context}.
 *
 * <p>May also carry the {@link ScriptErrorContext} that located the failure. {@link MizerException#rewrap}
 * renders that context into this exception's message, which is enough for a log or an HTTP body but not for
 * a caller that needs the fields separately — notably the DICOM C-STORE responder, whose Error Comment
 * (0000,0902) is capped at 64 characters and whose Offending Element (0000,0901) wants the tag as an int.
 * Retaining the object as well as the rendered text lets those callers re-render rather than parse.
 */
public class MizerContextException extends MizerException {
    public MizerContextException(final MizerContext context, final String message) {
        // Deliberately not delegating: super(message) leaves the cause uninitialized, so a caller
        // may still use initCause(). Passing an explicit null cause would forbid that.
        super(message);
        _context = context;
        _scriptErrorContext = null;
    }

    public MizerContextException(final MizerContext context, final Throwable throwable) {
        super(throwable);
        _context = context;
        _scriptErrorContext = null;
    }

    public MizerContextException(final MizerContext context, final String message, final Exception exception) {
        this(context, null, message, (Throwable) exception);
    }

    public MizerContextException(final MizerContext context, final String message, final Throwable cause) {
        this(context, null, message, cause);
    }

    /**
     * @param context            the mizer context in play, or {@code null}
     * @param scriptErrorContext the location of the failure, or {@code null}
     * @param message            the detail message
     * @param cause              the underlying cause, or {@code null}
     */
    public MizerContextException(final MizerContext context,
                                 final ScriptErrorContext scriptErrorContext,
                                 final String message,
                                 final Throwable cause) {
        super(message, cause);
        _context = context;
        _scriptErrorContext = scriptErrorContext;
    }

    public MizerContext getContext() {
        return _context;
    }

    /**
     * @return the context locating the failure, or {@code null} if none was attached. Prefer
     *         {@link #findScriptErrorContext(Throwable)} when walking an arbitrary cause chain.
     */
    public ScriptErrorContext getScriptErrorContext() {
        return _scriptErrorContext;
    }

    /**
     * Walk a cause chain and return the first non-empty {@link ScriptErrorContext} found, outermost
     * first, so the most decorated boundary wins. Returns {@code null} when the chain carries none.
     *
     * <p>Guards against a self-referential cause, as {@link MizerException#formatCauseChain} does.
     */
    public static ScriptErrorContext findScriptErrorContext(final Throwable t) {
        for (Throwable cause = t; cause != null; ) {
            if (cause instanceof MizerContextException) {
                final ScriptErrorContext ctx = ((MizerContextException) cause).getScriptErrorContext();
                if (ctx != null && !ctx.isEmpty()) {
                    return ctx;
                }
            }
            final Throwable next = cause.getCause();
            if (next == null || next == cause) {
                break;
            }
            cause = next;
        }
        return null;
    }

    private final MizerContext       _context;
    private final ScriptErrorContext _scriptErrorContext;
}
