/*
 * web: org.nrg.xnat.helpers.ZipEntryFileWriterWrapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

public class TarEntryFileWriterWrapper extends ArchiveEntryFileWriterWrapper {
    public TarEntryFileWriterWrapper(final TarArchiveEntry ze, final TarArchiveInputStream in) {
        super(ze.getName(), ze.getSize(), in);
    }
}
