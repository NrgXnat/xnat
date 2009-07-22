/**
 * Copyright (c) 2009 Washington University
 */
package org.nrg;

import java.io.IOException;

/**
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
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
