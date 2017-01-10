/*
 * core: org.nrg.xdat.security.validators.PasswordValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.validators;

import org.nrg.xft.security.UserI;

/**
 * Defines objects for validating passwords.
 */
public interface PasswordValidator {
    /**
     * Tests the validity of the submitted password. If valid, this returns an empty string. If invalid, it returns a
     * string with a message indicating the reason.
     *
     * @param password The password to test
     * @param user     The user to test for
     * @return The reason for failure if invalid, blank otherwise.
     */
    String isValid(final String password, final UserI user);
}
