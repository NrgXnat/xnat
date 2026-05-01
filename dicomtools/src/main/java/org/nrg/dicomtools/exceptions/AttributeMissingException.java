/*
 * DicomEdit: org.nrg.dicomtools.exceptions.AttributeMissingException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicomtools.exceptions;

import org.dcm4che2.util.TagUtils;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class AttributeMissingException extends AttributeException {
    public AttributeMissingException(final int tag) {
        super("Cannot use DICOM attribute " + TagUtils.toString(tag)
              + ": attribute is missing");
    }
}
