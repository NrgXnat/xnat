/*
 * DicomEdit: org.nrg.dicomtools.exceptions.AttributeVRMismatchException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicomtools.exceptions;

import org.dcm4che2.data.VR;
import org.dcm4che2.util.TagUtils;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class AttributeVRMismatchException extends AttributeException {
    public AttributeVRMismatchException(final int tag, final VR vr) {
        super("Cannot use DICOM attribute " + TagUtils.toString(tag)
              + " -- uninterpretable type " + vr);
    }
}
