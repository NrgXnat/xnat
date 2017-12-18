/*
 * web: org.nrg.xnat.services.cache.UserProjectCacheManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.cache;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.model.XnatProjectdataAliasI;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import reactor.fn.Predicate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static reactor.bus.selector.Selectors.predicate;

@Service
@Slf4j
public class UserProjectCache implements Consumer<Event<XftItemEvent>> {

    @Autowired
    public UserProjectCache(final CacheManager cacheManager, final EventBus eventBus) {
        _cache = cacheManager.getCache("UserProjectCacheManagerCache");
        eventBus.on(predicate(new Predicate<Object>() {
            @Override
            public boolean test(final Object object) {
                return object != null && object instanceof XftItemEvent && isGroupItem((XftItemEvent) object);
            }
        }), this);
    }

    /**
     * This event handling method looks for changes to a project's permissions by checking for groups that match the standard project access group
     * names, i.e. <b>&lt;projectId&gt;_owner</b>, <b>&lt;projectId&gt;_member</b>, and <b>&lt;projectId&gt;_collaborator</b>. When those groups
     * are modified, all cached references to the project are evicted and must be regenerated the next time a user performs an action that requires
     * a check of this cache.
     *
     * @param event The event indicating a
     */
    @Override
    public void accept(final Event<XftItemEvent> event) {
        final XdatUsergroup userGroup = getUserGroupFromEvent(event);
        if (userGroup == null) {
            return;
        }

        final String  groupId = userGroup.getId();
        final Matcher matcher = PROJECT_GROUP.matcher(groupId);
        if (!matcher.matches()) {
            return;
        }

        evictProject(matcher.group("project"));
    }

    public boolean has(final String userId, final String projectId) {
        return doGet(userId, projectId) != null;
    }

    public XnatProjectdata get(final UserI user, final String idOrAlias) {
        final String userId = user.getUsername();

        // Start the stopwatch here.
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // added caching here to prevent duplicate project queries in every file transaction
        // the cache is shared with the one in gradual dicom importer, which does a similar query.
        if (has(userId, idOrAlias)) {
            stopWatch.split();
            final long timeElapsedHas = stopWatch.getTime();
            log.debug("It took {} ms to run has({}, {}), which returned true.", timeElapsedHas, userId, idOrAlias);
            stopWatch.unsplit();

            if (isCachedNonWritableProject(userId, idOrAlias)) {
                stopWatch.stop();
                final long timeElapsedGet = stopWatch.getTime();
                log.debug("Found a cached project for {} that is non-writable for the user {}. This took {} ms for a total of {} ms for the operation.", idOrAlias, userId, timeElapsedGet - timeElapsedHas, timeElapsedGet);
            } else if (isCachedNonexistentProject(idOrAlias)) {
                stopWatch.stop();
                final long timeElapsedGet = stopWatch.getTime();
                log.debug("Found a cached alias {} for the user {} that is not a project ID or alias. This took {} ms for a total of {} ms for the operation.", idOrAlias, userId, timeElapsedGet - timeElapsedHas, timeElapsedGet);
            } else {
                log.debug("Found a cached project for {} that is writable for the user {}. This took {} ms for a total of {} ms for the operation.", idOrAlias, userId);
                final XnatProjectdata project = doGet(userId, idOrAlias);
                if (project != null) {
                    stopWatch.stop();
                    final long timeElapsedGet = stopWatch.getTime();
                    log.debug("Found a cached project for ID or alias {} for user {}. This took {} ms for a total of {} ms for the operation.", idOrAlias, userId, timeElapsedGet - timeElapsedHas, timeElapsedGet);
                    return project;
                } else {
                    log.warn("A weird condition occurred in the user project cache: has({}, {}) returned true, no non-writable or nonexistent project was found, but doGet({}, {}) returned null. This method will return null, but this isn't a case that should arise.", userId, idOrAlias, userId, idOrAlias);
                }
            }
        } else {
            stopWatch.split();
            final long timeElapsedHas = stopWatch.getTime();
            log.debug("It took {} ms to run has({}, {}), which returned false.", timeElapsedHas, userId, idOrAlias);
            stopWatch.unsplit();

            // no cached value, look in the db
            final XnatProjectdata project = XnatProjectdata.getProjectByIDorAlias(idOrAlias, user, false);
            stopWatch.split();
            final long timeElapsedGetProject = stopWatch.getTime();
            stopWatch.unsplit();

            // We found a project...
            if (project != null) {
                final String projectId = project.getId();
                log.debug("It took {} ms to run XnatProjectdata.getProjectByIDorAlias({}, {}, false) for a total of {} ms, successfully found the project {}.", timeElapsedGetProject - timeElapsedHas, timeElapsedGetProject, idOrAlias, userId, projectId);

                // If the user can create stuff in there, then hooray!
                if (canCreateIn(user, projectId)) {
                    // Cache the project. The cache will store it by project ID and any aliases.
                    log.debug("Found a writable project with ID {} under alias {} for user {}, caching and returning the project", projectId, idOrAlias, userId);
                    doPut(userId, project, true);
                    return project;
                } else {
                    // If not, store it as a non-writable project and we won't have to look for it again.
                    log.debug("Found a non-writable project with ID {} under alias {} for user {}, caching and returning null", projectId, idOrAlias, userId);
                    doPut(userId, project, false);
                }
            } else {
                // This value is not a project ID or alias.
                log.debug("It took {} ms to run XnatProjectdata.getProjectByIDorAlias({}, {}, false) for a total of {} ms, that doesn't seem to exist.", timeElapsedGetProject - timeElapsedHas, timeElapsedGetProject, idOrAlias, userId);
                cacheNonexistentProject(idOrAlias);
            }
        }

        return null;
    }

    public void remove(final String userId, final String projectId) {
        _cache.get(userId, List.class);
        final XnatProjectdata project = _cache.get(getUserProjectKey(userId, projectId), XnatProjectdata.class);
        doEvict(userId, project.getId());
        for (final XnatProjectdataAliasI found : project.getAliases_alias()) {
            doEvict(userId, found.getAlias());
        }
    }

    public void clearUserCache(final String userId) {
        final Set<String> registry = getUserCacheRegistry(userId);
        for (final String idOrAlias : registry) {
            doEvict(userId, idOrAlias);
        }
        _cache.evict(userId);
    }

    /**
     * Checks whether the cache contains a reference to the project ID or alias for the specified user that
     * indicates that the target project exists but is not writable by the user. Note that this doesn't mean
     * that the user can write to the project, just that this information is not already cached!
     *
     * @param userId    The ID of the user checking the project
     * @param idOrAlias The ID or alias of the project being checked
     *
     * @return Return true if the cache indicates that the user can not write to the specified project.
     */
    public boolean isCachedNonWritableProject(final String userId, final String idOrAlias) {
        final Map<String, String> userCache = getUserProjectCache(userId);
        return userCache.containsKey(idOrAlias) && StringUtils.equals(userCache.get(idOrAlias), NOT_A_WRITABLE_PROJECT);
    }

    /**
     * Checks whether the cache contain the sentinel value indicating that its alias does not map
     * to a project that exists. Note that returning false doesn't mean that a project with the
     * indicated ID or alias does exist, just that no previous requests have been made with that
     * ID or alias and so it hasn't been cached.
     *
     * @param projectId The project ID or alias being checked
     *
     * @return Returns true if this element indicates a cached non-existent project
     */
    public boolean isCachedNonexistentProject(final String projectId) {
        final XnatProjectdata project = _cache.get(projectId, XnatProjectdata.class);
        return project != null && Objects.equals(NOT_A_PROJECT, project);
    }

    /**
     * Caches a reference to the submitted project ID or alias indicating that the project doesn't
     * exist.
     *
     * @param projectId The project to cache as nonexistent.
     */
    public void cacheNonexistentProject(final String projectId) {
        _cache.put(projectId, NOT_A_PROJECT);
    }

    private void register(final String userId, final String idOrAlias) {
        final Set<String> registry = getUserCacheRegistry(userId);
        registry.add(idOrAlias);
        _cache.put(userId, registry);

    }

    private Set<String> getUserCacheRegistry(final String userId) {
        //noinspection unchecked
        final Set<String> userCacheRegistry = (Set<String>) _cache.get(userId, Set.class);
        if (userCacheRegistry != null) {
            return userCacheRegistry;
        }
        final Set<String> newCache = new HashSet<>();
        _cache.put(userId, newCache);
        return newCache;
    }

    private XnatProjectdata doGet(final String userId, final String projectId) {
        //noinspection unchecked
        final Map<String, Boolean> projects = getUserProjectCache(userId);
        if (projects == null || projects.isEmpty()) {
            return null;
        }
        final Boolean isWritable = projects.get(projectId);
        if (!BooleanUtils.isNotFalse(isWritable)) {
            return null;
        }
        _cache.get(getProjectKey(projectId), XnatProjectdata.class);
    }

    private void doPut(final String userId, final XnatProjectdata project, final boolean writable) {
        final String projectId = project.getId();

        // Then store by any aliases.
        final List<String> ids = new ArrayList<>();
        ids.add(projectId);
        ids.addAll(getAliasIds(project));

        final Map<String, String> userCache = getUserProjectCache(userId);
        for (final String id : ids) {

        }

        // If the project is writable, we'll store the project. Otherwise we'll store the not-writable object.
        final XnatProjectdata entity = writable ? project : NOT_A_WRITABLE_PROJECT;

        for (final XnatProjectdataAliasI found : aliases) {
            final String alias = found.getAlias();
            log.debug("Caching project with ID {} under alias {} for user {}", projectId, alias, userId);
            doCache(userId, alias, entity);
        }
    }

    private void doCache(final String userId, final String idOrAlias, final XnatProjectdata project) {
        final Map map = _cache.get(userId, Map.class);
        if (map == null) {

        }
        _cache.put(key, value);
        register(userId, idOrAlias);
    }

    private void doEvict(final String userId, final String projectId) {
        _cache.evict(getUserProjectKey(userId, projectId));
        final Cache.ValueWrapper object = _cache.get(projectId);
        if (object == null) {
            return;
        }
        //noinspection unchecked
        final List<String> users = (List<String>) object.get();
        users.remove(userId);
        _cache.put(projectId, users);
    }

    /**
     * Gets the canonical project ID for the submitted ID or alias. Note that the returned value will be the same as the submitted value when
     * the submitted value is actually the project ID.
     *
     * @param idOrAlias The project ID or alias to be converted.
     *
     * @return The project ID.
     */
    private String getProjectId(final String idOrAlias) {
        final Cache.ValueWrapper cacheObject = _cache.get(getProjectReferenceKey(idOrAlias));
        if (cacheObject != null) {
            final Object value = cacheObject.get();
            if (value != null) {
                // If the value is a list, that means the key is the project ID (the list is a list of user IDs with references to the project),
                // but, if the value is a string, it's an alias, i.e. a reference to another key that's the canonical project ID.
                return value instanceof String ? (String) value : idOrAlias;
            }
        }
        return null;
    }

    /**
     * Returns the cached project by ID or alias.
     *
     * @param idOrAlias The ID or alias of the project to be retrieved from the cache.
     *
     * @return The project if found in the cache.
     */
    private XnatProjectdata getProject(final String idOrAlias) {
        final String projectId = getProjectId(idOrAlias);
        return StringUtils.isNotBlank(projectId) ? _cache.get(getProjectKey(projectId), XnatProjectdata.class) : null;
    }

    /**
     * Caches the submitted project by its ID and also caches all of the project aliases with references to the canonical project ID.
     *
     * @param project The project to be cached.
     */
    private void cacheProject(final XnatProjectdata project) {
        final String          projectKey          = getProjectKey(project.getId());
        final String          projectReferenceKey = getProjectReferenceKey(project.getId());
        final XnatProjectdata current             = _cache.get(projectKey, XnatProjectdata.class);

        // It's OK if current ID is blank and submitted ID isn't because we're replacing a non-existent
        // project with an existing (probably new) project. If
        if (current != null && StringUtils.isNotBlank(current.getId())) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Tried to replace a project with ID " + current.getId() + " with a project with ID " + project.getId() + ", you shouldn't do this.");
        }
        _cache.put(projectKey, project);
        _cache.putIfAbsent(projectReferenceKey, new ArrayList<String>());
        for (final XnatProjectdataAliasI alias : project.getAliases_alias()) {
            _cache.put(getProjectReferenceKey(alias.getAlias()), project.getId());
        }
    }

    private void evictProject(final String idOrAlias) {
        final String projectId  = getProjectId(idOrAlias);
        final String projectKey = getProjectKey(projectId);

        final XnatProjectdata project = _cache.get(projectKey, XnatProjectdata.class);
        if (project == null) {
            return;
        }

        final Set<String> aliases             = getAliasIds(project);
        final String      projectReferenceKey = getProjectReferenceKey(projectId);

        // Get the list of users that have references to this project.
        //noinspection unchecked
        final Set<String> userIds = (Set<String>) _cache.get(projectReferenceKey, List.class);

        // Go through each user...
        for (final String userId : userIds) {
            // Retrieve the user project cache for that user.
            final Map<String, String> cache = getUserProjectCache(userId);

            // Remove the project ID and aliases.
            cache.remove(projectId);
            cache.keySet().removeAll(aliases);

            // Set the user project cache back.
            setUserProjectCache(userId, cache);
        }

        // Now evict the project and all aliases.
        _cache.evict(projectKey);
        for (final String alias : aliases) {
            _cache.evict(getProjectKey(alias));
        }
    }

    /**
     * Returns cached user data. The map returned uses project IDs and aliases as keys. The corresponding values indicate the canonical project ID,
     * which can then be used to retrieve the project object from the project cache.
     *
     * @param userId The user ID to get the cache for.
     *
     * @return The project IDs and aliases currently cached for the submitted user ID.
     */
    private Map<String, String> getUserProjectCache(final String userId) {
        //noinspection unchecked
        final Map<String, String> userCache = _cache.get(userId, Map.class);
        if (userCache != null) {
            return userCache;
        }
        final Map<String, String> newUserCache = new HashMap<>();
        _cache.put(userId, newUserCache);
        return newUserCache;
    }

    private void setUserProjectCache(final String userId, final Map<String, String> userCache) {
        //noinspection unchecked
        _cache.put(userId, userCache);
    }

    /**
     * Returns a list of users that hold references to this project.
     *
     * @param idOrAlias The project ID or alias to retrieve.
     *
     * @return A list of IDs of users that have a cached reference to the submitted project ID or alias.
     */
    private List<String> getProjectReferenceCache(final String idOrAlias) {
        final Cache.ValueWrapper cacheObject = _cache.get(getProjectReferenceKey(idOrAlias));
        if (cacheObject != null) {
            final Object value = cacheObject.get();
            if (value != null) {
                // This means the submitted string is an alias, so we have to get the project.
                if (value instanceof String) {
                    //noinspection unchecked
                    return (List<String>) _cache.get(value, List.class);
                }
                //noinspection unchecked
                return (List<String>) value;
            }
        }
        return null;
    }

    private boolean canCreateIn(final UserI user, final String projectId) {
        return Permissions.canEditProject(user, projectId);
    }

    private static Set<String> getAliasIds(final XnatProjectdata project) {
        return new HashSet<>(Lists.transform(project.getAliases_alias(), new Function<XnatProjectdataAliasI, String>() {
            @Override
            public String apply(final XnatProjectdataAliasI alias) {
                return alias.getAlias();
            }
        }));
    }

    private static boolean isGroupItem(final XftItemEvent event) {
        final Object item = event.getItem();
        try {
            return (item instanceof XFTItem && ((XFTItem) item).instanceOf(XdatUsergroup.SCHEMA_ELEMENT_NAME)) || XdatUsergroup.class.isAssignableFrom(item.getClass());
        } catch (ElementNotFoundException ignored) {
            return false;
        }
    }

    private static XdatUsergroup getUserGroupFromEvent(final Event<XftItemEvent> event) {
        final XftItemEvent data = event.getData();
        if (data == null) {
            return null;
        }
        final Object item = data.getItem();
        if (item == null) {
            log.warn("Got a weird XftItemEvent " + event.getId() + " where the XFTItem was null. Beats me.");
        } else if (item instanceof XFTItem) {
            final XFTItem xftItem = (XFTItem) item;
            try {
                if (xftItem.instanceOf(XdatUsergroup.SCHEMA_ELEMENT_NAME)) {
                    return new XdatUsergroup(xftItem);
                }
            } catch (ElementNotFoundException e) {
                log.warn("Got an element not found exception testing object of type " + xftItem.getXSIType() + " as a " + XdatUsergroup.SCHEMA_ELEMENT_NAME, e);
            }
        } else if (XdatUsergroup.class.isAssignableFrom(item.getClass())) {
            return (XdatUsergroup) item;
        }
        return null;
    }

    private static String getProjectKey(final String idOrAlias) {
        return "project:" + idOrAlias;
    }

    private static String getProjectReferenceKey(final String idOrAlias) {
        return "projectReferences:" + idOrAlias;
    }

    private static final String          NOT_A_WRITABLE_PROJECT = "NOT_A_WRITABLE_PROJECT";
    private static final XnatProjectdata NOT_A_PROJECT          = new XnatProjectdata();
    private static final Pattern         PROJECT_GROUP          = Pattern.compile("(?<project>.*)_(owner|member|collaborator)?");

    private final Cache _cache;
}
