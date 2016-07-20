package org.nrg.xapi.exceptions;

import org.nrg.framework.exceptions.NrgServiceException;

public class DuplicateEnabledDICOMReceiverPort extends NrgServiceException {
    public DuplicateEnabledDICOMReceiverPort(final String message) {
        super(message);
    }
}
