/**
 * Copyright (c) 2006,2010 Washington University
 */
package org.nrg.attr;

import java.util.Set;

/**
 * Bundle of external attribute definitions
 * @author Kevin A. Archie <karchie@wustl.edu>
 */
public interface AttrDefs<S,V> extends Iterable<ExtAttrDef<S,V>> {
    Set<S> getNativeAttrs();
}
