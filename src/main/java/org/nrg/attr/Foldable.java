/**
 * Copyright (c) 2012 Washington University
 */
package org.nrg.attr;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public interface Foldable<T,A> {
    /**
     * Get the reduction value type empty value.
     * @return empty value
     */
    A start();
    
    /**
     * Extract a value from the provided map and fold it into the
     * accumulator.
     * @param a fold accumulator
     * @param t individual data element
     * @return updated accumulator
     * @throws Exception
     */
    A foldl(A a, T t) throws Exception;
}
