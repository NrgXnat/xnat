/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.ReplaceFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.mizer.exceptions.RejectedInstanceException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.impl.MizerContextBase;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * throw RejectedJInstanceException to interrupt script processing and signal rejection.
 * Takes an optional boolean argument. Reject if true. Default is true.
 */
public class RejectFunction extends AbstractInjectedScriptFunction {

    private final int SOPInstanceUID = 0x00080018;
    private Logger logger = LoggerFactory.getLogger(RejectFunction.class);

    public RejectFunction() {
        super("reject", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
        internalContext.put(MizerContextBase.REJECT_ENABLE, true);
        internalContext.put(MizerContextBase.REJECT_WARN_REJECTION_DISABLED, true);
    }

    @Override
    public Value apply(List<Value> values, Map<String, Variable> variables, DicomObjectI dicomObject) throws ScriptEvaluationException {
        boolean isRejected = (values.size() > 0)? values.get(0).asBoolean(): true;
        if (isRejected) {
            if (isContextEnabled(variables)) {
                throw new RejectedInstanceException(String.format("Rejected: SOPInstanceUID %s", dicomObject.getString(0x00080018)));
            } else if (warnDisabledRejection(variables)){
                logger.warn("Rejection of SOPInstanceUID {} disabled.",dicomObject.getString(SOPInstanceUID));
            }
        }
        return new ConstantValue(AbstractMizerValue.VOID);
    }

    private boolean isContextEnabled(Map<String,Variable> variables) {
        return variables.get(MizerContextBase.REJECT_ENABLE).getValue().asBoolean();
    }
    private boolean warnDisabledRejection(Map<String,Variable> variables) {
        return variables.get(MizerContextBase.REJECT_WARN_REJECTION_DISABLED).getValue().asBoolean();
    }

}
