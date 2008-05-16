/**
 * $Id: ReadableAttrDefSet.java,v 1.1 2006/12/21 18:27:04 karchie Exp $
 * Copyright (c) 2006 Washington University
 */
package org.nrg.attr;

import java.util.Collection;

/**
 * Read-only interface to an AttrDefSet
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 * @version $Revision: 1.1 $
 */
public interface ReadableAttrDefSet<S,V> extends Iterable<ExtAttrDef<S,V>> {
  public ExtAttrDef<S,V> getExtAttrDef(String name);
  public Collection<S> getNativeAttrs();
}
