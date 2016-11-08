/*
 * ExtAttr: org.nrg.attr.Foldable
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public interface Foldable<T, A> {
    /**
     * Get the reduction value type empty value.
     *
     * @return empty value
     */
    A start();

    /**
     * Extract a value from the provided map and fold it into the
     * accumulator.
     *
     * @param a fold accumulator
     * @param t individual data element
     *
     * @return updated accumulator
     *
     * @throws Exception When an error occurs.
     */
    A foldl(A a, T t) throws Exception;
}
