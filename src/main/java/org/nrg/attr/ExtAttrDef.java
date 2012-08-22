/**
 * Copyright (c) 2012 Washington University
 */
package org.nrg.attr;

import java.util.Set;

/**
 * Interface for classes that convert from a native attribute format
 * with index type S.
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public interface ExtAttrDef<S> {
    /**
     * Get the native attribute indices on which this attribute is dependent.
     * @return
     */
    Set<S> getAttrs();

    /**
     * Get the external attribute name.
     * @return name
     */
    String getName();

    boolean requires(S attr);
}
