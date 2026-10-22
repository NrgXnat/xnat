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
 */
public class MizerContextException extends MizerException {
    public MizerContextException(final MizerContext context, final String message) {
        super(message);
        _context = context;
    }

    public MizerContextException(final MizerContext context, final Throwable throwable) {
        super(throwable);
        _context = context;
    }

    public MizerContextException(final MizerContext context, final String message, final Exception exception) {
        super(message, exception);
        _context = context;
    }

    public MizerContext getContext() {
        return _context;
    }

    private final MizerContext _context;
}
