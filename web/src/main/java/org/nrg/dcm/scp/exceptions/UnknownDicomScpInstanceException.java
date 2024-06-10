/*
 * web: org.nrg.dcm.scp.exceptions.EnabledDICOMReceiverWithDuplicatePortException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UnknownDicomScpInstanceException extends NrgServiceException {
    public UnknownDicomScpInstanceException(final String description) {
        super(NrgServiceError.ConfigurationError, "Couldn't find a DICOM SCP instance with the ID or title " + description);
    }

    public UnknownDicomScpInstanceException(final int id) {
        this(Integer.toString(id));
    }
}
