/*
 * org.nrg.xdat.exceptions.InvalidSearchException
 * XNAT http://www.xnat.org
 * Copyright (c) 2015, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.exceptions;

public class InvalidSearchException extends RuntimeException {
    public InvalidSearchException(final String message) {
        super(message);
    }
}
