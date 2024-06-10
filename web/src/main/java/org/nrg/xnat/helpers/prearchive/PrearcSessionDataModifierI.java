/*
 * web: org.nrg.xnat.helpers.prearchive.PrearcSessionDataModifierI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

public interface PrearcSessionDataModifierI {
	void setStatus(SessionData sd, final PrearcUtils.PrearcStatus status);
}
