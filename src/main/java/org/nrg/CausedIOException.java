/*
 * ExtAttr: org.nrg.CausedIOException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import java.io.IOException;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class CausedIOException extends IOException {
  private static final long serialVersionUID = 1L;
  
  public CausedIOException(final String message, final Throwable cause) {
    super(message);
    initCause(cause);
  }

  public CausedIOException(final Throwable cause) {
    super(cause == null ? null : cause.toString());
    initCause(cause);
  }
}
