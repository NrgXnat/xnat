/*
 * web: org.nrg.xnat.helpers.ZipEntryFileWriterWrapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipEntryFileWriterWrapper extends ArchiveEntryFileWriterWrapper {
    public ZipEntryFileWriterWrapper(final ZipEntry ze, final ZipInputStream zin) {
        super(ze.getName(), ze.getSize(), zin);
    }
}
