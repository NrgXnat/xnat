// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * Created on Apr 24, 2006
 *
 */
package org.nrg.xft.exception;


/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class InvalidPermissionException extends Exception {

    public InvalidPermissionException(String error) {
    	super("This user has insufficient create privileges for the data type '" +error + "'.");	
    }

}
