/*
 * ExtAttr: org.nrg.attr.ExtAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Set;

/**
 * Interface for classes that convert from a native attribute format
 * with index type S.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public interface ExtAttrDef<S> {
    /**
     * Get the native attribute indices on which this attribute is dependent.
     * @return The attributes included in the attribute definition.
     */
    Set<S> getAttrs();

    /**
     * Get the external attribute name.
     * @return name
     */
    String getName();

    boolean requires(S attr);
}
