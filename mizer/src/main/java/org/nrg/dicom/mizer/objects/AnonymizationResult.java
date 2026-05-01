package org.nrg.dicom.mizer.objects;

import java.util.List;

public interface AnonymizationResult {

    DicomObjectI getDicomObject();
    List<String> getMessages();
    String getMessage();
    AnonymizationResultSeverity getSeverity();
    void addMessages(List<String> msgs);
    void addMessage(String msg);
    AnonymizationResult merge( AnonymizationResult other);
    boolean isOtherMoreSevere(AnonymizationResult other);
    String getAbsolutePath();
    void setAbsolutePath(String absolutePath);

    void releaseObjectFromMemory();
}
