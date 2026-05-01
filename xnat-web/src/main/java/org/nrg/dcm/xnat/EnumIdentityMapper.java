/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.EnumIdentityMapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Function;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class EnumIdentityMapper<T> implements Function<T,T> {
    private final Set<T> values;

    public EnumIdentityMapper(final Collection<T> values) {
        this.values = new HashSet<T>(values);
    }

    public EnumIdentityMapper(final T...ts) {
        this.values = new HashSet<T>(Arrays.asList(ts));
    }

    /*
     * (non-Javadoc)
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    public T apply(final T t) {
        return values.contains(t) ? t : null;
    }
}
