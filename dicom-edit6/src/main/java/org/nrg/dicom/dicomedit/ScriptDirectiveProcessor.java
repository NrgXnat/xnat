package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.exceptions.MizerException;

import java.util.List;

public interface ScriptDirectiveProcessor {
    DE6Script process( DE6Script script);
    List<String> process( List<String> statements) throws MizerException;
}
