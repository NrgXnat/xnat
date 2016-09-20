/*
 * core: org.nrg.xdat.security.user.exceptions.EnabledException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.user.exceptions;

public class EnabledException extends FailedLoginException {
    public EnabledException(String login) {
        super("User (" + login + ") Account is disabled.", login);
    }
}