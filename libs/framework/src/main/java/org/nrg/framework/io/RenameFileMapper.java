/*
 * framework: org.nrg.framework.io.RenameFileMapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.framework.io;

import java.io.File;
import java.text.MessageFormat;

import com.google.common.base.Function;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class RenameFileMapper implements Function<File, File> {
    private final String _format;

    public RenameFileMapper(final String format) {
        _format = format;
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.FileMapper#map(java.io.File)
     */
    public File apply(final File original) {
        return new File(original.getParentFile(), MessageFormat.format(_format, original.getName()));
    }
}
