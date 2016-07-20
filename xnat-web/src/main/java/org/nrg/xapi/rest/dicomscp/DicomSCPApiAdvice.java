package org.nrg.xapi.rest.dicomscp;

import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.exceptions.DuplicateEnabledDICOMReceiverPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class DicomSCPApiAdvice {
    @ExceptionHandler(DuplicateEnabledDICOMReceiverPort.class)
    public ResponseEntity<String> handleDuplicateEnabledDICOMReceiverPort(final DuplicateEnabledDICOMReceiverPort exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NrgServiceException.class)
    public ResponseEntity<String> handleNrgServiceException(final NrgServiceException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }
}
