/*
 * org.nrg.xnat.exceptions.InvalidArchiveStructure
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
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
