/*
 * dicom-edit6: org.nrg.dicom.dicomedit.functions.AbstractIndexedLabelFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.dicomedit.functions;

import org.apache.commons.lang3.StringUtils;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationRuntimeException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.MizerValueShim;
import org.nrg.dicom.mizer.values.Value;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides the base functionality for DicomEdit functions that generate indexed labels.
 */
public abstract class AbstractIndexedLabelFunction extends AbstractScriptFunction {
    public AbstractIndexedLabelFunction(String name, String nameSpace, String usage, String description) {
        super(name, nameSpace, usage, description);
    }

    @SuppressWarnings("unused")
    public AbstractIndexedLabelFunction(Properties properties) {
        super(properties);
    }

    @Override
    public Value apply(final List<Value> args, DicomObjectI dicomObject) throws ScriptEvaluationException{
        return new MizerValueShim(getFormat(args)) {
            @Override
            public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
                return getValue().on(dicomObject);
            }

            @Override
            public String on(final Map<Integer, String> map) throws ScriptEvaluationException {
                return getValue().on(map);
            }

            public Object asObject() {
                return asString();
            }

            public String asString() {
                return valueFor(getValue().asString());
            }

            private String valueFor(final String format) throws ScriptEvaluationRuntimeException {
                if (StringUtils.isBlank(format) || !format.contains("#")) {
                    return "";
                }

                final Matcher matcher = FORMAT_PATTERN.matcher(format);
                if (!matcher.matches()) {
                    throw new ScriptEvaluationRuntimeException("An indexed pattern was specified but doesn't match the appropriate regular expression: " + format + " doesn't match " + FORMAT_REGEX);
                }

                final int indexLength = matcher.group("index").length();
                final NumberFormat formatter = new DecimalFormat(StringUtils.leftPad("", indexLength, "0"));
                final String formatString = matcher.group("open") + "%s" + matcher.group("close");
                for (int i = 0; true; i++) {
                    final String label = String.format(formatString, formatter.format(i));
                    if (isAvailable(label, args)) {
                        return label;
                    }
                }
            }
        };
    }

    protected abstract boolean isAvailable(final String label, final List<Value> args) throws ScriptEvaluationRuntimeException;

    private Value getFormat(final List<? extends Value> values) throws ScriptEvaluationException {
        try {
            return values.get(0);
        } catch (IndexOutOfBoundsException e) {
            try {
                throw new ScriptEvaluationException(getClass().getField("name").get(null) + " requires format argument");
            } catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchFieldException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    private static final String  FORMAT_REGEX   = "^(?<open>[^#]+)(?<index>[#]+)(?<close>.*)$";
    private static final Pattern FORMAT_PATTERN = Pattern.compile(FORMAT_REGEX);
}
