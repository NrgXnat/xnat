/*
 * framework: org.nrg.framework.exceptions.CheckedExceptionFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.framework.exceptions;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public interface CheckedExceptionFunction<K,V,E extends Exception> {
    V apply(K k) throws E;
}
