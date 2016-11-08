/*
 * ExtAttr: org.nrg.attr.ConversionFailureException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Indicates that an error occurred in attribute value conversion
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class ConversionFailureException extends ExtAttrException {
    private static final long serialVersionUID = 1L;
    private final Object attr;
    private final Object val;
    private final String[] extAttrs;

    /**
     * @param attr Attribute for which conversion failed
     * @param val Attribute value
     * @param msg message for exception
     */
    public ConversionFailureException(final Object attr, final Object val, final String msg) {
        this(attr, val, (String[])null, msg);
    }

    /**
     * @param attr Attribute for which conversion failed
     * @param val Attribute value
     * @param ext values of contributing external attributes
     * @param msg message for exception
     */
    public ConversionFailureException(final Object attr, final Object val, final String[] ext, final String msg) {
        super(msg);
        this.attr = attr;
        this.val = val;
        extAttrs = ext;
    }

    /**
     * @param attr Attribute for which conversion failed
     * @param val Attribute value
     * @param ext value of contributing external attribute
     * @param msg message for exception
     */
    public ConversionFailureException(final Object attr, final Object val, final String ext, final String msg) {
        this(attr, val, new String[]{ext}, msg);
    }

    /**
     * @param cause Caught ConversionFailureException to which we can add contributing external attributes
     * @param ext Additional contributing external attributes
     */
    public ConversionFailureException(final ConversionFailureException cause, final String[] ext) {
        super(cause);
        this.attr = cause.attr;
        this.val = cause.val;
        final Set<String> extAttrs = new LinkedHashSet<String>();
        if (null != cause.extAttrs) {
            extAttrs.addAll(Arrays.asList(cause.extAttrs));
        }
        if (null != ext) {
            extAttrs.addAll(Arrays.asList(ext));
        }
        this.extAttrs = extAttrs.toArray(new String[0]);
    }

    /**
     * @param attr Attribute for which conversion failed
     * @param val Attribute value
     * @param msg message for exception
     * @param cause Caught exception underlying the conversion failure
     */
    public ConversionFailureException(final Object attr, final Object val, final String msg, final Throwable cause) {
        super(msg, cause);
        this.attr = attr;
        this.val = val;
        extAttrs = new String[]{};
    }

    public final Object getAttr() { return attr; }
    public final Object getValue() { return val; }
    public final String[] getExtAttrs() { return extAttrs; }
}
