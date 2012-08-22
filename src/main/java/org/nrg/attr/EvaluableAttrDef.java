/**
 * Copyright (c) 2012 Washington University
 */
package org.nrg.attr;

import java.util.Map;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public interface EvaluableAttrDef<S,V,A>
extends ExtAttrDef<S>, Foldable<Map<? extends S,? extends V>,A> {
    Iterable<ExtAttrValue> apply(A a) throws ExtAttrException;

    A foldl(A a, Map<? extends S,? extends V> m) throws ExtAttrException;

    Iterable<ExtAttrValue> foldl(Iterable<? extends Map<? extends S,? extends V>> ms) throws ExtAttrException;
}
