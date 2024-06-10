package org.nrg.dcm.scp.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DicomScpInvalidAeTitleException extends NrgServiceException {
    public DicomScpInvalidAeTitleException(String message) {
        super(NrgServiceError.ConfigurationError, message);
    }
}
