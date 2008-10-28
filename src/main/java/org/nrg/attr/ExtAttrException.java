/**
 * Copyright (c) 2008 Washington University
 */
package org.nrg.attr;

/**
 * Represents an error in retrieving or translating an external attribute value
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 *
 */
public class ExtAttrException extends Exception {
  private final static long serialVersionUID = 1L;
  
  /**
   * @param message
   */
  public ExtAttrException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public ExtAttrException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public ExtAttrException(String message, Throwable cause) {
    super(message, cause);
  }
}
