/*
 * core: org.nrg.xft.cache.CacheManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.cache;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.services.cache.XnatCache;

public interface CacheManager extends XnatCache {
    static CacheManager GetInstance() {
        return XDAT.getContextService().getBeanSafely(CacheManager.class);
    }


    /**
     * Clears all entries in the cache.
     */
    void clearAll();

    /**
     * Clears all objects cached with the given XSI type.
     *
     * @param xsiType The XSI type of the cached objects to be cleared
     */
    void clearXsiType(String xsiType);

    /**
     * Retrieves the object cached with the given XSI type and object ID.
     *
     * @param xsiType The XSI type of the cached object
     * @param id      The ID of the cached object
     *
     * @return The cached object if it exists in the cache, <pre>null</pre> otherwise.
     */
    Object retrieve(String xsiType, Object id);

    /**
     * Puts the item into the cache with the specified XSI type and ID.
     *
     * @param xsiType The XSI type of the object to be cached
     * @param id      The ID of the object to be cached
     * @param item    The object to be cached
     */
    Object put(String xsiType, Object id, Object item);

    /**
     * Removes the item cached with the specified XSI type and ID from the cache. This returns the object if it exists in
     * the cache.
     *
     * @param xsiType The XSI type of the object to be cached
     * @param id      The ID of the object to be cached
     *
     * @return The cached object if it exists
     */
    Object remove(String xsiType, Object id);
}
