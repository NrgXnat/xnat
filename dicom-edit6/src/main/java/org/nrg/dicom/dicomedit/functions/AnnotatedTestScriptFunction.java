package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.dicomedit.annotation.DicomEditFunction;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationRuntimeException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

/**
 * Demonstrates the use of DicomEditFunction annotation to create functions.
 */
@DicomEditFunction(name="annotatedTestScriptFunction", namespace = "org.nrg.dcm.edit")
public class AnnotatedTestScriptFunction extends AbstractScriptFunction {
    public AnnotatedTestScriptFunction(Properties properties) {
        super(properties);
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) {
        try {
            return values.size() > 0 ? new ConstantValue(MessageFormat.format(values.get(0).asString(), values.subList(1, values.size()).toArray())) : null;
        } catch(IllegalArgumentException e) {
            throw new ScriptEvaluationRuntimeException(getFormattedErrorMessage(values));
        }
    }
}
