/*
 * DicomEdit: org.nrg.dcm.edit.DicomFileObjectProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import java.io.File;
import java.io.IOException;

import org.dcm4che2.data.DicomObject;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public interface DicomFileObjectProcessor {
    Object process(File file, DicomObject o) throws IOException;
}
