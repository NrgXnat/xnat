package org.nrg.dicom.mizer.objects;

import java.util.List;

public class AnonymizationResultReject extends BaseAnonymizationResult {
    public AnonymizationResultReject(DicomObjectI dicomObject, List<String> messages) {
        super(dicomObject, AnonymizationResultSeverity.REJECT, messages);
    }
    public AnonymizationResultReject(DicomObjectI dicomObject, String message) {
        super(dicomObject, AnonymizationResultSeverity.REJECT, message);
    }
}
