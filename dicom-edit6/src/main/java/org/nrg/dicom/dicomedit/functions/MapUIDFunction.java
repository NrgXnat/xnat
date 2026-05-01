/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.NewUIDFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * MapUIDFunction
 *
 * MapUIDFunction takes two arguments:
 * 1. the value to be mapped to a UID. Happily maps any value including null and empty string. No hashing involved.
 * 2. a string prefix that will be prepended to the generated UID.
 *
 * This function keeps state of previous values it has encountered and is guaranteed to return the same mapped value
 * for previously encountered values.
 *
 */
public class MapUIDFunction extends AbstractScriptFunction {
    private Map<String, String> uidMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(MapUIDFunction.class);

    public MapUIDFunction() {
        super("mapUID", AbstractScriptFunction.DEFAULT_NAMESPACE,
                "Usage: mapUID[ <prefix>, value ]",
                "Description: Generate a UID with given prefix. UID is random (no hashing) but function will return the same UID for previously encountered value.");
    }

    @Override
    public Value apply(final List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {

        if (values.size() == 2) {

            final String prefix = values.get(1).asString();
            final String uid = values.get(0).asString();
            logger.debug("arguments= {}, {}", uid, prefix);

            if( ! uidMap.containsKey( uid)) {
                String newUID = UIDUtils.createUID( prefix);
                uidMap.put( uid, newUID);
            }
            logger.debug("result= {}", uidMap.get(uid));
            return new ConstantValue( uidMap.get( uid));

        } else {
            badArguments( values);
            // unreachable because badArguments always throws ScriptEvaluationException
            return AbstractMizerValue.VOID;
        }
    }

    protected void badArguments(List<Value> values) throws ScriptEvaluationException {

        String arguments;
        switch (values.size()) {
            case 0:
                arguments = "null";
                break;
            default:
                StringBuilder sb = new StringBuilder();
                Iterator<Value> iterator = values.iterator();
                while( iterator.hasNext()) {
                    sb.append( iterator.next().asString());
                    if( iterator.hasNext()) sb.append(", ");
                }
                arguments = sb.toString();
        }

        MessageFormat message = new MessageFormat("mapUID illegal arguments: '{0}'. Expect tagpath, uid-prefix-string");
        String msg = message.format(arguments);
        logger.debug(msg);
        throw new ScriptEvaluationException(msg);
    }

}
