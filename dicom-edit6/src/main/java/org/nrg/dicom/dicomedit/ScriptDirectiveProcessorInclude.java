package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.exceptions.MizerException;

import java.util.List;

/**
 * This processor recognizes the following directives:
 * 1. include <url>. <url> is the locator of another script. Import directives are followed recursively. An error
 * is thrown if the includes have circular references. The script statements are followed sequentially. Import statements
 * are replaced with the content found at the specified URL.
 *
 */
public class ScriptDirectiveProcessorInclude implements ScriptDirectiveProcessor {
    public ScriptDirectiveProcessorInclude() {}

    @Override
    public DE6Script process(DE6Script script) {
        return null;
    }

    @Override
    public List<String> process(List<String> lines) throws MizerException {
        return new IncludeChecker().process(lines);
    }
}
