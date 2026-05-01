/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.ReplaceFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Find all the tagPath arguments and delete all tags matching those tagPaths. TagPaths may contain wildcards.
 */
public class RemoveTagsFunction extends AbstractScriptFunction {

    private Logger logger = LoggerFactory.getLogger(RemoveTagsFunction.class);

    public RemoveTagsFunction() {
        super("removeTags", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) {
        getTagPaths(flattenValueList(values)).forEach(tagPath -> dicomObject.delete(tagPath));
        return new ConstantValue(AbstractMizerValue.VOID);
    }

}
