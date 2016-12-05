/*
 * DicomUtils: org.nrg.dcm.RequiredAttributeUnsetException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che2.data.DicomObject;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class RequiredAttributeUnsetException extends DicomContentException {
  private final static long serialVersionUID = 1L;
  
  public RequiredAttributeUnsetException(final DicomObject o, final int tag) {
    super("Required DICOM attribute " + o.nameOf(tag) + " is unset or empty");
  }
}
