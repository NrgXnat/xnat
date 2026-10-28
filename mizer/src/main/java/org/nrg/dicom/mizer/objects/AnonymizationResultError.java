package org.nrg.dicom.mizer.objects;

import org.nrg.dicom.mizer.exceptions.MizerException;

import java.util.List;

public class AnonymizationResultError extends BaseAnonymizationResult {
    private final Throwable cause;

    public AnonymizationResultError(DicomObjectI dicomObject, List<String> messages) {
        this(dicomObject, messages, null);
    }
    public AnonymizationResultError(DicomObjectI dicomObject, String message) {
        this(dicomObject, message, null);
    }
    public AnonymizationResultError(DicomObjectI dicomObject, List<String> messages, Throwable cause) {
        super(dicomObject, AnonymizationResultSeverity.ERROR, messages);
        this.cause = cause;
    }
    public AnonymizationResultError(DicomObjectI dicomObject, String message, Throwable cause) {
        super(dicomObject, AnonymizationResultSeverity.ERROR, message);
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Augments the joined messages with a {@code "caused by: "} chain when an originating
     * throwable was supplied. Causes whose messages are already substrings of the top-level
     * message are skipped to avoid noise.
     */
    @Override
    public String getMessage() {
        final String base = super.getMessage();
        if (cause == null) {
            return base;
        }
        final String chain = MizerException.formatCauseChain(cause, base);
        return chain.isEmpty() ? base : base + chain;
    }
}
