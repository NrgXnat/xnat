package org.nrg.dicom.mizer.objects;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class BaseAnonymizationResult implements AnonymizationResult {
    private DicomObjectI dicomObject;
    @NonNull private AnonymizationResultSeverity severity;
    @NonNull private List<String> messages;
    @Setter
    @Getter
    private String absolutePath;

    public BaseAnonymizationResult(DicomObjectI dicomObject, AnonymizationResultSeverity severity, List<String> msg) {
        this.dicomObject = dicomObject;
        this.severity = severity;
        this.messages = msg;
    }
    public BaseAnonymizationResult(DicomObjectI dicomObject, AnonymizationResultSeverity severity, String msg) {
        this(dicomObject,severity, Arrays.asList(msg));
    }

    public void addMessages(List<String> msgs) {
        messages.addAll(msgs);
    }
    public void addMessage(String msg) {
        messages.add(msg);
    }
    public String getMessage() {
        return String.join("\n", messages);
    }

    public AnonymizationResult merge( AnonymizationResult other) {
        AnonymizationResultSeverity severity = (this.isOtherMoreSevere(other))? other.getSeverity(): this.getSeverity();
        AnonymizationResult result;
        List<String> mergedMessages = new ArrayList<>();
        mergedMessages.addAll( this.getMessages());
        mergedMessages.add("Merging results.");
        mergedMessages.addAll(other.getMessages());
        switch (severity) {
            case SUCCESS:
                result = new AnonymizationResultSuccess(this.getDicomObject(), mergedMessages);
                break;
            case SUCCESS_WITH_WARNING:
                result = new AnonymizationResultSuccess(this.getDicomObject(), mergedMessages);
                break;
            case ERROR:
                result = new AnonymizationResultError(this.getDicomObject(), mergedMessages);
                break;
            case REJECT:
                result = new AnonymizationResultReject(this.getDicomObject(), mergedMessages);
                break;
            default:
                mergedMessages.add("Merging results of unaccounted for severity: " + severity);
                result = new AnonymizationResultError(this.getDicomObject(), mergedMessages);
        }
        return result;
    }

    public boolean isOtherMoreSevere(AnonymizationResult other) {
        return severity.compareTo(other.getSeverity()) > 0;
    }

    @Override
    public void releaseObjectFromMemory() {
        this.dicomObject=null;
    }
}
