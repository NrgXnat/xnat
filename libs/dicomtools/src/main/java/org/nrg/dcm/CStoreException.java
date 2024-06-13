/*
 * dicomtools: org.nrg.dcm.CStoreException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class CStoreException extends DicomServiceException {
  private final static long serialVersionUID = 1L;
  
  public CStoreException() {}

  public CStoreException(final String message) {
    super(message);
  }

  public CStoreException(final Throwable cause) {
    super(cause);
  }

  public CStoreException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
