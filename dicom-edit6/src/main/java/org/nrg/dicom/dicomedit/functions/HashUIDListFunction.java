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
import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.objects.DicomObjectVisitor;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.UIDValue;
import org.nrg.dicom.mizer.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Arrays.stream;

/**
 * Creates a one-way hash UID by first creating a Version 5 UUID (SHA-1 hash) from the provided string,
 * then converting that UUID to a UID.
 *
 * All arguments not a tagPath are ignored.
 */
public class HashUIDListFunction extends AbstractScriptFunction {
    private static final Logger logger = LoggerFactory.getLogger(HashUIDListFunction.class);

    public HashUIDListFunction() {
        super("hashUIDList", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(final List<Value> tagPathValues, DicomObjectI dicomObject) throws ScriptEvaluationException {

        DicomObjectVisitor visitor = new DicomObjectVisitor() {
            @Override
            public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
                for (TagPath v : getTagPaths(tagPathValues)) {
                    if (v.isMatch(tagPath) && !tagPath.isPrivateCreatorID()) {
                        if( ! dicomElement.isUID()) {
                            logger.warn("Creating hashed UID for tag {} with VR = {}", dicomElement, dicomElement.getVRAsString());
                        }
                        String[] uidStrings = dicomObject.getStrings(dicomElement.tag());
                        String[] hashedStrings = stream(uidStrings).map(UIDValue::getHashedUID).toArray(String[]::new);
                        dicomObject.putStrings(dicomElement.tag(), hashedStrings);
                        return;
                    }
                }
            }
        };

        visitor.visit(dicomObject);

        return AbstractMizerValue.VOID;
    }

}
