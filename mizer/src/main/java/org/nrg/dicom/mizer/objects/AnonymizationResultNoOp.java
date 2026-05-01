package org.nrg.dicom.mizer.objects;

import java.util.List;

public class AnonymizationResultNoOp extends BaseAnonymizationResult {
    public AnonymizationResultNoOp(DicomObjectI dicomObject, List<String> messages) {
        super(dicomObject, AnonymizationResultSeverity.NO_OP, messages);
    }
    public AnonymizationResultNoOp(DicomObjectI dicomObject, String message) {
        super(dicomObject, AnonymizationResultSeverity.NO_OP, message);
    }
    public AnonymizationResultNoOp(DicomObjectI dicomObject) {
        super(dicomObject, AnonymizationResultSeverity.NO_OP, "No op.");
    }
}
