/*
 * DicomEdit: org.nrg.dcm.edit.AbstractIndexedLabelFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.scripts.ScriptFunction;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.IndexedLabelValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public abstract class AbstractIndexedLabelFunction implements ScriptFunction {
    protected abstract Function<String, Boolean> isAvailable() throws ScriptEvaluationException;

    private Value getFormat(final List<? extends Value> values)
            throws ScriptEvaluationException {
        try {
            return values.get(0);
        } catch (IndexOutOfBoundsException e) {
            try {
                throw new ScriptEvaluationException(getClass().getField("name").get(null) + " requires format argument");
            } catch (IllegalArgumentException | SecurityException | NoSuchFieldException | IllegalAccessException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value apply(final List<? extends Value> args) throws ScriptEvaluationException {
        final Value format = getFormat(args);
        return new IndexedLabelValue(format, isAvailable());
    }
}
