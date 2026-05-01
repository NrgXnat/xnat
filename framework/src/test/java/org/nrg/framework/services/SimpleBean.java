/*
 * framework: org.nrg.framework.services.SimpleBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import org.nrg.framework.annotations.XnatMixIn;

@XnatMixIn(SimpleBeanMixIn.class)
public class SimpleBean {
    public SimpleBean(final String relevant, final String ignored) {
        _relevant = relevant;
        _ignored = ignored;
    }

    public String getRelevantField() {
        return _relevant;
    }

    public String getIgnoredField() {
        return _ignored;
    }

    private final String _relevant;
    private final String _ignored;
}
