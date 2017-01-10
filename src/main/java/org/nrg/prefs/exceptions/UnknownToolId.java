/*
 * prefs: org.nrg.prefs.exceptions.UnknownToolId
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;

public class UnknownToolId extends NrgServiceRuntimeException {
    public UnknownToolId(final String toolId) {
        super(NrgServiceError.Unknown, "Didn't find a tool with the corresponding tool ID: " + toolId);
    }
}
