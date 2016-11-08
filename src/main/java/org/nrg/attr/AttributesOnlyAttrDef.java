/*
 * ExtAttr: org.nrg.attr.AttributesOnlyAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Collections;
import java.util.Map;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class AttributesOnlyAttrDef<S,V>
extends TextWithAttrsAttrDef<S,V> {
    public AttributesOnlyAttrDef(final String name, final Map<String,S> attrs,
            final boolean ignoreNull, final Iterable<S> optional) {
        super(name, null, attrs, ignoreNull, optional);
    }
    
    public AttributesOnlyAttrDef(final String name, final Map<String,S> attrs,
            final boolean ignoreNull) {
        this(name, attrs, ignoreNull, Collections.<S>emptyList());
    }
    
    public AttributesOnlyAttrDef(final String name, final String[] names, final S[] attrs,
            final boolean ignoreNull, final Iterable<S> optional) {
        this(name, Utils.zipmap(names, attrs), ignoreNull, optional);
    }
    
    public AttributesOnlyAttrDef(final String name, final String[] names, final S[] attrs,
            final boolean ignoreNull) {
        this(name, Utils.zipmap(names, attrs), ignoreNull, Collections.<S>emptyList());
    }
}
