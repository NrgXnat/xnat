/*
 * dicomtools: org.nrg.dcm.DicomContentException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class DicomContentException extends Exception {
  private final static long serialVersionUID = 1L;
  
  /**
   * Default constructor.
   */
  public DicomContentException() { super(); }

  /**
   * @param message    Message to set for the exception.
   */
  public DicomContentException(final String message) {
    super(message);
  }

  /**
   * @param cause    Cause to set for the exception.
   */
  public DicomContentException(final Throwable cause) {
    super(cause);
  }

  /**
   * @param message    Message to set for the exception.
   * @param cause    Cause to set for the exception.
   */
  public DicomContentException(final String message, final Throwable cause) {
    super(message, cause);
   }
}
