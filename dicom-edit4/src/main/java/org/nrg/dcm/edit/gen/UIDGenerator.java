/*
 * DicomEdit: org.nrg.dcm.edit.gen.UIDGenerator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit.gen;

import org.dcm4che3.util.UIDUtils;
import org.nrg.dcm.edit.ValueGenerator;

/**
 * Generates fresh UIDs from randomly generated UUIDs
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class UIDGenerator implements ValueGenerator {
    public final static String name = "UID";

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.ValueGenerator#valueFor(java.lang.String, java.lang.Iterable)
     */
    public String valueFor(final String old, final Iterable<String> params) {
        return UIDUtils.createUID();
    }
}
