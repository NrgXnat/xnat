/*
 * core: org.nrg.xft.exception.MetaDataException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.exception;

/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class MetaDataException extends XftItemException {
    /**
     * Creates a new metadata exception.
     */
    public MetaDataException() {
        super();
    }

    /**
     * Creates a new metadata exception.
     *
     * @param message The message to set for the exception.
     */
    public MetaDataException(final String message) {
        super(message);
    }

    /**
     * Creates a new metadata exception.
     *
     * @param cause The cause of the exception.
     */
    public MetaDataException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new metadata exception.
     *
     * @param message The message to set for the exception.
     * @param cause   The cause of the exception.
     */
    public MetaDataException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
