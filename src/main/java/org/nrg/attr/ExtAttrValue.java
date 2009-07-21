/**
 * Copyright (c) 2009 Washington University
 */
package org.nrg.attr;

import java.util.Map;
import java.util.Set;

/**
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 *
 */
public interface ExtAttrValue {
  Map<String,String> getAttrs();
  @Deprecated
  Set<String> getAttrNames();
  @Deprecated
  String getAttrValue(String name);
  String getName();
  String getText();
}
