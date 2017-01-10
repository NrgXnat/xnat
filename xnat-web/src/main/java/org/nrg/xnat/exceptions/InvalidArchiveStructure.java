/*
 * web: org.nrg.xnat.exceptions.InvalidArchiveStructure
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.exceptions;

/**
 * @author timo
 *
 */
@SuppressWarnings("serial")
public class InvalidArchiveStructure extends Exception {

    /**
     * 
     */
    public InvalidArchiveStructure() {
    }

    /**
     * @param message
     */
    public InvalidArchiveStructure(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public InvalidArchiveStructure(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public InvalidArchiveStructure(String message, Throwable cause) {
        super(message, cause);
    }

}
