/*
 * ExtAttr: org.nrg.attr.ExtAttrException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

/**
 * Represents an error in retrieving or translating an external attribute value
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class ExtAttrException extends Exception {
  private final static long serialVersionUID = 1L;
  
  /**
   * @param message    The message to set for the exception.
   */
  public ExtAttrException(String message) {
    super(message);
  }

  /**
   * @param cause    The root exception for the error.
   */
  public ExtAttrException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message    The message to set for the exception.
   * @param cause      The root exception for the error.
   */
  public ExtAttrException(String message, Throwable cause) {
    super(message, cause);
  }
}
