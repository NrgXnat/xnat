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
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.utilities.LapStopWatch;
import org.nrg.xdat.XDAT;
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
import org.nrg.xnat.services.cache.jms.InitializeGroupRequest;
import org.nrg.xnat.services.cache.jms.InitializeGroupRequestListener;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.text.NumberFormat;
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
    public DefaultGroupsAndPermissionsCache(final CacheManager cacheManager, final NamedParameterJdbcTemplate template, final JmsTemplate jmsTemplate, final EventBus eventBus) {
        _cache = cacheManager.getCache(CACHE_NAME);
        _template = template;
        _jmsTemplate = jmsTemplate;
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
        if (StringUtils.isBlank(groupId)) {
            return null;
        }

        // Check that the group is cached and, if so, return it.
        if (has(groupId)) {
            // Here we can just return the value directly as a group, because we know there's something cached
            // and that what's cached is not a string.
            log.debug("Found a cache entry for group {}", groupId);
            return _cache.get(groupId, UserGroupI.class);
        }

        return initializeGroup(groupId);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public List<UserGroupI> getGroupsForTag(final String tag) {
        log.info("Getting groups for tag {}", tag);
        final String cacheId = getCacheIdForTag(tag);

        // Check whether this tag is already cached.
        final List<String> groupIds;
        if (has(cacheId)) {
            // If it's cached, we can just return the list.
            groupIds = getTagGroups(cacheId);
            log.info("Found {} groups already cached for tag {}", groupIds.size(), tag);
        } else {
            groupIds = initializeTag(tag);
            log.info("Initialized {} groups for tag {}: {}", groupIds.size(), tag, StringUtils.join(groupIds, ", "));
        }

        return getUserGroupList(groupIds);
    }

    @Nonnull
    @Override
    public Map<String, UserGroupI> getGroupsForUser(final String username) throws UserNotFoundException {
        final List<String>            groupIds = getGroupIdsForUser(username);
        final Map<String, UserGroupI> groups   = new HashMap<>();
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
    public Date getLastUpdateTime(final String groupId) {
        if (!has(groupId)) {
            return null;
        }
        return new Date(getLatestOfCreationAndUpdateTime(groupId));
    }

    @Override
    public Date getLastUpdateTime(final UserI user) {
        try {
            final List<String> groupIds  = getGroupIdsForUser(user.getUsername());
            long               timestamp = 0;
            for (final String groupId : groupIds) {
                final Date lastUpdateTime = getLastUpdateTime(groupId);
                if (lastUpdateTime != null) {
                    timestamp = Math.max(timestamp, lastUpdateTime.getTime());
                }
            }
            return new Date(timestamp);
        } catch (UserNotFoundException ignored) {
            // This doesn't happen because we've passed the user object in.
            return new Date();
        }
    }

    @Override
    public boolean canInitialize() {
        try {
            final boolean userGroupTableExists = _helper.tableExists("xdat_usergroup");
            final boolean xftManagerComplete   = XFTManager.isComplete();
            log.info("User group table {}, XFTManager initialization completed {}", userGroupTableExists, xftManagerComplete);
            return userGroupTableExists && xftManagerComplete;
        } catch (SQLException e) {
            log.info("Got an SQL exception checking for xdat_usergroup table", e);
            return false;
        }
    }

    @Async
    @Override
    public Future<Boolean> initialize() {
        final LapStopWatch stopWatch = LapStopWatch.createStarted(log, Level.INFO);

        final int tags = initializeTags();
        stopWatch.lap("Processed {} tags", tags);

        final List<String> groupIds = _template.queryForList(QUERY_ALL_GROUPS, EmptySqlParameterSource.INSTANCE, String.class);
        if (_listener != null) {
            _listener.setGroupIds(groupIds);
        }

        try {
            final UserI adminUser = Users.getAdminUser();
            assert adminUser != null;

            stopWatch.lap("Found {} group IDs to run through, initializing cache with these as user {}", groupIds.size(), adminUser.getUsername());
            for (final String groupId : groupIds) {
                stopWatch.lap("Creating queue entry for group {}", groupId);
                XDAT.sendJmsRequest(_jmsTemplate, new InitializeGroupRequest(groupId));
            }
        } finally {
            if (stopWatch.isStarted()) {
                stopWatch.stop();
            }
            log.info("Total time to queue {} groups was {} ms", groupIds.size(), FORMATTER.format(stopWatch.getTime()));
            if (log.isInfoEnabled()) {
                log.info(stopWatch.toTable());
            }
        }

        return new AsyncResult<>(_initialized = true);
    }

    @Override
    public boolean isInitialized() {
        return _initialized;
    }

    public void registerListener(final InitializeGroupRequestListener listener) {
        _listener = listener;
    }

    private synchronized UserGroupI initializeGroup(final String groupId) {
        // We may have just checked before coming into this method, but since it's synchronized we may have waited while someone else was caching it so...
        if (has(groupId)) {
            log.info("Group {} was already initialized and in the cache, returning cached instance");
            return _cache.get(groupId, UserGroupI.class);
        }

        // Nothing cached for group ID, so let's try to retrieve it.
        log.debug("Initializing group {}", groupId);
        final XdatUsergroup found = XdatUsergroup.getXdatUsergroupsById(groupId, Users.getAdminUser(), false);

        // If we didn't find a group for that ID...
        if (found == null) {
            // Return null.
            log.info("Someone tried to get the user group {}, but a group with that ID doesn't exist.", groupId);
            return null;
        }

        return cacheGroup(createUserGroupFromXdatUsergroup(found));
    }

    private synchronized UserGroupI cacheGroup(final UserGroupI group) {
        final String groupId = group.getId();
        _cache.put(groupId, group);
        log.debug("Retrieved and cached the group for the ID {}", groupId);
        return group;
    }

    private synchronized int initializeTags() {
        final List<String> tags = _template.queryForList(QUERY_ALL_TAGS, EmptySqlParameterSource.INSTANCE, String.class);
        for (final String tag : tags) {
            final String cacheId = getCacheIdForTag(tag);
            if (has(cacheId)) {
                log.info("Found tag {} but that is already in the cache", tag);
                continue;
            }

            log.debug("Initializing tag {}", tag);
            initializeTag(tag);
        }

        final int size = tags.size();
        log.info("Completed initialization of {} tags", size);
        return size;
    }

    private synchronized List<String> initializeTag(final String tag) {
        // If there's a blank tag...
        if (StringUtils.isBlank(tag)) {
            log.info("Requested to initialize a blank tag, but that's not a thing.");
            return Collections.emptyList();
        }

        final String cacheId = getCacheIdForTag(tag);

        // We may have just checked before coming into this method, but since it's synchronized we may have waited while someone else was caching it so...
        if (has(cacheId)) {
            log.info("Got a request to initialize the tag {} but that is already in the cache", tag);
            return getTagGroups(cacheId);
        }

        // Then retrieve and cache the groups if found or cache DOES_NOT_EXIST if the tag isn't found.
        final List<String> groups = _template.queryForList(QUERY_GET_GROUPS_FOR_TAG, new MapSqlParameterSource("tag", tag), String.class);

        // If this is empty, then the tag doesn't exist and we'll just put DOES_NOT_EXIST there.
        if (groups.isEmpty()) {
            log.info("Someone tried to get groups for the tag {}, but there are no groups with that tag.", tag);
            return Collections.emptyList();
        } else {
            log.debug("Cached tag {} for {} groups: {}", tag, groups.size(), StringUtils.join(groups, ", "));
            _cache.put(cacheId, groups);
            return groups;
        }
    }

    private List<String> getTagGroups(final String cacheId) {
        return Lists.newArrayList(Iterables.filter(_cache.get(cacheId, List.class), String.class));
    }

    private List<String> getGroupIdsForUser(final String username) throws UserNotFoundException {
        final MapSqlParameterSource parameters = checkUser(username);
        return _template.queryForList(QUERY_GET_GROUPS_FOR_USER, parameters, String.class);
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

    private long getLatestOfCreationAndUpdateTime(final String cacheId) {
        return getEhCache().get(cacheId).getLatestOfCreationAndUpdateTime();
    }

    private void registerCacheEventListener() {
        try {
            final net.sf.ehcache.Cache cache = getEhCache();
            cache.getCacheEventNotificationService().registerListener(this);
            log.debug("Registered default groups and permissions cache as net.sf.ehcache.Cache listener");
        } catch (RuntimeException e) {
            log.warn("I don't know how to handle the native cache type {}", _cache.getNativeCache().getClass().getName());
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

    private net.sf.ehcache.Cache getEhCache() {
        final Object nativeCache = _cache.getNativeCache();
        if (nativeCache instanceof net.sf.ehcache.Cache) {
            return ((net.sf.ehcache.Cache) nativeCache);
        }
        throw new RuntimeException("The native cache is not an ehcache instance, but instead is " + nativeCache.getClass().getName());
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

    private static final String QUERY_GET_GROUPS_FOR_USER        = "SELECT groupid " +
                                                                   "FROM xdat_user_groupid xug " +
                                                                   "  LEFT JOIN xdat_user xu ON groups_groupid_xdat_user_xdat_user_id = xdat_user_id " +
                                                                   "WHERE xu.login = :username " +
                                                                   "ORDER BY groupid";
    private static final String QUERY_GET_GROUP_FOR_USER_AND_TAG = "SELECT id " +
                                                                   "FROM xdat_usergroup xug " +
                                                                   "  LEFT JOIN xdat_user_groupid xugid ON xug.id = xugid.groupid " +
                                                                   "  LEFT JOIN xdat_user xu ON xugid.groups_groupid_xdat_user_xdat_user_id = xu.xdat_user_id " +
                                                                   "WHERE xu.login = :username AND tag = :tag " +
                                                                   "ORDER BY groupid";
    private static final String QUERY_ALL_GROUPS                 = "SELECT id FROM xdat_usergroup";
    private static final String QUERY_ALL_TAGS                   = "SELECT DISTINCT tag FROM xdat_usergroup WHERE tag IS NOT NULL AND tag <> ''";
    private static final String QUERY_GET_GROUPS_FOR_TAG         = "SELECT id FROM xdat_usergroup WHERE tag = :tag";
    private static final String QUERY_CHECK_USER_EXISTS          = "SELECT EXISTS(SELECT TRUE FROM xdat_user WHERE login = :username) AS exists";
    private static final String TAG_PREFIX                       = "tag:";

    private static final NumberFormat FORMATTER = NumberFormat.getNumberInstance(Locale.getDefault());

    private final Cache                      _cache;
    private final NamedParameterJdbcTemplate _template;
    private final JmsTemplate                _jmsTemplate;
    private final DatabaseHelper             _helper;

    private InitializeGroupRequestListener _listener;
    private boolean                        _initialized;
}
