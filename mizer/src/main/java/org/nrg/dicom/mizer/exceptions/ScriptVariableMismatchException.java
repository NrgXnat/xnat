package org.nrg.dicom.mizer.exceptions;

import java.util.Collection;

/**
 * There is a mismatch between the variables required to be in a Mizer script
 * and the variables actually defined in the script.
 */
public class ScriptVariableMismatchException extends MizerException {
    private static final long serialVersionUID = -223133238805109993L;

    public ScriptVariableMismatchException(final String baseMessage, final Collection<String> labels) {
        this(baseMessage, labels, null);
    }

    public ScriptVariableMismatchException(final String baseMessage, final Collection<String> labels, final Throwable cause) {
        super(baseMessage + (labels.isEmpty() ? "" : ": " + String.join(",", labels)), cause);
    }
}
