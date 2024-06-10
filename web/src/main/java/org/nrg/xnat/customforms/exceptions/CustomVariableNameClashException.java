package org.nrg.xnat.customforms.exceptions;

import java.util.List;

public class CustomVariableNameClashException extends Exception {
    private static final long serialVersionUID = 1L;
    private List<String> clashes;

    public CustomVariableNameClashException(List<String> clashes) {
        super(String.join(", ", clashes));
        this.clashes = clashes;
    }

    public List<String> getClashes() {
        return clashes;
    }
}
