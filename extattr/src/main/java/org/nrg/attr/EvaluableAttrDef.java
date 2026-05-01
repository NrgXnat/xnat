/*
 * ExtAttr: org.nrg.attr.EvaluableAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Map;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public interface EvaluableAttrDef<S,V,A>
extends ExtAttrDef<S>, Foldable<Map<? extends S,? extends V>,A> {
    Iterable<ExtAttrValue> apply(A a) throws ExtAttrException;

    A foldl(A a, Map<? extends S,? extends V> m) throws ExtAttrException;

    Iterable<ExtAttrValue> foldl(Iterable<? extends Map<? extends S,? extends V>> ms) throws ExtAttrException;
}
