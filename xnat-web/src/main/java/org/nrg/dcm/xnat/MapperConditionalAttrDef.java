/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.MapperConditionalAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import java.util.Arrays;
import java.util.Map;

import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.attr.NoUniqueValueException;
import org.nrg.dcm.DicomAttributeIndex;

import com.google.common.base.Function;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MapperConditionalAttrDef extends AbstractConditionalAttrDef<String> {
    private final Function<String,String> mapper;

    /**
     * @param name
     * @param mapper
     * @param rules
     */
    public MapperConditionalAttrDef(final String name,
            final Function<String,String> mapper,
            final Rule... rules) {
        super(name, rules);
        this.mapper = mapper;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AbstractExtAttrDef#apply(java.lang.String)
     */
    public Iterable<ExtAttrValue> apply(final String a) throws ExtAttrException {
        return applyString(a);
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#foldl(java.lang.Object, java.util.Map)
     */
    public String foldl(final String a, final Map<? extends DicomAttributeIndex,? extends String> m)
            throws ExtAttrException {
        String v = null;
        for (final Rule rule : this) {
            final String val = rule.getValue(m);
            if (null != val) {
                final String match = mapper.apply(val);
                if (null != match) {
                    v = match;
                    break;
                }
            }
        }
        if (null == v) {
            return a;
        } else if (a == null || a.equals(v)) {
            return v;
        } else {
            throw new NoUniqueValueException(getName(), Arrays.asList(a, v));
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.Foldable#start()
     */
    public String start() { return null; }
}
