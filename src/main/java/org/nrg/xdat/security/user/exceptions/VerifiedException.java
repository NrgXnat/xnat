/*
 * core: org.nrg.xdat.security.user.exceptions.VerifiedException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.user.exceptions;

public class VerifiedException extends FailedLoginException {
    public VerifiedException(String login) {
        super("User (" + login + ") Account is unverified.", login);
    }
}