/*
 * core: org.nrg.xft.exception.InvalidPermissionException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.exception;

/**
 * @author Tim
 */
@SuppressWarnings("serial")
public class InvalidPermissionException extends XftItemException {
    public InvalidPermissionException(final String error) {
        super("This user has insufficient privileges for the data type '" + error + "'.");
    }
}
