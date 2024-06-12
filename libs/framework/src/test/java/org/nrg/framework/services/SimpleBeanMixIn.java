/*
 * framework: org.nrg.framework.services.SimpleBeanMixIn
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class SimpleBeanMixIn {
    @JsonIgnore
    public abstract String getIgnoredField();
}
