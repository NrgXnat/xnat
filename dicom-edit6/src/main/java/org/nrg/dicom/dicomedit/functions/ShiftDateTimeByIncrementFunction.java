/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.HashUIDFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.dicom.dicomedit.DicomDateTimeShifter;
import org.nrg.dicom.dicomedit.datetime.DefaultDicomDateTimeShifter;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;

@Slf4j
public class ShiftDateTimeByIncrementFunction extends AbstractScriptFunction {
    private final DicomDateTimeShifter dateTimeShifter;
    public ShiftDateTimeByIncrementFunction() {
        super("shiftDateTimeByIncrement", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
        this.dateTimeShifter = new DefaultDicomDateTimeShifter();
    }

    @Override
    public Value apply(final List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {
        if ( ! (values.size() == 2 || values.size() == 3)) {
            throw new ScriptEvaluationException("usage: shiftDateTimeByIncrement[<date> increment [, increment-units] ]");
        }

        String dateString = null;
        if (values.get(0).asObject() instanceof TagPath) {
            TagPath tp = (TagPath) values.get(0).asObject();
            if (!tp.isSingular()) {
                throw new ScriptEvaluationException("shiftDateByIncrement-argument must resolve to a single attribute: " + tp);
            }
            if ( ! ("DA".equals(dicomObject.getVR(tp)) || "DT".equals(dicomObject.getVR(tp)))) {
                String msg = String.format("shiftDateByIncrement-argument must resolve to an attribute with VR = DA or DT: %s, %s", tp,dicomObject.getVR(tp));
                throw new ScriptEvaluationException(msg);
            }
            dateString = dicomObject.getString(tp);
        } else {
            dateString = values.get(0).asString();
        }
        if(StringUtils.isEmpty( dateString)) {
            log.warn("Skipping empty dateTime.");
        }

        final int increment = values.get(1).asInteger();
        String incrementUnits = (values.size() == 3)? values.get(2).asString(): "seconds";

        String shiftedDate = dateTimeShifter.shiftDateTime( dateString, increment, incrementUnits);

        return new ConstantValue(shiftedDate);
   }

}
