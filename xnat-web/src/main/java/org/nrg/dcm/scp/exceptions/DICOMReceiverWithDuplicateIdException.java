/*
 * web: org.nrg.dcm.scp.exceptions.EnabledDICOMReceiverWithDuplicatePortException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp.exceptions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
@Accessors(prefix = "_")
public class DICOMReceiverWithDuplicateIdException extends DICOMReceiverWithDuplicatePropertiesException {
    public DICOMReceiverWithDuplicateIdException(final int id) {
        _duplicates.add(id);
    }

    @SuppressWarnings("unused")
    public DICOMReceiverWithDuplicateIdException(@Nonnull final DicomSCPInstance instance) {
        this( (int) instance.getId());
    }

    @SuppressWarnings("unused")
    public DICOMReceiverWithDuplicateIdException(@Nonnull final List<Integer> ids) {
        if (ids.isEmpty()) {
            throw new NrgServiceRuntimeException("Can't create an instance of this class from an empty list.");
        }
        _duplicates.addAll(ids);
    }

    @SuppressWarnings("unused")
    public static DICOMReceiverWithDuplicateIdException fromExceptionList(@Nonnull final List<DICOMReceiverWithDuplicateIdException> exceptions) {
        return new DICOMReceiverWithDuplicateIdException(Lists.newArrayList(Iterables.concat(Lists.transform(exceptions, new Function<DICOMReceiverWithDuplicateIdException, List<Integer>>() {
            @Override
            public List<Integer> apply(final DICOMReceiverWithDuplicateIdException exception) {
                return exception.getDuplicates();
            }
        }))));
    }

    protected int getId() {
        return _duplicates.get(0);
    }

    @Override
    public String toString() {
        return "Tried to create or update a DICOM SCP receiver with the ID " + getId() + ", but a receiver with the same ID already exists.";
    }

    private final List<Integer> _duplicates = new ArrayList<>();
}
