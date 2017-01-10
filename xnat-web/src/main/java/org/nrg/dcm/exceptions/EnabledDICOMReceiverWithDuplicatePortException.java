/*
 * web: org.nrg.dcm.exceptions.EnabledDICOMReceiverWithDuplicatePortException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.exceptions;

import org.nrg.dcm.preferences.DicomSCPInstance;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;

public class EnabledDICOMReceiverWithDuplicatePortException extends NrgServiceException {
    public EnabledDICOMReceiverWithDuplicatePortException(final DicomSCPInstance existing, final DicomSCPInstance inserted) {
        super(NrgServiceError.AlreadyInitialized);
        _existing = existing;
        _inserted = inserted;
    }

    public DicomSCPInstance getExisting() {
        return _existing;
    }

    public DicomSCPInstance getInserted() {
        return _inserted;
    }

    @Override
    public String toString() {
        return "Tried to insert DICOM SCP receiver ID " + _inserted.getId() + " [" + _inserted.toString() + "], which replicated the port as the enabled receiver ID " + _existing.getId() + " [" + _existing.toString() + "]";
    }
    
    private final DicomSCPInstance _existing;
    private final DicomSCPInstance _inserted;
}
