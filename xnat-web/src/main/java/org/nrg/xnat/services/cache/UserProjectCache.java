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
import com.google.common.base.Joiner;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.model.XnatProjectdataAliasI;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import reactor.fn.Predicate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nrg.framework.exceptions.NrgServiceError.Unknown;
import static org.nrg.xdat.security.helpers.AccessLevel.*;
import static reactor.bus.selector.Selectors.predicate;

@SuppressWarnings("Duplicates")
@Service
@Slf4j
public class UserProjectCache extends CacheEventListenerAdapter implements Consumer<Event<XftItemEvent>> {
    public static final String NOT_A_PROJECT           = "NOT_A_PROJECT";
    public static final String USER_PROJECT_CACHE_NAME = "UserProjectCacheManagerCache";

    @Autowired
    public UserProjectCache(final CacheManager cacheManager, final EventBus eventBus, final NamedParameterJdbcTemplate template) {
        _cache = cacheManager.getCache(USER_PROJECT_CACHE_NAME);
        _template = template;

        registerCacheEventListener();

        eventBus.on(predicate(new Predicate<Object>() {
            @Override
            public boolean test(final Object object) {
                return object != null && object instanceof XftItemEvent && isGroupItem((XftItemEvent) object);
            }
        }), this);
    }

    private void registerCacheEventListener() {
        final Object nativeCache = _cache.getNativeCache();
        if (nativeCache instanceof net.sf.ehcache.Cache) {
            ((net.sf.ehcache.Cache) nativeCache).getCacheEventNotificationService().registerListener(this);
            log.debug("Registered user project cache as net.sf.ehcache.Cache listener");
        } else {
            log.warn("I don't know how to handle the native cache type {}", nativeCache.getClass().getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementRemoved(final Ehcache cache, final Element element) throws CacheException {
        handleCacheRemoveEvent(cache, element, "removed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementExpired(final Ehcache cache, final Element element) {
        handleCacheRemoveEvent(cache, element, "expired");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementEvicted(final Ehcache cache, final Element element) {
        handleCacheRemoveEvent(cache, element, "evicted");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyRemoveAll(final Ehcache cache) {
        if (isProjectCacheEvent(cache)) {
            _aliasMapping.clear();
        }
    }

    /**
     * Handles events with {@link XftItemEvent} type. This specific implementation looks for changes in permissions and group definitions so that the project/user caching can be updated
     * as appropriate.
     *
     * @param event The event that occurred.
     */
    @Override
    public void accept(final Event<XftItemEvent> event) {
        // TODO: Catch event where user is added to or removed from site admins.
        final XdatUsergroup userGroup = getUserGroupFromEvent(event);
        if (userGroup == null) {
            log.info("Got an XFTItemEvent, but not one I'm interested in (looking for user group events)");
            return;
        }

        final String  groupId = userGroup.getId();
        final Matcher matcher = PROJECT_GROUP.matcher(groupId);
        if (!matcher.matches()) {
            log.info("Got an XFTItemEvent for a user group, but not one I'm interested in (it isn't a project group): {}", groupId);
            return;
        }

        final String       projectId    = matcher.group("project");
        final ProjectCache projectCache = _cache.get(projectId, ProjectCache.class);
        log.info("Got an XFTItemEvent for a user project group: {}", groupId);

        // If there was no cached project, maybe it was cached as a non-existent project and has been created?
        if (projectCache == null) {
            log.info("No cache found for the project: {}", projectId);
            // Remove the canonical ID if present.
            if (_aliasMapping.containsKey(projectId)) {
                log.debug("Removed the cached alias mapping found for the project: {}", projectId);
                _aliasMapping.remove(projectId);
            }
            // This would be really weird, where the project isn't in the cache but it's mapped, but OK.
            if (_aliasMapping.containsValue(projectId)) {
                final List<String> aliases = new ArrayList<>();
                for (final Map.Entry<String, String> entry : _aliasMapping.entrySet()) {
                    if (StringUtils.equals(projectId, entry.getValue())) {
                        aliases.add(entry.getKey());
                    }
                }
                log.warn("Strange situation found where the project {} had no project cache but still had {} alias mappings: {}", projectId, aliases.size(), Joiner.on(", ").join(aliases));
                for (final String alias : aliases) {
                    _aliasMapping.remove(alias);
                }
            }
        } else {
            log.info("Found project cache for project {}, evicting the project cache.", projectId);
            _cache.evict(projectId);
        }
    }

    /**
     * Indicates whether the specified project ID or alias is already cached.
     *
     * @param idOrAlias The ID or alias of the project to check.
     *
     * @return Returns true if the ID or alias is mapped to a project cache entry, false otherwise.
     */
    public boolean has(final String idOrAlias) {
        return _aliasMapping.containsKey(idOrAlias);
    }

    /**
     * Indicates whether permissions for the specified user in the specified project ID or alias is already cached.
     *
     * @param user      The user object for the user requesting the project.
     * @param idOrAlias The ID or alias of the project to check.
     *
     * @return Returns true if the user is mapped to a cache entry for the cached project ID or alias, false otherwise.
     */
    public boolean has(final UserI user, final String idOrAlias) {
        if (!has(idOrAlias)) {
            return false;
        }

        try {
            final String       userId       = user.getUsername();
            final ProjectCache projectCache = getProjectCache(idOrAlias, user, userId);
            return projectCache != null && projectCache.hasUser(userId);
        } catch (UserNotFoundException ignored) {
            // We're passing the user object in directly so this can't actually happen here.
            return false;
        }
    }

    /**
     * Indicates whether the specified user can delete the project identified by the specified ID or alias. Note that this returns false if
     * the project can't be found by the specified ID or alias or the username can't be located.
     *
     * @param userId    The username of the user to test.
     * @param idOrAlias The ID or an alias of the project to be tested.
     *
     * @return Returns true if the user can delete the specified project or false otherwise.
     */
    public boolean canDelete(final String userId, final String idOrAlias) {
        return hasAccess(userId, idOrAlias, Delete);
    }

    /**
     * Indicates whether the specified user can write to the project identified by the specified ID or alias. Note that this returns false if
     * the project can't be found by the specified ID or alias or the username can't be located.
     *
     * @param userId    The username of the user to test.
     * @param idOrAlias The ID or an alias of the project to be tested.
     *
     * @return Returns true if the user can write to the specified project or false otherwise.
     */
    public boolean canWrite(final String userId, final String idOrAlias) {
        return hasAccess(userId, idOrAlias, Edit);
    }

    /**
     * Indicates whether the specified user can read from the project identified by the specified ID or alias. Note that this returns false if
     * the project can't be found by the specified ID or alias or the username can't be located.
     *
     * @param userId    The username of the user to test.
     * @param idOrAlias The ID or an alias of the project to be tested.
     *
     * @return Returns true if the user can read from the specified project or false otherwise.
     */
    public boolean canRead(final String userId, final String idOrAlias) {
        return hasAccess(userId, idOrAlias, Read);
    }

    /**
     * Gets the specified project if the user has any access to it. Returns null otherwise.
     *
     * @param user      The user trying to access the project.
     * @param idOrAlias The ID or alias of the project to retrieve.
     *
     * @return The project object if the user can access it, null otherwise.
     */
    public XnatProjectdata get(final UserI user, final String idOrAlias) {
        if (isCachedNonexistentProject(idOrAlias)) {
            return null;
        }

        final String userId = user.getUsername();

        // Check that the project is readable by the user and, if so, return it.
        if (canRead(userId, idOrAlias)) {
            log.debug("Found a cached project for {} that is accessible for the user {}.", idOrAlias, userId);
            final ProjectCache projectCache = getProjectCache(idOrAlias, user);
            if (projectCache != null) {
                log.debug("Found a cached project for ID or alias {} for user {}.", idOrAlias, userId);
                return projectCache.getProject();
            } else {
                log.warn("A weird condition occurred in the user project cache: has({}, {}) returned true, no non-writable or nonexistent project was found, but getProjectCache({}, {}) returned null. This method will return null, but this isn't a case that should arise.", userId, idOrAlias, idOrAlias, userId);
            }
        }

        // If we made it here, the project is either inaccessible to the user or doesn't exist. In either case, return null.
        return null;
    }

    private boolean hasAccess(final String userId, final String idOrAlias, final AccessLevel accessLevel) {
        if (isCachedNonexistentProject(idOrAlias)) {
            return false;
        }

        // If they've tried to access a non-existent user more than once, we can just return false here.
        if (_nonexistentUsers.contains(userId)) {
            return false;
        }

        // If the user is not in the user lists, try to retrieve and cache it.
        final XDATUser user;
        final boolean  isSiteAdmin;
        if (!_nonAdmins.contains(userId) && !_siteAdmins.contains(userId)) {
            try {
                // Get the user...
                user = new XDATUser(userId);
                // If the user is an admin, add the user ID to the admin list and return true.
                if (Roles.isSiteAdmin(user)) {
                    _siteAdmins.add(userId);
                    isSiteAdmin = true;
                } else {
                    // Not an admin but let's track that we've retrieved the user by adding it to the non-admin list.
                    _nonAdmins.add(userId);
                    isSiteAdmin = false;
                }
            } catch (UserNotFoundException e) {
                // User doesn't exist, so cache that and we can just return false if asked again later.
                _nonexistentUsers.add(userId);
                return false;
            } catch (UserInitException e) {
                // Something bad happened so note it and move on.
                log.error("Something bad happened trying to retrieve the user " + userId, e);
                return false;
            }
        } else {
            // Set the user to null. It will only get initialized later in the initProjectCache() method if required.
            user = null;
            isSiteAdmin = _siteAdmins.contains(userId);
        }

        try {
            // Check for existing cache for the current project.
            final String projectId = getCanonicalProjectId(idOrAlias, user, userId);
            if (StringUtils.equals(NOT_A_PROJECT, projectId)) {
                return false;
            }

            final ProjectCache projectCache = getProjectCache(idOrAlias, user, userId);

            // If the project cache is null, the project doesn't exist (same as isCachedNonexistentProject() but it wasn't cached previously).
            if (projectCache == null) {
                return false;
            }

            // We don't care about checking the user against the project if it's a site admin: they have access to everything.
            if (isSiteAdmin) {
                return true;
            }

            // If the user isn't already cached...
            if (!projectCache.hasUser(userId)) {
                // Cache the user!
                projectCache.addUser(userId, getUserProjectAccess(ObjectUtils.defaultIfNull(user, new XDATUser(userId)), projectId));
            }
            return CollectionUtils.containsAny(projectCache.getUserAccessLevels(userId), ACCESS_LEVELS.get(accessLevel));
        } catch (UserInitException e) {
            log.error("Something bad happened trying to retrieve the user " + userId, e);
        } catch (UserNotFoundException e) {
            log.error("A user not found exception occurred searching for the user " + userId + ". This really shouldn't happen as I checked for the user's existence earlier.", e);
        } catch (Exception e) {
            log.error("An error occurred trying to test whether the user " + userId + " can read the project specified by ID or alias " + idOrAlias, e);
        }
        return false;
    }

    /**
     * Returns the canonical ID for the submitted ID or alias. If the specified ID or alias can't be found, this method returns the value {@link #NOT_A_PROJECT}.
     * If the ID or alias is already cached, this method just returns the canonical project ID corresponding to the submitted ID or alias. If it's not already
     * cached, the project is retrieved, the project ID and its aliases are cached in the alias mapping table, and the project object is inserted into the cache
     * under its canonical project ID. This allows the project to be retrieved once on initial reference then just pulled from the cache later.
     *
     * @param idOrAlias The ID or alias to test.
     * @param user      The user object for the user requesting the project.
     * @param userId    The user ID for the user requesting the project. This is used to retrieve the user object if the user parameter is null.
     *
     * @return The ID of the project with the submitted ID or alias.
     */
    private String getCanonicalProjectId(final String idOrAlias, final UserI user, final String userId) throws UserNotFoundException {
        // First check for cached ID or alias.
        if (_aliasMapping.containsKey(idOrAlias)) {
            // Found it so return that.
            final String projectId = _aliasMapping.get(idOrAlias);
            log.debug("Found cached project ID {} for the ID or alias {}", projectId, idOrAlias);
            return projectId;
        }

        log.info("User {} requested project by ID or alias {}, not found so starting initialization.", getUserId(user, userId), idOrAlias);
        initializeProjectCache(idOrAlias, user, userId);

        // Return the mapped value for the ID or alias, which should now be initialized (even if it's initialized to "not a project")
        final String projectId = _aliasMapping.get(idOrAlias);
        log.debug("After initialization, found cached project ID {} for the ID or alias {}", projectId, idOrAlias);
        return projectId;
    }

    /**
     * This is a simple wrapper around the {@link #getProjectCache(String, UserI, String)} method. It omits the user ID parameter and thus removes
     * the throws clause for {@link UserNotFoundException}, since the user has already been found.
     *
     * @param idOrAlias The ID or alias of the project to retrieve.
     * @param user      The user object for the user requesting the project.
     *
     * @return The project cache consisting of the project object and a map of users and their access level to the project.
     */
    @SuppressWarnings("unchecked")
    private ProjectCache getProjectCache(final String idOrAlias, final UserI user) {
        try {
            return getProjectCache(idOrAlias, user, null);
        } catch (UserNotFoundException ignored) {
            // We just won't get this exception here.
            return null;
        }
    }

    /**
     * Gets the cache for the project identified by the ID or alias parameter. If the project is cached as non-existent, this method returns null. Otherwise,
     * it checks the project cache and, if the project is already there, just returns the corresponding project cache. If the project isn't already in the cache,
     * this method tries to find the project by ID or alias. If it's not found, it's then cached as non-existent and the method returns null as before. If it is
     * found, the project is cached along with the access level for the specified user.
     *
     * @param idOrAlias The ID or alias of the project to retrieve.
     * @param user      The user object for the user requesting the project.
     * @param userId    The user ID for the user requesting the project. This is used to retrieve the user object if the user parameter is null.
     *
     * @return The project cache consisting of the project as the "left" or "key" value and a map of users and their access level to the project as the "right" or "value" value.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private ProjectCache getProjectCache(final String idOrAlias, final UserI user, final String userId) throws UserNotFoundException {
        assert user != null || StringUtils.isNotBlank(userId) : "You must provide either the user object or the ID for the user.";

        // If we've already determined that the project doesn't exist, return null.
        if (isCachedNonexistentProject(idOrAlias)) {
            log.debug("User {} requested project by ID or alias {}, but this is cached as a non-existent project.", getUserId(user, userId), idOrAlias);
            return null;
        }

        // If we've already mapped and cached the project...
        if (_aliasMapping.containsKey(idOrAlias)) {
            // Resolve the ID or alias to the mapped project ID.
            final String projectId = _aliasMapping.get(idOrAlias);
            log.debug("Found project ID {} for the ID or alias {}, returning project cache for that ID.", projectId, idOrAlias);

            // And then return the project cache.
            final ProjectCache projectCache = _cache.get(projectId, ProjectCache.class);
            if (projectCache == null) {
                throw new NrgServiceRuntimeException(NrgServiceError.Uninitialized, "Found cached project ID " + projectId + " for ID or alias " + idOrAlias + ", which should only ever happen if the project is cached, but the project cache is null.");
            }
            return projectCache;
        }

        return initializeProjectCache(idOrAlias, user, userId);
    }

    @Nullable
    private synchronized ProjectCache initializeProjectCache(final String idOrAlias, final UserI user, final String userId) throws UserNotFoundException {
        if (isCachedNonexistentProject(idOrAlias)) {
            log.info("Found non-existent project for ID or alias {}, this was probably initialized by another thread.", idOrAlias);
            return null;
        }

        if (_aliasMapping.containsKey(idOrAlias)) {
            final String projectId = _aliasMapping.get(idOrAlias);
            log.info("Found project ID {} for ID or alias {}, this was probably initialized by another thread.", projectId, idOrAlias);
            return _cache.get(projectId, ProjectCache.class);
        }

        assert user != null || StringUtils.isNotBlank(userId) : "You must provide either the user object or the ID for the user.";
        log.info("Initializing project cache for ID or alias {} for user {}", idOrAlias, getUserId(user, userId));

        try {
            // Make sure we have a concrete user object then try to retrieve the project.
            final UserI           resolved = ObjectUtils.defaultIfNull(user, new XDATUser(userId));
            final XnatProjectdata project  = XnatProjectdata.getProjectByIDorAlias(idOrAlias, resolved, false);
            // If that came back as null, cache this as non-existent and quit.
            if (project == null) {
                log.info("Didn't find a project with ID or alias {}, caching as non-existent", idOrAlias);
                cacheNonexistentProject(idOrAlias);
                return null;
            }

            // Create the project cache and user list.
            final ProjectCache projectCache = new ProjectCache(project);

            // This caches all of the users from the standard user groups and their permissions ahead of time in the most efficient way possible.
            final String projectId = project.getId();
            for (final String accessLevel : USER_GROUP_SUFFIXES.keySet()) {
                final List<AccessLevel> accessLevelPermissions = USER_GROUP_SUFFIXES.get(accessLevel);
                for (final String userIdByAccess : _template.queryForList(QUERY_USERS_BY_GROUP, getProjectAccessParameterSource(projectId, accessLevel), String.class)) {
                    log.debug("Caching user {} for access level {} on project {}", userIdByAccess, accessLevel, projectId);
                    projectCache.addUser(userIdByAccess, accessLevelPermissions);
                }
            }

            log.debug("Caching project cache for {}", projectId);
            _cache.put(projectId, projectCache);

            // Hooray, we found the project, so let's cache the ID and all the aliases.
            cacheProjectIdsAndAliases(projectId, project.getAliases_alias());

            return projectCache;
        } catch (UserInitException e) {
            throw new NrgServiceRuntimeException(Unknown, "An error occurred trying to retrieve the user " + userId, e);
        }
    }

    private void handleCacheRemoveEvent(final Ehcache cache, final Element element, final String event) {
        if (isProjectCacheEvent(cache, element)) {
            final XnatProjectdata project = getProjectCacheEventInstance(element);
            log.debug("Got a {} event for the project {}, removing the corresponding ID and alias mappings", event, project.getId());
            removeProjectIdMappings(project);
        }
    }

    private void cacheProjectIdsAndAliases(final String projectId, final List<XnatProjectdataAliasI> aliases) {
        _aliasMapping.put(projectId, projectId);
        for (final XnatProjectdataAliasI alias : aliases) {
            _aliasMapping.put(alias.getAlias(), projectId);
        }
        // Test for debug here because the list transform in the debug logging statement is non-trivial.
        if (log.isDebugEnabled()) {
            log.debug("Just cached ID and aliases for project {}: {}", projectId, Joiner.on(", ").join(Lists.transform(aliases, new Function<XnatProjectdataAliasI, String>() {
                @Override
                public String apply(final XnatProjectdataAliasI alias) {
                    return alias.getAlias();
                }
            })));
        }
    }

    private boolean isCachedNonexistentProject(final String idOrAlias) {
        return _aliasMapping.containsKey(idOrAlias) && StringUtils.equals(NOT_A_PROJECT, _aliasMapping.get(idOrAlias));
    }

    private void cacheNonexistentProject(final String idOrAlias) {
        _aliasMapping.put(idOrAlias, NOT_A_PROJECT);
    }

    private void removeProjectIdMappings(final XnatProjectdata project) {
        final String projectId = project.getId();
        _aliasMapping.remove(projectId);
        log.debug("Removed mapping for project ID {}", projectId);

        final List<XnatProjectdataAliasI> aliases = project.getAliases_alias();
        for (final XnatProjectdataAliasI alias : aliases) {
            final String aliasId = alias.getAlias();
            _aliasMapping.remove(aliasId);
            log.debug("Removed alias {} mapping for project ID {}", aliasId, projectId);
        }
    }

    /**
     * Tries to resolve the user ID. If the <b>userId</b> parameter isn't blank, this method just returns that. If the <b>userId</b> parameter is blank,
     * this method checks whether the <b>user</b> object is null. If it is, an error message is logged and an unknown value is returned. If it's not, this
     * method calls the {@link UserI#getUsername()} method and returns that value.
     *
     * @param user   The user object.
     * @param userId The user ID.
     *
     * @return A user ID sussed out from a combination of the two parameters.
     */
    private static String getUserId(final UserI user, final String userId) {
        if (StringUtils.isNotBlank(userId)) {
            return userId;
        }
        if (user == null) {
            log.warn("This method was called with both userId blank and user as null. This will be a problem elsewhere most likely.");
            return "UNKNOWN";
        }
        return user.getUsername();
    }

    private static XnatProjectdata getProjectCacheEventInstance(final Element element) {
        return ((ProjectCache) element.getObjectValue()).getProject();
    }

    private static boolean isProjectCacheEvent(final Ehcache cache) {
        return StringUtils.equals(USER_PROJECT_CACHE_NAME, cache.getName());
    }

    private static boolean isProjectCacheEvent(final Ehcache cache, final Element element) {
        return isProjectCacheEvent(cache) && (element == null || element.getObjectValue() instanceof ProjectCache);
    }

    private static List<AccessLevel> getUserProjectAccess(final UserI user, final String projectId) {
        final List<AccessLevel> levels = new ArrayList<>();
        if (Permissions.canDeleteProject(user, projectId)) {
            levels.add(Delete);
        } else if (Permissions.canEditProject(user, projectId)) {
            levels.add(Edit);
        } else {
            try {
                if (Permissions.canReadProject(user, projectId) || Permissions.isProjectPublic(projectId)) {
                    levels.add(Read);
                }
            } catch (Exception e) {
                log.error("An error occurred trying to check read permissions for the user " + user.getUsername() + " on the project " + projectId, e);
            }
        }
        if (Permissions.isProjectOwner(user, projectId)) {
            levels.add(Owner);
        } else if (Permissions.isProjectMember(user, projectId)) {
            levels.add(Member);
        } else if (Permissions.isProjectCollaborator(user, projectId)) {
            levels.add(Collaborator);
        }
        return levels;
    }

    private static MapSqlParameterSource getProjectAccessParameterSource(final String projectId, final String accessLevel) {
        return new MapSqlParameterSource(QUERY_KEY_PROJECT_ID, projectId).addValue(QUERY_KEY_ACCESS_LEVEL, accessLevel);
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

    private static class ProjectCache {
        ProjectCache(final XnatProjectdata project) {
            _project = project;
        }

        public XnatProjectdata getProject() {
            return _project;
        }

        public boolean hasUser(final String userId) {
            return _userCache.containsKey(userId);
        }

        public Collection<AccessLevel> getUserAccessLevels(final String userId) {
            return _userCache.get(userId);
        }

        public void addUser(final String userId, final List<AccessLevel> userProjectAccess) {
            _userCache.putAll(userId, userProjectAccess);
        }

        private final XnatProjectdata _project;

        private final Multimap<String, AccessLevel> _userCache = ArrayListMultimap.create();
    }

    private static final Pattern PROJECT_GROUP = Pattern.compile("(?<project>.*)_(owner|member|collaborator)?");

    private static final List<AccessLevel>                   DELETABLE_ACCESS       = Arrays.asList(Owner, Delete);
    private static final List<AccessLevel>                   WRITABLE_ACCESS        = Arrays.asList(Member, Edit);
    private static final List<AccessLevel>                   READABLE_ACCESS        = Arrays.asList(Collaborator, Read);
    private static final Map<AccessLevel, List<AccessLevel>> ACCESS_LEVELS          = ImmutableMap.of(Delete, DELETABLE_ACCESS,
                                                                                                      Edit, Lists.newArrayList(Iterables.concat(DELETABLE_ACCESS, WRITABLE_ACCESS)),
                                                                                                      Read, Lists.newArrayList(Iterables.concat(DELETABLE_ACCESS, WRITABLE_ACCESS, READABLE_ACCESS)));
    private static final Map<String, List<AccessLevel>>      USER_GROUP_SUFFIXES    = ImmutableMap.of("owner", DELETABLE_ACCESS, "member", WRITABLE_ACCESS, "collaborator", READABLE_ACCESS);
    private static final String                              QUERY_KEY_PROJECT_ID   = "projectId";
    private static final String                              QUERY_KEY_ACCESS_LEVEL = "accessLevel";

    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
    private static final String QUERY_USERS_BY_GROUP = "SELECT DISTINCT login "
                                                       + "FROM xdat_user "
                                                       + "  RIGHT JOIN xdat_user_groupid xug ON xdat_user.xdat_user_id = xug.groups_groupid_xdat_user_xdat_user_id "
                                                       + "WHERE groupid = :projectId || :accessLevel";

    private final Cache                      _cache;
    private final NamedParameterJdbcTemplate _template;

    private final Set<String>         _siteAdmins       = new HashSet<>();
    private final Set<String>         _nonAdmins        = new HashSet<>();
    private final Set<String>         _nonexistentUsers = new HashSet<>();
    private final Map<String, String> _aliasMapping     = new HashMap<>();
}
