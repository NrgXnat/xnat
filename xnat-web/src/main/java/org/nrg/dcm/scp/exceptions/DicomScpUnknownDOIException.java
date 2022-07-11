package org.nrg.dcm.scp.exceptions;

import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DicomScpUnknownDOIException extends NrgServiceException {

    public DicomScpUnknownDOIException(DicomSCPInstance instance) {
        super(NrgServiceError.ConfigurationError,
                String.format("Tried to provision DICOM SCP receiver '%s' but that instance specifies an unknown DICOM Object Identifier '%s'.", instance.getLabel(), instance.getIdentifier()));
    }

}
