/*
 * ExtAttr: org.nrg.attr.AttrDefs
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Set;

/**
 * Bundle of external attribute definitions
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public interface AttrDefs<S> extends Iterable<ExtAttrDef<S>> {
    Set<S> getNativeAttrs();
}
