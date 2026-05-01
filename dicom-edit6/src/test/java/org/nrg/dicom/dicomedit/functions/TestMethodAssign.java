package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.dicomedit.TagPathFactory;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationRuntimeException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

public class TestMethodAssign extends AbstractScriptFunction {
    private Logger logger = LoggerFactory.getLogger( TestMethodAssign.class);

    public TestMethodAssign(String name, String nameSpace, String usage, String description) {
        super( name, nameSpace, usage, description);
    }

    public TestMethodAssign(Properties properties) {
        super(properties);
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {
        if (values.size() != 2) {
            throw new ScriptEvaluationException("usage: testMethodAssign[ <tagpath>, value ]");
        }

        try {
            TagPath tp = TagPathFactory.createDE6Instance(values.get(0).asString());
            dicomObject.assign( tp, values.get(1).asString());
        }
        catch( ScriptEvaluationRuntimeException e) {
            String msg = "function arg tagPath: " + values.get(0).asString();
            logger.error( msg);
//         throw new ScriptEvaluationException( msg, e);
        }
        return ConstantValue.VOID;
    }
}
