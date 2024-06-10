/*
 * web: org.nrg.dcm.scp.exceptions.EnabledDICOMReceiverWithDuplicatePortException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(BAD_REQUEST)
@Getter
@Accessors(prefix = "_")
public class DICOMReceiverWithDuplicateTitleAndPortException extends DICOMReceiverWithDuplicatePropertiesException {
    public DICOMReceiverWithDuplicateTitleAndPortException(@Nonnull final String aeTitle, @Nonnull final Integer port) {
        this(Collections.singletonList(Pair.of(aeTitle, port)));
    }

    public DICOMReceiverWithDuplicateTitleAndPortException(@Nonnull final DicomSCPInstance instance) {
        this(instance.getAeTitle(), instance.getPort());
    }

    public DICOMReceiverWithDuplicateTitleAndPortException(@Nonnull final List<Pair<String, Integer>> duplicates) {
        if (duplicates.isEmpty()) {
            throw new NrgServiceRuntimeException("Can't create an instance of this class from an empty list.");
        }
        _duplicates.addAll(duplicates);
    }

    @SuppressWarnings("unused")
    public static DICOMReceiverWithDuplicateTitleAndPortException fromExceptionList(@Nonnull final List<DICOMReceiverWithDuplicateTitleAndPortException> exceptions) {
        return new DICOMReceiverWithDuplicateTitleAndPortException(Lists.newArrayList(Iterables.concat(Lists.transform(exceptions, new Function<DICOMReceiverWithDuplicateTitleAndPortException, List<Pair<String, Integer>>>() {
            @Override
            public List<Pair<String, Integer>> apply(final DICOMReceiverWithDuplicateTitleAndPortException exception) {
                return exception.getDuplicates();
            }
        }))));
    }

    public String getAeTitle() {
        return _duplicates.isEmpty() ? null : _duplicates.get(0).getLeft();
    }

    public int getPort() {
        return _duplicates.isEmpty() ? 0 : _duplicates.get(0).getRight();
    }

    @Override
    public String toString() {
        return "Tried to create or update a DICOM SCP receiver with the title and port " + DicomSCPInstance.formatDicomSCPInstanceKey(getAeTitle(), getPort()) + ", but a receiver with the same title and port already exists.";
    }

    private final List<Pair<String, Integer>> _duplicates = new ArrayList<>();
}
