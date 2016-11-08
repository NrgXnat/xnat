/*
 * ExtAttr: org.nrg.attr.MultiplexTextAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MultiplexTextAttrDef<S,V> extends AbstractExtAttrDef<S,V,Map<V,V>> {
    private final String format;
    private final S attr, indexattr;
    
    @SuppressWarnings("unchecked")
    public MultiplexTextAttrDef(final String name, final String multiNameFormat,
            final S attr, final S indexAttr) {
        super(name, attr, indexAttr);
        this.format = multiNameFormat;
        this.attr = attr;
        this.indexattr = indexAttr;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#apply(java.lang.Object)
     */
    public Iterable<ExtAttrValue> apply(Map<V,V> vs) throws ConversionFailureException {
        final List<ExtAttrValue> out = Lists.newArrayList();
        for (final Map.Entry<V,V> pair : vs.entrySet()) {
        	final V v = pair.getValue();
            out.add(new BasicExtAttrValue(String.format(format, pair.getKey()),
                    null == v ? null : v.toString()));
        }
        return out;
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#foldl(java.lang.Object, java.util.Map)
     */
    public Map<V,V> foldl(final Map<V,V> a, final Map<? extends S,? extends V> m)
    throws ConversionFailureException {
        final V index = m.get(indexattr);
        final V v = m.get(attr);
        if (null == index) {
            throw new ConversionFailureException(this, v, "index attribute is missing");
        }
        final V av = a.get(index);
        if (null == av) {
            a.put(index, v);
        } else if (!av.equals(v)) {
            throw new ConversionFailureException(this, av,
                    "attribute has conflicting value " + v + " for index " + index);
        }
        return a;
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.attr.Foldable#start()
     */
    public Map<V,V> start() { return Maps.newLinkedHashMap(); }
}
