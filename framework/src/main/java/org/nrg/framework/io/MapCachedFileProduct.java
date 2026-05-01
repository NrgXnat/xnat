/*
 * framework: org.nrg.framework.io.MapCachedFileProduct
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.framework.io;

import org.nrg.framework.exceptions.CheckedExceptionFunction;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MapCachedFileProduct<T,E extends IOException> implements CheckedExceptionFunction<File,T,E> {
    private final Map<File,CacheEntry<T>> cache;
    private final CheckedExceptionFunction<File,T,E> product;
    
    public MapCachedFileProduct(final Map<File,CacheEntry<T>> m, final CheckedExceptionFunction<File,T,E> product) {
        this.cache = m;
        this.product = product;
    }
    
    public T apply(File f) throws E {
        final CacheEntry<T> ce = cache.get(f);
        if (null == ce) {
            cache.put(f, new CacheEntry<T>(product.apply(f), f));
        } else synchronized (cache) {
            final long lm = f.lastModified();
            if (lm > ce.lastModified) {
                cache.put(f, new CacheEntry<T>(product.apply(f), f));
            }
        }
        return cache.get(f).t;
    }
    
    public static final class CacheEntry<T> {
        private final T t;
        private final long lastModified;
        
        private CacheEntry(T t, File f) {
            this.t = t;
            this.lastModified = f.lastModified();
        }
    }
}
