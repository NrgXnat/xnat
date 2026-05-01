package org.nrg.dicom.mizer.objects;

import java.util.List;

public class AnonymizationResultSuccess extends BaseAnonymizationResult {
    public AnonymizationResultSuccess(DicomObjectI dicomObject, List<String> messages) {
        super(dicomObject, AnonymizationResultSeverity.SUCCESS, messages);
    }
    public AnonymizationResultSuccess(DicomObjectI dicomObject, String message) {
        super(dicomObject, AnonymizationResultSeverity.SUCCESS, message);
    }
    public AnonymizationResultSuccess(DicomObjectI dicomObject) {
        super(dicomObject, AnonymizationResultSeverity.SUCCESS, "Success");
    }
}
