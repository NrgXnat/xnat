package org.nrg.dicom.mizer.objects;

import java.util.List;

public class AnonymizationResultError extends BaseAnonymizationResult {
    public AnonymizationResultError(DicomObjectI dicomObject, List<String> messages) {
        super(dicomObject, AnonymizationResultSeverity.ERROR, messages);
    }
    public AnonymizationResultError(DicomObjectI dicomObject, String message) {
        super(dicomObject, AnonymizationResultSeverity.ERROR, message);
    }

}
