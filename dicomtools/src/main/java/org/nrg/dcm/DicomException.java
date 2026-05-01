/*
 * DicomDB: org.nrg.dcm.DicomException
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
public abstract class DicomException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public DicomException(String arg0) {
    super(arg0);
  }

  public DicomException(Throwable arg0) {
    super(arg0);
  }

  public DicomException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

}
