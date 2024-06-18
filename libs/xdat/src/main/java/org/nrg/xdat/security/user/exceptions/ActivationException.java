/*
 * core: org.nrg.xdat.security.user.exceptions.ActivationException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.user.exceptions;

public class ActivationException extends FailedLoginException {
    public ActivationException(String login) {
        super("User (" + login + ") Account is in quarantine.", login);
    }
}
