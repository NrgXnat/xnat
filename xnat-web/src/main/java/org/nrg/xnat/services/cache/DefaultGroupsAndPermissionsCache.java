/*
 * web: org.nrg.xnat.services.cache.DefaultUserProjectCache
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.cache;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.UserGroup;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.Initializing;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Future;

import static org.nrg.xdat.security.helpers.Groups.IS_GROUP_XFTITEM_EVENT;
import static org.nrg.xdat.security.helpers.Groups.isXdatUsergroupEvent;
import static reactor.bus.selector.Selectors.predicate;

@SuppressWarnings("Duplicates")
@Service
@Slf4j
public class DefaultGroupsAndPermissionsCache extends CacheEventListenerAdapter implements Consumer<Event<XftItemEvent>>, GroupsAndPermissionsCache, Initializing {
    @Autowired
    public DefaultGroupsAndPermissionsCache(final CacheManager cacheManager, final NamedParameterJdbcTemplate template, final EventBus eventBus) {
        _cache = cacheManager.getCache(CACHE_NAME);
        _template = template;
        _helper = new DatabaseHelper((JdbcTemplate) _template.getJdbcOperations());
        registerCacheEventListener();
        eventBus.on(predicate(IS_GROUP_XFTITEM_EVENT), this);
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
        handleCacheRemoveEvent(cache, null, "removed");
    }

    /**
     * Handles events with {@link XftItemEvent} type. This specific implementation looks for changes in
     * permissions and group definitions so that the project/user caching can be updated as appropriate.
     *
     * @param event The event that occurred.
     */
    @Override
    public void accept(final Event<XftItemEvent> event) {
        if (!isXdatUsergroupEvent(event)) {
            return;
        }

        final XftItemEvent  data    = event.getData();
        final String        action  = data.getAction();
        final XdatUsergroup group   = (XdatUsergroup) data.getItem();
        final String        groupId = group.getId();

        switch (action) {
            case XftItemEvent.CREATE:
                log.debug("New group created with ID {}, caching new instance", groupId);
                cacheGroup(createUserGroupFromXdatUsergroup(group));
                break;

            case XftItemEvent.UPDATE:
                log.debug("The group {} was updated, caching updated instance", groupId);
                cacheGroup(createUserGroupFromXdatUsergroup(group));
                break;

            case XftItemEvent.DELETE:
                log.debug("The group {} was deleted, removing instance from cache", groupId);
                _cache.evict(groupId);
                break;

            default:
                log.debug("The {} action happened on the group with ID {}, no cache update required", action, groupId);
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCacheName() {
        return CACHE_NAME;
    }

    /**
     * Indicates whether the specified project ID or alias is already cached.
     *
     * @param cacheId The ID or alias of the project to check.
     *
     * @return Returns true if the ID or alias is mapped to a project cache entry, false otherwise.
     */
    @Override
    public boolean has(final String cacheId) {
        return _cache.get(cacheId) != null;
    }

    /**
     * Gets the specified project if the user has any access to it. Returns null otherwise.
     *
     * @param groupId The ID or alias of the project to retrieve.
     *
     * @return The project object if the user can access it, null otherwise.
     */
    @Override
    public UserGroupI get(final String groupId) {
        // Check that the group is cached and, if so, return it.
        if (has(groupId)) {
            if (isCachedNonexistentId(groupId)) {
                log.debug("Found cached non-existent ID for group {}, returning null", groupId);
                return null;
            }

            // Here we can just return the value directly as a group, because we know there's something cached
            // and that what's cached is not a string.
            log.debug("Found a cache entry for group {}", groupId);
            return _cache.get(groupId, UserGroupI.class);
        }

        // Nothing cached for group ID, so let's try to retrieve it.
        log.debug("Nothing cached for ID {}, so trying to retrieve it from the system", groupId);
        final XdatUsergroup found = XdatUsergroup.getXdatUsergroupsById(groupId, Users.getAdminUser(), false);

        // If we didn't find a group for that ID...
        if (found == null) {
            // Cache the ID as non-existent and return null.
            cacheNonexistentId(groupId);
            return null;
        }

        final UserGroupI group = createUserGroupFromXdatUsergroup(found);
        cacheGroup(group);
        final List<UserGroupI> groups = getGroupsForTag(group.getTag());
        log.debug("Cached the group for the ID {} and cached it, also cached {} groups with the tag {}", groupId, groups.size(), group.getTag());
        return group;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public List<UserGroupI> getGroupsForTag(final String tag) {
        final String actual = getCacheIdForTag(tag);

        // Check whether this tag is already cached.
        if (has(actual)) {
            // If it's cached, see if it's cached as non-existent.
            if (isCachedNonexistentId(actual)) {
                return Collections.emptyList();
            }

            // If it's cached and not marked as non-existent, we can just return the list.
            return getUserGroupList(_cache.get(actual, List.class));
        }

        // If nothing is already cached, then retrieve and cache the groups if found or cache NOT_A_GROUP if the tag isn't found.
        final List<String> foundGroups = _template.queryForList(QUERY_GET_GROUPS_FOR_TAG, new MapSqlParameterSource("tag", actual), String.class);

        // If this is empty, then the tag doesn't exist and we'll just put NOT_A_GROUP there.
        if (foundGroups.isEmpty()) {
            cacheNonexistentId(actual);
            return Collections.emptyList();
        }

        // If the found groups weren't empty, transform that to a list of UserGroups, cache each group then the list, then return it.
        final List<UserGroupI> groups = Lists.transform(foundGroups, USER_GROUP_FUNCTION);

        final Set<String> groupIds = new HashSet<>();
        for (final UserGroupI group : groups) {
            // Only cache groups that aren't already cached.
            final String groupId = group.getId();
            groupIds.add(groupId);
            if (!has(groupId)) {
                _cache.put(groupId, group);
            }
        }
        _cache.put(actual, groupIds);

        return groups;
    }

    @Nonnull
    @Override
    public Map<String, UserGroupI> getGroupsForUser(final String username) throws UserNotFoundException {
        final MapSqlParameterSource   parameters = checkUser(username);
        final List<String>            groupIds   = _template.queryForList(QUERY_GET_GROUPS_FOR_USER, parameters, String.class);
        final Map<String, UserGroupI> groups     = new HashMap<>();
        for (final String groupId : groupIds) {
            final UserGroupI group = get(groupId);
            if (group == null) {
                log.info("Found non-existent group for ID {}, ignoring", groupId);
            } else {
                log.debug("Adding group {} to groups for user {}", groupId, username);
                groups.put(groupId, group);
            }
        }
        return groups;
    }

    @Override
    public UserGroupI getGroupForUserAndTag(final String username, final String tag) throws UserNotFoundException {
        final MapSqlParameterSource parameters = checkUser(username);
        parameters.addValue("tag", tag);
        final String groupId = _template.queryForObject(QUERY_GET_GROUP_FOR_USER_AND_TAG, parameters, String.class);
        return StringUtils.isNotBlank(groupId) ? get(groupId) : null;
    }

    @Override
    public boolean canInitialize() {
        try {
            return _helper.tableExists("xdat_usergroup") && XFTManager.isInitialized();
        } catch (SQLException e) {
            log.info("Got an SQL exception checking for xdat_usergroup table", e);
            return false;
        }
    }

    @Async
    @Override
    public Future<Boolean> initialize() {
        final List<String> groupIds  = _template.queryForList(QUERY_ALL_GROUPS, EmptySqlParameterSource.INSTANCE, String.class);
        final UserI        adminUser = Users.getAdminUser();
        assert adminUser != null;

        log.info("Found {} group IDs to run through, initializing cache with these as user {}", groupIds.size(), adminUser.getUsername());
        for (final String groupId : groupIds) {
            log.debug("Initializing group {}", groupId);
            final XdatUsergroup group = XdatUsergroup.getXdatUsergroupsById(groupId, adminUser, false);
            if (group == null) {
                log.warn("No group returned for ID {}", groupId);
                continue;
            }
            cacheGroup(createUserGroupFromXdatUsergroup(group));
            final String      tag    = group.getTag();
            final Set<String> groups = getGroupIdListForTag(tag);
            groups.add(groupId);
            cacheTag(group.getTag(), groups);
        }

        return new AsyncResult<>(_initialized = true);
    }

    @Override
    public boolean isInitialized() {
        return _initialized;
    }

    /**
     * Checks whether the users exists. If not, this throws the {@link UserNotFoundException}. Otherwise it returns
     * a parameter source containing the username that can be used in subsequent queries.
     *
     * @param username The user to test
     *
     * @return A parameter source containing the username parameter.
     *
     * @throws UserNotFoundException If the user doesn't exist.
     */
    private MapSqlParameterSource checkUser(final String username) throws UserNotFoundException {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("username", username);
        if (!_template.queryForObject(QUERY_CHECK_USER_EXISTS, parameters, Boolean.class)) {
            throw new UserNotFoundException(username);
        }
        return parameters;
    }

    private void cacheGroup(final UserGroupI group) {
        _cache.put(group.getId(), group);
        log.debug("Cached group {}", group.getId());
    }

    private void cacheTag(final String tag, final Set<String> groupIds) {
        _cache.put(getCacheIdForTag(tag), groupIds);
        log.debug("Cached {} group IDs for tag {}: {}", groupIds.size(), tag, StringUtils.join(groupIds, ", "));
    }

    private Set<String> getGroupIdListForTag(final String tag) {
        final Set groupIds = _cache.get(getCacheIdForTag(tag), Set.class);
        if (groupIds == null) {
            return new HashSet<>();
        }
        return Sets.newHashSet(Iterables.filter(groupIds, String.class));
    }

    private List<UserGroupI> getUserGroupList(final List groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new ArrayList<>();
        }

        return Lists.newArrayList(Iterables.filter(Iterables.transform(Iterables.filter(groupIds, String.class), new Function<String, UserGroupI>() {
            @Nullable
            @Override
            public UserGroupI apply(@Nullable final String groupId) {
                return get(groupId);
            }
        }), Predicates.notNull()));
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

    private void handleCacheRemoveEvent(final Ehcache cache, final Element element, final String event) {
        if (isGroupsAndPermissionsCacheEvent(cache)) {
            if (element == null) {
                log.debug("Got a {} event for cache {}, no specific element affected", event, cache.getName());
                return;
            }
            log.debug("Got a {} event for cache {} on ID {} with value of type {}", event, cache.getName(), element.getObjectKey(), element.getObjectValue().getClass().getName());
        }
    }

    /**
     * Indicates whether the specified cache ID stores a string. Since this cache implementation only stores strings to
     * represent groups and tags that don't exist, there's no need to check the value of the string. Note that this doesn't
     * indicate that the object indicated by the ID necessarily exists: it just indicates that we haven't checked for its
     * existence and cached that.
     *
     * @param cacheId The ID to test for non-existent status.
     *
     * @return Returns true if the ID was cached as non-existent, false otherwise.
     */
    private boolean isCachedNonexistentId(final String cacheId) {
        final Cache.ValueWrapper value = _cache.get(cacheId);

        // This thing doesn't exist in the cache, so return false;
        if (value == null) {
            return false;
        }

        // Now just test whether the cached value is a string: if so, the thing indicated by the ID doesn't exist.
        return value.get() instanceof String;
    }

    private void cacheNonexistentId(final String cacheId) {
        _cache.put(cacheId, NOT_A_GROUP);
    }

    private static String getCacheIdForTag(final String tag) {
        return StringUtils.startsWith(tag, TAG_PREFIX) ? tag : TAG_PREFIX + tag;
    }

    private static boolean isGroupsAndPermissionsCacheEvent(final Ehcache cache) {
        return StringUtils.equals(CACHE_NAME, cache.getName());
    }

    private static UserGroupI createUserGroupFromXdatUsergroup(final XdatUsergroup group) {
        if (group != null) {
            try {
                return new UserGroup(group);
            } catch (Exception e) {
                log.error("An error occurred trying to create a UserGroup object from the XdatUsergroup object for group " + group.getId(), e);
            }
        }
        return null;
    }

    private static final Function<String, UserGroupI> USER_GROUP_FUNCTION = new Function<String, UserGroupI>() {
        @Nullable
        @Override
        public UserGroupI apply(final String groupId) {
            return createUserGroupFromXdatUsergroup(XdatUsergroup.getXdatUsergroupsById(groupId, Users.getAdminUser(), false));
        }
    };

    private static final String NOT_A_GROUP                      = "NOT_A_GROUP";
    private static final String QUERY_GET_GROUPS_FOR_USER        = "SELECT groupid " +
                                                                   "FROM xdat_user_groupid xug " +
                                                                   "  LEFT JOIN xdat_user xu ON groups_groupid_xdat_user_xdat_user_id = xdat_user_id " +
                                                                   "WHERE xu.login = :username " +
                                                                   "ORDER BY groupid";
    private static final String QUERY_GET_GROUP_FOR_USER_AND_TAG = "SELECT id "
                                                                   + "FROM xdat_usergroup xug "
                                                                   + "  LEFT JOIN xdat_user_groupid xugid ON xug.id = xugid.groupid "
                                                                   + "  LEFT JOIN xdat_user xu ON xugid.groups_groupid_xdat_user_xdat_user_id = xu.xdat_user_id "
                                                                   + "WHERE xu.login = :username AND tag = :tag "
                                                                   + "ORDER BY groupid";
    private static final String QUERY_ALL_GROUPS                 = "SELECT id FROM xdat_usergroup";
    private static final String QUERY_GET_GROUPS_FOR_TAG         = "SELECT * FROM xdat_usergroup WHERE tag = :tag";
    private static final String QUERY_CHECK_USER_EXISTS          = "SELECT EXISTS(SELECT TRUE FROM xdat_user WHERE login = :username) AS exists";
    public static final  String TAG_PREFIX                       = "tag:";


    private final Cache                      _cache;
    private final NamedParameterJdbcTemplate _template;
    private final DatabaseHelper             _helper;

    private boolean _initialized;
}
