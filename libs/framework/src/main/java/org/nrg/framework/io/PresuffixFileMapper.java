/*
 * framework: org.nrg.framework.io.PresuffixFileMapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.framework.io;

import java.io.File;

import com.google.common.base.Function;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class PresuffixFileMapper implements Function<File,File> {
    private final String presuffix;

    public PresuffixFileMapper(final String presuffix) {
        this.presuffix = presuffix;
    }


    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.FileMapper#map(java.io.File)
     */
    public File apply(final File original) {
        final File dir = original.getParentFile();
        final StringBuilder name = new StringBuilder(original.getName());
        final int presuffixLoc = name.lastIndexOf(".");
        if (presuffixLoc < 0) {
            name.append(presuffix);
        } else {
            name.insert(presuffixLoc, presuffix);
        }
        return new File(dir, name.toString());
    }
}
