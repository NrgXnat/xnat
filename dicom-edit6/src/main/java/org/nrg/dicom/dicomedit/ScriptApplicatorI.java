package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public interface ScriptApplicatorI {
    AnonymizationResult apply(File file) throws MizerException;

    AnonymizationResult apply(InputStream is) throws MizerException;

    AnonymizationResult apply(DicomObjectI dicomObject);

    Set<String> getVariableNames();

    Set<String> getExternalVariableNames();

    Variable getVariable(String name);

    Value getValue(String name);

    long getTopTag();

    void apply(File matchFile, DicomObjectI dicomObject) throws MizerException;

    Map<String, Variable> getVariables();

    void setVariable(String name, String value);

}
