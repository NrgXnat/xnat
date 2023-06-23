/*
 * web: org.nrg.xnat.services.cache.DefaultUserProjectCache
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.cache;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.utilities.LapStopWatch;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.om.*;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.*;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.Initializing;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xdat.servlet.XDATServlet;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.methods.XftItemEventCriteria;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.cache.jms.InitializeGroupRequest;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.lang.Long.max;
import static org.nrg.framework.exceptions.NrgServiceError.ConfigurationError;
import static org.nrg.xapi.rest.users.DataAccessApi.*;
import static org.nrg.xdat.security.PermissionCriteria.dumpCriteriaList;
import static org.nrg.xdat.security.helpers.Groups.*;
import static org.nrg.xdat.security.helpers.Users.DEFAULT_GUEST_USERNAME;
import static org.nrg.xft.event.XftItemEventI.*;

@SuppressWarnings("Duplicates")
@Service("groupsAndPermissionsCache")
@Slf4j
public class DefaultGroupsAndPermissionsCache extends AbstractXftItemAndCacheEventHandlerMethod implements GroupsAndPermissionsCache, Initializing, GroupsAndPermissionsCache.Provider {

    @Autowired
    public DefaultGroupsAndPermissionsCache(final CacheManager cacheManager, final NamedParameterJdbcTemplate template, final JmsTemplate jmsTemplate, final DatabaseHelper helper) throws SQLException {
        super(cacheManager,
              XftItemEventCriteria.builder().xsiType(XnatProjectdata.SCHEMA_ELEMENT_NAME).actions(CREATE, UPDATE, DELETE).build(),
              XftItemEventCriteria.builder().xsiType(XnatSubjectdata.SCHEMA_ELEMENT_NAME).xsiType(XnatExperimentdata.SCHEMA_ELEMENT_NAME).actions(CREATE, DELETE, SHARE).build(),
              XftItemEventCriteria.getXsiTypeCriteria(XdatUsergroup.SCHEMA_ELEMENT_NAME),
              XftItemEventCriteria.getXsiTypeCriteria(XdatElementSecurity.SCHEMA_ELEMENT_NAME));

        _template        = template;
        _jmsTemplate     = jmsTemplate;
        _helper          = helper;
        _totalCounts     = new ConcurrentHashMap<>();
        _missingElements = new HashMap<>();
        _userChecks      = new ConcurrentHashMap<>();
        _initialized     = new AtomicBoolean(false);
        if(_helper.tableExists("xnat_projectdata")) {
            resetTotalCounts();
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
     * Gets the specified project if the user has any access to it. Returns null otherwise.
     *
     * @param groupId The ID or alias of the project to retrieve.
     *
     * @return The project object if the user can access it, null otherwise.
     */
    @Override
    public UserGroupI get(final String groupId) {
        if(StringUtils.isBlank(groupId)) {
            throw new IllegalArgumentException("Can not request a group with a blank group ID");
        }

        // Check that the group is cached and, if so, return it.
        log.trace("Retrieving group through cache ID {}", groupId);
        final UserGroupI cachedGroup = getCachedGroup(groupId);
        if(cachedGroup != null) {
            log.debug("Found cached group entry for cache ID '{}'", groupId);
            return cachedGroup;
        }

        try {
            log.trace("Initializing group entry for cache ID '{}'", groupId);
            return initGroup(groupId);
        } catch (ItemNotFoundException e) {
            log.info("Can't find a group with the group ID '{}', returning null", groupId);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> getReadableCounts(final UserI user) {
        if(user == null) {
            return Collections.emptyMap();
        }
        return getReadableCounts(user.getUsername());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ElementDisplay> getBrowseableElementDisplays(final UserI user) {
        if(user == null) {
            return Collections.emptyMap();
        }

        final Map<String, ElementDisplay> guestBrowseableElementDisplays = getGuestBrowseableElementDisplays();
        if(user.isGuest()) {
            log.debug("Got a request for browseable element displays for the guest user, returning {} entries", guestBrowseableElementDisplays.size());
            return guestBrowseableElementDisplays;
        }

        final String username = user.getUsername();
        final String cacheId  = getCacheIdForUserElements(username, BROWSEABLE);

        // Check whether the element types are cached and, if so, return that.
        log.trace("Retrieving browseable element displays for user {} through cache ID {}", username, cacheId);
        final Map<String, ElementDisplay> cachedUserEntry = getCachedMap(cacheId);
        if(cachedUserEntry != null) {
            final Map<String, ElementDisplay> browseables = buildImmutableMap(Arrays.asList(cachedUserEntry, guestBrowseableElementDisplays));
            log.debug("Found a cached entry for user '{}' browseable element displays under cache ID '{}' with {} entries", username, cacheId, browseables.size());
            return browseables;
        }

        log.trace("Initializing browseable element displays for user '{}' with cache ID '{}'", username, cacheId);
        final Map<String, ElementDisplay> browseables = buildImmutableMap(Arrays.asList(initBrowseableElementDisplaysForUser(cacheId, user), guestBrowseableElementDisplays));
        log.debug("Initialized browseable element displays for user '{}' with cache ID '{}' with {} entries (including guest browseable element displays)", username, cacheId, browseables.size());
        return browseables;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ElementDisplay> getSearchableElementDisplays(final UserI user) {
        if(user == null) {
            return Collections.emptyList();
        }

        final String username = user.getUsername();
        final String cacheId  = getCacheIdForUserElements(username, SEARCHABLE);
        log.debug("Retrieving searchable element displays for user {} through cache ID {}", username, cacheId);

        final Map<String, Long> counts = getReadableCounts(user);
        try {
            return getActionElementDisplays(username, SecurityManager.READ).stream().filter(display -> {
                if(display == null) {
                    return false;
                }
                final String name = display.getElementName();
                try {
                    return ElementSecurity.IsSearchable(name) && counts.containsKey(name) && counts.get(name) > 0;
                } catch (Exception e) {
                    return false;
                }
            }).collect(Collectors.toList());
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
        return getActionElementDisplays(user.getUsername(), action);
    }

    @Override
    public List<ElementDisplay> getActionElementDisplays(final String username, final String action) {
        if(!ACTIONS.contains(action)) {
            throw new NrgServiceRuntimeException(ConfigurationError, "The action '" + action + "' is invalid, must be one of: " + StringUtils.join(ACTIONS, ", "));
        }
        final List<ElementDisplay> elementDisplays = getActionElementDisplays(username).get(action);
        if(log.isTraceEnabled()) {
            log.trace("Found {} element displays for user {} action {}: {}", elementDisplays.size(), username, action, formatElementDisplays(elementDisplays));
        }
        return elementDisplays;
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
            if(managers.isEmpty()) {
                log.info("Couldn't find element access managers for user {} trying to retrieve permissions for data type {}", username, dataType);
            } else {
                final ElementAccessManager manager = managers.get(dataType);
                if(manager == null) {
                    log.info("Couldn't find element access manager for data type {} for user {} while trying to retrieve permissions ", dataType, username);
                } else {
                    criteria.addAll(manager.getCriteria());
                    if(criteria.isEmpty()) {
                        log.debug("Couldn't find any permission criteria for data type {} for user {} while trying to retrieve permissions ", dataType, username);
                    }
                }
            }

            final Map<String, UserGroupI> userGroups = getMutableGroupsForUser(username);
            log.debug("Found {} user groups for the user {}{}", userGroups.size(), username, userGroups.isEmpty() ? "" : ": " + StringUtils.join(userGroups.keySet(), ", "));
            final Set<String> groups = userGroups.keySet();
            if(CollectionUtils.containsAny(groups, ALL_DATA_GROUPS)) {
                if(groups.contains(ALL_DATA_ADMIN_GROUP)) {
                    _template.query(QUERY_GET_ALL_MEMBER_GROUPS, resultSet -> {
                        final String projectId = resultSet.getString("project_id");
                        final String groupId   = resultSet.getString("group_id");

                        // If the user is a collaborator on a project, we're going to upgrade them to member,
                        // so remove that collaborator nonsense, this is the big time.
                        userGroups.remove(projectId + "_collaborator");

                        // If the user is already a member of owner of a project, then don't bother: they already have
                        // sufficient access to the project.
                        if(!userGroups.containsKey(projectId + "_owner") && !userGroups.containsKey(projectId + "_member")) {
                            userGroups.put(groupId, get(groupId));
                        }
                    });
                } else if(userGroups.containsKey(ALL_DATA_ACCESS_GROUP)) {
                    _template.query(QUERY_GET_ALL_COLLAB_GROUPS, resultSet -> {
                        final String projectId = resultSet.getString("project_id");
                        final String groupId   = resultSet.getString("group_id");

                        // If the user has no group membership, then add as a collaborator.
                        if(!CollectionUtils.containsAny(groups, Arrays.asList(groupId, projectId + "_member", projectId + "_owner"))) {
                            userGroups.put(groupId, get(groupId));
                        }
                    });
                }
            }

            for (final UserGroupI group : userGroups.values()) {
                final List<PermissionCriteriaI> permissions = group.getPermissionsByDataType(dataType);
                if(permissions != null) {
                    if(log.isTraceEnabled()) {
                        log.trace("Searched for permission criteria for user {} on type {} in group {}: {}", username, dataType, group.getId(), dumpCriteriaList(permissions));
                    } else {
                        log.debug("Searched for permission criteria for user {} on type {} in group {}: {} permissions found", username, dataType, group.getId(), permissions.size());
                    }
                    criteria.addAll(permissions);
                } else {
                    log.warn("Tried to retrieve permissions for data type {} for user {} in group {}, but this returned null.", dataType, username, group.getId());
                }
            }

            if(!isGuest(username)) {
                try {
                    final List<PermissionCriteriaI> permissions = getPermissionCriteria(getGuest().getUsername(), dataType);
                    if(permissions != null) {
                        criteria.addAll(permissions);
                    } else {
                        log.warn("Tried to retrieve permissions for data type {} for the guest user, but this returned null.", dataType);
                    }
                } catch (Exception e) {
                    log.error("An error occurred trying to retrieve the guest user", e);
                }
            }

            if(log.isTraceEnabled()) {
                log.trace("Retrieved permission criteria for user {} on the data type {}: {}", username, dataType, dumpCriteriaList(criteria));
            } else {
                log.debug("Retrieved permission criteria for user {} on the data type {}: {} criteria found", username, dataType, criteria.size());
            }

            return ImmutableList.copyOf(criteria);
        } catch (UserNotFoundException e) {
            log.error("Couldn't find the indicated user");
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Long> getTotalCounts() {
        if(_totalCounts.isEmpty()) {
            resetTotalCounts();
        }

        return ImmutableMap.copyOf(_totalCounts);
    }

    @Nonnull
    @Override
    public List<String> getProjectsForUser(final String username, final String access) {
        log.info("Getting projects with {} access for user {}", access, username);
        switch (access) {
            case SecurityManager.READ:
                return getUserReadableProjects(username);

            case SecurityManager.EDIT:
                return getUserEditableProjects(username);

            case SecurityManager.DELETE:
                return getUserOwnedProjects(username);

            default:
                throw new IllegalArgumentException("Invalid access level '" + access + "', valid values are: '" + SecurityManager.READ + "', '" + SecurityManager.EDIT + "', and '" + SecurityManager.DELETE + "'.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public List<UserGroupI> getGroupsForTag(final String tag) {
        // Get the group IDs associated with the tag.
        final List<String> groupIds = getTagGroups(tag);
        log.info("Getting {} groups for tag {}", groupIds.size(), tag);
        return getUserGroupList(groupIds);
    }

    @Nonnull
    @Override
    public Map<String, UserGroupI> getGroupsForUser(final String username) throws UserNotFoundException {
        return ImmutableMap.copyOf(getMutableGroupsForUser(username));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshGroupsForUser(final String username) throws UserNotFoundException {
        initUserGroupIds(getCacheIdForUserGroups(username), username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public UserGroupI getGroupForUserAndTag(final String username, final String tag) throws UserNotFoundException {
        final String groupId = _template.query(QUERY_GET_GROUP_FOR_USER_AND_TAG, checkUser(username).addValue("tag", tag), results -> results.next() ? results.getString("id") : null);
        return StringUtils.isNotBlank(groupId) ? get(groupId) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getUserIdsForGroup(final String groupId) {
        final UserGroupI userGroup = get(groupId);
        if(userGroup == null) {
            return Collections.emptyList();
        }
        return ImmutableList.copyOf(userGroup.getUsernames());
    }

    @Override
    public void refreshGroup(final String groupId) throws ItemNotFoundException {
        final UserGroupI group = initGroup(groupId);
        for (final String username : group.getUsernames()) {
            try {
                if(!getGroupIdsForUser(username).contains(groupId)) {
                    refreshGroupsForUser(username);
                }
            } catch (UserNotFoundException ignored) {
                //
            }
        }
    }

    @Override
    public Date getUserLastUpdateTime(UserI user) {
        if(user == null) {
            try {
                user = Users.getGuest();
            } catch (UserNotFoundException | UserInitException e) {
                return new Date();
            }
        }
        return getUserLastUpdateTime(user.getUsername());
    }

    @Override
    public Date getUserLastUpdateTime(final String username) {
        try {
            @SuppressWarnings("unchecked") final List<String> cacheIds = new ArrayList<>(buildImmutableSet(getGroupIdsForUser(username), getCacheIdsForUsername(username)));
            if(cacheIds.isEmpty()) {
                return new Date();
            }
            if(log.isDebugEnabled()) {
                log.debug("Found {} cache entries related to user {}: {}", cacheIds.size(), username, StringUtils.join(cacheIds, ", "));
            }
            final long lastUpdateTime = cacheIds.stream().map(cacheId -> {
                final Date time = getCacheEntryLastUpdateTime(cacheId);
                log.trace("User {} cache entry '{}' last updated: {}", username, cacheId, time == null ? "null" : time.getTime());
                return time == null ? 0L : time.getTime();
            }).max(Long::compareTo).orElse(0L);
            log.debug("Found latest cache entry last updated time for user {}: {}", username, lastUpdateTime);
            return new Date(lastUpdateTime);
        } catch (UserNotFoundException ignored) {
            log.warn("Someone requested the cache entry last updated time for user {} but that user wasn't found", username);
            return new Date();
        }
    }

    /**
     * Finds all user element cache IDs for the specified user and evicts them from the cache.
     *
     * @param username The username to be cleared.
     */
    @Override
    public void clearUserCache(final String username) {
        final List<String> cacheIds = getCacheIdsForUserElements(username);
        if(log.isDebugEnabled()) {
            log.debug("Clearing caches for user '{}': {}", username, StringUtils.join(cacheIds, ", "));
        }
        evict(cacheIds);
    }

    @Override
    public boolean canInitialize() {
        try {
            if(_listener == null) {
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
                stopWatch.lap(Level.DEBUG, "Creating queue entry for group {}", groupId);
                XDAT.sendJmsRequest(_jmsTemplate, new InitializeGroupRequest(groupId));
            }
        } finally {
            if(stopWatch.isStarted()) {
                stopWatch.stop();
            }
            log.info("Total time to queue {} groups was {} ms", groupIds.size(), NUMBER_FORMAT.format(stopWatch.getTime()));
            if(log.isInfoEnabled()) {
                log.info(stopWatch.toTable());
            }
        }

        resetGuestBrowseableElementDisplays();
        _initialized.set(true);
        return new AsyncResult<>(true);
    }

    @Override
    public boolean isInitialized() {
        return _initialized.get();
    }

    @Override
    public Map<String, String> getInitializationStatus() {
        final Map<String, String> status = new HashMap<>();
        if(_listener == null) {
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

        final Date completed = _listener.getCompleted();

        if(unprocessed.isEmpty() && completed != null) {
            final String duration = DurationFormatUtils.formatPeriodISO(start.getTime(), completed.getTime());
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
        status.put("message", unprocessed.isEmpty()
                              ? "Cache initialization is on-going, with " + processedCount + " groups processed and no groups remaining, time elapsed so far is " + duration
                              : "Cache initialization is on-going, with " + processedCount + " groups processed and " + unprocessedCount + " groups remaining, time elapsed so far is " + duration);

        return status;
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

    private boolean isImageSession(String xsiType){
        try {
            return SchemaElement.GetElement(xsiType).instanceOf(XnatImagesessiondata.SCHEMA_ELEMENT_NAME);
        } catch (XFTInitException|ElementNotFoundException e) {
            log.error("Failed to parse passed xsiType in event handler",e);
            return false;
        }
    }

    private void incrementCount(final String xsiType) {
        if(isImageSession(xsiType)){
            _totalCounts.merge(XnatImagesessiondata.SCHEMA_ELEMENT_NAME, 1L, Long::sum);
        }else{
            _totalCounts.merge(xsiType, 1L, Long::sum);
        }
    }

    private void decrementCount(final String xsiType) {
        // decrement, but don't go below zero
        if(isImageSession(xsiType)){
            _totalCounts.merge(XnatImagesessiondata.SCHEMA_ELEMENT_NAME, 1L, (a, b) -> max(a - b,0L));
        }else{
            _totalCounts.merge(xsiType, 1L, (a, b) -> max(a - b,0L));
        }
    }

    private boolean handleProjectEvents(final XftItemEventI event) {
        final String         xsiType    = event.getXsiType();
        final String         id         = event.getId();
        final String         action     = event.getAction();
        final Map<String, ?> properties = event.getProperties();

        try {
            switch (action) {
                case CREATE:
                    log.debug("New project created with ID {}, caching new instance", id);
                    for (final String owner : getProjectOwners(id)) {
                        if(getActionElementDisplays(owner).get(SecurityManager.CREATE).stream().noneMatch(CONTAINS_MR_SESSION)) {
                            initActionElementDisplays(owner, true);
                        }
                    }

                    final boolean created = !initGroups(getGroups(xsiType, id)).isEmpty();
                    final String access = Permissions.getProjectAccess(_template, id);
                    if(StringUtils.isNotBlank(access)) {
                        switch (access) {
                            case "private":
                                break;

                            case "public":
                                if(getActionElementDisplays(DEFAULT_GUEST_USERNAME).get(SecurityManager.CREATE).stream().noneMatch(CONTAINS_MR_SESSION)) {
                                    initActionElementDisplays(DEFAULT_GUEST_USERNAME, true);
                                }

                            case "protected":
                                updateProjectRelatedCaches(xsiType, id, false);
                                break;
                        }
                    }
                    resetProjectCount();
                    return created;

                case UPDATE:
                    log.debug("The {} object {} was updated, caching updated instance", xsiType, id);
                    if(properties.containsKey("accessibility")) {
                        final String accessibility = (String) properties.get("accessibility");
                        switch (accessibility) {
                            case "private":
                                return updateProjectRelatedCaches(xsiType, id, true);

                            case "public":
                                if(getActionElementDisplays(DEFAULT_GUEST_USERNAME).get(SecurityManager.CREATE).stream().noneMatch(CONTAINS_MR_SESSION)) {
                                    initActionElementDisplays(DEFAULT_GUEST_USERNAME, true);
                                }

                            case "protected":
                                return updateProjectRelatedCaches(xsiType, id, true);

                            default:
                                log.warn("The project {}'s accessibility setting was updated to an invalid value: {}. Must be one of private, protected, or public.", id, accessibility);
                        }
                    }
                    break;

                case DELETE:
                    log.debug("The {} {} was deleted, removing related instances from cache", xsiType, id);
                    final String cacheId = getCacheIdForProject(id);
                    evict(cacheId);
                    getCacheIdsForUserElements().stream().filter(current -> REGEX_USER_PROJECT_ACCESS_CACHE_ID.matcher(current).matches()).forEach(accessCacheId -> {
                        final List<String> projectIds = getCachedList(accessCacheId);
                        if(projectIds != null && projectIds.contains(id)) {
                            final List<String> updated = new ArrayList<>(projectIds);
                            updated.remove(id);
                            forceCacheObject(accessCacheId, updated);
                        }
                    });
                    resetGuestBrowseableElementDisplays();
                    initReadableCountsForUsers(getCachedSet(cacheId));
                    resetTotalCounts();//adding this because project deletes often effect other data types too (subjects, expts)
                    return true;

                default:
                    log.warn("I was informed that the '{}' action happened to the project with ID '{}'. I don't know what to do with this action.", action, id);
                    break;
            }
        } catch (ItemNotFoundException e) {
            log.warn("While handling action {}, I couldn't find a group for type {} ID {}.", action, xsiType, id);
        }

        return false;
    }

    private boolean updateProjectRelatedCaches(final String xsiType, final String id, final boolean affectsOtherDataTypes) throws ItemNotFoundException {
        final boolean cachedRelatedGroups = !initGroups(getGroups(xsiType, id)).isEmpty();

        evict(GUEST_CACHE_ID);
        evict(GUEST_ACTION_READ);
        resetGuestBrowseableElementDisplays();
        initActionElementDisplays(DEFAULT_GUEST_USERNAME, true);

        final Set<String> readableCountCacheIds = new HashSet<>(getCacheIdsForUserReadableCounts());
        if(affectsOtherDataTypes) {
            for (final String cacheId : readableCountCacheIds) {
                evict(cacheId);
            }
        } else {
            // Update existing user element displays
            final List<String> cacheIds = getCacheIdsForActions();
            cacheIds.addAll(getCacheIdsForUserElements());
            clearAllUserProjectAccess();
            initReadableCountsForUsers(cacheIds.stream().map(DefaultGroupsAndPermissionsCache::getUsernameFromCacheId).filter(StringUtils::isNotBlank).collect(Collectors.toSet()));
        }

        return cachedRelatedGroups;
    }

    private boolean handleSubjectEvents(final XftItemEventI event) {
        final String action = event.getAction();
        log.debug("Handling subject {} event for {} {}", XftItemEventI.ACTIONS.get(action), event.getXsiType(), event.getId());
        final Set<String> projectIds = new HashSet<>();
        switch (action) {
            case CREATE:
                projectIds.add(_template.queryForObject(QUERY_GET_SUBJECT_PROJECT, new MapSqlParameterSource("subjectId", event.getId()), String.class));
                incrementCount(XnatSubjectdata.SCHEMA_ELEMENT_NAME);
                break;

            case SHARE:
                projectIds.add((String) event.getProperties().get("target"));
                break;

            case MOVE:
                projectIds.add((String) event.getProperties().get("origin"));
                projectIds.add((String) event.getProperties().get("target"));
                break;

            case DELETE:
                projectIds.add((String) event.getProperties().get("target"));
                handleGroupRelatedEvents(event);
                decrementCount(XnatSubjectdata.SCHEMA_ELEMENT_NAME);
                break;

            default:
                log.warn("I was informed that the '{}' action happened to subject '{}'. I don't know what to do with this action.", action, event.getId());
        }
        if(projectIds.isEmpty()) {
            return false;
        }

        initReadableCountsForProjectUsers(projectIds,action,event.getXsiType());
        return true;
    }

    private boolean handleGroupRelatedEvents(final XftItemEventI event) {
        final String         xsiType    = event.getXsiType();
        final String         id         = event.getId();
        final String         action     = event.getAction();
        final Map<String, ?> properties = event.getProperties();
        final Set<String>    usernames  = new HashSet<>();

        try {
            final List<UserGroupI> groups = getGroups(xsiType, id);
            switch (action) {
                case CREATE:
                    log.debug("New {} created with ID {}, caching new instance", xsiType, id);
                    for (final UserGroupI group : groups) {
                        usernames.addAll(group.getUsernames());
                    }
                    log.debug("Handling create group event with ID '{}' for users: {}", id, StringUtils.join(usernames, ", "));
                    return !initGroups(groups).isEmpty();

                case UPDATE:
                    log.debug("The {} object {} was updated, caching updated instance", xsiType, id);
                    final boolean hasOperation = properties.containsKey(OPERATION);
                    for (final UserGroupI group : groups) {
                        // If there's no operation, we have no way of knowing which users will be affected by the update and have to update all of them.
                        if(!hasOperation) {
                            usernames.addAll(group.getUsernames());
                        }
                        evict(group.getId());
                    }
                    if(hasOperation && properties.containsKey(USERS)) {
                        //noinspection unchecked
                        usernames.addAll((Collection<? extends String>) properties.get(USERS));
                    }
                    log.debug("Handling update group event with ID '{}' for users: {}", id, StringUtils.join(usernames, ", "));
                    return !initGroups(groups).isEmpty();

                case DELETE:
                    if(StringUtils.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME, xsiType)) {
                        final List<String> groupIds = getTagGroups(id);
                        if(CollectionUtils.isNotEmpty(groupIds)) {
                            log.info("Found {} groups cached for deleted project {}", groupIds.size(), id);
                            for (final String groupId : groupIds) {
                                evictGroup(groupId, usernames);
                            }
                        }
                    } else {
                        evictGroup(id, usernames);
                    }
                    break;

                default:
                    log.warn("I was informed that the '{}' action happened to the {} object with ID '{}'. I don't know what to do with this action.", action, xsiType, id);
            }
        } catch (ItemNotFoundException e) {
            log.warn("While handling action {}, I couldn't find a group for type {} ID {}.", action, xsiType, id);
        } finally {
            for (final String username : usernames) {
                try {
                    final String cacheIdForUserGroups = getCacheIdForUserGroups(username);
                    evict(Arrays.asList(cacheIdForUserGroups, getCacheIdForUserElements(username, READABLE), getCacheIdForUserElements(username, SEARCHABLE), getCacheIdForUserElements(username, BROWSEABLE), getCacheIdForUserElementAccessManagers(username), getCacheIdForActionElements(username)));

                    initUserGroupIds(cacheIdForUserGroups, username);
                    initBrowseableElementDisplaysForUser(getCacheIdForUserElements(username, BROWSEABLE), username);
                    initActionElementDisplays(username);
                } catch (UserNotFoundException e) {
                    log.warn("While handling action {} for type {} ID {}, I couldn't find a user with username {}.", action, xsiType, id, username);
                }
            }
        }
        return false;
    }

    private boolean handleElementSecurityEvents(final XftItemEventI event) {
        log.debug("Handling {} event for '{}' IDs {}. Updating guest browseable element displays...", event.getAction(), event.getXsiType(), event.getIds());
        final Map<String, ElementDisplay> displays = resetGuestBrowseableElementDisplays();

        if(log.isTraceEnabled()) {
            log.trace("Got back {} browseable element displays for guest user after refresh: {}", displays.size(), StringUtils.join(displays.keySet(), ", "));
        }

        log.debug("Evicting all action and user element cache IDs");
        Stream.concat(getCacheIdsForActions().stream(), getCacheIdsForUserElements().stream()).forEach(this::evict);

        for (final String dataType : event.getIds()) {
            final List<String> groupIds = getGroupIdsForDataType(dataType);
            log.debug("Found {} groups that reference the '{}' data type, updating cache entries for: {}", groupIds.size(), dataType, String.join(", ", groupIds));
            groupIds.forEach(groupId -> {
                log.trace("Evicting group '{}' due to change in element securities for data type {}", groupId, dataType);
                evict(groupId);
            });
        }

        return true;
    }

    private boolean handleExperimentEvents(final XftItemEventI event) {
        final String action  = event.getAction();
        final String xsiType = event.getXsiType();
        log.debug("Handling experiment {} event for {} {}", XftItemEventI.ACTIONS.get(action), xsiType, event.getId());
        final String target, origin;
        final Set<String> projectIds = new HashSet<>();
        switch (action) {
            case CREATE:
                target = _template.queryForObject(QUERY_GET_EXPERIMENT_PROJECT, new MapSqlParameterSource("experimentId", event.getId()), String.class);
                origin = null;
                projectIds.add(target);
                incrementCount(xsiType);
                break;

            case SHARE:
                target = (String) event.getProperties().get("target");
                origin = null;
                projectIds.add(target);
                break;

            case DELETE:
                target = (String) event.getProperties().get("target");
                origin = null;
                projectIds.add(target);
                decrementCount(xsiType);
                break;

            case MOVE:
                origin = (String) event.getProperties().get("origin");
                target = (String) event.getProperties().get("target");
                projectIds.add(target);
                projectIds.add(origin);
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

        if(needsPublicXsiTypeAdded || needsXsiTypeChecked) {
            if(needsPublicXsiTypeAdded) {
                log.debug("Updating guest browseable element displays: guest doesn't have the event XSI type '{}' and the target project {} is public.", xsiType, target);
            } else {
                log.debug("Updating guest browseable element displays: guest has the event XSI type '{}' and item was moved from public project {} to non-public project {}.", xsiType, origin, target);
            }
            resetGuestBrowseableElementDisplays();
        } else {
            log.debug("Not updating guest browseable element displays: guest {} '{}' and {}",
                      hasEventXsiType ? "already has the event XSI type " : "doesn't have the event XSI type",
                      xsiType,
                      isTargetProjectPublic ? "target project is public" : "target project is not public");
        }

        initReadableCountsForProjectUsers(projectIds,action,event.getXsiType());

        return true;
    }

    private void initReadableCountsForProjectUsers(final Set<String> projectIds, final String action, final String dataType){
        final Set<String> users = getProjectUsers(projectIds);
        for (final String username : users) {
            final Map<String,Long> cachedCounts=getCachedReadableCounts(username);
            if(cachedCounts==null || cachedCounts.size()==0){
                initReadableCountsForUser(username);
            }else if(StringUtils.equals(CREATE,action) && (!cachedCounts.containsKey(dataType) || cachedCounts.get(dataType)==0)){
                //we only need to worry about this if it is null or 0
                initReadableCountsForUser(username);
            }else if(StringUtils.equals(DELETE,action)){
                //do we need this?  Only if its going from 1 to 0, but we don't know that if counts aren't refreshed as they get closer to 1
                //leaving it, but if it becomes an issue in profiling, then we could consider removing it.
                initReadableCountsForUser(username);
            }
        }
    }

    private void handleCacheRemoveEvent(final Ehcache cache, final Element element, final String event) {
        if(isGroupsAndPermissionsCacheEvent(cache)) {
            if(element == null) {
                log.debug("Got a {} event for cache {}, no specific element affected", event, cache.getName());
                return;
            }
            final Object objectValue = element.getObjectValue();
            log.debug("Got a {} event for cache {} on ID {} with value of type {}", event, cache.getName(), element.getObjectKey(), objectValue != null ? objectValue.getClass().getName() : "<null>");
        }
    }

    private List<String> getProjectOwners(final String projectId) {
        return _template.queryForList(QUERY_PROJECT_OWNERS, new MapSqlParameterSource("projectId", projectId), String.class);
    }

    private Map<String, Long> getCachedReadableCounts(final String username) {
        if(StringUtils.isBlank(username)) {
            return Collections.emptyMap();
        }

        final String cacheId = getCacheIdForUserElements(username, READABLE);

        // Check whether the element types are cached and, if so, return that.
        log.trace("Retrieving readable counts for user {} through cache ID {}", username, cacheId);
        final Map<String, Long> cachedReadableCounts = getCachedMap(cacheId);
        if(cachedReadableCounts != null) {
            log.debug("Found cached readable counts entry for user '{}' with cache ID '{}' containing {} entries", username, cacheId, cachedReadableCounts.size());
            return cachedReadableCounts;
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<String, Long> getReadableCounts(final String username) {
        if(StringUtils.isBlank(username)) {
            return Collections.emptyMap();
        }

        String cacheId = getCacheIdForUserElements(username, READABLE);

        log.trace("Retrieving readable counts for user {} through cache ID {}", username, cacheId);
        Map<String, Long> cachedReadableCounts = getCachedMap(cacheId);
        if(cachedReadableCounts != null) {
            log.debug("Found cached readable counts entry for user '{}' with cache ID '{}' containing {} entries", username, cacheId, cachedReadableCounts.size());
            return cachedReadableCounts;
        }

        synchronized (this){
            //check if another thread set the counts
            cachedReadableCounts = getCachedMap(cacheId);
            if(cachedReadableCounts != null) {
                log.debug("Found cached readable counts entry for user '{}' with cache ID '{}' containing {} entries", username, cacheId, cachedReadableCounts.size());
                return cachedReadableCounts;
            }

            return initReadableCountsForUser(cacheId, username);
        }
    }

    private ListMultimap<String, ElementDisplay> getActionElementDisplays(final String username) {
        final String cacheId = getCacheIdForActionElements(username);

        // Check whether the action elements are cached and, if so, return that.
        final ListMultimap<String, ElementDisplay> cachedActions = getCachedListMultimap(cacheId);
        if(cachedActions != null) {
            log.debug("Found a cache entry for user '{}' action elements by ID '{}'", username, cacheId);
            return cachedActions;
        }

        return initActionElementDisplays(username);
    }

    private Long getUserReadableWorkflowCount(final String username) {
        return _template.queryForObject(QUERY_USER_READABLE_WORKFLOW_COUNT, new MapSqlParameterSource("username", username), Long.class);
    }

    @Nonnull
    private Map<String, UserGroupI> getMutableGroupsForUser(final String username) throws UserNotFoundException {
        final List<String>            groupIds = getGroupIdsForUser(username);
        final Map<String, UserGroupI> groups   = new HashMap<>();
        for (final String groupId : groupIds) {
            final UserGroupI group = get(groupId);
            if(group != null) {
                log.trace("Adding group {} to groups for user {}", groupId, username);
                groups.put(groupId, group);
            } else {
                log.info("User '{}' is associated with the group ID '{}', but I couldn't find that actual group", username, groupId);
            }
        }
        return groups;
    }

    @Nonnull
    private Map<String, ElementAccessManager> getElementAccessManagers(final String username) {
        if(StringUtils.isBlank(username)) {
            return Collections.emptyMap();
        }
        final String                            cacheId                     = getCacheIdForUserElementAccessManagers(username);
        final Map<String, ElementAccessManager> cachedElementAccessManagers = getCachedMap(cacheId);
        if(cachedElementAccessManagers != null) {
            log.debug("Found a cache entry for user '{}' element access managers by ID '{}' with {} entries", username, cacheId, cachedElementAccessManagers.size());
            return cachedElementAccessManagers;
        }
        return initElementAccessManagersForUser(cacheId, username);
    }

    private Map<String, ElementDisplay> getGuestBrowseableElementDisplays() {
        final Map<String, ElementDisplay> guestBrowseableElementDisplays = getCachedMap(GUEST_CACHE_ID);
        if(guestBrowseableElementDisplays != null) {
            return guestBrowseableElementDisplays;
        }
        return resetBrowseableElementDisplays(getGuest());
    }

    private void clearAllUserProjectAccess() {
        final List<String> cacheIds = getCacheIdsForUserElements();
        for (final String cacheId : cacheIds) {
            evict(cacheId);
        }
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
    @CacheLock(true)
    private UserGroupI initGroup(@CacheId final String groupId) throws ItemNotFoundException {
        return initGroupImpl(groupId, new UserGroup(groupId, _template));
    }

    @SuppressWarnings("UnusedReturnValue")
    @CacheLock(true)
    private UserGroupI initGroup(@CacheId final String groupId, final UserGroupI group) {
        return initGroupImpl(groupId, group);
    }

    private UserGroupI initGroupImpl(final String groupId, final UserGroupI group) {
        log.info("Initializing cache entry for group '{}'", groupId);
        cacheObject(groupId, group);
        String tag = group.getTag();
        if (StringUtils.isNotBlank(tag)) {
            initTag(getCacheIdForTag(tag), tag);
        }
        log.debug("Retrieved and cached the group for the ID {}", groupId);
        return group;
    }

    @SuppressWarnings("UnusedReturnValue")
    @CacheLock(true)
    private Map<String, Long> initReadableCountsForUser(final String cacheId, final UserI user) {
        return initReadableCountsForUser(cacheId, user.getUsername());
    }

    @CacheLock(true)
    private Map<String, Long> initReadableCountsForUser(final String cacheId, final String username) {
        log.info("Initializing readable counts for user '{}' with cache entry '{}'", username, cacheId);
        try {
            if (!StringUtils.equalsIgnoreCase("guest", username)) {
                initProjectMember(cacheId, username);
            }

            final Map<String, Long> readableCounts = new HashMap<>();
            final List<String> readableProjects = getUserReadableProjects(username);
            readableCounts.put(XnatProjectdata.SCHEMA_ELEMENT_NAME, (long) readableProjects.size());

            readableCounts.put(WrkWorkflowdata.SCHEMA_ELEMENT_NAME, getUserReadableWorkflowCount(username));

            readableCounts.putAll(getUserReadableSubjectsAndExperiments(readableProjects, Arrays.asList(Users.getUserId(username), Users.getUserId("guest"))));

            cacheObject(cacheId, readableCounts);
            if (log.isDebugEnabled()) {
                log.debug("Caching the following readable element counts for user '{}' with cache ID '{}': '{}'", username, cacheId, getDisplayForReadableCounts(readableCounts));
            }
            return ImmutableMap.copyOf(readableCounts);
        } catch (DataAccessException e) {
            log.error("An error occurred in the SQL for retrieving readable counts for the  user {}", username, e);
            return Collections.emptyMap();
        }
    }

    @CacheLock(true)
    private void initProjectMember(final String cacheId, final String username) {
        log.debug("Initializing cached project entries for user '{}' with cache ID '{}', initializing entry", username, cacheId);
        final List<String> projects = getUserProjects(username);
        for (final String project : projects) {
            final String      projectCacheId = getCacheIdForProject(project);
            final Set<String> cachedSet      = getCachedSet(projectCacheId);
            final Set<String> projectUsers   = new HashSet<>();
            projectUsers.add(username);
            if(cachedSet != null) {
                projectUsers.addAll(cachedSet);
            }
            forceCacheObject(projectCacheId, projectUsers);
        }
    }

    @CacheLock(true)
    private synchronized List<String> initTag(final String cacheId, final String tag) {
        // If there's a blank tag...
        if(StringUtils.isBlank(tag)) {
            throw new IllegalArgumentException("Can not request a blank tag, that's not a thing.");
        }

        log.info("Initializing cache entry for tag '{}' with cache ID '{}'", tag, cacheId);

        // Then retrieve and cache the groups if found or cache DOES_NOT_EXIST if the tag isn't found.
        final List<String> groups = getGroupIdsForProject(tag);

        // If this is empty, then the tag doesn't exist, and we'll just put DOES_NOT_EXIST there.
        if(groups.isEmpty()) {
            log.info("Someone tried to get groups for the tag {}, but there are no groups with that tag.", tag);
            return Collections.emptyList();
        } else {
            log.debug("Cached tag {} for {} groups: {}", tag, groups.size(), StringUtils.join(groups, ", "));
            cacheObject(cacheId, groups);
            return groups;
        }
    }

    @CacheLock(true)
    private Map<String, ElementAccessManager> initElementAccessManagersForUser(final String cacheId, final String username) {
        log.info("Initializing element access managers cache entry for user '{}' with cache ID '{}'", username, cacheId);
        final Map<String, ElementAccessManager> managers = ElementAccessManager.initialize(_template, QUERY_USER_PERMISSIONS, new MapSqlParameterSource("username", username));

        cacheObject(cacheId, managers);
        log.trace("Found {} element access managers for user '{}'", managers.size(), username);
        return managers;
    }

    @CacheLock(true)
    private synchronized Map<String, ElementDisplay> initBrowseableElementDisplaysForUser(final String cacheId, final UserI user) {
        return initBrowseableElementDisplaysForUser(cacheId, user.getUsername());
    }

    @CacheLock(true)
    private synchronized Map<String, ElementDisplay> initBrowseableElementDisplaysForUser(final String cacheId, final String username) {
        log.info("Initializing browseable element displays cache entry for user '{}' with cache ID '{}'", username, cacheId);
        final Map<String, Long> counts = getReadableCounts(username);
        log.debug("Found {} readable counts for user {}: {}", counts.size(), username, counts);

        try {
            final Map<String, ElementDisplay> browseableElements = new HashMap<>();
            final List<ElementDisplay>        elementDisplays    = getActionElementDisplays(username, SecurityManager.READ);
            if(log.isTraceEnabled()) {
                log.trace("Found {} readable action element displays for user {}: {}", elementDisplays.size(), username, formatElementDisplays(elementDisplays));
            }

            for (final ElementDisplay elementDisplay : elementDisplays) {
                final String  elementName         = elementDisplay.getElementName();
                final boolean isBrowseableElement = ElementSecurity.IsBrowseableElement(elementName);
                final boolean countsContainsKey   = counts.containsKey(elementName);
                final boolean hasOneOrMoreElement = countsContainsKey && counts.get(elementName) > 0;
                if(isBrowseableElement && countsContainsKey && hasOneOrMoreElement) {
                    log.debug("Adding element display {} to cache entry {} for user {}", elementName, cacheId, username);
                    browseableElements.put(elementName, elementDisplay);
                } else if(log.isTraceEnabled()) {
                    log.trace("Did not add element display {} for user {}: {}, {}, {}", elementName, username, isBrowseableElement ? "browseable" : "not browseable", countsContainsKey ? "counts contains key" : "counts does not contain key", hasOneOrMoreElement ? "counts has one or more elements of this type" : "counts does not have any elements of this type");
                }
            }

            log.info("Adding {} element displays to cache entry {}", browseableElements.size(), cacheId);
            cacheObject(cacheId, browseableElements);
            return browseableElements;
        } catch (ElementNotFoundException e) {
            if(!_missingElements.containsKey(e.ELEMENT)) {
                log.warn("Element '{}' not found. This may be a data type that was installed previously but can't be located now. This warning will only be displayed once. Set logging level to DEBUG to see a message each time this occurs for each element, along with a count of the number of times the element was referenced.", e.ELEMENT);
                _missingElements.put(e.ELEMENT, 1L);
            } else {
                final long count = _missingElements.get(e.ELEMENT) + 1;
                _missingElements.put(e.ELEMENT, count);
                log.debug("Element '{}' not found. This element has been referenced {} times.", e.ELEMENT, count);
            }
        } catch (XFTInitException e) {
            log.error("There was an error initializing or accessing XFT", e);
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
        }

        log.info("No browseable element displays found for user {}", username);
        return Collections.emptyMap();
    }

    @Nonnull
    @CacheLock(true)
    private List<String> initUserGroupIds(final String cacheId, final String username) throws UserNotFoundException {
        log.info("Initializing user group IDs cache entry for user '{}' with cache ID '{}'", username, cacheId);
        final List<String> groupIds = _template.queryForList(QUERY_GET_GROUPS_FOR_USER, checkUser(username), String.class);
        log.debug("Found {} user group IDs cache entry for user '{}'", groupIds.size(), username);
        cacheObject(cacheId, groupIds);
        return ImmutableList.copyOf(groupIds);
    }

    private ListMultimap<String, ElementDisplay> initActionElementDisplays(final String username) {
        return initActionElementDisplays(username, false);
    }

    private synchronized ListMultimap<String, ElementDisplay> initActionElementDisplays(final String username, final boolean evict) {
        final String cacheId = getCacheIdForActionElements(username);
        log.info("Initializing action element displays cache entry {} for user '{}', evict is {}", cacheId, username, evict);

        // If they want to evict the cache entry, then do that and proceed. They explicitly don't want any cached entry to be returned.
        if(evict) {
            evict(cacheId);
        } else {
            final ListMultimap<String, ElementDisplay> cachedObject = getCachedListMultimap(cacheId);
            if(cachedObject != null) {
                log.debug("Found an existing cached entry for cache ID {}, which probably means the cache was initialized while this call was locked out by method sync", cacheId);
                return cachedObject;
            }
        }

        final ListMultimap<String, ElementDisplay> elementDisplays = ArrayListMultimap.create();
        try {
            final List<ElementSecurity> securities = ElementSecurity.GetSecureElements();
            if(log.isDebugEnabled()) {
                log.debug("Evaluating {} element security objects: {}", securities.size(), securities.stream().map(security -> {
                    try {
                        return security.getElementName();
                    } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
                        log.error("Got an error trying to get an element security object name", e);
                        return "";
                    }
                }).filter(StringUtils::isNotBlank).collect(Collectors.joining(", ")));
            }
            for (final ElementSecurity elementSecurity : securities) {
                try {
                    final SchemaElement schemaElement = elementSecurity.getSchemaElement();
                    if(schemaElement != null) {
                        final String fullXMLName = schemaElement.getFullXMLName();
                        log.debug("Evaluating schema element {}", fullXMLName);
                        if(schemaElement.hasDisplay()) {
                            log.debug("Schema element {} has a display", fullXMLName);
                            for (final String action : ACTIONS) {
                                log.debug("Check user {} permission for action {} on schema element {}", username, action, fullXMLName);
                                if(Permissions.canAny(username, elementSecurity.getElementName(), action)) {
                                    log.debug("User {} can {} schema element {}", username, action, fullXMLName);
                                    final ElementDisplay elementDisplay = schemaElement.getDisplay();
                                    if(elementDisplay != null) {
                                        log.debug("Adding element display {} to action {} for user {}", elementDisplay.getElementName(), action, username);
                                        elementDisplays.put(action, elementDisplay);
                                    }
                                } else {
                                    log.debug("User {} can not {} schema element {}", username, action, fullXMLName);
                                }
                            }
                        } else {
                            log.debug("Schema element {} does not have a display, rejecting", fullXMLName);
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
                    if(schemaElement.hasDisplay()) {
                        final ElementDisplay elementDisplay = schemaElement.getDisplay();
                        log.debug("Adding all actions for insecure schema element {} to user {} permissions", elementDisplay.getElementName(), username);
                        for (final String action : ACTIONS) {
                            elementDisplays.put(action, elementDisplay);
                        }
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

        if(log.isTraceEnabled()) {
            final List<ElementDisplay> readElements   = elementDisplays.get(SecurityManager.READ);
            final List<ElementDisplay> editElements   = elementDisplays.get(SecurityManager.EDIT);
            final List<ElementDisplay> createElements = elementDisplays.get(SecurityManager.CREATE);
            log.trace("Cached {} elements with READ access, {} elements with EDIT access, and {} elements with CREATE access for user {} with cache ID {}:\n * READ: {}\n * EDIT: {}\n * CREATE: {}", readElements.size(), editElements.size(), createElements.size(), username, cacheId, formatElementDisplays(readElements), formatElementDisplays(editElements), formatElementDisplays(createElements));
        } else {
            log.info("Cached {} elements with READ access, {} elements with EDIT access, and {} elements with CREATE access for user {} with cache ID {}", elementDisplays.get(SecurityManager.READ).size(), elementDisplays.get(SecurityManager.EDIT).size(), elementDisplays.get(SecurityManager.CREATE).size(), username, cacheId);
        }

        cacheObject(cacheId, elementDisplays);

        return elementDisplays;
    }

    private void initReadableCountsForUsers(@Nullable final Collection<String> usernames) {
        if(CollectionUtils.isNotEmpty(usernames)) {
            log.info("Initializing readable counts cache entry for {} users: {}", usernames.size(), usernames);
            for (final String username : usernames) {
                initReadableCountsForUser(username);
            }
        } else {
            log.info("Request to initialize readable counts cache entry for users but the list of users is null or empty");
        }
    }

    private void initReadableCountsForUser(final String username) {
        final String cacheId = getCacheIdForUserElements(username, READABLE);
        log.debug("Retrieving readable counts for user {} through cache ID {}", username, cacheId);

        // Check whether the user has readable counts and browseable elements cached. We only need to refresh
        // for those who have them cached.
        final Map<String, Long> cachedReadableCounts = getCachedMap(cacheId);
        if(cachedReadableCounts != null) {
            try {
                log.debug("Found a cache entry for user '{}' readable counts by ID '{}', updating cache entry", username, cacheId);
                final XDATUser user = (XDATUser) Users.getUser(username);
                initReadableCountsForUser(cacheId, user);
                initBrowseableElementDisplaysForUser(getCacheIdForUserElements(username, BROWSEABLE), user);
            } catch (UserNotFoundException e) {
                log.warn("Got a user not found exception for username '{}', which is weird because this user has a cache entry.", username, e);
            } catch (UserInitException e) {
                log.error("An error occurred trying to retrieve the user '{}'", username, e);
            } catch (Exception e) {
                log.error("An unexpected error occurred trying to retrieve the readable counts for user '{}'", username, e);
            }
        }
    }

    private synchronized List<UserGroupI> initGroups(final List<UserGroupI> groups) {
        log.debug("Caching {} groups", groups.size());
        for (final UserGroupI group : groups) {
            initGroup(group.getId(), group);
        }
        return groups;
    }

    private void resetTotalCounts() {
        _totalCounts.clear();
        resetProjectCount();
        resetSubjectCount();
        resetImageSessionCount();
        final List<Map<String, Object>> elementCounts = _template.queryForList(EXPT_COUNTS_BY_TYPE, EmptySqlParameterSource.INSTANCE);
        for (final Map<String, Object> elementCount : elementCounts) {
            final String elementName = (String) elementCount.get("element_name");
            final Long   count       = (Long) elementCount.get("count");
            if(StringUtils.isBlank(elementName)) {
                log.warn("Found {} elements that are not associated with a valid data type:\n\n{}\n\nYou can correct some of these orphaned experiments by running the query:\n\n{}\n\nAny experiment IDs and data types returned from that query indicate data types that can not be resolved on the system (i.e. they don't exist in the primary data-type table).", count, _template.queryForList(QUERY_ORPHANED_EXPERIMENTS, EmptySqlParameterSource.INSTANCE).stream().map(experiment -> {
                    final String experimentId = (String) experiment.get("experiment_id");
                    final String dataType     = (String) experiment.get("data_type");
                    final int    extensionId  = (Integer) experiment.get("xdat_meta_element_id");
                    return " * " + experimentId + " was a " + dataType + ", " + (extensionId >= 0 ? "should be extension ID " + extensionId : "data type doesn't appear in xdat_meta_element table");
                }).collect(Collectors.joining("\n")), QUERY_CORRECT_ORPHANED_EXPERIMENTS);
            } else {
                _totalCounts.put(elementName, count);
            }
        }
    }

    private void resetProjectCount() {
        _totalCounts.put(XnatProjectdata.SCHEMA_ELEMENT_NAME, _template.queryForObject(PROJECT_COUNTS, EmptySqlParameterSource.INSTANCE, Long.class));
    }

    private void resetSubjectCount() {
        _totalCounts.put(XnatSubjectdata.SCHEMA_ELEMENT_NAME, _template.queryForObject(SUBJECT_COUNTS, EmptySqlParameterSource.INSTANCE, Long.class));
    }

    private void resetImageSessionCount() {
        _totalCounts.put(XnatImagesessiondata.SCHEMA_ELEMENT_NAME, _template.queryForObject(SESSION_COUNTS, EmptySqlParameterSource.INSTANCE, Long.class));
    }

    private Map<String, ElementDisplay> resetGuestBrowseableElementDisplays() {
        return resetBrowseableElementDisplays(getGuest());
    }

    private Map<String, ElementDisplay> resetBrowseableElementDisplays(final XDATUser user) {
        log.debug("Updating guest browseable element displays, clearing local cache, updating element access managers, and updating browseable element displays");
        user.clearLocalCache();
        final String username = user.getUsername();
        initElementAccessManagersForUser(getCacheIdForUserElementAccessManagers(username), username);
        return ImmutableMap.copyOf(initBrowseableElementDisplaysForUser(getCacheIdForUserElements(username, BROWSEABLE), user));
    }

    private Map<String, Long> getUserReadableSubjectsAndExperiments(final List<String> readableProjectIds, final List<Integer> userIds) {
        if(readableProjectIds.isEmpty()) {
            return Collections.emptyMap();
        }

        final MapSqlParameterSource userIdParameters               = new MapSqlParameterSource("userIds", userIds);
        final Map<String, Long>     readableExperimentCounts = _template.query(QUERY_USER_READABLE_EXPERIMENT_COUNT, userIdParameters, ELEMENT_COUNT_EXTRACTOR);


        final Map<String, Long>     readableScanCounts       = _template.query(QUERY_USER_READABLE_SCAN_COUNT, userIdParameters, ELEMENT_COUNT_EXTRACTOR);


        readableExperimentCounts.putAll(readableScanCounts);
        final Long readableSubjectCount = _template.queryForObject(QUERY_USER_READABLE_SUBJECT_COUNT, userIdParameters, Long.class);

        readableExperimentCounts.put(XnatSubjectdata.SCHEMA_ELEMENT_NAME, readableSubjectCount);
        return readableExperimentCounts;
    }

    private List<UserGroupI> getGroups(final String type, final String id) throws ItemNotFoundException {
        switch (type) {
            case XnatProjectdata.SCHEMA_ELEMENT_NAME:
                return getGroupsForTag(id);

            case XdatUsergroup.SCHEMA_ELEMENT_NAME:
                return Collections.singletonList(new UserGroup(id, _template));

            case XdatElementSecurity.SCHEMA_ELEMENT_NAME:
                return initGroups(getGroupIdsForDataType(id).stream().map(groupId -> {
                    try {
                        return new UserGroup(groupId, _template);
                    } catch (ItemNotFoundException e) {
                        log.warn("Somehow didn't find a usergroup that should exist: {}", groupId, e);
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return Collections.emptyList();
    }

    private List<String> getGroupIdsForDataType(final String dataType) {
        return _template.queryForList(QUERY_GET_GROUPS_FOR_DATATYPE, new MapSqlParameterSource("dataType", dataType), String.class);
    }

    private List<String> getGroupIdsForProject(final String projectId) {
        return _template.queryForList(QUERY_GET_GROUPS_FOR_TAG, new MapSqlParameterSource("tag", projectId), String.class);
    }

    /**
     * Retrieves a list of projects where the specified user is a member. This differs from {@link #getUserReadableProjects(String)} in that it
     * includes neither public projects nor projects to which the user has read access due to all data access privileges.
     *
     * @param username The username to retrieve projects for.
     *
     * @return A list of projects for which the specified user is a member.
     */
    private List<String> getUserProjects(final String username) {
        return _template.queryForList(QUERY_GET_PROJECTS_FOR_USER, new MapSqlParameterSource("username", username), String.class);
    }

    /**
     * Retrieves a list of projects where the specified user has read access. This differs from {@link #getUserProjects(String)} in that it
     * includes protected and public projects and projects to which the user has read access due to all data access privileges.
     *
     * @param username The username to retrieve projects for.
     *
     * @return A list of projects to which the specified user has read access.
     */
    private List<String> getUserReadableProjects(final String username) {
        return getProjectsByAccessQuery(username, QUERY_READABLE_PROJECTS, false);
    }

    private List<String> getUserEditableProjects(final String username) {
        return getProjectsByAccessQuery(username, QUERY_EDITABLE_PROJECTS, true);
    }

    private List<String> getUserOwnedProjects(final String username) {
        return getProjectsByAccessQuery(username, QUERY_OWNED_PROJECTS, true);
    }

    private List<String> getProjectsByAccessQuery(final String username, final String query, final boolean requireAdminAccess) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("username", username);
        return hasAllDataAdmin(username) || (hasAllDataAccess(username) && !requireAdminAccess)
               ? _template.queryForList(QUERY_ALL_DATA_ACCESS_PROJECTS, parameters, String.class)
               : _template.queryForList(query, parameters, String.class);
    }

    /**
     * Cheap test to see if the user is in a group that has data read access on all projects.
     *
     * @param username The username to be checked.
     *
     * @return Returns true if the user has all-data-access, false otherwise.
     */
    private boolean hasAllDataAccess(final String username) {
        return _template.queryForObject(QUERY_HAS_ALL_DATA_ACCESS, new MapSqlParameterSource("username", username), Boolean.class);
    }

    /**
     * Cheap test to see if the user is in a group that has data admin access on all projects.
     *
     * @param username The username to be checked.
     *
     * @return Returns true if the user has all-data-admin, false otherwise.
     */
    private boolean hasAllDataAdmin(final String username) {
        return _template.queryForObject(QUERY_HAS_ALL_DATA_ADMIN, new MapSqlParameterSource("username", username), Boolean.class);
    }

    private int initializeTags() {
        final List<String> tags = _template.queryForList(QUERY_ALL_TAGS, EmptySqlParameterSource.INSTANCE, String.class);
        for (final String tag : tags) {
            getTagGroups(tag);
        }
        final int size = tags.size();
        log.info("Completed initialization of {} tags", size);
        return size;
    }

    private Set<String> getProjectUsers(final String... projectIds) {
        return getProjectUsers(Arrays.asList(projectIds));
    }

    private Set<String> getProjectUsers(final Collection<String> projectIds) {
        return projectIds.isEmpty() ? Collections.emptySet() : new HashSet<>(_template.queryForList(QUERY_GET_USERS_FOR_PROJECTS, new MapSqlParameterSource("projectIds", projectIds), String.class));
    }

    private List<String> getTagGroups(final String tag) {
        final String       cacheId  = getCacheIdForTag(tag);
        final List<String> groupIds = getCachedList(cacheId);
        if(groupIds != null) {
            // If it's cached, we can just return the list.
            log.info("Found {} groups cached for tag '{}'", groupIds.size(), tag);
            return groupIds;
        }

        return initTag(getCacheIdForTag(tag), tag);
    }

    private List<String> getGroupIdsForUser(final String username) throws UserNotFoundException {
        final String       cacheId    = getCacheIdForUserGroups(username);
        final List<String> cachedList = getCachedList(cacheId);
        if(cachedList != null) {
            if(log.isTraceEnabled()) {
                log.trace("Found cached groups list for user '{}' with cache ID '{}': {}", username, cacheId, StringUtils.join(cachedList, ", "));
            } else {
                log.info("Found cached groups list for user '{}' with cache ID '{}' with {} entries", username, cacheId, cachedList.size());
            }
            return cachedList;
        }
        return initUserGroupIds(cacheId, username);
    }

    private void evictGroup(final String groupId, final Set<String> usernames) {
        final UserGroupI group = get(groupId);
        if(group != null) {
            final List<String> groupUsernames = group.getUsernames();
            log.debug("Found group for ID '{}' with {} associated users", groupId, groupUsernames.size());
            usernames.addAll(groupUsernames);
            evict(groupId);
        } else {
            log.info("Requested to evict group with ID '{}', but I couldn't find that actual group", groupId);
        }
    }

    /**
     * Checks whether the user exists. If not, this throws the {@link UserNotFoundException}. Otherwise, it returns
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
        if(!_userChecks.containsKey(username) || !_userChecks.get(username)) {
            // See if the user exists now. The non-existent user existing should be updated with the add user event,
            // but we don't have a clearly defined handler for that yet.
            _userChecks.put(username, _template.queryForObject(UserManagementServiceI.QUERY_CHECK_USER_EXISTS, parameters, Boolean.class));
        }
        if(!_userChecks.get(username)) {
            throw new UserNotFoundException(username);
        }
        return parameters;
    }

    private XDATUser getGuest() {
        if(_guest == null) {
            log.debug("No guest user initialized, trying to retrieve now.");
            try {
                final UserI guest = Users.getGuest();
                if(guest instanceof XDATUser) {
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
        return _guest != null ? StringUtils.equalsIgnoreCase(_guest.getUsername(), username) : StringUtils.equalsIgnoreCase(DEFAULT_GUEST_USERNAME, username);
    }

    private List<UserGroupI> getUserGroupList(final List<String> groupIds) {
        return groupIds == null || groupIds.isEmpty() ? new ArrayList<>() : groupIds.stream().map(this::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private UserGroupI getCachedGroup(final String cacheId) {
        return getCachedObject(cacheId, UserGroupI.class);
    }

    private List<String> getCacheIdsForUserReadableCounts() {
        return getEhCacheKeys().stream().filter(key -> REGEX_USER_READABLE_COUNTS.matcher(key).matches()).collect(Collectors.toList());
    }

    private List<String> getCacheIdsForActions() {
        return getCacheIdsForPrefix(ACTIONS_PREFIX);
    }

    private List<String> getCacheIdsForUserElements() {
        return getCacheIdsForPrefix(USER_ELEMENT_PREFIX);
    }

    private List<String> getCacheIdsForUserElements(final String username) {
        return getCacheIdsForPrefix(USER_ELEMENT_PREFIX, username);
    }

    private List<String> getCacheIdsForPrefix(final String... prefixes) {
        final Pattern pattern = Pattern.compile(("^" + String.join(":", prefixes) + ":.*$"));
        return getEhCacheKeys().stream().filter(key -> pattern.matcher(key).matches()).collect(Collectors.toList());
    }

    private List<String> getCacheIdsForUsername(final String username) {
        return getEhCacheKeys().stream().filter(cacheId -> StringUtils.equals(username, getUsernameFromCacheId(cacheId))).collect(Collectors.toList());
    }

    private static String getUsernameFromCacheId(final @Nullable String cacheId) {
        if(StringUtils.isBlank(cacheId)) {
            return null;
        }
        final Matcher matcher = REGEX_EXTRACT_USER_FROM_CACHE_ID.matcher(cacheId);
        if(!matcher.matches()) {
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
        if(readableCounts.size() > 3) {
            for (final String type : readableCounts.keySet()) {
                if(!StringUtils.equalsAny(type, XnatProjectdata.SCHEMA_ELEMENT_NAME, WrkWorkflowdata.SCHEMA_ELEMENT_NAME, XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
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

    private static String getCacheIdForUserElements(final String username, final String elementType) {
        return createCacheIdFromElements(USER_ELEMENT_PREFIX, username, elementType);
    }

    private static String getCacheIdForActionElements(final String username) {
        return createCacheIdFromElements(ACTIONS_PREFIX, username);
    }

    private static boolean isGroupsAndPermissionsCacheEvent(final Ehcache cache) {
        return StringUtils.equals(CACHE_NAME, cache.getName());
    }

    private static String formatElementDisplays(final List<ElementDisplay> elementDisplays) {
        return elementDisplays.stream().map(ElementDisplay::getElementName).collect(Collectors.joining(", "));
    }

    private static final ResultSetExtractor<Map<String, Long>> ELEMENT_COUNT_EXTRACTOR = results -> {
        final Map<String, Long> elementCounts = new HashMap<>();
        while (results.next()) {
            final String elementName  = results.getString("element_name");
            final long   elementCount = results.getLong("element_count");
            elementCounts.put(elementName, elementCount);
        }
        return elementCounts;
    };

    private static final DateFormat   DATE_FORMAT   = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.getDefault());

    private static final String QUERY_GET_GROUP_FOR_USER_AND_TAG     = "SELECT id " +
                                                                       "FROM xdat_usergroup xug " +
                                                                       "  LEFT JOIN xdat_user_groupid xugid ON xug.id = xugid.groupid " +
                                                                       "  LEFT JOIN xdat_user xu ON xugid.groups_groupid_xdat_user_xdat_user_id = xu.xdat_user_id " +
                                                                       "WHERE xu.login = :username AND tag = :tag " +
                                                                       "ORDER BY groupid";
    private static final String QUERY_GET_GROUPS_FOR_DATATYPE        = "SELECT DISTINCT usergroup.id AS group_name " +
                                                                       "FROM xdat_usergroup usergroup " +
                                                                       "  LEFT JOIN xdat_element_access xea ON usergroup.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
                                                                       "WHERE " +
                                                                       "  xea.element_name = :dataType " +
                                                                       "ORDER BY group_name";
    private static final String QUERY_ALL_GROUPS                     = "SELECT id FROM xdat_usergroup";
    private static final String QUERY_ALL_TAGS                       = "SELECT DISTINCT tag FROM xdat_usergroup WHERE tag IS NOT NULL AND tag <> ''";
    private static final String QUERY_GET_GROUPS_FOR_TAG             = "SELECT id FROM xdat_usergroup WHERE tag = :tag";
    private static final String QUERY_GET_ALL_MEMBER_GROUPS          = "SELECT " +
                                                                       "  tag AS project_id, " +
                                                                       "  id AS group_id " +
                                                                       "FROM " +
                                                                       "  xdat_usergroup " +
                                                                       "WHERE " +
                                                                       "  tag IS NOT NULL AND " +
                                                                       "  id LIKE '%_member' " +
                                                                       "ORDER BY project_id, group_id";
    private static final String QUERY_GET_ALL_COLLAB_GROUPS          = "SELECT " +
                                                                       "  tag AS project_id, " +
                                                                       "  id AS group_id " +
                                                                       "FROM " +
                                                                       "  xdat_usergroup " +
                                                                       "WHERE " +
                                                                       "  tag IS NOT NULL AND " +
                                                                       "  id LIKE '%_collaborator' " +
                                                                       "ORDER BY project_id, group_id";
    private static final String QUERY_GET_EXPERIMENT_PROJECT         = "SELECT project FROM xnat_experimentdata WHERE id = :experimentId";
    private static final String QUERY_GET_SUBJECT_PROJECT            = "SELECT project FROM xnat_subjectdata WHERE id = :subjectId OR label = :subjectId";
    private static final String QUERY_GET_USERS_FOR_PROJECTS         = "SELECT DISTINCT login " +
                                                                       "FROM xdat_user u " +
                                                                       "  LEFT JOIN xdat_user_groupid gid ON u.xdat_user_id = gid.groups_groupid_xdat_user_xdat_user_id " +
                                                                       "  LEFT JOIN xdat_usergroup g ON gid.groupid = g.id " +
                                                                       "  LEFT JOIN xdat_element_access xea ON g.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
                                                                       "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                       "  LEFT JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                                       "WHERE tag IN (:projectIds) OR (tag IS NULL AND field_value = '*') " +
                                                                       "ORDER BY login";
    private static final String QUERY_GET_PROJECTS_FOR_USER          = "SELECT DISTINCT " +
                                                                       "  g.tag AS project " +
                                                                       "FROM xdat_usergroup g " +
                                                                       "  LEFT JOIN xdat_user_groupid gid ON g.id = gid.groupid " +
                                                                       "  LEFT JOIN xdat_user u ON gid.groups_groupid_xdat_user_xdat_user_id = u.xdat_user_id " +
                                                                       "WHERE g.tag IS NOT NULL AND " +
                                                                       "      u.login = :username";
    private static final String QUERY_PROJECT_OWNERS                 = "SELECT DISTINCT u.login AS owner " +
                                                                       "FROM xdat_user                     u " +
                                                                       "  LEFT JOIN xdat_user_groupid      map ON u.xdat_user_id = map.groups_groupid_xdat_user_xdat_user_id " +
                                                                       "  LEFT JOIN xdat_usergroup         g ON map.groupid = g.id " +
                                                                       "  LEFT JOIN xdat_element_access    xea ON (g.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id OR u.xdat_user_id = xea.xdat_user_xdat_user_id) " +
                                                                       "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                       "  LEFT JOIN xdat_field_mapping     xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                                       "WHERE " +
                                                                       "  xfm.field_value != '*' AND " +
                                                                       "  xea.element_name = 'xnat:projectData' AND " +
                                                                       "  xfm.delete_element = 1 AND " +
                                                                       "  g.id LIKE '%_owner' AND " +
                                                                       "  g.tag = :projectId " +
                                                                       "ORDER BY owner";
    private static final String QUERY_USER_PERMISSIONS               = "SELECT " +
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
                                                                       "  LEFT JOIN xdat_user_groupid i ON u.xdat_user_id = i.groups_groupid_xdat_user_xdat_user_id " +
                                                                       "  LEFT JOIN xdat_usergroup g ON i.groupid = g.id " +
                                                                       "  LEFT JOIN xdat_element_access xea ON u.xdat_user_id = xea.xdat_user_xdat_user_id OR g.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
                                                                       "  LEFT JOIN xdat_element_access_meta_data xeamd ON xea.element_access_info = xeamd.meta_data_id " +
                                                                       "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                       "  LEFT JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                                       "WHERE " +
                                                                       "  u.login = :username";
    private static final String QUERY_ACCESSIBLE_DATA_PROJECTS       = "SELECT  " +
                                                                       "  project  " +
                                                                       "FROM  " +
                                                                       "  (SELECT DISTINCT f.field_value AS project  " +
                                                                       "   FROM  " +
                                                                       "     xdat_user u  " +
                                                                       "     LEFT JOIN xdat_user_groupid map ON u.xdat_user_id = map.groups_groupid_xdat_user_xdat_user_id  " +
                                                                       "     LEFT JOIN xdat_usergroup g ON map.groupid = g.id  " +
                                                                       "     LEFT JOIN xdat_element_access a ON (g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id OR u.xdat_user_id = a.xdat_user_xdat_user_id)  " +
                                                                       "     LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id  " +
                                                                       "     LEFT JOIN xdat_field_mapping f ON s.xdat_field_mapping_set_id = f.xdat_field_mapping_set_xdat_field_mapping_set_id  " +
                                                                       "   WHERE  " +
                                                                       "     f.field_value != '*' AND  " +
                                                                       "     a.element_name = 'xnat:subjectData' AND  " +
                                                                       "     f.%s = 1 AND  " +
                                                                       "     u.login IN ('guest', :username)) projects";
    private static final String QUERY_OWNED_PROJECTS                 = String.format(QUERY_ACCESSIBLE_DATA_PROJECTS, "delete_element");
    private static final String QUERY_EDITABLE_PROJECTS              = String.format(QUERY_ACCESSIBLE_DATA_PROJECTS, "edit_element");
    private static final String QUERY_READABLE_PROJECTS              = String.format(QUERY_ACCESSIBLE_DATA_PROJECTS, "read_element");
    private static final String QUERY_HAS_ALL_DATA_PRIVILEGES        = "SELECT  " +
                                                                       "  EXISTS(SELECT TRUE  " +
                                                                       "         FROM  " +
                                                                       "           xdat_user u  " +
                                                                       "           LEFT JOIN xdat_user_groupid map ON u.xdat_user_id = map.groups_groupid_xdat_user_xdat_user_id  " +
                                                                       "           LEFT JOIN xdat_usergroup ug ON map.groupid = ug.id  " +
                                                                       "           LEFT JOIN xdat_element_access ea ON (ug.xdat_usergroup_id = ea.xdat_usergroup_xdat_usergroup_id OR u.xdat_user_id = ea.xdat_user_xdat_user_id)  " +
                                                                       "           LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id  " +
                                                                       "           LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id = fm.xdat_field_mapping_set_xdat_field_mapping_set_id  " +
                                                                       "         WHERE  " +
                                                                       "           fm.field_value = '*' AND  " +
                                                                       "           ea.element_name = 'xnat:projectData' AND  " +
                                                                       "           fm.%s = 1 AND  " +
                                                                       "           u.login = :username)";
    private static final String QUERY_HAS_ALL_DATA_ACCESS            = String.format(QUERY_HAS_ALL_DATA_PRIVILEGES, "read_element");
    private static final String QUERY_HAS_ALL_DATA_ADMIN             = String.format(QUERY_HAS_ALL_DATA_PRIVILEGES, "edit_element");
    private static final String QUERY_ALL_DATA_ACCESS_PROJECTS       = "SELECT id AS project FROM xnat_projectdata ORDER BY project";
    private static final String QUERY_USER_READABLE_WORKFLOW_COUNT   = "SELECT reltuples::bigint AS COUNT FROM   pg_class WHERE  oid = 'public.wrk_workflowdata'::regclass";
    private static final String QUERY_USER_READABLE_SUBJECT_COUNT    = "SELECT SUM(subjs.COUNT) AS ELEMENT_COUNT\n"
                                                                        + "FROM xdat_element_access xea \n"
                                                                        + "LEFT JOIN xdat_usergroup grp ON xea.xdat_usergroup_xdat_usergroup_id=grp.xdat_usergroup_id\n"
                                                                        + "LEFT JOIN xdat_user_groupid gid ON grp.id=gid.groupid\n"
                                                                        + "LEFT JOIN xdat_field_mapping_set fms ON xea.xdat_element_access_id=fms.permissions_allow_set_xdat_elem_xdat_element_access_id\n"
                                                                        + "LEFT JOIN xdat_field_mapping xfm ON fms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND xfm.read_element=1 AND 'xnat:subjectData/project'=xfm.field\n"
                                                                        + "JOIN (\n"
                                                                        + "\tSELECT project, COUNT(id) FROM xnat_subjectData GROUP BY project UNION SELECT project, COUNT(subject_id) FROM xnat_projectParticipant GROUP BY project\n"
                                                                        + ") subjs ON xfm.field_value=subjs.project\n"
                                                                        + "WHERE gid.groups_groupid_xdat_user_xdat_user_id IN (:userIds) OR xea.xdat_user_xdat_user_id IN (:userIds)";
    private static final String QUERY_USER_READABLE_EXPERIMENT_COUNT = "SELECT xea.element_name, SUM(expts.SUM) AS ELEMENT_COUNT\n"
                                                                        + "FROM xdat_user_groupid gid\n"
                                                                        + "LEFT JOIN xdat_usergroup grp ON gid.groupid=grp.id\n"
                                                                        + "LEFT JOIN xdat_element_access xea ON grp.xdat_usergroup_id=xea.xdat_usergroup_xdat_usergroup_id\n"
                                                                        + "LEFT JOIN xdat_field_mapping_set fms ON xea.xdat_element_access_id=fms.permissions_allow_set_xdat_elem_xdat_element_access_id\n"
                                                                        + "LEFT JOIN xdat_field_mapping xfm ON fms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND xfm.read_element=1 AND xea.element_name || '/project'=xfm.field\n"
                                                                        + "JOIN (\n"
                                                                        + "\tSELECT project, element_name, SUM(COUNT) FROM (\n"
                                                                        + "\t\tSELECT project, element_name, COUNT(id) FROM xnat_experimentData LEFT JOIN xdat_meta_element xme ON xnat_experimentData.extension=xme.xdat_meta_element_id GROUP BY project, element_name\n"
                                                                        + "  \t\tUNION \n"
                                                                        + "  \t\tSELECT shr.project, element_name, COUNT(expt.id) FROM xnat_experimentData_share shr LEFT JOIN xnat_experimentData expt ON shr.sharing_share_xnat_experimentda_id=expt.id LEFT JOIN xdat_meta_element xme ON expt.extension=xme.xdat_meta_element_id GROUP BY shr.project, element_name\n"
                                                                        + "  \t) SRCH GROUP BY project,element_name\n"
                                                                        + ") expts ON xfm.field_value=expts.project AND xea.element_name=expts.element_name\n"
                                                                        + "WHERE gid.groups_groupid_xdat_user_xdat_user_id IN (:userIds)\n"
                                                                        + "GROUP BY xea.element_name";
    private static final String QUERY_USER_READABLE_SCAN_COUNT       = "SELECT xea.element_name, SUM(expts.SUM) AS ELEMENT_COUNT "
                                                                        + " FROM xdat_user_groupid gid"
                                                                        + " LEFT JOIN xdat_usergroup grp ON gid.groupid=grp.id"
                                                                        + " LEFT JOIN xdat_element_access xea ON grp.xdat_usergroup_id=xea.xdat_usergroup_xdat_usergroup_id"
                                                                        + " LEFT JOIN xdat_field_mapping_set fms ON xea.xdat_element_access_id=fms.permissions_allow_set_xdat_elem_xdat_element_access_id"
                                                                        + " LEFT JOIN xdat_field_mapping xfm ON fms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND xfm.read_element=1 AND xea.element_name || '/project'=xfm.field"
                                                                        + " JOIN ("
                                                                        + "  SELECT project, element_name, SUM(COUNT) FROM ("
                                                                        + "   SELECT project, element_name, COUNT(id) FROM xnat_imageScandata LEFT JOIN xdat_meta_element xme ON xnat_imageScandata.extension=xme.xdat_meta_element_id GROUP BY project, element_name"
                                                                        + "   UNION "
                                                                        + "   SELECT shr.project, element_name, COUNT(expt.id) FROM xnat_imageScandata_share shr LEFT JOIN xnat_imageScandata expt ON shr.sharing_share_xnat_imagescandat_xnat_imagescandata_id=expt.xnat_imagescandata_id LEFT JOIN xdat_meta_element xme ON expt.extension=xme.xdat_meta_element_id GROUP BY shr.project, element_name"
                                                                        + "  ) SRCH GROUP BY project,element_name"
                                                                        + " ) expts ON xfm.field_value=expts.project AND xea.element_name=expts.element_name"
                                                                        + " WHERE gid.groups_groupid_xdat_user_xdat_user_id IN (:userIds)"
                                                                        + " GROUP BY xea.element_name";

    private static final String QUERY_ORPHANED_EXPERIMENTS         = "SELECT " +
                                                                     "    experiment_id, " +
                                                                     "    data_type, " +
                                                                     "    coalesce(xdat_meta_element_id, -1) AS xdat_meta_element_id " +
                                                                     "FROM " +
                                                                     "    data_type_views_experiments_without_data_type";
    private static final String QUERY_CORRECT_ORPHANED_EXPERIMENTS = "SELECT\n" +
                                                                     "    orphaned_experiment,\n" +
                                                                     "    original_data_type\n" +
                                                                     "FROM\n" +
                                                                     "    data_type_fns_correct_experiment_extension()";


    private static final String EXPT_COUNTS_BY_TYPE                = "SELECT element_name, COUNT(ID) AS count FROM xnat_experimentData expt LEFT JOIN xdat_meta_element xme ON expt.extension=xme.xdat_meta_element_id GROUP BY element_name";
    private static final String PROJECT_COUNTS                     = "SELECT COUNT(*) FROM xnat_projectdata";
    private static final String SUBJECT_COUNTS                     = "SELECT COUNT(*) FROM xnat_subjectdata";
    private static final String SESSION_COUNTS                     = "SELECT COUNT(*) FROM xnat_experimentdata";
    private static final String ACTIONS_PREFIX                     = "actions";
    private static final String TAG_PREFIX                         = "tag";
    private static final String PROJECT_PREFIX                     = "project";
    private static final String USER_ELEMENT_PREFIX                = "user";
    private static final String ELEMENT_ACCESS_MANAGERS_PREFIX     = "eam";
    private static final String GROUPS_ELEMENT_PREFIX              = "groups";
    private static final String GUEST_ACTION_READ                  = getCacheIdForActionElements(DEFAULT_GUEST_USERNAME);

    private static final Pattern REGEX_EXTRACT_USER_FROM_CACHE_ID   = Pattern.compile("^(?<prefix>" + ACTIONS_PREFIX + "|" + USER_ELEMENT_PREFIX + "):(?<username>[^:]+)(?<remainder>:.*)?$");
    private static final Pattern REGEX_USER_READABLE_COUNTS         = Pattern.compile("^(?<prefix>" + USER_ELEMENT_PREFIX + "):(?<username>[^:]+):" + READABLE + "$");
    private static final Pattern REGEX_USER_PROJECT_ACCESS_CACHE_ID = Pattern.compile("^" + USER_ELEMENT_PREFIX + ":(?<username>[A-z0-9_]+):" + XnatProjectdata.SCHEMA_ELEMENT_NAME + ":(?<access>[A-z]+)$");
    private static final String  GUEST_CACHE_ID                     = getCacheIdForUserElements(DEFAULT_GUEST_USERNAME, BROWSEABLE);

    private static final Predicate<ElementDisplay> CONTAINS_MR_SESSION = display -> StringUtils.equalsIgnoreCase(XnatMrsessiondata.SCHEMA_ELEMENT_NAME, display.getElementName());

    private final NamedParameterJdbcTemplate _template;
    private final JmsTemplate                _jmsTemplate;
    private final DatabaseHelper             _helper;
    private final Map<String, Long>          _totalCounts;
    private final Map<String, Long>          _missingElements;
    private final Map<String, Boolean>       _userChecks;
    private final AtomicBoolean              _initialized;

    private Listener _listener;
    private XDATUser _guest;
}
