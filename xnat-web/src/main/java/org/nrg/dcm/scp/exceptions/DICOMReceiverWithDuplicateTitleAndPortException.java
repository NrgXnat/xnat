/*
 * web: org.nrg.dcm.scp.exceptions.EnabledDICOMReceiverWithDuplicatePortException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp.exceptions;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DICOMReceiverWithDuplicateTitleAndPortException extends NrgServiceException {
    public DICOMReceiverWithDuplicateTitleAndPortException(@Nonnull final String aeTitle, @Nonnull final Integer port) {
        super(NrgServiceError.AlreadyInitialized);
        _duplicates.add(new ImmutablePair<>(aeTitle, port));
    }

    public DICOMReceiverWithDuplicateTitleAndPortException(@Nonnull final DicomSCPInstance instance) {
        this(instance.getAeTitle(), instance.getPort());
    }

    public DICOMReceiverWithDuplicateTitleAndPortException(@Nonnull final List<DICOMReceiverWithDuplicateTitleAndPortException> exceptions) {
        super(NrgServiceError.AlreadyInitialized);
        for (final DICOMReceiverWithDuplicateTitleAndPortException exception : exceptions) {
            _duplicates.addAll(exception.getDuplicates());
        }
    }

    public String getAeTitle() {
        return _duplicates.isEmpty() ? null : _duplicates.get(0).getLeft();
    }

    public int getPort() {
        return _duplicates.isEmpty() ? 0 : _duplicates.get(0).getRight();
    }

    public List<ImmutablePair<String, Integer>> getDuplicates() {
        return _duplicates;
    }

    @Override
    public String toString() {
        return "Tried to create or update a DICOM SCP receiver with the title and port " + getAeTitle() + ":" + getPort() + ", but a receiver with the same title and port already exists.";
    }

    private final List<ImmutablePair<String, Integer>> _duplicates = new ArrayList<>();
}
