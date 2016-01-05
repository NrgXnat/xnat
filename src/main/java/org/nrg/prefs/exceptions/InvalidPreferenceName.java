package org.nrg.prefs.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;

public class InvalidPreferenceName extends NrgServiceException {
    public InvalidPreferenceName(final String message) {
        super(NrgServiceError.ConfigurationError, message);
    }
}
