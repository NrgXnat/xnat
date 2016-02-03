/*
 * org.nrg.xnat.helpers.prearchive.PrearcSessionDataModifierI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.prearchive;

public interface PrearcSessionDataModifierI {
	void setStatus(SessionData sd, final PrearcUtils.PrearcStatus status);
}