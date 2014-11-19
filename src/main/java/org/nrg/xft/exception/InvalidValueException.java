/*
 * org.nrg.xft.exception.InvalidValueException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.exception;

/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class InvalidValueException extends Exception {
    public InvalidValueException(String message)
    {
        super(message);
    }
}

