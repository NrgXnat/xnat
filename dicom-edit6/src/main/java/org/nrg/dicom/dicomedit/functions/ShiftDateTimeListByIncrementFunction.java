/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.HashUIDFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.apache.commons.lang3.StringUtils;
import org.nrg.dicom.dicomedit.DicomDateTimeShifter;
import org.nrg.dicom.dicomedit.datetime.DefaultDicomDateTimeShifter;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.objects.DicomObjectVisitor;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShiftDateTimeListByIncrementFunction extends AbstractScriptFunction {

    public ShiftDateTimeListByIncrementFunction() {
        super("shiftDateTimeListByIncrement", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(final List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {
        if (values.size() != 3) {
            throw new ScriptEvaluationException("usage: shiftDateByIncrement[src-date, increment, incrementUnits ]");
        }

        List<TagPath> specifiedTagPaths = getTagPaths( values.get(0).asValueList());
        final int increment = values.get(1).asInteger();
        final String incrementUnits = values.get(2).asString();
        Visitor visitor = new Visitor( specifiedTagPaths, increment, incrementUnits);

        visitor.visit( dicomObject);

        return new ConstantValue(AbstractMizerValue.VOID);
    }

    protected String getShiftedDateTime( String dateTimeString, int increment, String incrementUnits) {
        if(StringUtils.isEmpty( dateTimeString)) {
            return dateTimeString;
        } else {
            DicomDateTimeShifter shifter = new DefaultDicomDateTimeShifter();
            return shifter.shiftDateTime(dateTimeString, increment, incrementUnits);
        }
    }

    private class Visitor extends DicomObjectVisitor {
        private final List<TagPath> specifiedTagPaths;
        private final int increment;
        private final String incrementUnits;
        public Visitor( List<TagPath> specifiedTagPaths, int increment, String incrementUnits) {
            this.specifiedTagPaths = Collections.unmodifiableList(new ArrayList<>(specifiedTagPaths));
            this.increment = increment;
            this.incrementUnits = incrementUnits;
        }

        public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
            specifiedTagPaths.forEach(stp -> doTagPath(stp, tagPath, dicomElement, dicomObject));
        }

        protected void doTagPath( TagPath specifiedTagPath, TagPath testTagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
            if( specifiedTagPath.isMatch(testTagPath)) {
                String dateTimeString = dicomObject.getString( dicomElement.tag());
                dicomObject.putString(dicomElement.tag(), getShiftedDateTime(dateTimeString, increment, incrementUnits));
            }
        }
    }
}
