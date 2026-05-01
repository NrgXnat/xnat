/*
 * DicomEdit: org.nrg.dcm.edit.BindingContext
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class BindingContext {
    private final Map<String,Object> defined = Maps.newHashMap();

    final void bind(final String label, final Object value) throws AlreadyBoundLabelException {
        final String canonical = label.toLowerCase();
        if (defined.containsKey(canonical)) {
            throw new AlreadyBoundLabelException(canonical);
        }
        defined.put(canonical, value);
    }

    final Object getValue(final String label) {
        final String canonical = label.toLowerCase();
        if (defined.containsKey(canonical)) {
            return defined.get(canonical);
        } else {
            throw new UnboundLabelException(label);
        }
    }


    public static class UnboundLabelException extends RuntimeException {
        private final static long serialVersionUID = 1L;

        UnboundLabelException(final String s) {
            super("Label not defined: " + s);
        }
    }

    public static class AlreadyBoundLabelException extends RuntimeException {
        private final static long serialVersionUID = 1L;

        AlreadyBoundLabelException(final String s) {
            super("Label already bound: " + s);
        }
    }
}
