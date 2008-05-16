/**
 * $Id: NoUniqueValueException.java,v 1.1 2006/12/21 18:27:04 karchie Exp $
 * Copyright (c) 2006 Washington University
 */
package org.nrg.attr;

import java.util.Arrays;

/**
 * Indicates that an attribute that was expected to have a unique value
 * over some subset of a native file set is either undefined or has
 * multiple values.
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 * @version $Revision: 1.1 $
 */
public class NoUniqueValueException extends Exception {
  static final long serialVersionUID = 0x0de1L;
  private final String attribute;
  private final String[] values;

  private static String[] attrValuesToStrings(final ExtAttrValue[] vals) {
    String[] values = new String[vals.length];
    for (int i = 0; i < vals.length; i++) {
      StringBuilder sb = new StringBuilder();
      sb.append(vals[i].getText());
      for (String attr : vals[i].getAttrNames())
        sb.append(" " + attr + "=" + vals[i].getAttrValue(attr));
      values[i] = sb.toString();
    }
    return values;
  }

  public NoUniqueValueException(final String attr) {
    super("Attribute " + attr + " has no value defined");
    this.attribute = attr;
    this.values = new String[0];
  }
  
  public NoUniqueValueException(final String attr, String[] vals) {
    super("Attribute " + attr + " does not have a unique value (" + Arrays.asList(vals) + ")");
    this.attribute = attr;
    this.values = vals;
  }

  public NoUniqueValueException(final String attr, final ExtAttrValue[] vals) {
    this(attr,attrValuesToStrings(vals));
  }

  public String getAttribute() { return attribute; }
  public String[] getValues() { return values; }
}