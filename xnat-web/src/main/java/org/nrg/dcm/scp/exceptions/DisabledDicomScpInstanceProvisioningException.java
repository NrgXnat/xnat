/*
 * web: org.nrg.dcm.scp.exceptions.EnabledDICOMReceiverWithDuplicatePortException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp.exceptions;

import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DisabledDicomScpInstanceProvisioningException extends NrgServiceException {
    public DisabledDicomScpInstanceProvisioningException(final DicomSCPInstance disabled) {
        super(NrgServiceError.ConfigurationError);
        _disabled = disabled;
    }

    public DicomSCPInstance getDisabled() {
        return _disabled;
    }

    @Override
    public String toString() {
        return "Tried to provision DICOM SCP receiver ID " + _disabled.getId() + " [" + _disabled.toString() + "], but that instance is disabled.";
    }
    
    private final DicomSCPInstance _disabled;
}
