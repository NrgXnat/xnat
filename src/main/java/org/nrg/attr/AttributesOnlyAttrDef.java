/**
 * Copyright (c) 2012 Washington University
 */
package org.nrg.attr;

import java.util.Collections;
import java.util.Map;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
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
