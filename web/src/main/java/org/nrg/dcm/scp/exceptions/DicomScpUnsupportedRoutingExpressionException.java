package org.nrg.dcm.scp.exceptions;

import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DicomScpUnsupportedRoutingExpressionException extends NrgServiceException {
    public DicomScpUnsupportedRoutingExpressionException(DicomSCPInstance instance) {
        super(NrgServiceError.ConfigurationError,
                String.format("Dicom SCP receiver '%s' does not support custom routing but custom routing is enabled.", instance.getLabel()));
    }
}
