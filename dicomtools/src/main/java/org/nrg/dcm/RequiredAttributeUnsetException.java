/*
 * dicomtools: org.nrg.dcm.RequiredAttributeUnsetException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import org.dcm4che3.data.Attributes;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicomtools.utilities.DicomUtils;

public final class RequiredAttributeUnsetException extends DicomContentException {
    private final static long serialVersionUID = 1L;

    public RequiredAttributeUnsetException(final DicomObjectI doi, final int tag) {
        super("Required DICOM attribute " + DicomUtils.getAttributeName(doi, tag) + " is unset or empty");
    }

    public RequiredAttributeUnsetException(final Attributes attributes, final int tag) {
        super("Required DICOM attribute " + DicomUtils.getAttributeName(attributes, tag) + " is unset or empty");
    }
}
