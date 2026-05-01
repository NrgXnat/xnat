package org.nrg.dicom.mizer.objects;

public enum AnonymizationResultSeverity {
    // order is by severity, least severe to most for compareTo
    NO_OP,
    SUCCESS,
    SUCCESS_WITH_WARNING,
    REJECT,
    ERROR;

}
