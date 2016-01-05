package org.nrg.prefs.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;

public class UnknownToolId extends NrgServiceRuntimeException {
    public UnknownToolId(final String toolId) {
        super(NrgServiceError.Unknown, "Didn't find a tool with the corresponding tool ID: " + toolId);
    }
}
