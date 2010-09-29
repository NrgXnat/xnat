/**
 * Copyright (c) 2006,2010 Washington University
 */
package org.nrg.attr;

import java.util.Set;

/**
 * A set (not Set) of translated attribute definitions
 * @author Kevin A. Archie <karchie@wustl.edu>
 */
public interface AttrDefs<S,V> extends Iterable<ExtAttrDef<S,V>> {
	/**
	 * Returns the native attributes underlying the translation.
	 * @return Set of native attributes
	 */
	Set<S> getNativeAttrs();
}
