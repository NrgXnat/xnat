package org.nrg.dicom.dicomedit;

import java.util.List;

/**
 * This processor strips all escaped newlines out of the script.
 */
public class ScriptProcessorRemoveEscNewline implements ScriptDirectiveProcessor {

    public ScriptProcessorRemoveEscNewline() {}

    @Override
    public DE6Script process(DE6Script script) {
        return null;
    }

    /**
     * strips all escaped newlines
     *
     * @param lines list of lines to strip.
     * @return list of stripped lines.
     */
    @Override
    public List<String> process(List<String> lines) {
        return new EscNewlineRemover().process(lines);
    }
}
