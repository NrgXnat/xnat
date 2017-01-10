/*
 * web: org.nrg.xnat.restlet.util.BrowserDetectorI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.util;

import javax.servlet.http.HttpServletRequest;

public interface BrowserDetectorI {

	public abstract String getUserAgent(HttpServletRequest request);

	public abstract boolean isBrowser(HttpServletRequest request);

}
