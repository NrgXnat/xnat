/*
 * web: org.nrg.xnat.services.cache.UserProjectCacheManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.cache;

import org.nrg.xdat.om.XnatProjectdata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class UserProjectCache {
    @Autowired
    public UserProjectCache(final CacheManager cacheManager) {
        _cache = cacheManager.getCache("UserProjectCacheManagerCache");
    }

    public boolean has(final String userId, final String projectId) {
        final Cache.ValueWrapper object = _cache.get(getUserProjectKey(userId, projectId));
        return object != null;
    }

    public XnatProjectdata get(final String userId, final String projectId) {
        final Cache.ValueWrapper object = _cache.get(getUserProjectKey(userId, projectId));
        if (object != null) {
            return (XnatProjectdata) object.get();
        }
        return null;
    }

    public void put(final String userId, final String projectId, final XnatProjectdata project) {
        _cache.put(getUserProjectKey(userId, projectId), project);
    }

    public void remove(final String userId, final String projectId) {
        _cache.evict(getUserProjectKey(userId, projectId));
    }

    /**
     * Does e contain the sentinel value indicating that its alias
     * does not map to a project where user has create perms?
     *
     * @param userId    The ID of the user checking the project
     * @param projectId The ID of the project being checked
     *
     * @return true if this element indicates no writable project
     */
    public boolean isCachedNonWriteableProject(final String userId, final String projectId) {
        final Cache.ValueWrapper object = _cache.get(getUserProjectKey(userId, projectId));
        return object != null && NOT_A_WRITABLE_PROJECT == object.get();
    }

    /**
     * Indicate in the provided cache that the provided name does not describe
     * a writable project.
     *
     * @param userId    The ID of the user caching the project
     * @param projectId The name of the project to cache as non-writable.
     */
    public void cacheNonWriteableProject(final String userId, final String projectId) {
        _cache.put(getUserProjectKey(userId, projectId), NOT_A_WRITABLE_PROJECT);
    }

    private static String getUserProjectKey(final String userId, final String projectId) {
        return userId + "projects-" + projectId;
    }

    private static final Object NOT_A_WRITABLE_PROJECT = new Object();

    private final Cache _cache;
}
