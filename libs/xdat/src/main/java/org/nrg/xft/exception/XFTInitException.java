/*
 * core: org.nrg.xft.exception.XFTInitException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.exception;

@SuppressWarnings("serial")
public class XFTInitException extends XftItemException {
    public XFTInitException(final String message) {
        super(message);
    }

    public XFTInitException() {
        this("XFT accessed before initialization.");
    }
}
