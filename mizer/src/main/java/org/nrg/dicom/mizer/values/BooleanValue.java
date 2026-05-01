/*
 * mizer: org.nrg.dicom.mizer.values.BooleanValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.mizer.values;

/**
 * Manages integer values.
 */
public final class BooleanValue extends ObjectValue<Boolean> {
    public BooleanValue(final String value) {
        this(Boolean.parseBoolean(value));
    }

    public BooleanValue(final boolean value) {
        super(value);
    }
}
