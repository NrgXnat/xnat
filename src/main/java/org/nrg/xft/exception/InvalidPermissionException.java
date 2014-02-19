/*
 * org.nrg.xft.exception.InvalidPermissionException
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
public class InvalidPermissionException extends Exception {

    public InvalidPermissionException(String error) {
    	super("This user has insufficient privileges for the data type '" +error + "'.");	
    }

}
