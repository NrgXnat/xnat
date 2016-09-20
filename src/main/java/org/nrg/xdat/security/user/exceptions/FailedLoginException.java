/*
 * core: org.nrg.xdat.security.user.exceptions.FailedLoginException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.user.exceptions;

public class FailedLoginException extends Exception {
    public String FAILED_LOGIN = null;

    public FailedLoginException(String message, String login) {
        super(message);
        FAILED_LOGIN = login;
    }
}