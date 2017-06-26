/*
 * web: org.nrg.xnat.services.cache.UserProjectCacheManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.cache;

import com.google.common.collect.Lists;
import org.nrg.xdat.model.XnatProjectdataAliasI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserProjectCache {
    @Autowired
    public UserProjectCache(final CacheManager cacheManager) {
        _cache = cacheManager.getCache("UserProjectCacheManagerCache");
    }

    public boolean has(final String userId, final String projectId) {
        final Cache.ValueWrapper object = _cache.get(getUserProjectKey(userId, projectId));
        return object != null && (object instanceof XnatProjectdata || object.equals(NOT_A_WRITABLE_PROJECT) || object.equals(NOT_A_WRITABLE_PROJECT));
    }

    public XnatProjectdata get(final UserI user, final String idOrAlias) {
        final String userId = user.getUsername();

        // added caching here to prevent duplicate project queries in every file transaction
        // the cache is shared with the one in gradual dicom importer, which does a similar query.
        if (has(userId, idOrAlias)) {
            if (isCachedNonWritableProject(userId, idOrAlias)) {
                _log.debug("Found a cached project for {} that is non-writable for the user {}", idOrAlias, userId);
            } else if (isCachedNonexistentProject(userId, idOrAlias)) {
                _log.debug("Found a cached alias {} for the user {} that is not a project ID or alias", idOrAlias, userId);
            } else {
                _log.debug("Found a cached project for {} that is writable for the user {}", idOrAlias, userId);
                final Cache.ValueWrapper object = _cache.get(getUserProjectKey(userId, idOrAlias));
                if (object != null && object instanceof XnatProjectdata) {
                    return (XnatProjectdata) object.get();
                }
            }
        } else {
            // no cached value, look in the db
            final XnatProjectdata project = XnatProjectdata.getProjectByIDorAlias(idOrAlias, user, false);

            // We found a project...
            if (project != null) {
                final String projectId = project.getId();

                // If the user can create stuff in there, then hooray!
                if (canCreateIn(user, projectId)) {
                    // Cache the project. The cache will store it by project ID and any aliases.
                    _log.debug("Found a writable project with ID {} under alias {} for user {}, caching and returning the project", projectId, idOrAlias, userId);
                    put(userId, project);
                    return project;
                } else {
                    // If not, store it as a non-writable project and we won't have to look for it again.
                    _log.debug("Found a non-writable project with ID {} under alias {} for user {}, caching and returning null", projectId, idOrAlias, userId);
                    cacheNonWritableProject(userId, project);
                }
            } else {
                // This value is not a project ID or alias.
                _log.debug("Searched for project with ID or alias {} for user {}, that doesn't exist.", idOrAlias, userId);
                cacheNonexistentProject(userId, idOrAlias);
            }
        }

        return null;
    }

    public void put(final String userId, final XnatProjectdata project) {
        doPut(userId, project, true);
    }

    public void remove(final String userId, final String projectId) {
        final XnatProjectdata project = _cache.get(getUserProjectKey(userId, projectId), XnatProjectdata.class);
        for (final XnatProjectdataAliasI found : project.getAliases_alias()) {
            final String alias = found.getAlias();
            _cache.evict(getUserProjectKey(userId, alias));
        }
        _cache.evict(getUserProjectKey(userId, project.getId()));
    }

    public void clearUserCache(final String userId) {
        final List<String> registry = getUserCacheRegistry(userId);
        for (final String idOrAlias : registry) {
            _cache.evict(getUserProjectKey(userId, idOrAlias));
        }
        _cache.evict(userId);
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
    public boolean isCachedNonWritableProject(final String userId, final String projectId) {
        final Cache.ValueWrapper object = _cache.get(getUserProjectKey(userId, projectId));
        return object != null && object.get() == NOT_A_WRITABLE_PROJECT;
    }

    /**
     * Indicate in the provided cache that the provided name does not describe
     * a writable project.
     *
     * @param userId  The ID of the user caching the project
     * @param project The project to cache as non-writable.
     */
    public void cacheNonWritableProject(final String userId, final XnatProjectdata project) {
        doPut(userId, project, false);
    }

    /**
     * Does e contain the sentinel value indicating that its alias
     * does not map to a project that exists?
     *
     * @param userId    The ID of the user checking the project
     * @param projectId The ID of the "project" being checked
     *
     * @return true if this element indicates no writable project
     */
    public boolean isCachedNonexistentProject(final String userId, final String projectId) {
        final Cache.ValueWrapper object = _cache.get(getUserProjectKey(userId, projectId));
        return object != null && object.get() == NOT_A_PROJECT;
    }

    /**
     * Indicate in the provided cache that the provided name does not describe
     * a writable project.
     *
     * @param userId    The ID of the user caching the project
     * @param projectId The project to cache as nonexistent.
     */
    public void cacheNonexistentProject(final String userId, final String projectId) {
        _cache.put(getUserProjectKey(userId, projectId), NOT_A_PROJECT);
    }

    private void register(final String userId, final String idOrAlias) {
        final List<String> registry = getUserCacheRegistry(userId);
        registry.add(idOrAlias);
        _cache.put(userId, registry);
    }

    private List<String> getUserCacheRegistry(final String userId) {
        final Cache.ValueWrapper object = _cache.get(userId);
        final List<String> userCacheRegistry;
        if (object == null) {
            userCacheRegistry = Lists.newArrayList();
            _cache.put(userId, userCacheRegistry);
        } else {
            final Object value = object.get();
            if (value == null || !(value instanceof List)) {
                userCacheRegistry = Lists.newArrayList();
                _cache.put(userId, userCacheRegistry);
            } else {
                //noinspection unchecked
                userCacheRegistry = (List<String>) value;
            }
        }
        return userCacheRegistry;
    }

    private void doPut(final String userId, final XnatProjectdata project, final boolean writable) {
        // If the project is writable, we'll store the project. Otherwise we'll store the not-writable object.
        final Object entity = writable ? project : NOT_A_WRITABLE_PROJECT;

        // Regardless of how we found it, store the project by project ID
        final String projectId = project.getId();
        _log.debug("Caching project with ID {} for user {}", projectId, userId);
        doCache(userId, projectId, entity);

        // Then store by any aliases.
        final List<XnatProjectdataAliasI> aliases = project.getAliases_alias();
        for (final XnatProjectdataAliasI found : aliases) {
            final String alias = found.getAlias();
            _log.debug("Caching project with ID {} under alias {} for user {}", projectId, alias, userId);
            doCache(userId, alias, entity);
        }
    }

    private void doCache(final String userId, final String idOrAlias, final Object value) {
        final String key = getUserProjectKey(userId, idOrAlias);
        _cache.put(key, value);
        register(userId, idOrAlias);
    }

    private boolean canCreateIn(final UserI user, final String projectId) {
        return Permissions.canEditProject(user, projectId);
    }

    private static String getUserProjectKey(final String userId, final String projectId) {
        return userId + "projects-" + projectId;
    }

    private static final Logger _log = LoggerFactory.getLogger(UserProjectCache.class);

    private static final Object NOT_A_WRITABLE_PROJECT = new Object();
    private static final Object NOT_A_PROJECT          = new Object();

    private final Cache _cache;
}
