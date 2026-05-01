/*
 * mizer: org.nrg.dicom.mizer.values.IntegerValue
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
public final class IntegerValue extends ObjectValue<Integer> {
    public IntegerValue(final String value) {
        super(Integer.parseInt(value));
    }
    
    public IntegerValue(final int value) {
        super(value);
    }
}
