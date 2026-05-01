/*
 * DicomEdit: org.nrg.util.NamedFileFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.util;

import org.nrg.framework.exceptions.CheckedExceptionFunction;

import java.io.File;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class NamedFileFunction<V,E extends Exception> implements
                                                            CheckedExceptionFunction<String,V, E> {
    private final CheckedExceptionFunction<File,V,E> f;
    
    public NamedFileFunction(final CheckedExceptionFunction<File,V,E> f) {
        this.f = f;
    }
    
    public V apply(final String name) throws E {
        return f.apply(new File(name));
    }
}
