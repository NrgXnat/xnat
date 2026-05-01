/*
 * DicomEdit: org.nrg.dcm.io.DicomObjectExporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.io;

import java.io.File;

import org.dcm4che2.data.DicomObject;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public interface DicomObjectExporter {
    void close();
    void export(DicomObject o, File source) throws Exception;
}
