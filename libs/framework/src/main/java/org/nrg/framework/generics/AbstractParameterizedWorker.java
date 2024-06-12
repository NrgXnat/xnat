/*
 * framework: org.nrg.framework.generics.AbstractParameterizedWorker
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.generics;

import static org.nrg.framework.utilities.Reflection.getParameterizedTypeForClass;

abstract public class AbstractParameterizedWorker<E> {

    @SuppressWarnings("unchecked")
    protected AbstractParameterizedWorker() {
        this(null);
    }

    protected AbstractParameterizedWorker(final Class<E> clazz) {
        if (clazz != null) {
            _parameterizedType = clazz;
        } else {
            _parameterizedType = getParameterizedTypeForClass(getClass());
        }
    }

    @SuppressWarnings("unused")
    public boolean isMatchingType(AbstractParameterizedWorker other) {
        return getParameterizedType().equals(other.getParameterizedType());
    }

    protected Class<E> getParameterizedType() {
        return _parameterizedType;
    }

    private final Class<E> _parameterizedType;
}
