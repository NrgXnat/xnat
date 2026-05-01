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
import org.nrg.dicom.mizer.exceptions.MizerException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Map the referenced uids and prepend the prefix.
 *
 */
public class MapReferencedUIDsMethod extends AbstractScriptFunction {

    private static final Logger logger = LoggerFactory.getLogger(MapReferencedUIDsMethod.class);
    private Map<String, String> uidMap = new HashMap<>();

    public MapReferencedUIDsMethod() {
        super("mapReferencedUIDs", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(final List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {

        if (values.size() > 1) {

            final String prefix = values.get(0).asString();
            final List<Value> tagPathValues = values.subList(1, values.size());

            DicomObjectVisitor visitor = new DicomObjectVisitor() {
                @Override
                public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
                    logger.debug(tagPath + " : " + dicomElement);
                    for (Value v : tagPathValues) {
                        try {
                            TagPath tp = TagPathFactory.createDE6Instance(v.asString());
                            if (tp.isMatch(tagPath)) {
                                String uid = dicomObject.getString( dicomElement.tag());
                                if( ! uidMap.containsKey( uid)) {
                                    String newUID = UIDUtils.createUID( prefix);
                                    uidMap.put( uid, newUID);
                                }
                                dicomObject.putString( dicomElement.tag(), uidMap.get( uid));
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

        MessageFormat message = new MessageFormat("mapReferencedUIDs illegal arguments: '{0}'. Expect uid-prefix-string, tagpath-string");
        throw new ScriptEvaluationRuntimeException(message.format(arguments));
    }

}
