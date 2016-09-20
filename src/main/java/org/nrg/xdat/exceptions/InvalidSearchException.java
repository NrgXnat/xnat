/*
 * core: org.nrg.xdat.exceptions.InvalidSearchException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
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
