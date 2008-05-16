/**
 * $Id: ExtAttrValue.java,v 1.3 2008/01/30 16:40:46 karchie Exp $
 * Copyright (c) 2006-2008 Washington University
 */
package org.nrg.attr;

import java.lang.IllegalArgumentException;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.LinkedHashMap;


/**
 * Represents the value of an external attribute element.
 * A ExtAttrValue may have a text value (corresponding to XML element text),
 * attribute values (element attributes), or both.
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 *
 */
public final class ExtAttrValue {
  private final String name;
  private final String textValue;
  private final Map<String,String> attrValues = new LinkedHashMap<String,String>();;

  @Override
  public boolean equals(final Object o) {
    if (o == null || !(o instanceof ExtAttrValue)) return false;
    final ExtAttrValue ov = (ExtAttrValue) o;
    if (textValue != ov.textValue) {
      if (textValue == null || ov.textValue == null) return false;
      if (!textValue.equals(ov.textValue)) return false;
    }
    assert textValue == ov.textValue || textValue.equals(ov.textValue);
    if (attrValues == ov.attrValues) return true;
    if (attrValues == null || ov.attrValues == null) return false;
    return (attrValues.equals(ov.attrValues));
  }

  @Override
  public int hashCode() {
    int code = 17;      // generate a good hashCode a la Bloch
    assert name != null;
    code = 37*code + name.hashCode();
    if (textValue != null)
      code = 37*code + textValue.hashCode();
    if (attrValues != null)
      code = 37*code + attrValues.hashCode();
    return code;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("<");
    sb.append(name);
    for (final String attrName : attrValues.keySet()) {
      sb.append(" ");
      sb.append(attrName);
      sb.append("=\"");
      sb.append(attrValues.get(attrName));
      sb.append("\"");
    }
    if (null == textValue) {
      sb.append("/>");
    } else {
      sb.append(">");
      sb.append(textValue);
      sb.append("</");
      sb.append(name);
      sb.append(">");
    }
    return sb.toString();
  }
 
  /**
   * Constructs a new value with the given element text.
   * @param text
   */
  public ExtAttrValue(final String name, final String value) {
    if (name == null)
      throw new NullPointerException("ExtAttrValue name must be non-null");
    this.name = name;
    textValue = value;
  }
 
  /**
   * Constructs a new value with null element text.
   */
  public ExtAttrValue(final String name) {
    this(name, null);
  }

  /**
   * Adds an attribute to the value element.
   * @param name attribute name
   * @param value
   */
  public void addAttr(final String name, final String value) {
    if (attrValues.containsKey(name))
      throw new IllegalArgumentException("Redefined value attribute " + name);
    attrValues.put(name,value);
  }

  /**
   * Returns the name of this attribute.
   */
  public String getName() {
    return name;
  }
  
  /**
   * Returns the element text for this value.
   */
  public String getText() {
    return textValue;
  }

  /**
   * Returns the names of all attributes in this value.
   */
  public List<String> getAttrNames() {
    return new LinkedList<String>(attrValues.keySet());
  }
 
  /**
   * Returns the value of a specific attribute, given the attribute name.
   * @param name name of the attribute
   * @return attribute value
   */
  public String getAttrValue(final String name) {
    return attrValues.get(name);
  }
}