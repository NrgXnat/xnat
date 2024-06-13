/*
 * ExtAttr: org.nrg.attr.MultiValueAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MultiValueAttrDef<S,V,A> extends AbstractExtAttrDef<S,V,Set<A>> {
    private final EvaluableAttrDef<S,V,A> attr;
    
    public MultiValueAttrDef(final EvaluableAttrDef<S,V,A> attr) {
        super(attr.getName(), attr.getAttrs());
        this.attr = attr;
    }
    
    public static <S,V,A> MultiValueAttrDef<S,V,A> wrap(final EvaluableAttrDef<S,V,A> attr) {
        return new MultiValueAttrDef<S,V,A>(attr);
    }
    
    public Set<A> start() { return Sets.newLinkedHashSet(); }
    
    public Set<A> foldl(final Set<A> a, final Map<? extends S, ? extends V> m) {
        try {
            a.add(attr.foldl(attr.start(), m));
        } catch (ExtAttrException e) {
            System.out.println("error " + e.getMessage());
            e.printStackTrace();
        }
        return a;
    }
    
    public Iterable<ExtAttrValue> apply(final Set<A> as) {
        final List<ExtAttrValue> vals = Lists.newArrayList();
        for (final A a : as) {
            try {
            Iterables.addAll(vals, attr.apply(a));
            } catch (ExtAttrException e) {
                System.out.println("error " + e.getMessage());
                e.printStackTrace();
            }
        }
        return vals;
    }
}
