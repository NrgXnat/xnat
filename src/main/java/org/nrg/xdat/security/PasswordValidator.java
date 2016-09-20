/*
 * core: org.nrg.xdat.security.PasswordValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import org.nrg.xft.security.UserI;

public interface PasswordValidator {
	boolean isValid(String password, UserI user);
	String getMessage();
}
