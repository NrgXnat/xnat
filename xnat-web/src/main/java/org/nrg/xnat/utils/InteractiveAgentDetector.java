/*
 * web: org.nrg.xnat.utils.BrowserDetectorI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * Detects whether a particular request is from an interactive agent&mdash;usually a browser, but possibly some other
 * kind of interactive client application&mdash;or a non-interactive agent, such as a command-line tool like <b>curl</b>
 * or <b>wget</b>.
 */
public interface InteractiveAgentDetector {
    /**
     * Indicates whether the request is from an interactive agent.
     *
     * @param request The request you want to evaluate.
     *
     * @return Returns <b>true</b> if the request appears to be from an interactive agent, <b>false</b> otherwise.
     */
    boolean isInteractiveAgent(final HttpServletRequest request);

    /**
     * Indicates whether the path is a data (or REST) path.
     *
     * @param request The path you want to evaluate.
     *
     * @return Returns <b>true</b> if the request is a call to a data path, <b>false</b> otherwise.
     */
    boolean isDataPath(final HttpServletRequest request);
}
