/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ConditionalAttrDef
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;


/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class ConditionalAttrDef extends AbstractConditionalAttrDef<String> {
	private final static String NULL_FORMAT = "%s";
	private final Logger logger = LoggerFactory.getLogger(ConditionalAttrDef.class);
	private final String format;

	public ConditionalAttrDef(final String name, final Rule...rules) {
		this(name, NULL_FORMAT, rules);
	}

	public ConditionalAttrDef(final String name, final String format, final Rule...rules) {
		super(name, rules);
		this.format = format;
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
	throws NoUniqueValueException {
	    String v = null;
        for (final Rule rule : this) {
            final String val = rule.getValue(m);
            if (Strings.isNullOrEmpty(val)) {
                logger.trace("no match for {} in {}", rule, m);
            } else {
                v = format.formatted(val);
                logger.trace("{} obtained value {} from {} on {}",
                        new Object[]{ this, v, rule, m });
                break;
            }
        }
        if (null == v) {
            return a;
        } else if (null == a || a.equals(v)) {
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
