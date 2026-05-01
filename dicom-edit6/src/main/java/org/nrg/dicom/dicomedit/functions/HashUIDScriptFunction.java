/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.HashUIDFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.UIDValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;

/**
 * Creates a one-way hash UID by first creating a Version 5 UUID (SHA-1 hash) from the provided string,
 * then converting that UUID to a UID.
 * Portions of this code are derived from dcm4che, an MPL 1.1-licensed open source project
 * hosted at http://sourceforge.net/projects/dcm4che.
 *
 * @author David Maffitt david.maffitt@wustl.edu { modifications for migration to DE5, antlr4. }
 * @author Kevin A. Archie karchie@wustl.edu
 * @author Gunter Zeilinger gunterze@gmail.com { original dcm4che implementation of UUID-to-UID conversion }
 */
public class HashUIDScriptFunction extends AbstractScriptFunction {
    public HashUIDScriptFunction() {
        super("hashUID", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(final List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {
        if (values.isEmpty()) {
            throw new ScriptEvaluationException("usage: hashUID[string-to-hash {, optional algorithm-name} ]");
        }
        final String valueString = (values.get(0).asObject() instanceof TagPath) ? dicomObject.getString((TagPath) values.get(0).asObject()) : values.get(0).asString();
        return UIDValue.getHashedUIDValue(valueString);
    }
}
