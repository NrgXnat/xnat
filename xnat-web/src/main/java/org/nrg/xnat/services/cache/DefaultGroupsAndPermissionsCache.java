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
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.Initializing;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.methods.XftItemEventCriteria;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.exception.XFTInitException;
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
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static org.nrg.framework.exceptions.NrgServiceError.ConfigurationError;
import static org.nrg.xapi.rest.users.DataAccessApi.BROWSEABLE;
import static org.nrg.xapi.rest.users.DataAccessApi.READABLE;
import static org.nrg.xft.event.XftItemEventI.*;

@SuppressWarnings("Duplicates")
@Service
@Slf4j
public class DefaultGroupsAndPermissionsCache extends AbstractXftItemAndCacheEventHandlerMethod implements GroupsAndPermissionsCache, Initializing, GroupsAndPermissionsCache.Provider {
    @Autowired
    public DefaultGroupsAndPermissionsCache(final CacheManager cacheManager, final NamedParameterJdbcTemplate template, final JmsTemplate jmsTemplate) {
        super(cacheManager,
              XftItemEventCriteria.getXsiTypeCriteria(XnatProjectdata.SCHEMA_ELEMENT_NAME),
              XftItemEventCriteria.builder().xsiType(XnatSubjectdata.SCHEMA_ELEMENT_NAME).xsiType(XnatExperimentdata.SCHEMA_ELEMENT_NAME).actions(CREATE, DELETE, SHARE).build(),
              XftItemEventCriteria.getXsiTypeCriteria(XdatUsergroup.SCHEMA_ELEMENT_NAME),
              XftItemEventCriteria.getXsiTypeCriteria(XdatElementSecurity.SCHEMA_ELEMENT_NAME));

        _template = template;
        _jmsTemplate = jmsTemplate;
        _helper = new DatabaseHelper((JdbcTemplate) _template.getJdbcOperations());
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
            // Here we can just return the value directly as a map, because we know there's something cached
            // and that what's cached is not a string.
            log.info("Found a cache entry for user '{}' readable counts by ID '{}'", username, cacheId);
            return getCachedMap(cacheId);
        }

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
        final Map<String, ElementDisplay> browseables = cacheBrowseableElementDisplays(user, cacheId);
        browseables.putAll(getBrowseableElementDisplays(_guest));
        return browseables;
    }

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

        try {
            final Map<String, Long> readableCounts = new HashMap<>();
            try {
                //projects
                getUserReadableCount(user, readableCounts, "xnat:projectData", "xnat:projectData/ID");

                //workflows
                getUserReadableCount(user, readableCounts, "wrk:workflowData", "wrk:workflowData/ID");

                //subjects
                getUserReadableCount(user, readableCounts, "xnat:subjectData", "xnat:subjectData/ID");

                log.debug("Found {} readable elements for user {}, caching with ID {}", readableCounts.size(), username, cacheId);
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

    /**
     * List of {@link ElementDisplay element displays} that this user can invoke.
     *
     * @return A list of all {@link ElementDisplay element displays} that this user can invoke.
     *
     * @throws Exception When an error occurs.
     */
    @Override
    public List<ElementDisplay> getActionElementDisplays(final UserI user, final String action) throws Exception {
        if (!ACTIONS.contains(action)) {
            throw new NrgServiceRuntimeException(ConfigurationError, "The action '" + action + "' is invalid, must be one of: " + StringUtils.join(ACTIONS, ", "));
        }

        final String username = user.getUsername();
        final String cacheId  = getCacheIdForActionElements(username, action);

        // Check whether the action elements are cached and, if so, return that.
        if (has(cacheId)) {
            // Here we can just return the value directly as a list, because we know there's something cached
            // and that what's cached is not a string.
            log.debug("Found a cache entry for user '{}' action '{}' elements by ID '{}'", username, action, cacheId);
            return getCachedList(cacheId);
        }

        final Multimap<String, ElementDisplay> elementDisplays = ArrayListMultimap.create();
        for (final ElementSecurity elementSecurity : ElementSecurity.GetSecureElements()) {
            try {
                final SchemaElement schemaElement = elementSecurity.getSchemaElement();
                if (schemaElement != null) {
                    if (schemaElement.hasDisplay()) {
                        if (Permissions.canAny(user, elementSecurity.getElementName(), action)) {
                            final ElementDisplay elementDisplay = schemaElement.getDisplay();
                            if (elementDisplay != null) {
                                elementDisplays.put(action, elementDisplay);
                            }
                        }
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
        for (final String foundAction : elementDisplays.keySet()) {
            final String               actionCacheId         = getCacheIdForActionElements(username, foundAction);
            final List<ElementDisplay> actionElementDisplays = new ArrayList<>(elementDisplays.get(foundAction));
            log.info("Caching {} elements for action {} for user {} with cache ID {}", actionElementDisplays.size(), action, username, actionCacheId);
            cacheObject(actionCacheId, actionElementDisplays);
        }

        return ImmutableList.copyOf(elementDisplays.get(action));
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
        return getCache().get(cacheId) != null;
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
            return getCachedGroup(groupId);
        }

        return cacheGroup(groupId);
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
        final String groupId = _template.query(QUERY_GET_GROUP_FOR_USER_AND_TAG, parameters, new ResultSetExtractor<String>() {
            @Override
            public String extractData(final ResultSet results) throws DataAccessException, SQLException {
                return results.next() ? results.getString("id") : null;
            }
        });
        return StringUtils.isNotBlank(groupId) ? get(groupId) : null;
    }

    @Override
    public Date getGroupLastUpdateTime(final String groupId) {
        if (!has(groupId)) {
            return null;
        }
        return new Date(getLatestOfCreationAndUpdateTime(groupId));
    }

    @Override
    public Date getUserLastUpdateTime(final UserI user) {
        return getUserLastUpdateTime(user.getUsername());
    }

    @Override
    public Date getUserLastUpdateTime(final String username) {
        try {
            final List<String> groupIds  = getGroupIdsForUser(username);
            long               timestamp = 0;
            for (final String groupId : groupIds) {
                final Date lastUpdateTime = getGroupLastUpdateTime(groupId);
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
            if (_listener == null) {
                return false;
            }
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

        try {
            _guest = new XDATUser("guest");
            updateGuestBrowseableElementDisplays();
        } catch (UserNotFoundException ignored) {
            // Guest is always available.
        } catch (UserInitException e) {
            log.error("An error occurred initializing the user guest", e);
        }
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
            case XnatSubjectdata.SCHEMA_ELEMENT_NAME:
                return handleSubjectEvents(event);

            case XnatProjectdata.SCHEMA_ELEMENT_NAME:
                return handleProjectEvents(event);

            case XdatUsergroup.SCHEMA_ELEMENT_NAME:
            case XdatElementSecurity.SCHEMA_ELEMENT_NAME:
                return handleGroupRelatedEvents(event);

            default:
                // This is always some type of experiment.
                return handleExperimentEvents(event);
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
                    log.debug("New project created with ID {}, caching new instance", xsiType, id);
                    updateUserReadableCounts(getProjectUsers(id));
                    return !cacheGroups(getGroups(xsiType, id)).isEmpty();

                case UPDATE:
                    log.debug("The {} object {} was updated, caching updated instance", xsiType, id);
                    if (properties.containsKey("accessibility")) {
                        final String accessibility = (String) properties.get("accessibility");
                        if (StringUtils.equalsAnyIgnoreCase(accessibility, "protected", "public")) {
                            // Just update existing user element displays
                            final List<String> cacheIds = getCacheIdsForActions();
                            cacheIds.addAll(getCacheIdsForUserElements());
                            updateGuestBrowseableElementDisplays();
                            updateUserReadableCounts(Sets.newHashSet(Iterables.filter(Lists.transform(cacheIds, new Function<String, String>() {
                                @Nullable
                                @Override
                                public String apply(@Nullable final String cacheId) {
                                    if (StringUtils.isBlank(cacheId)) {
                                        return null;
                                    }
                                    final Matcher matcher = REGEX_EXTRACT_USER_FROM_CACHE_ID.matcher(cacheId);
                                    if (!matcher.matches()) {
                                        return null;
                                    }
                                    return matcher.group("username");
                                }
                            }), Predicates.notNull())));
                            return !cacheGroups(getGroups(xsiType, id)).isEmpty();
                        }
                    }

                case DELETE:
                    log.debug("The {} {} was deleted, removing related instances from cache", xsiType, id);
                    final String cacheId = getCacheIdForProject(id);
                    updateGuestBrowseableElementDisplays();
                    updateUserReadableCounts(this.<String>getCachedSet(cacheId));
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

    private boolean handleExperimentEvents(final XftItemEventI event) {
        final String action = event.getAction();
        log.debug("Handling experiment {} event for {} {}", XftItemEventI.ACTIONS.get(action), event.getXsiType(), event.getId());
        final Set<String> projectIds = new HashSet<>();
        switch (action) {
            case CREATE:
                projectIds.add(_template.queryForObject(QUERY_GET_EXPERIMENT_PROJECT, new MapSqlParameterSource("experimentId", event.getId()), String.class));
                break;

            case SHARE:
                projectIds.add((String) event.getProperties().get("target"));
                break;

            case MOVE:
                projectIds.add((String) event.getProperties().get("origin"));
                projectIds.add((String) event.getProperties().get("target"));
                break;

            default:
                log.warn("I was informed that the '{}' action happened to experiment '{}' with ID '{}'. I don't know what to do with this action.", action, event.getXsiType(), event.getId());
        }
        if (projectIds.isEmpty()) {
            return false;
        }
        final Map<String, ElementDisplay> displays = getBrowseableElementDisplays(_guest);
        if (!displays.containsKey(event.getXsiType())) {
            updateGuestBrowseableElementDisplays();
        }
        updateUserReadableCounts(getProjectUsers(projectIds));
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

                case DELETE:
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

    private void updateUserReadableCounts(final Set<String> usernames) {
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
                log.debug("Found a cache entry for user '{}' readable counts by ID '{}'", username, cacheId);
                final XDATUser user = new XDATUser(username);

                final Map<String, Long> readableCounts = getCachedMap(cacheId);
                getUserReadableCount(user, readableCounts, "xnat:subjectData", "xnat:subjectData/ID");

                log.debug("Found {} readable elements for user {}, caching with ID {}", readableCounts.size(), username, cacheId);
                cacheObject(cacheId, readableCounts);

                log.debug("Retrieving browseable element displays for user {} through cache ID {}", username, cacheId);
                cacheBrowseableElementDisplays(user, getCacheIdForUserElements(username, BROWSEABLE));
            } catch (UserNotFoundException e) {
                log.warn("Got a user not found exception for username '{}', which is weird because this user has a cache entry.", username, e);
            } catch (UserInitException e) {
                log.error("An error occurred trying to retrieve the user '{}'", username, e);
            } catch (Exception e) {
                log.error("An unexpected error occurred trying to retrieve the readable counts for user '{}'", username, e);
            }
        }
    }

    private Map<String, ElementDisplay> cacheBrowseableElementDisplays(final UserI user, final String cacheId) {
        final String                      username   = user.getUsername();
        final Map<String, Long>           counts     = getReadableCounts(user);
        final Map<String, ElementDisplay> browseable = new HashMap<>();

        try {
            final List<ElementDisplay> actionElementDisplays = getActionElementDisplays(user, SecurityManager.READ);
            log.debug("Found {} action element displays for user {}", actionElementDisplays.size(), username);
            for (final ElementDisplay elementDisplay : actionElementDisplays) {
                final String elementName = elementDisplay.getElementName();
                if (ElementSecurity.IsBrowseableElement(elementName) && counts.containsKey(elementName) && counts.get(elementName) > 0) {
                    log.debug("Adding element display {} to cache entry {}", elementName, cacheId);
                    browseable.put(elementName, elementDisplay);
                }
            }

            log.info("Adding {} element displays to cache entry {}", browseable.size(), cacheId);
            cacheObject(cacheId, browseable);
            return browseable;
        } catch (ElementNotFoundException e) {
            log.warn("Element '{}' not found. This may be a data type that was installed previously but can't be located now.", e.ELEMENT);
        } catch (XFTInitException e) {
            log.error("There was an error initializing or accessing XFT", e);
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
        }

        log.info("No browseable element displays found for user {}", username);
        return Collections.emptyMap();
    }

    protected Map<String, ElementDisplay> updateGuestBrowseableElementDisplays() {
        return cacheBrowseableElementDisplays(_guest, GUEST_CACHE_ID);
    }

    private void getUserReadableCount(final UserI user, final Map<String, Long> readableCounts, final String dataType, final String dataTypeIdField) throws Exception {
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
                log.debug("Executed element count query and found {} elements: {}\nQuery: \"{}\"", results.size(), results, elementCountQuery);
            }
            readableCounts.putAll(results);
        }
    }

    private List<UserGroupI> getGroups(final String type, final String id) throws ItemNotFoundException {
        switch (type) {
            case XnatProjectdata.SCHEMA_ELEMENT_NAME:
                return getGroupsForTag(id);

            case XdatUsergroup.SCHEMA_ELEMENT_NAME:
                return Collections.<UserGroupI>singletonList(new UserGroup(id, _template));

            case XdatElementSecurity.SCHEMA_ELEMENT_NAME:
                final List<String> groupIds = _template.queryForList(QUERY_GET_GROUPS_FOR_DATATYPE, new MapSqlParameterSource("dataType", id), String.class);
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

    private List<String> getGroupIds(final String projectId) {
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

    private Set<String> getProjectUsers(final String projectId) {
        return getProjectUsers(Collections.singletonList(projectId));
    }

    private Set<String> getProjectUsers(final Collection<String> projectIds) {
        return new HashSet<>(_template.queryForList(QUERY_GET_USERS_FOR_PROJECTS, new MapSqlParameterSource("projectIds", projectIds), String.class));
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
        final List<String> groups = getGroupIds(tag);

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

    private List<String> getTagGroups(final String cacheId) {
        return getCachedList(cacheId);
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

        return newArrayList(Iterables.filter(Iterables.transform(Iterables.filter(groupIds, String.class), new Function<String, UserGroupI>() {
            @Nullable
            @Override
            public UserGroupI apply(@Nullable final String groupId) {
                return get(groupId);
            }
        }), Predicates.notNull()));
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

    private List<UserGroupI> cacheGroups(final List<UserGroupI> groups) {
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

    private UserGroupI getCachedGroup(final String cacheId) {
        return getCachedObject(cacheId, UserGroupI.class);
    }

    private List<String> getCacheIdsForActions() {
        return getCacheIdsForPrefix(ACTION_PREFIX);
    }

    private List<String> getCacheIdsForUserElements() {
        return getCacheIdsForPrefix(USER_ELEMENT_PREFIX);
    }

    private List<String> getCacheIdsForPrefix(final String prefix) {
        return Lists.newArrayList(Iterables.filter(Iterables.filter(getEhCache().getKeys(), String.class), Predicates.containsPattern("^" + prefix + ":.*$")));
    }

    private static String getCacheIdForTag(final String tag) {
        return StringUtils.startsWith(tag, TAG_PREFIX) ? tag : createCacheIdFromElements(TAG_PREFIX, tag);
    }

    private static String getCacheIdForProject(final String projectId) {
        return StringUtils.startsWith(projectId, PROJECT_PREFIX) ? projectId : createCacheIdFromElements(PROJECT_PREFIX, projectId);
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
    private static final String QUERY_GET_GROUPS_FOR_DATATYPE    = "SELECT DISTINCT " +
                                                                   "  usergroup.id       AS group_name " +
                                                                   "FROM xdat_usergroup usergroup " +
                                                                   "  LEFT JOIN xdat_element_access xea ON usergroup.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
                                                                   "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                   "  LEFT JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                                   "WHERE " +
                                                                   "  xfm.field_value != '*' AND " +
                                                                   "  xea.element_name = :dataType " +
                                                                   "ORDER BY usergroup.id";
    private static final String QUERY_ALL_GROUPS                 = "SELECT id FROM xdat_usergroup";
    private static final String QUERY_ALL_TAGS                   = "SELECT DISTINCT tag FROM xdat_usergroup WHERE tag IS NOT NULL AND tag <> ''";
    private static final String QUERY_GET_GROUPS_FOR_TAG         = "SELECT id FROM xdat_usergroup WHERE tag = :tag";
    private static final String QUERY_CHECK_USER_EXISTS          = "SELECT EXISTS(SELECT TRUE FROM xdat_user WHERE login = :username) AS exists";
    private static final String QUERY_GET_EXPERIMENT_PROJECT     = "SELECT project FROM xnat_experimentdata WHERE id = :experimentId";
    private static final String QUERY_GET_SUBJECT_PROJECT        = "SELECT project FROM xnat_subjectdata WHERE id = :subjectId OR label = :subjectId";
    private static final String QUERY_GET_USERS_FOR_PROJECTS     = "SELECT DISTINCT " +
                                                                   "  u.login AS username " +
                                                                   "FROM xdat_user u " +
                                                                   "  LEFT JOIN xdat_user_groupid gid ON u.xdat_user_id = gid.groups_groupid_xdat_user_xdat_user_id " +
                                                                   "  LEFT JOIN xdat_usergroup g ON gid.groupid = g.id " +
                                                                   "WHERE g.tag IN (:projectIds)";
    private static final String QUERY_GET_PROJECTS_FOR_USER      = "SELECT DISTINCT " +
                                                                   "  g.tag AS project " +
                                                                   "FROM xdat_usergroup g " +
                                                                   "  LEFT JOIN xdat_user_groupid gid ON g.id = gid.groupid " +
                                                                   "  LEFT JOIN xdat_user u ON gid.groups_groupid_xdat_user_xdat_user_id = u.xdat_user_id " +
                                                                   "WHERE g.tag IS NOT NULL AND " +
                                                                   "      u.login = :username";

    private static final String ACTION_PREFIX       = "action";
    private static final String TAG_PREFIX          = "tag";
    private static final String PROJECT_PREFIX      = "project";
    private static final String USER_ELEMENT_PREFIX = "user";

    private static final Pattern      REGEX_EXTRACT_USER_FROM_CACHE_ID = Pattern.compile("^(?<prefix>" + ACTION_PREFIX + "|" + USER_ELEMENT_PREFIX + "):(?<username>[^:]+):(?<remainder>.*)$");
    private static final NumberFormat FORMATTER                        = NumberFormat.getNumberInstance(Locale.getDefault());
    private static final String       GUEST_CACHE_ID                   = getCacheIdForUserElements("guest", BROWSEABLE);

    private final NamedParameterJdbcTemplate _template;
    private final JmsTemplate                _jmsTemplate;
    private final DatabaseHelper             _helper;

    private Listener _listener;
    private boolean  _initialized;
    private XDATUser _guest;
}
