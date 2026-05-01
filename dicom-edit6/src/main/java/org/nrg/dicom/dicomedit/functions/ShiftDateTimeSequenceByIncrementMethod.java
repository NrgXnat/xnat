/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.UppercaseFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.dicomedit.TagPathFactory;
import org.nrg.dicom.dicomedit.datetime.DefaultDicomDateTimeShifter;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationRuntimeException;
import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.objects.DicomObjectVisitor;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;

public class ShiftDateTimeSequenceByIncrementMethod extends AbstractScriptFunction {

    private static final Logger logger = LoggerFactory.getLogger(ShiftDateTimeSequenceByIncrementMethod.class);

    public ShiftDateTimeSequenceByIncrementMethod() {
        super("shiftDateTimeSequenceByIncrement", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(final List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {

        if (values.size() > 1) {

            final int increment = values.get(0).asInteger();
            final List<Value> tagPathValues = values.subList(1, values.size());

            DicomObjectVisitor visitor = new DicomObjectVisitor() {
                @Override
                public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
                    logger.debug(tagPath + " : " + dicomElement);
                    for (Value v : tagPathValues) {
                        try {
                            TagPath tp = TagPathFactory.createDE6Instance(v.asString());
                            if (tp.isMatch(tagPath)) {
                                String dateTimeString = dicomObject.getString( dicomElement.tag());
                                String shiftedDateTimeString = new DefaultDicomDateTimeShifter().shiftDateTime( dateTimeString, increment, "seconds");
                                dicomObject.putString( dicomElement.tag(), shiftedDateTimeString);
                                return;
                            }
                        } catch (ScriptEvaluationRuntimeException e) {
                            String msg = "function arg tagPath: " + v.asString();
                            logger.error(msg);
//                            throw new ScriptEvaluationException( msg, e);
                        }
                    }
                }
            };

            visitor.visit(dicomObject);

        } else {
            tooFewArguments( values);
        }

        return AbstractMizerValue.VOID;
    }

    protected void tooFewArguments(List<Value> values) {

        String arguments;
        switch (values.size()) {
            case 0:
                arguments = "null";
                break;
            case 1:
                arguments = values.get(0).asString();
                break;
            default:
                arguments = null;
        }

        MessageFormat message = new MessageFormat("shiftDateTimeSequenceByIncrement illegal arguments: ''{0}''. Expect increment-in-seconds, tagpath-string");
        throw new ScriptEvaluationRuntimeException(message.format(arguments));
    }

}
