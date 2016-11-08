/*
 * ExtAttr: org.nrg.attr.NoUniqueValueException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Indicates that an attribute that was expected to have a unique value
 * over some subset of a native file set is either undefined or has
 * multiple values.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 * @version $Revision: 1.1 $
 */
public class NoUniqueValueException extends ExtAttrException {
  static final long serialVersionUID = 0x0de1L;
  private final String attribute;
  private final String[] values;

  private static String[] attrValuesToStrings(final ExtAttrValue[] vals) {
    String[] values = new String[vals.length];
    for (int i = 0; i < vals.length; i++) {
      StringBuilder sb = new StringBuilder();
      sb.append(vals[i].getText());
      for (final Map.Entry<String,String> me : vals[i].getAttrs().entrySet()) {
        sb.append(" ").append(me.getKey()).append("=").append(me.getValue());
      }
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
  
  public NoUniqueValueException(final String attr, Collection<?> vals) {
    super("Attribute " + attr + " does not have a unique value (" + vals + ")");
    this.attribute = attr;
    this.values = new String[vals.size()];
    final Iterator<?> vi = vals.iterator();
    for (int i = 0; vi.hasNext(); i++) {
      this.values[i] = vi.next().toString();
    }
  }

  public NoUniqueValueException(final String attr, final ExtAttrValue[] vals) {
    this(attr,attrValuesToStrings(vals));
  }

  public String getAttribute() { return attribute; }
  public String[] getValues() { return values; }
}
