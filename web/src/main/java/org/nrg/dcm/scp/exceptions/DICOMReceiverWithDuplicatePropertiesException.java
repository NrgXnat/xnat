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

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(BAD_REQUEST)
@Getter
@Accessors(prefix = "_")
@Slf4j
public class DICOMReceiverWithDuplicatePropertiesException extends NrgServiceException {
    public DICOMReceiverWithDuplicatePropertiesException(final List<DICOMReceiverWithDuplicatePropertiesException> exceptions) {
        _exceptions = ImmutableList.<DICOMReceiverWithDuplicatePropertiesException>builder().addAll(exceptions).build();
    }

    protected DICOMReceiverWithDuplicatePropertiesException() {
        super(NrgServiceError.AlreadyInitialized);
        _exceptions = Collections.emptyList();
    }

    private final List<? extends DICOMReceiverWithDuplicatePropertiesException> _exceptions;
}
