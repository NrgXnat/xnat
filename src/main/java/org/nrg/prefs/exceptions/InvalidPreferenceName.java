/*
 * prefs: org.nrg.prefs.exceptions.InvalidPreferenceName
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;

public class InvalidPreferenceName extends NrgServiceException {
    public InvalidPreferenceName(final String message) {
        super(NrgServiceError.ConfigurationError, message);
    }
}
