package org.nrg.xapi.rest.dicomscp;

import org.nrg.dcm.exceptions.EnabledDICOMReceiverWithDuplicatePortException;
import org.nrg.framework.exceptions.NrgServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class DicomSCPApiAdvice {
    @ExceptionHandler(EnabledDICOMReceiverWithDuplicatePortException.class)
    public ResponseEntity<String> handleEnabledDICOMReceiverWithDuplicatePort(final EnabledDICOMReceiverWithDuplicatePortException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NrgServiceException.class)
    public ResponseEntity<String> handleNrgServiceException(final NrgServiceException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }
}
