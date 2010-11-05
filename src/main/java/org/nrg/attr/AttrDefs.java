/**
 * Copyright (c) 2006,2010 Washington University
 */
package org.nrg.attr;

import java.util.Collection;

/**
 * Bundle of external attribute definitions
 * @author Kevin A. Archie <karchie@wustl.edu>
 */
public interface AttrDefs<S,V> extends Iterable<ExtAttrDef<S,V>> {
    ExtAttrDef<S,V> getExtAttrDef(String name);
    Collection<S> getNativeAttrs();
}
