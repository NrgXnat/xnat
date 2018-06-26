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
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.utilities.LapStopWatch;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.om.*;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.*;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.Initializing;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xdat.servlet.XDATServlet;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.methods.XftItemEventCriteria;
import org.nrg.xft.exception.*;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.cache.jms.InitializeGroupRequest;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static org.nrg.framework.exceptions.NrgServiceError.ConfigurationError;
import static org.nrg.xapi.rest.users.DataAccessApi.*;
import static org.nrg.xdat.security.PermissionCriteria.*;
import static org.nrg.xft.event.XftItemEventI.*;

@SuppressWarnings("Duplicates")
@Service
@Slf4j
public class DefaultGroupsAndPermissionsCache extends AbstractXftItemAndCacheEventHandlerMethod implements GroupsAndPermissionsCache, Initializing, GroupsAndPermissionsCache.Provider {
    @Autowired
    public DefaultGroupsAndPermissionsCache(final CacheManager cacheManager, final NamedParameterJdbcTemplate template, final JmsTemplate jmsTemplate) throws SQLException {
        super(cacheManager,
              XftItemEventCriteria.getXsiTypeCriteria(XnatProjectdata.SCHEMA_ELEMENT_NAME),
              XftItemEventCriteria.builder().xsiType(XnatSubjectdata.SCHEMA_ELEMENT_NAME).xsiType(XnatExperimentdata.SCHEMA_ELEMENT_NAME).actions(CREATE, XftItemEventI.DELETE, SHARE).build(),
              XftItemEventCriteria.getXsiTypeCriteria(XdatUsergroup.SCHEMA_ELEMENT_NAME),
              XftItemEventCriteria.getXsiTypeCriteria(XdatElementSecurity.SCHEMA_ELEMENT_NAME));

        _template = template;
        _jmsTemplate = jmsTemplate;
        _helper = new DatabaseHelper((JdbcTemplate) _template.getJdbcOperations());
        _totalCounts = new HashMap<>();
        _missingElements = new HashMap<>();
        _userChecks = new ConcurrentHashMap<>();
        if (_helper.tableExists("xnat_projectdata")) {
            updateTotalCounts();
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
        handleCacheRemoveEvent(cache, null, "removed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCacheName() {
        return CACHE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ElementDisplay> getBrowseableElementDisplays(final UserI user) {
        if (user == null) {
            return Collections.emptyMap();
        }

        final String username = user.getUsername();
        final String cacheId  = getCacheIdForUserElements(username, BROWSEABLE);
        log.debug("Retrieving browseable element displays for user {} through cache ID {}", username, cacheId);

        // Check whether the element types are cached and, if so, return that.
        if (has(cacheId)) {
            // Here we can just return the value directly as a map, because we know
            // there's something cached and that what's cached is not a string.
            final Map<String, ElementDisplay> browseables = getCachedMap(cacheId);
            browseables.putAll(getGuestBrowseableElementDisplays());
            log.info("Found an entry for user '{}' browseable element displays under cache ID '{}' with {} entries", username, cacheId, browseables.size());
            return browseables;
        }

        if (!user.isGuest()) {
            log.debug("No cache entry found for user '{}' readable counts by ID '{}', initializing entry", username, cacheId);
            final List<String> projects = getUserProjects(username);
            for (final String project : projects) {
                final String      projectCacheId = getCacheIdForProject(project);
                final Set<String> projectUsers;
                if (!has(projectCacheId)) {
                    projectUsers = new HashSet<>();
                } else {
                    projectUsers = getCachedSet(projectCacheId);
                }
                projectUsers.add(username);
                cacheObject(projectCacheId, projectUsers);
            }
        }
        final Map<String, ElementDisplay> browseables = updateBrowseableElementDisplays(user, cacheId);
        browseables.putAll(getGuestBrowseableElementDisplays());
        return browseables;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> getReadableCounts(final UserI user) {
        if (user == null) {
            return Collections.emptyMap();
        }

        final String username = user.getUsername();
        final String cacheId  = getCacheIdForUserElements(username, READABLE);
        log.debug("Retrieving readable counts for user {} through cache ID {}", username, cacheId);

        // Check whether the element types are cached and, if so, return that.
        if (has(cacheId)) {
            // Here we can just return the value directly as a map, because we know there's something cached
            // and that what's cached is not a string.
            log.debug("Found a cache entry for user '{}' readable counts by ID '{}'", username, cacheId);
            return getCachedMap(cacheId);
        }

        return updateReadableCounts(user, cacheId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ElementDisplay> getSearchableElementDisplays(final UserI user) {
        if (user == null) {
            return Collections.emptyList();
        }

        final String username = user.getUsername();
        final String cacheId  = getCacheIdForUserElements(username, SEARCHABLE);
        log.debug("Retrieving searchable element displays for user {} through cache ID {}", username, cacheId);

        final Map<String, Long> counts = getReadableCounts(user);
        try {
            return Lists.newArrayList(Iterables.filter(getActionElementDisplays(user, SecurityManager.READ), new Predicate<ElementDisplay>() {
                @Override
                public boolean apply(@Nullable final ElementDisplay elementDisplay) {
                    if (elementDisplay == null) {
                        return false;
                    }
                    final String elementName = elementDisplay.getElementName();
                    try {
                        return ElementSecurity.IsSearchable(elementName) && counts.containsKey(elementName) && counts.get(elementName) > 0;
                    } catch (Exception e) {
                        return false;
                    }
                }
            }));
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
        }

        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ElementDisplay> getActionElementDisplays(final UserI user, final String action) {
        if (!ACTIONS.contains(action)) {
            throw new NrgServiceRuntimeException(ConfigurationError, "The action '" + action + "' is invalid, must be one of: " + StringUtils.join(ACTIONS, ", "));
        }

        final String username = user.getUsername();
        final String cacheId  = getCacheIdForActionElements(username, action);

        // Check whether the action elements are cached and, if so, return that.
        if (has(cacheId)) {
            // Here we can just return the value directly as a list, because we know there's something cached
            // and that what's cached is not a string.
            final List<ElementDisplay> actions = getCachedList(cacheId);
            log.debug("Found a cache entry for user '{}' action '{}' elements by ID '{}' with {} entries", username, action, cacheId, actions.size());
            return actions;
        }

        return updateActionElementDisplays(user, action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PermissionCriteriaI> getPermissionCriteria(final UserI user, final String dataType) {
        return getPermissionCriteria(user.getUsername(), dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PermissionCriteriaI> getPermissionCriteria(final String username, final String dataType) {
        try {
            PoolDBUtils.CheckSpecialSQLChars(dataType);
        } catch (Exception e) {
            return null;
        }

        try {
            final List<PermissionCriteriaI> criteria = new ArrayList<>();

            final Map<String, ElementAccessManager> managers = getElementAccessManagers(username);
            if (managers == null) {
                log.warn("Couldn't find element access managers for user {} trying to retrieve permissions for data type {}", username, dataType);
            } else {
                final ElementAccessManager manager = managers.get(dataType);
                if (manager == null) {
                    log.info("Couldn't find element access manager for data type {} for user {} while trying to retrieve permissions ", dataType, username);
                } else {
                    criteria.addAll(manager.getCriteria());
                    if (criteria.isEmpty()) {
                        log.debug("Couldn't find any permission criteria for data type {} for user {} while trying to retrieve permissions ", dataType, username);
                    }
                }
            }

            final Map<String, UserGroupI> userGroups = Groups.getGroupsForUser(username);
            if (log.isDebugEnabled()) {
                log.debug("Found {} user groups for the user {}", userGroups.size(), username, userGroups.isEmpty() ? "" : ": " + Joiner.on(", ").join(userGroups.keySet()));
            }

            for (final String groupId : userGroups.keySet()) {
                final UserGroupI group = userGroups.get(groupId);
                if (group != null) {
                    final List<PermissionCriteriaI> permissions = group.getPermissionsByDataType(dataType);
                    if (permissions != null) {
                        if (log.isInfoEnabled()) {
                            log.info("Searched for permission criteria for user {} on type {} in group {}: {}", username, dataType, groupId, dumpCriteriaList(permissions));
                        }
                        criteria.addAll(permissions);
                    } else {
                        log.warn("Tried to retrieve permissions for data type {} for user {} in group {}, but this returned null.", dataType, username, groupId);
                    }
                } else {
                    log.warn("Tried to retrieve group {} for user {}, but this returned null.", groupId, username);
                }
            }

            if (!isGuest(username)) {
                try {
                    final List<PermissionCriteriaI> permissions = getPermissionCriteria(getGuest().getUsername(), dataType);
                    if (permissions != null) {
                        if (log.isInfoEnabled()) {
                            log.info("Searched for permission criteria from guest for user {} on type {}: {}", username, dataType, dumpCriteriaList(permissions));
                        }
                        criteria.addAll(permissions);
                    } else {
                        log.warn("Tried to retrieve permissions for data type {} for the guest user, but this returned null.", dataType);
                    }
                } catch (Exception e) {
                    log.error("An error occurred trying to retrieve the guest user", e);
                }
            }

            if (log.isInfoEnabled()) {
                log.info("Retrieved permission criteria for user {} on the data type {}: {}", username, dataType, dumpCriteriaList(criteria));
            }

            return ImmutableList.copyOf(criteria);
        } catch (UserNotFoundException e) {
            log.error("Couldn't find the indicated user");
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Long> getTotalCounts() {
        if (_totalCounts.isEmpty()) {
            updateTotalCounts();
        }

        return _totalCounts;
    }

    /**
     * Finds all user element cache IDs for the specified user and evicts them from the cache.
     *
     * @param username The username to be cleared.
     */
    @Override
    public void clearUserCache(final String username) {
        for (final String cacheId : getCacheIdsForUserElements(username)) {
            log.debug("Clearing cache ID '{}'", cacheId);
            getCache().evict(cacheId);
        }
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
            log.trace("Found a cache entry for group {}", groupId);
            return getCachedGroup(groupId);
        }

        return cacheGroup(groupId);
    }

    @Nonnull
    @Override
    public List<String> getProjectsForUser(final String username, final String access) {
        log.info("Getting projects with {} access for user {}", access, username);
        final String cacheId = getCacheIdForUserProjectAccess(username, access);

        if (has(cacheId)) {
            log.debug("Found a cache entry for user '{}' '{}' access with ID: {}", username, access, cacheId);
            return getCachedList(cacheId);
        }
        return updateUserProjectAccess(username, access, cacheId);
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
        final String groupId = _template.query(QUERY_GET_GROUP_FOR_USER_AND_TAG, checkUser(username).addValue("tag", tag), new ResultSetExtractor<String>() {
            @Override
            public String extractData(final ResultSet results) throws DataAccessException, SQLException {
                return results.next() ? results.getString("id") : null;
            }
        });
        return StringUtils.isNotBlank(groupId) ? get(groupId) : null;
    }

    @Override
    public Date getUserLastUpdateTime(final UserI user) {
        return getUserLastUpdateTime(user.getUsername());
    }

    @Override
    public Date getUserLastUpdateTime(final String username) {
        try {
            final List<String> cacheIds = getGroupIdsForUser(username);
            cacheIds.addAll(getCacheIdsForUsername(username));
            if (cacheIds.isEmpty()) {
                return new Date();
            }
            if (log.isDebugEnabled()) {
                log.debug("Found {} cache entries related to user {}: {}", cacheIds.size(), username, StringUtils.join(cacheIds, ", "));
            }
            final long lastUpdateTime = Collections.max(Lists.transform(cacheIds, new Function<String, Long>() {
                @Override
                public Long apply(@Nullable final String cacheId) {
                    final Date lastUpdateTime = getCacheEntryLastUpdateTime(cacheId);
                    log.trace("User {} cache entry '{}' last updated: {}", username, cacheId, lastUpdateTime == null ? "null" : lastUpdateTime.getTime());
                    return lastUpdateTime == null ? 0L : lastUpdateTime.getTime();
                }
            }));
            log.debug("Found latest cache entry last updated time for user {}: {}", username, lastUpdateTime);
            return new Date(lastUpdateTime);
        } catch (UserNotFoundException ignored) {
            log.warn("Someone requested the cache entry last updated time for user {} but that user wasn't found", username);
            return new Date();
        }
    }

    @Override
    public boolean canInitialize() {
        try {
            if (_listener == null) {
                return false;
            }
            final boolean doesUserGroupTableExists            = _helper.tableExists("xdat_usergroup");
            final boolean isXftManagerComplete                = XFTManager.isComplete();
            final boolean isDatabasePopulateOrUpdateCompleted = XDATServlet.isDatabasePopulateOrUpdateCompleted();
            log.info("User group table {}, XFTManager initialization completed {}, database populate or updated completed {}", doesUserGroupTableExists, isXftManagerComplete, isDatabasePopulateOrUpdateCompleted);
            return doesUserGroupTableExists && isXftManagerComplete && isDatabasePopulateOrUpdateCompleted;
        } catch (SQLException e) {
            log.info("Got an SQL exception checking for xdat_usergroup table", e);
            return false;
        }
    }

    @Async
    @Override
    public Future<Boolean> initialize() {
        final LapStopWatch stopWatch = LapStopWatch.createStarted(log, Level.INFO);

        // This clears out any group initialization requests that may be left in the database from earlier starts.
        _template.update("DELETE FROM activemq_msgs WHERE container LIKE '%initializeGroupRequest'", EmptySqlParameterSource.INSTANCE);

        final int tags = initializeTags();
        stopWatch.lap("Processed {} tags", tags);

        final List<String> groupIds = _template.queryForList(QUERY_ALL_GROUPS, EmptySqlParameterSource.INSTANCE, String.class);
        _listener.setGroupIds(groupIds);
        stopWatch.lap("Initialized listener of type {} with {} tags", _listener.getClass().getName(), tags);

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

        getGuest();
        updateGuestBrowseableElementDisplays();

        return new AsyncResult<>(_initialized = true);
    }

    @Override
    public boolean isInitialized() {
        return _initialized;
    }

    @Override
    public Map<String, String> getInitializationStatus() {
        final Map<String, String> status = new HashMap<>();
        if (_listener == null) {
            status.put("message", "No listener registered, so no status to report.");
            return status;
        }

        final Set<String> processed      = _listener.getProcessed();
        final int         processedCount = processed.size();
        final Set<String> unprocessed    = _listener.getUnprocessed();
        final Date        start          = _listener.getStart();

        status.put("start", DATE_FORMAT.format(start));
        status.put("processedCount", Integer.toString(processedCount));
        status.put("processed", StringUtils.join(processed, ", "));

        if (unprocessed.isEmpty()) {
            final Date   completed = _listener.getCompleted();
            final String duration  = DurationFormatUtils.formatPeriodISO(start.getTime(), completed.getTime());

            status.put("completed", DATE_FORMAT.format(completed));
            status.put("duration", duration);
            status.put("message", "Cache initialization is complete. Processed " + processedCount + " groups in " + duration);
            return status;
        }

        final Date   now              = new Date();
        final String duration         = DurationFormatUtils.formatPeriodISO(start.getTime(), now.getTime());
        final int    unprocessedCount = unprocessed.size();

        status.put("unprocessedCount", Integer.toString(unprocessedCount));
        status.put("unprocessed", StringUtils.join(unprocessed, ", "));
        status.put("current", DATE_FORMAT.format(now));
        status.put("duration", duration);
        status.put("message", "Cache initialization is on-going, with " + processedCount + " groups processed and " + unprocessedCount + " groups remaining, time elapsed so far is " + duration);
        return status;
    }

    /**
     * This method retrieves the group with the specified group ID and puts it in the cache. This method
     * does <i>not</i> check to see if the group is already in the cache! This is primarily for use during
     * cache initialization and shouldn't be used for routine access.
     *
     * @param groupId The ID or alias of the group to retrieve.
     *
     * @return The group object for the specified ID if it exists, null otherwise.
     */
    @Override
    public UserGroupI cacheGroup(final String groupId) {
        log.debug("Initializing group {}", groupId);
        try {
            return cacheGroup(new UserGroup(groupId, _template));
        } catch (ItemNotFoundException e) {
            log.info("Someone tried to get the user group {}, but a group with that ID doesn't exist.", groupId);
            return null;
        }
    }

    @Override
    public void registerListener(final Listener listener) {
        _listener = listener;
    }

    @Override
    public Listener getListener() {
        return _listener;
    }

    @Override
    protected boolean handleEventImpl(final XftItemEventI event) {
        switch (event.getXsiType()) {
            case XnatProjectdata.SCHEMA_ELEMENT_NAME:
                return handleProjectEvents(event);

            case XnatSubjectdata.SCHEMA_ELEMENT_NAME:
                return handleSubjectEvents(event);

            case XdatUsergroup.SCHEMA_ELEMENT_NAME:
                return handleGroupRelatedEvents(event);

            case XdatElementSecurity.SCHEMA_ELEMENT_NAME:
                return handleElementSecurityEvents(event);

            default:
                // This is always some type of experiment.
                return handleExperimentEvents(event);
        }
    }

    private synchronized List<String> updateUserProjectAccess(final String username, final String access, final String cacheId) {
        final String query;
        switch (access) {
            case SecurityManager.READ:
                query = QUERY_READABLE_PROJECTS;
                break;
            case SecurityManager.EDIT:
                query = QUERY_EDITABLE_PROJECTS;
                break;
            case SecurityManager.DELETE:
                query = QUERY_OWNED_PROJECTS;
                break;
            default:
                throw new RuntimeException("Unknown access level '" + access + "'. Must be one of " + SecurityManager.READ + ", " + SecurityManager.EDIT + ", or " + SecurityManager.DELETE + ".");
        }
        final List<String> projectIds = _template.queryForList(query, new MapSqlParameterSource("usernames", Arrays.asList("guest", username)), String.class);
        cacheObject(cacheId, projectIds);
        return projectIds;
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
        final List<String> groups = getGroupIdsForProject(tag);

        // If this is empty, then the tag doesn't exist and we'll just put DOES_NOT_EXIST there.
        if (groups.isEmpty()) {
            log.info("Someone tried to get groups for the tag {}, but there are no groups with that tag.", tag);
            return Collections.emptyList();
        } else {
            log.debug("Cached tag {} for {} groups: {}", tag, groups.size(), StringUtils.join(groups, ", "));
            cacheObject(cacheId, groups);
            return groups;
        }
    }

    private synchronized List<UserGroupI> cacheGroups(final List<UserGroupI> groups) {
        log.debug("Caching {} groups", groups.size());
        for (final UserGroupI group : groups) {
            if (group != null) {
                cacheGroup(group);
            }
        }
        return groups;
    }

    private synchronized UserGroupI cacheGroup(final UserGroupI group) {
        final String groupId = group.getId();
        cacheObject(groupId, group);
        log.debug("Retrieved and cached the group for the ID {}", groupId);
        return group;
    }

    private boolean handleProjectEvents(final XftItemEventI event) {
        final String         xsiType    = event.getXsiType();
        final String         id         = event.getId();
        final String         action     = event.getAction();
        final Map<String, ?> properties = event.getProperties();

        try {
            switch (action) {
                case CREATE:
                    log.debug("New project created with ID {}, caching new instance", xsiType, id);
                    updateUserReadableCounts(Lists.transform(getCacheIdsForUserElements(), FUNCTION_CACHE_IDS_TO_USERNAMES));
                    updateActionElementDisplays(Lists.transform(getCacheIdsForActions(), FUNCTION_CACHE_IDS_TO_USERNAMES));
                    updateTotalCounts();
                    return !cacheGroups(getGroups(xsiType, id)).isEmpty();

                case UPDATE:
                    log.debug("The {} object {} was updated, caching updated instance", xsiType, id);
                    if (properties.containsKey("accessibility")) {
                        final String accessibility = (String) properties.get("accessibility");
                        if (StringUtils.equalsAnyIgnoreCase(accessibility, "private", "protected", "public")) {
                            // Just update existing user element displays
                            final List<String> cacheIds = getCacheIdsForActions();
                            cacheIds.addAll(getCacheIdsForUserElements());
                            updateGuestBrowseableElementDisplays();
                            updateUserReadableCounts(Sets.newHashSet(Iterables.filter(Lists.transform(cacheIds, FUNCTION_CACHE_IDS_TO_USERNAMES), Predicates.notNull())));
                            return !cacheGroups(getGroups(xsiType, id)).isEmpty();
                        } else {
                            log.warn("The project {}'s accessibility setting was updated to an invalid value: {}. Must be one of private, protected, or public.", id, accessibility);
                        }
                    }
                    break;

                case XftItemEventI.DELETE:
                    log.debug("The {} {} was deleted, removing related instances from cache", xsiType, id);
                    final String cacheId = getCacheIdForProject(id);
                    updateGuestBrowseableElementDisplays();
                    updateUserReadableCounts(this.<String>getCachedSet(cacheId));
                    updateTotalCounts();
                    getCache().evict(cacheId);
                    break;

                default:
                    log.warn("I was informed that the '{}' action happened to the project with ID '{}'. I don't know what to do with this action.", action, xsiType, id);
                    break;
            }
        } catch (ItemNotFoundException e) {
            log.warn("While handling action {}, I couldn't find a group for type {} ID {}.", action, xsiType, id);
        }

        return false;
    }

    private boolean handleSubjectEvents(final XftItemEventI event) {
        final String action = event.getAction();
        log.debug("Handling subject {} event for {} {}", XftItemEventI.ACTIONS.get(action), event.getXsiType(), event.getId());
        final Set<String> projectIds = new HashSet<>();
        switch (action) {
            case CREATE:
                projectIds.add(_template.queryForObject(QUERY_GET_SUBJECT_PROJECT, new MapSqlParameterSource("subjectId", event.getId()), String.class));
                updateTotalCounts();
                break;

            case SHARE:
                projectIds.add((String) event.getProperties().get("target"));
                break;

            case MOVE:
                projectIds.add((String) event.getProperties().get("origin"));
                projectIds.add((String) event.getProperties().get("target"));
                break;

            case XftItemEventI.DELETE:
                projectIds.add((String) event.getProperties().get("target"));
                updateTotalCounts();
                break;

            default:
                log.warn("I was informed that the '{}' action happened to subject '{}'. I don't know what to do with this action.", action, event.getId());
        }
        if (projectIds.isEmpty()) {
            return false;
        }
        final Set<String> users = getProjectUsers(projectIds);
        for (final String username : users) {
            updateUserReadableCounts(username);
        }
        return true;
    }

    private boolean handleGroupRelatedEvents(final XftItemEventI event) {
        final String xsiType = event.getXsiType();
        final String id      = event.getId();
        final String action  = event.getAction();

        try {
            switch (action) {
                case CREATE:
                    log.debug("New {} created with ID {}, caching new instance", xsiType, id);
                    return !cacheGroups(getGroups(xsiType, id)).isEmpty();

                case UPDATE:
                    log.debug("The {} object {} was updated, caching updated instance", xsiType, id);
                    return !cacheGroups(getGroups(xsiType, id)).isEmpty();

                case XftItemEventI.DELETE:
                    log.debug("The {} {} was deleted, removing related instances from cache", xsiType, id);
                    final Set<String> usernames = new HashSet<>();
                    switch (xsiType) {
                        case XnatProjectdata.SCHEMA_ELEMENT_NAME:
                            final String projectCacheId = getCacheIdForProject(id);
                            usernames.addAll(this.<String>getCachedSet(projectCacheId));
                            getCache().evict(projectCacheId);
                            break;

                        case XdatUsergroup.SCHEMA_ELEMENT_NAME:
                            // Not sure what to do for deleted group...
                            break;
                    }
                    updateUserReadableCounts(usernames);
                    return true;

                default:
                    log.warn("I was informed that the '{}' action happened to the {} object with ID '{}'. I don't know what to do with this action.", action, xsiType, id);
            }
        } catch (ItemNotFoundException e) {
            log.warn("While handling action {}, I couldn't find a group for type {} ID {}.", action, xsiType, id);
        }
        return false;
    }

    private boolean handleElementSecurityEvents(final XftItemEventI event) {
        final String dataType = event.getId();

        log.debug("Handling {} event for '{}' ID {}. Updating guest browseable element displays...", event.getAction(), event.getXsiType(), dataType);
        final Map<String, ElementDisplay> displays = updateGuestBrowseableElementDisplays();

        if (log.isTraceEnabled()) {
            log.trace("Got back {} browseable element displays for guest user after refresh: {}", displays.size(), StringUtils.join(displays.keySet(), ", "));
        }

        log.debug("Evicting all action and user element cache IDs");
        for (final String cacheId : Iterables.concat(getCacheIdsForActions(), getCacheIdsForUserElements())) {
            log.trace("Evicting cache entry with ID '{}'", cacheId);
            getCache().evict(cacheId);
        }

        final List<String> groupIds = getGroupIdsForDataType(dataType);
        log.debug("Found {} groups that reference the '{}' data type, updating cache entries for: {}", groupIds.size(), dataType, StringUtils.join(groupIds, ", "));
        for (final String groupId : groupIds) {
            log.trace("Updating data type '{}' for the group '{}'", dataType, groupId);
            updateDataTypeForCachedGroup(groupId, dataType);
        }

        return true;
    }

    private boolean handleExperimentEvents(final XftItemEventI event) {
        final String action  = event.getAction();
        final String xsiType = event.getXsiType();
        log.debug("Handling experiment {} event for {} {}", XftItemEventI.ACTIONS.get(action), xsiType, event.getId());
        final String target, origin;
        switch (action) {
            case CREATE:
                target = _template.queryForObject(QUERY_GET_EXPERIMENT_PROJECT, new MapSqlParameterSource("experimentId", event.getId()), String.class);
                origin = null;
                break;

            case SHARE:
                target = (String) event.getProperties().get("target");
                origin = null;
                break;

            case MOVE:
                origin = (String) event.getProperties().get("origin");
                target = (String) event.getProperties().get("target");
                break;

            case XftItemEventI.DELETE:
                target = (String) event.getProperties().get("target");
                origin = null;
                break;

            default:
                log.warn("I was informed that the '{}' action happened to experiment '{}' with ID '{}'. I don't know what to do with this action.", action, xsiType, event.getId());
                return false;
        }

        final Map<String, ElementDisplay> displays = getGuestBrowseableElementDisplays();
        log.debug("Found {} elements for guest user: {}", displays.size(), StringUtils.join(displays.keySet(), ", "));

        // If the data type of the experiment isn't in the guest list AND the target project is public,
        // OR if the origin project is both specified and public (meaning the data type might be REMOVED
        // from the guest browseable element displays), then we update the guest browseable element displays.
        final boolean hasEventXsiType        = displays.containsKey(xsiType);
        final boolean isTargetProjectPublic  = Permissions.isProjectPublic(_template, target);
        final boolean hasOriginProject       = StringUtils.isNotBlank(origin);
        final boolean isMovedFromPublicToNon = !isTargetProjectPublic && hasOriginProject && Permissions.isProjectPublic(_template, origin);

        // We need to add the XSI type if guest doesn't already have it and the target project is public.
        final boolean needsPublicXsiTypeAdded = !hasEventXsiType && isTargetProjectPublic;

        // We need to check if the XSI type should be removed if guest has XSI type and item was moved from public to non-public.
        final boolean needsXsiTypeChecked = hasEventXsiType && isMovedFromPublicToNon;

        if (needsPublicXsiTypeAdded || needsXsiTypeChecked) {
            if (needsPublicXsiTypeAdded) {
                log.debug("Updating guest browseable element displays: guest doesn't have the event XSI type '{}' and the target project {} is public.", xsiType, target);
            } else {
                log.debug("Updating guest browseable element displays: guest has the event XSI type '{}' and item was moved from public project {} to non-public project {}.", xsiType, origin, target);
            }
            updateGuestBrowseableElementDisplays();
        } else {
            log.debug("Not updating guest browseable element displays: guest {} '{}' and {}",
                      hasEventXsiType ? "already has the event XSI type " : "doesn't have the event XSI type",
                      xsiType,
                      isTargetProjectPublic ? "target project is public" : "target project is not public");
        }
        updateUserReadableCounts(hasOriginProject ? getProjectUsers(target) : getProjectUsers(target, origin));
        if (StringUtils.equalsAny(action, CREATE, XftItemEventI.DELETE)) {
            updateTotalCounts();
        }
        return true;
    }

    private void handleCacheRemoveEvent(final Ehcache cache, final Element element, final String event) {
        if (isGroupsAndPermissionsCacheEvent(cache)) {
            if (element == null) {
                log.debug("Got a {} event for cache {}, no specific element affected", event, cache.getName());
                return;
            }
            final Object objectValue = element.getObjectValue();
            log.debug("Got a {} event for cache {} on ID {} with value of type {}", event, cache.getName(), element.getObjectKey(), objectValue != null ? objectValue.getClass().getName() : "<null>");
        }
    }

    private void updateDataTypeForCachedGroup(final String groupId, final String dataType) {
        if (has(groupId)) {
            // If the group is already cached, we can just add any permissions for the group and data type to the existing entry.
            log.trace("Updating cache entry for group {} with new permissions for data type", groupId, dataType);
            final UserGroup group = (UserGroup) getCachedGroup(groupId);
            group.addPermissionCriteria(Lists.transform(Lists.newArrayList(Iterables.filter(_template.queryForList(QUERY_GROUP_DATATYPE_PERMISSIONS, new MapSqlParameterSource("groupId", groupId).addValue("dataType", dataType)), new Predicate<Map<String, Object>>() {
                @Override
                public boolean apply(@Nullable final Map<String, Object> definition) {
                    // Use having read and active elements as a proxy for not actually being populated properly.
                    return definition != null && definition.get(READ_ELEMENT) != null && definition.get(ACTIVATE_ELEMENT) != null;
                }
            })), new Function<Map<String, Object>, PermissionCriteriaI>() {
                @Override
                public PermissionCriteriaI apply(final Map<String, Object> properties) {
                    return new PermissionCriteria(dataType, properties);
                }
            }));
        } else {
            // Otherwise, cache the group like it's new because, really, it is.
            cacheGroup(groupId);
        }
    }

    private Map<String, ElementAccessManager> getElementAccessManagers(final String username) {
        if (StringUtils.isBlank(username)) {
            return Collections.emptyMap();
        }
        final String cacheId = getCacheIdForUserElementAccessManagers(username);
        if (has(cacheId)) {
            log.debug("Found a cache entry for user '{}' element access managers by ID '{}'", username, cacheId);
            return getCachedMap(cacheId);
        }
        return updateElementAccessManagers(username, cacheId);
    }

    private void updateGuestElementAccessManagers() {
        updateElementAccessManagers(GUEST_USERNAME, getCacheIdForUserElementAccessManagers(GUEST_USERNAME));
    }

    private Map<String, ElementAccessManager> updateElementAccessManagers(final String username, final String cacheId) {
        final Map<String, ElementAccessManager> managers = ElementAccessManager.initialize(_template, QUERY_USER_PERMISSIONS, new MapSqlParameterSource("username", username));
        log.debug("Found {} element access managers for user '{}', caching with ID {}: {}", managers.size(), username, cacheId, managers.isEmpty() ? "N/A" : StringUtils.join(managers.keySet(), ", "));

        cacheObject(cacheId, managers);

        return managers;
    }

    private Map<String, ElementDisplay> updateBrowseableElementDisplays(final UserI user, final String cacheId) {
        final String            username = user.getUsername();
        final Map<String, Long> counts   = updateReadableCounts(user);
        log.debug("Found {} readable counts for user {}: {}", counts.size(), username, counts);

        try {
            final Map<String, ElementDisplay> browseable            = new HashMap<>();
            final List<ElementDisplay>        actionElementDisplays = updateActionElementDisplays(user, SecurityManager.READ);
            if (log.isDebugEnabled()) {
                log.debug("Found {} readable action element displays for user {}: {}", actionElementDisplays.size(), username, StringUtils.join(Lists.transform(actionElementDisplays, FUNCTION_ELEMENT_DISPLAY_TO_STRING), ", "));
            }
            for (final ElementDisplay elementDisplay : actionElementDisplays) {
                final String elementName = elementDisplay.getElementName();
                log.debug("Evaluating element display {}", elementName);
                final boolean isBrowseableElement = ElementSecurity.IsBrowseableElement(elementName);
                final boolean countsContainsKey   = counts.containsKey(elementName);
                final boolean hasOneOrMoreElement = countsContainsKey && counts.get(elementName) > 0;
                if (isBrowseableElement && countsContainsKey && hasOneOrMoreElement) {
                    log.debug("Adding element display {} to cache entry {}", elementName, cacheId);
                    browseable.put(elementName, elementDisplay);
                } else {
                    log.debug("Did not add element display {}: {}, {}", elementName, isBrowseableElement ? "browseable" : "not browseable", countsContainsKey ? "counts contains key" : "counts does not contain key", hasOneOrMoreElement ? "counts has one or more elements of this type" : "counts does not have any elements of this type");
                }
            }

            log.info("Adding {} element displays to cache entry {}", browseable.size(), cacheId);
            cacheObject(cacheId, browseable);
            return browseable;
        } catch (ElementNotFoundException e) {
            if (!_missingElements.containsKey(e.ELEMENT)) {
                log.warn("Element '{}' not found. This may be a data type that was installed previously but can't be located now. This warning will only be displayed once. Set logging level to DEBUG to see a message each time this occurs for each element, along with a count of the number of times the element was referenced.", e.ELEMENT);
                _missingElements.put(e.ELEMENT, 1L);
            } else {
                final long count = _missingElements.get(e.ELEMENT) + 1;
                _missingElements.put(e.ELEMENT, count);
                if (log.isDebugEnabled()) {
                    log.debug("Element '{}' not found. This element has been referenced {} times.", e.ELEMENT, count);
                }
            }
        } catch (XFTInitException e) {
            log.error("There was an error initializing or accessing XFT", e);
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
        }

        log.info("No browseable element displays found for user {}", username);
        return Collections.emptyMap();
    }

    private void updateTotalCounts() {
        _totalCounts.clear();
        final Long projectCount = _template.queryForObject("SELECT COUNT(*) FROM xnat_projectData", EmptySqlParameterSource.INSTANCE, Long.class);
        _totalCounts.put(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectCount);
        final Long subjectCount = _template.queryForObject("SELECT COUNT(*) FROM xnat_subjectData", EmptySqlParameterSource.INSTANCE, Long.class);
        _totalCounts.put(XnatSubjectdata.SCHEMA_ELEMENT_NAME, subjectCount);
        final List<Map<String, Object>> elementCounts = _template.queryForList("SELECT element_name, COUNT(ID) AS count FROM xnat_experimentData expt LEFT JOIN xdat_meta_element xme ON expt.extension=xme.xdat_meta_element_id GROUP BY element_name", EmptySqlParameterSource.INSTANCE);
        for (final Map<String, Object> elementCount : elementCounts) {
            _totalCounts.put((String) elementCount.get("element_name"), (Long) elementCount.get("count"));
        }
    }

    private void updateActionElementDisplays(final Collection<String> users) {
        for (final String username : users) {
            for (final String action : ALL_ACTIONS) {
                updateActionElementDisplays(username, action);
            }
        }
    }

    private List<ElementDisplay> updateActionElementDisplays(final UserI user, final String action) {
        return updateActionElementDisplays(user.getUsername(), action);
    }

    private List<ElementDisplay> updateActionElementDisplays(final String username, final String action) {
        final Multimap<String, ElementDisplay> elementDisplays = ArrayListMultimap.create();
        try {
            final List<ElementSecurity> securities = ElementSecurity.GetSecureElements();
            if (log.isDebugEnabled()) {
                log.debug("Evaluating {} element security objects: {}", securities.size(), StringUtils.join(Lists.transform(securities, FUNCTION_ELEMENT_SECURITY_TO_STRING), ", "));
            }
            for (final ElementSecurity elementSecurity : securities) {
                try {
                    final SchemaElement schemaElement = elementSecurity.getSchemaElement();
                    if (schemaElement != null) {
                        log.debug("Evaluating schema element {}", schemaElement.getFullXMLName());
                        if (schemaElement.hasDisplay()) {
                            log.debug("Schema element {} has a display", schemaElement.getFullXMLName());
                            if (Permissions.canAny(username, elementSecurity.getElementName(), action)) {
                                log.debug("User {} can {} schema element {}", username, action, schemaElement.getFullXMLName());
                                final ElementDisplay elementDisplay = schemaElement.getDisplay();
                                if (elementDisplay != null) {
                                    log.debug("Adding element display {} to action {} for user {}", elementDisplay.getElementName(), action, username);
                                    elementDisplays.put(action, elementDisplay);
                                }
                            } else {
                                log.debug("User {} can not {} schema element {}", username, action, schemaElement.getFullXMLName());
                            }
                        } else {
                            log.debug("Schema element {} does not have a display, rejecting", schemaElement.getFullXMLName());
                        }
                    } else {
                        log.warn("Element '{}' not found. This may be a data type that was installed previously but can't be located now.", elementSecurity.getElementName());
                    }
                } catch (ElementNotFoundException e) {
                    log.warn("Element '{}' not found. This may be a data type that was installed previously but can't be located now.", e.ELEMENT);
                } catch (Exception e) {
                    log.error("An exception occurred trying to retrieve a secure element schema", e);
                }
            }
        } catch (Exception e) {
            log.error("An error occurred trying to retrieve the list of secure elements. Proceeding but things probably won't go well from here on out.", e);
        }

        try {
            for (final ElementSecurity elementSecurity : ElementSecurity.GetInSecureElements()) {
                try {
                    final SchemaElement schemaElement = elementSecurity.getSchemaElement();
                    if (schemaElement.hasDisplay()) {
                        elementDisplays.put(action, schemaElement.getDisplay());
                    }
                } catch (ElementNotFoundException e) {
                    log.warn("Element '{}' not found. This may be a data type that was installed previously but can't be located now.", e.ELEMENT);
                } catch (Exception e) {
                    log.error("An exception occurred trying to retrieve an insecure element schema", e);
                }
            }
        } catch (Exception e) {
            log.error("An error occurred trying to retrieve the list of insecure elements. Proceeding but things probably won't go well from here on out.", e);
        }

        for (final String foundAction : elementDisplays.keySet()) {
            final String               actionCacheId         = getCacheIdForActionElements(username, foundAction);
            final List<ElementDisplay> actionElementDisplays = new ArrayList<>(elementDisplays.get(foundAction));
            log.info("Caching {} elements for action {} for user {} with cache ID {}", actionElementDisplays.size(), action, username, actionCacheId);
            if (log.isDebugEnabled()) {
                log.debug("Element displays for action {} for user {} include: {}", action, username, StringUtils.join(Lists.transform(actionElementDisplays, FUNCTION_ELEMENT_DISPLAY_TO_STRING), ", "));
            }
            cacheObject(actionCacheId, actionElementDisplays);
        }

        return ImmutableList.copyOf(elementDisplays.get(action));
    }

    private void updateUserReadableCounts(final Collection<String> usernames) {
        for (final String username : usernames) {
            updateUserReadableCounts(username);
        }
    }

    private void updateUserReadableCounts(final String username) {
        final String cacheId = getCacheIdForUserElements(username, READABLE);
        log.debug("Retrieving readable counts for user {} through cache ID {}", username, cacheId);

        // Check whether the user has readable counts and browseable elements cached. We only need to refresh
        // for those who have them cached.
        if (has(cacheId)) {
            try {
                log.debug("Found a cache entry for user '{}' readable counts by ID '{}', updating cache entry", username, cacheId);
                final XDATUser user = new XDATUser(username);
                updateReadableCounts(user, cacheId);
                updateBrowseableElementDisplays(user, getCacheIdForUserElements(username, BROWSEABLE));
            } catch (UserNotFoundException e) {
                log.warn("Got a user not found exception for username '{}', which is weird because this user has a cache entry.", username, e);
            } catch (UserInitException e) {
                log.error("An error occurred trying to retrieve the user '{}'", username, e);
            } catch (Exception e) {
                log.error("An unexpected error occurred trying to retrieve the readable counts for user '{}'", username, e);
            }
        }
    }

    private Map<String, Long> updateReadableCounts(final UserI user) {
        return updateReadableCounts(user, getCacheIdForUserElements(user.getUsername(), READABLE));
    }

    private Map<String, Long> updateReadableCounts(final UserI user, final String cacheId) {
        final String username = user.getUsername();
        try {
            try {

                final Map<String, Long> readableCounts = new HashMap<>();
                readableCounts.putAll(getUserReadableCount(user, "xnat:projectData", "xnat:projectData/ID"));
                readableCounts.putAll(getUserReadableCount(user, "wrk:workflowData", "wrk:workflowData/ID"));
                readableCounts.putAll(getUserReadableCount(user, "xnat:subjectData", "xnat:subjectData/ID"));

                if (log.isDebugEnabled()) {
                    log.debug("Caching the following readable element counts for user {} with cache ID {}: {}", username, cacheId, getDisplayForReadableCounts(readableCounts));
                }
                cacheObject(cacheId, readableCounts);
                return readableCounts;
            } catch (org.nrg.xdat.exceptions.IllegalAccessException e) {
                //not a member of anything
                log.info("USER: {} doesn't have access to any project data.", username);
            }
        } catch (SQLException e) {
            log.error("An error occurred in the SQL for retrieving readable counts for the  user {}", username, e);
        } catch (DBPoolException e) {
            log.error("A database error occurred when trying to retrieve readable counts for the  user {}", username, e);
        } catch (Exception e) {
            log.error("An unknown error occurred when trying to retrieve readable counts for the  user {}", username, e);
        }

        log.info("No readable elements found for user {}", username);
        return Collections.emptyMap();
    }

    private Map<String, ElementDisplay> getGuestBrowseableElementDisplays() {
        if (has(GUEST_CACHE_ID)) {
            return getCachedMap(GUEST_CACHE_ID);
        }
        return updateGuestBrowseableElementDisplays();
    }

    private Map<String, ElementDisplay> updateGuestBrowseableElementDisplays() {
        log.debug("Updating guest browseable element displays, clearing local cache, updating element access managers, and updating browseable element displays");
        _guest.clearLocalCache();
        updateGuestElementAccessManagers();
        return updateBrowseableElementDisplays(_guest, GUEST_CACHE_ID);
    }

    private Map<String, Long> getUserReadableCount(final UserI user, final String dataType, final String dataTypeIdField) throws Exception {
        final Map<String, Long> readableCounts = new HashMap<>();

        final QueryOrganizer organizer = new QueryOrganizer(dataType, user, ViewManager.ALL);
        organizer.addField(dataTypeIdField);

        final String dataTypeCountQuery = "SELECT COUNT(*) AS data_type_count FROM (" + organizer.buildQuery() + ") SEARCH";
        final Long   count              = _template.queryForObject(dataTypeCountQuery, EmptySqlParameterSource.INSTANCE, Long.class);
        log.debug("Executed count query for data type '{}' on ID field '{}' and found {} instances: \"{}\"", dataType, dataTypeIdField, count, dataTypeCountQuery);
        readableCounts.put(dataType, count);

        // Experiment types are updated when the subject is updated.
        if (StringUtils.equalsIgnoreCase(dataType, XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
            final String subquery = StringUtils.replaceEach(organizer.buildQuery(),
                                                            new String[]{organizer.translateXMLPath("xnat:subjectData/ID"), "xnat_subjectData", "xnat_projectParticipant", "subject_id"},
                                                            REPLACEMENT_LIST);

            final String            elementCountQuery = "SELECT element_name, COUNT(*) AS element_count FROM (" + subquery + ") SEARCH LEFT JOIN xnat_experimentData expt ON search.id = expt.id LEFT JOIN xdat_meta_element xme ON expt.extension = xme.xdat_meta_element_id GROUP BY element_name";
            final Map<String, Long> results           = _template.query(elementCountQuery, EmptySqlParameterSource.INSTANCE, ELEMENT_COUNT_EXTRACTOR);
            if (log.isDebugEnabled()) {
                final int size = results.size();
                log.debug("Executed element count query and found {} elements: {}\nQuery: \"{}\"", size, size > 0 ? results : "N/A", elementCountQuery);
            }
            readableCounts.putAll(results);
        }

        return readableCounts;
    }

    private List<UserGroupI> getGroups(final String type, final String id) throws ItemNotFoundException {
        switch (type) {
            case XnatProjectdata.SCHEMA_ELEMENT_NAME:
                return getGroupsForTag(id);

            case XdatUsergroup.SCHEMA_ELEMENT_NAME:
                return Collections.<UserGroupI>singletonList(new UserGroup(id, _template));

            case XdatElementSecurity.SCHEMA_ELEMENT_NAME:
                final List<String> groupIds = getGroupIdsForDataType(id);
                return cacheGroups(Lists.transform(groupIds, new Function<String, UserGroupI>() {
                    @Override
                    public UserGroupI apply(final String groupId) {
                        try {
                            return new UserGroup(groupId, _template);
                        } catch (ItemNotFoundException e) {
                            log.warn("Somehow didn't find a usergroup that should exist: {}", groupId, e);
                            return null;
                        }
                    }
                }));
        }
        return Collections.emptyList();
    }

    private List<String> getGroupIdsForDataType(final String dataType) {
        return _template.queryForList(QUERY_GET_GROUPS_FOR_DATATYPE, new MapSqlParameterSource("dataType", dataType), String.class);
    }

    private List<String> getGroupIdsForProject(final String projectId) {
        return _template.queryForList(QUERY_GET_GROUPS_FOR_TAG, new MapSqlParameterSource("tag", projectId), String.class);
    }

    private List<String> getUserProjects(final String username) {
        final Set<String> projects = new HashSet<>();
        projects.addAll(_template.queryForList(QUERY_GET_PROJECTS_FOR_USER, new MapSqlParameterSource("username", username), String.class));
        projects.addAll(Permissions.getAllPublicProjects(_template));
        projects.addAll(Permissions.getAllProtectedProjects(_template));
        return new ArrayList<>(projects);
    }

    private int initializeTags() {
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

    private Set<String> getProjectUsers(final String... projectIds) {
        return getProjectUsers(Arrays.asList(projectIds));
    }

    private Set<String> getProjectUsers(final Collection<String> projectIds) {
        return projectIds.isEmpty() ? Collections.<String>emptySet() : new HashSet<>(_template.queryForList(QUERY_GET_USERS_FOR_PROJECTS, new MapSqlParameterSource("projectIds", projectIds), String.class));
    }

    private List<String> getTagGroups(final String cacheId) {
        return getCachedList(cacheId);
    }

    private List<String> getGroupIdsForUser(final String username) throws UserNotFoundException {
        final String cacheId = getCacheIdForUserGroups(username);
        if (has(cacheId)) {
            log.info("Found cached groups list for user '{}' with cache ID '{}'", username, cacheId);
            return getCachedList(cacheId);
        }

        final List<String> groupIds = _template.queryForList(QUERY_GET_GROUPS_FOR_USER, checkUser(username), String.class);
        cacheObject(cacheId, groupIds);
        return groupIds;
    }

    /**
     * Checks whether the users exists. If not, this throws the {@link UserNotFoundException}. Otherwise it returns
     * a parameter source containing the username that can be used in subsequent queries.
     *
     * @param username The user to test.
     *
     * @return A parameter source containing the username parameter.
     *
     * @throws UserNotFoundException If the user doesn't exist.
     */
    private MapSqlParameterSource checkUser(final String username) throws UserNotFoundException {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("username", username);

        // If the user isn't in the check map OR the user is in the check map but is set as not existing...
        if (!_userChecks.containsKey(username) || !_userChecks.get(username)) {
            // See if the user exists now. The non-existent user existing should be updated with the add user event,
            // but we don't have a clearly defined handler for that yet.
            _userChecks.put(username, _template.queryForObject(QUERY_CHECK_USER_EXISTS, parameters, Boolean.class));
        }
        if (!_userChecks.get(username)) {
            throw new UserNotFoundException(username);
        }
        return parameters;
    }

    private UserI getGuest() {
        if (_guest == null) {
            log.debug("No guest user initialized, trying to retrieve now.");
            try {
                final UserI guest = Users.getGuest();
                if (guest instanceof XDATUser) {
                    _guest = (XDATUser) guest;
                } else {
                    _guest = new XDATUser(guest.getUsername());
                }
            } catch (UserNotFoundException e) {
                log.error("Got a user name not found exception for the guest user which is very strange.", e);
            } catch (UserInitException e) {
                log.error("Got a user init exception for the guest user which is very unfortunate.", e);
            }
        }
        return _guest;
    }

    private boolean isGuest(final String username) {
        return _guest != null ? StringUtils.equalsIgnoreCase(_guest.getUsername(), username) : StringUtils.equalsIgnoreCase(GUEST_USERNAME, username);
    }

    private List<UserGroupI> getUserGroupList(final List groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new ArrayList<>();
        }

        return newArrayList(Iterables.filter(Iterables.transform(Iterables.filter(groupIds, String.class), new Function<String, UserGroupI>() {
            @Nullable
            @Override
            public UserGroupI apply(@Nullable final String groupId) {
                return get(groupId);
            }
        }), Predicates.notNull()));
    }

    private UserGroupI getCachedGroup(final String cacheId) {
        return getCachedObject(cacheId, UserGroupI.class);
    }

    private List<String> getCacheIdsForActions() {
        return getCacheIdsForPrefix(ACTION_PREFIX);
    }

    private List<String> getCacheIdsForUserElements() {
        return getCacheIdsForPrefix(USER_ELEMENT_PREFIX);
    }

    private List<String> getCacheIdsForUserElements(final String username) {
        return getCacheIdsForPrefix(USER_ELEMENT_PREFIX, username);
    }

    private List<String> getCacheIdsForPrefix(final String... prefixes) {
        return Lists.newArrayList(Iterables.filter(Iterables.filter(getEhCache().getKeys(), String.class), Predicates.containsPattern("^" + StringUtils.join(prefixes, ":") + ":.*$")));
    }

    private List<String> getCacheIdsForUsername(final String username) {
        return Lists.newArrayList(Iterables.filter(Iterables.filter(getEhCache().getKeys(), String.class), new Predicate<String>() {
            @Override
            public boolean apply(@Nullable final String cacheId) {
                return StringUtils.equals(username, getUsernameFromCacheId(cacheId));
            }
        }));
    }

    private static String getUsernameFromCacheId(final @Nullable String cacheId) {
        if (StringUtils.isBlank(cacheId)) {
            return null;
        }
        final Matcher matcher = REGEX_EXTRACT_USER_FROM_CACHE_ID.matcher(cacheId);
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group("username");
    }

    private static String getCacheIdForTag(final String tag) {
        return StringUtils.startsWith(tag, TAG_PREFIX) ? tag : createCacheIdFromElements(TAG_PREFIX, tag);
    }

    private static String getCacheIdForProject(final String projectId) {
        return StringUtils.startsWith(projectId, PROJECT_PREFIX) ? projectId : createCacheIdFromElements(PROJECT_PREFIX, projectId);
    }

    private static String getDisplayForReadableCounts(final Map<String, Long> readableCounts) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(readableCounts.get(XnatProjectdata.SCHEMA_ELEMENT_NAME)).append(" projects, ");
        buffer.append(readableCounts.get(WrkWorkflowdata.SCHEMA_ELEMENT_NAME)).append(" workflows, ");
        buffer.append(readableCounts.get(XnatSubjectdata.SCHEMA_ELEMENT_NAME)).append(" subjects");
        if (readableCounts.size() > 3) {
            for (final String type : readableCounts.keySet()) {
                if (!StringUtils.equalsAny(type, XnatProjectdata.SCHEMA_ELEMENT_NAME, WrkWorkflowdata.SCHEMA_ELEMENT_NAME, XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
                    buffer.append(", ").append(readableCounts.get(type)).append(" ").append(type);
                }
            }
        }
        return buffer.toString();
    }

    private static String getCacheIdForUserElementAccessManagers(final String username) {
        return createCacheIdFromElements(USER_ELEMENT_PREFIX, username, ELEMENT_ACCESS_MANAGERS_PREFIX);
    }

    private static String getCacheIdForUserGroups(final String username) {
        return createCacheIdFromElements(USER_ELEMENT_PREFIX, username, GROUPS_ELEMENT_PREFIX);
    }

    private static String getCacheIdForUserProjectAccess(final String username, final String access) {
        return createCacheIdFromElements(USER_ELEMENT_PREFIX, username, XnatProjectdata.SCHEMA_ELEMENT_NAME, access);
    }

    private static String getCacheIdForUserElements(final String username, final String elementType) {
        return createCacheIdFromElements(USER_ELEMENT_PREFIX, username, elementType);
    }

    private static String getCacheIdForActionElements(final String username, final String action) {
        return createCacheIdFromElements(ACTION_PREFIX, username, action);
    }

    private static boolean isGroupsAndPermissionsCacheEvent(final Ehcache cache) {
        return StringUtils.equals(CACHE_NAME, cache.getName());
    }

    private static final ResultSetExtractor<Map<String, Long>> ELEMENT_COUNT_EXTRACTOR = new ResultSetExtractor<Map<String, Long>>() {
        @Override
        public Map<String, Long> extractData(final ResultSet results) throws SQLException, DataAccessException {
            final Map<String, Long> elementCounts = new HashMap<>();
            while (results.next()) {
                final String elementName  = results.getString("element_name");
                final long   elementCount = results.getLong("element_count");
                log.debug("Found element '{}' with {} instances", elementName, elementCount);
                elementCounts.put(elementName, elementCount);
            }
            return elementCounts;
        }
    };

    private static final DateFormat DATE_FORMAT      = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
    private static final String[]   REPLACEMENT_LIST = {"id", "xnat_experimentData", "xnat_experimentData_share", "sharing_share_xnat_experimentda_id"};

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
    private static final String QUERY_GET_GROUPS_FOR_DATATYPE    = "SELECT DISTINCT usergroup.id AS group_name " +
                                                                   "FROM xdat_usergroup usergroup " +
                                                                   "  LEFT JOIN xdat_element_access xea ON usergroup.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
                                                                   "WHERE " +
                                                                   "  xea.element_name = :dataType " +
                                                                   "ORDER BY group_name";
    private static final String QUERY_ALL_GROUPS                 = "SELECT id FROM xdat_usergroup";
    private static final String QUERY_ALL_TAGS                   = "SELECT DISTINCT tag FROM xdat_usergroup WHERE tag IS NOT NULL AND tag <> ''";
    private static final String QUERY_GET_GROUPS_FOR_TAG         = "SELECT id FROM xdat_usergroup WHERE tag = :tag";
    @SuppressWarnings("unused")
    private static final String QUERY_GET_ALL_GROUPS_FOR_TAG     = "SELECT DISTINCT " +
                                                                   "  login, " +
                                                                   "  groupid " +
                                                                   "FROM xdat_user u " +
                                                                   "  LEFT JOIN xdat_user_groupid xug ON u.xdat_user_id = xug.groups_groupid_xdat_user_xdat_user_id " +
                                                                   "  LEFT JOIN xdat_usergroup usergroup ON xug.groupid = usergroup.id " +
                                                                   "  LEFT JOIN xdat_element_access xea ON usergroup.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
                                                                   "  LEFT JOIN xdat_element_access_meta_data xeamd ON xea.element_access_info = xeamd.meta_data_id " +
                                                                   "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                   "  LEFT JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                                   "WHERE tag = :tag OR (tag IS NULL AND field_value = '*') " +
                                                                   "  GROUP BY login, groupid " +
                                                                   "ORDER BY login";
    private static final String QUERY_CHECK_USER_EXISTS          = "SELECT EXISTS(SELECT TRUE FROM xdat_user WHERE login = :username) AS exists";
    private static final String QUERY_GET_EXPERIMENT_PROJECT     = "SELECT project FROM xnat_experimentdata WHERE id = :experimentId";
    private static final String QUERY_GET_SUBJECT_PROJECT        = "SELECT project FROM xnat_subjectdata WHERE id = :subjectId OR label = :subjectId";
    private static final String QUERY_GET_USERS_FOR_PROJECTS     = "SELECT DISTINCT login " +
                                                                   "FROM xdat_user u " +
                                                                   "  LEFT JOIN xdat_user_groupid gid ON u.xdat_user_id = gid.groups_groupid_xdat_user_xdat_user_id " +
                                                                   "  LEFT JOIN xdat_usergroup g ON gid.groupid = g.id " +
                                                                   "  LEFT JOIN xdat_element_access xea ON g.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
                                                                   "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                   "  LEFT JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                                   "WHERE tag IN (:projectIds) OR (tag IS NULL AND field_value = '*') " +
                                                                   "ORDER BY login";
    private static final String QUERY_GET_PROJECTS_FOR_USER      = "SELECT DISTINCT " +
                                                                   "  g.tag AS project " +
                                                                   "FROM xdat_usergroup g " +
                                                                   "  LEFT JOIN xdat_user_groupid gid ON g.id = gid.groupid " +
                                                                   "  LEFT JOIN xdat_user u ON gid.groups_groupid_xdat_user_xdat_user_id = u.xdat_user_id " +
                                                                   "WHERE g.tag IS NOT NULL AND " +
                                                                   "      u.login = :username";
    private static final String QUERY_GROUP_DATATYPE_PERMISSIONS = "SELECT " +
                                                                   "  xea.element_name    AS element_name, " +
                                                                   "  xeamd.status        AS active_status, " +
                                                                   "  xfms.method         AS method, " +
                                                                   "  xfm.field           AS field, " +
                                                                   "  xfm.field_value     AS field_value, " +
                                                                   "  xfm.comparison_type AS comparison_type, " +
                                                                   "  xfm.read_element    AS read_element, " +
                                                                   "  xfm.edit_element    AS edit_element, " +
                                                                   "  xfm.create_element  AS create_element, " +
                                                                   "  xfm.delete_element  AS delete_element, " +
                                                                   "  xfm.active_element  AS active_element " +
                                                                   "FROM xdat_usergroup usergroup " +
                                                                   "  LEFT JOIN xdat_element_access xea ON usergroup.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
                                                                   "  LEFT JOIN xdat_element_access_meta_data xeamd ON xea.element_access_info = xeamd.meta_data_id " +
                                                                   "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                   "  LEFT JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                                   "WHERE " +
                                                                   "  usergroup.id = :groupId AND " +
                                                                   "  element_name = :dataType " +
                                                                   "ORDER BY element_name, field";
    private static final String QUERY_USER_PERMISSIONS           = "SELECT " +
                                                                   "  xea.element_name    AS element_name, " +
                                                                   "  xeamd.status        AS active_status, " +
                                                                   "  xfms.method         AS method, " +
                                                                   "  xfm.field           AS field, " +
                                                                   "  xfm.field_value     AS field_value, " +
                                                                   "  xfm.comparison_type AS comparison_type, " +
                                                                   "  xfm.read_element    AS read_element, " +
                                                                   "  xfm.edit_element    AS edit_element, " +
                                                                   "  xfm.create_element  AS create_element, " +
                                                                   "  xfm.delete_element  AS delete_element, " +
                                                                   "  xfm.active_element  AS active_element " +
                                                                   "FROM xdat_user u " +
                                                                   "  LEFT JOIN xdat_element_access xea ON u.xdat_user_id = xea.xdat_user_xdat_user_id " +
                                                                   "  LEFT JOIN xdat_element_access_meta_data xeamd ON xea.element_access_info = xeamd.meta_data_id " +
                                                                   "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                   "  LEFT JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                                   "WHERE " +
                                                                   "  u.login = :username";
    private static final String QUERY_USER_PROJECTS              = "SELECT " +
                                                                   "  DISTINCT xfm.field_value AS project " +
                                                                   "FROM xdat_user u " +
                                                                   "  LEFT JOIN xdat_user_groupid map ON u.xdat_user_id = map.groups_groupid_xdat_user_xdat_user_id " +
                                                                   "  LEFT JOIN xdat_usergroup usergroup on map.groupid = usergroup.id " +
                                                                   "  LEFT JOIN xdat_element_access xea on (usergroup.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id OR u.xdat_user_id = xea.xdat_user_xdat_user_id) " +
                                                                   "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                   "  LEFT JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                                   "WHERE " +
                                                                   "  xfm.field_value != '*' AND " +
                                                                   "  xea.element_name = 'xnat:projectData' AND " +
                                                                   "  xfm.%s = 1 AND " +
                                                                   "  u.login IN (:usernames) " +
                                                                   "ORDER BY project";
    private static final String QUERY_OWNED_PROJECTS             = String.format(QUERY_USER_PROJECTS, "delete_element");
    private static final String QUERY_EDITABLE_PROJECTS          = String.format(QUERY_USER_PROJECTS, "edit_element");
    private static final String QUERY_READABLE_PROJECTS          = String.format(QUERY_USER_PROJECTS, "read_element");


    private static final String       GUEST_USERNAME                 = "guest";
    private static final String       ACTION_PREFIX                  = "action";
    private static final String       TAG_PREFIX                     = "tag";
    private static final String       PROJECT_PREFIX                 = "project";
    private static final String       USER_ELEMENT_PREFIX            = "user";
    private static final String       ELEMENT_ACCESS_MANAGERS_PREFIX = "eam";
    private static final String       GROUPS_ELEMENT_PREFIX          = "groups";
    @SuppressWarnings("unused")
    private static final String       GUEST_ACTION_READ              = getCacheIdForActionElements(GUEST_USERNAME, SecurityManager.READ);
    private static final List<String> ALL_ACTIONS                    = Arrays.asList(SecurityManager.READ, SecurityManager.EDIT, SecurityManager.CREATE);

    private static final Pattern      REGEX_EXTRACT_USER_FROM_CACHE_ID = Pattern.compile("^(?<prefix>" + ACTION_PREFIX + "|" + USER_ELEMENT_PREFIX + "):(?<username>[^:]+):(?<remainder>.*)$");
    private static final NumberFormat FORMATTER                        = NumberFormat.getNumberInstance(Locale.getDefault());
    private static final String       GUEST_CACHE_ID                   = getCacheIdForUserElements("guest", BROWSEABLE);

    private static final Function<ElementDisplay, String>  FUNCTION_ELEMENT_DISPLAY_TO_STRING  = new Function<ElementDisplay, String>() {
        @Override
        public String apply(final ElementDisplay elementDisplay) {
            return elementDisplay.getElementName();
        }
    };
    private static final Function<ElementSecurity, String> FUNCTION_ELEMENT_SECURITY_TO_STRING = new Function<ElementSecurity, String>() {
        @Override
        public String apply(final ElementSecurity security) {
            try {
                return security.getElementName();
            } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
                log.error("Got an error trying to get an element security object name", e);
                return "";
            }
        }
    };
    private static final Function<String, String>          FUNCTION_CACHE_IDS_TO_USERNAMES     = new Function<String, String>() {
        @Nullable
        @Override
        public String apply(@Nullable final String cacheId) {
            return getUsernameFromCacheId(cacheId);
        }
    };

    private final NamedParameterJdbcTemplate _template;
    private final JmsTemplate                _jmsTemplate;
    private final DatabaseHelper             _helper;
    private final Map<String, Long>          _totalCounts;
    private final Map<String, Long>          _missingElements;
    private final Map<String, Boolean>       _userChecks;

    private Listener _listener;
    private boolean  _initialized;
    private XDATUser _guest;
}
