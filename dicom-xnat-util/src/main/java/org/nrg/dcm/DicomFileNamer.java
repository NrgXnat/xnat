/*
 * dicom-xnat-util: org.nrg.dcm.DicomFileNamer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che2.data.DicomObject;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 * Makes up a filename for a DICOM object.
 */
public interface DicomFileNamer {
    /**
     * Make up a filename for a DICOM object.
     * @param o DicomObject to be saved to a file.
     * @return filename to which the object should be saved
     */
    String makeFileName(DicomObject o);
}
