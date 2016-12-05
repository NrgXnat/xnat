/*
 * DicomUtils: org.nrg.dcm.DicomServiceException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class DicomServiceException extends Exception {
  private final static long serialVersionUID = 1L;
  
  public DicomServiceException() {}

  public DicomServiceException(final String message) {
    super(message);
  }

  public DicomServiceException(final Throwable cause) {
    super(cause);
  }

  public DicomServiceException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
