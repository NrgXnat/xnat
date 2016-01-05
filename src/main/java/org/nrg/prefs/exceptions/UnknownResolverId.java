package org.nrg.prefs.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;

public class UnknownResolverId extends NrgServiceRuntimeException {
    public UnknownResolverId() {
        super(NrgServiceError.Unknown, "Couldn't find a default resolver.");
    }
    public UnknownResolverId(final String resolverId) {
        super(NrgServiceError.Unknown, "Didn't find a resolver with the corresponding ID: " + resolverId);
    }
}
