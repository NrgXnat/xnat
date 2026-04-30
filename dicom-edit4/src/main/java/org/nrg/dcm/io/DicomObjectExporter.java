/*
 * DicomEdit: org.nrg.dcm.io.DicomObjectExporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.io;

import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.File;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public interface DicomObjectExporter {
    void close();
    void export(DicomObjectI o, File source) throws Exception;
}
