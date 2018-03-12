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
import net.sf.ehcache.event.CacheEventListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.utilities.LapStopWatch;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.Initializing;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.cache.jms.InitializeGroupRequest;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
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
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.Future;

import static org.nrg.framework.exceptions.NrgServiceError.ConfigurationError;
import static org.nrg.xapi.rest.users.DataAccessApi.BROWSEABLE;
import static org.nrg.xapi.rest.users.DataAccessApi.READABLE;
import static org.nrg.xdat.security.helpers.Groups.*;
import static reactor.bus.selector.Selectors.predicate;

@SuppressWarnings("Duplicates")
@Service
@Slf4j
public class DefaultGroupsAndPermissionsCache extends CacheEventListenerAdapter implements Consumer<Event<XftItemEvent>>, GroupsAndPermissionsCache, Initializing, GroupsAndPermissionsCache.Provider {

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

    @Override
    public Map<String, ElementDisplay> getBrowseableElementDisplays(final UserI user) {
        if (user == null) {
            return Collections.emptyMap();
        }

        final String username = user.getUsername();
        final String cacheId  = getCacheIdForUserElements(username, BROWSEABLE);
        log.debug("Retrieving browseable element displays for user {} thru cache ID {}", username, cacheId);

        // Check whether the element types are cached and, if so, return that.
        if (has(cacheId)) {
            // Here we can just return the value directly as a map, because we know there's something cached
            // and that what's cached is not a string.
            log.info("Found a cache entry for user '{}' readable counts by ID '{}'", username, cacheId);
            //noinspection unchecked
            return (Map<String, ElementDisplay>) _cache.get(cacheId, Map.class);
        }

        log.debug("No cache entry found for user '{}' readable counts by ID '{}', initializing entry", username, cacheId);
        final Map<Object, Object>         counts     = getReadableCounts(user);
        final Map<String, ElementDisplay> browseable = new HashMap<>();

        try {
            final List<ElementDisplay> actionElementDisplays = getActionElementDisplays(user, SecurityManager.READ);
            log.debug("Found {} action element displays for user {}", actionElementDisplays.size(), username);
            for (final ElementDisplay elementDisplay : actionElementDisplays) {
                final String elementName = elementDisplay.getElementName();
                if (ElementSecurity.IsBrowseableElement(elementName) && counts.containsKey(elementName) && (Long) counts.get(elementName) > 0) {
                    log.debug("Adding element display {} to cache entry {}", elementName, cacheId);
                    browseable.put(elementName, elementDisplay);
                }
            }

            log.info("Adding {} element displays to cache entry {}", browseable.size(), cacheId);
            _cache.put(cacheId, browseable);
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

    @Override
    public Map<Object, Object> getReadableCounts(final UserI user) {
        if (user == null) {
            return Collections.emptyMap();
        }

        final String username = user.getUsername();
        final String cacheId  = getCacheIdForUserElements(username, READABLE);
        log.debug("Retrieving readable counts for user {} thru cache ID {}", username, cacheId);

        // Check whether the element types are cached and, if so, return that.
        if (has(cacheId)) {
            // Here we can just return the value directly as a map, because we know there's something cached
            // and that what's cached is not a string.
            log.debug("Found a cache entry for user '{}' readable counts by ID '{}'", username, cacheId);
            //noinspection unchecked
            return Maps.newHashMap(_cache.get(cacheId, Map.class));
        }

        try {
            final Map<Object, Object> readableCounts = new HashMap<>();
            try {
                //projects
                final QueryOrganizer projects = new QueryOrganizer("xnat:projectData", user, ViewManager.ALL);
                projects.addField("xnat:projectData/ID");

                final String dbName       = user.getDBName();
                final Long   projectCount = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM (" + projects.buildQuery() + ") SEARCH;", "count", dbName, username);
                readableCounts.put("xnat:projectData", projectCount);

                //workflows
                final QueryOrganizer workflows = new QueryOrganizer("wrk:workflowData", user, ViewManager.ALL);
                workflows.addField("wrk:workflowData/ID");

                final Long workflowCount = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM (" + workflows.buildQuery() + ") SEARCH;", "count", dbName, username);
                readableCounts.put("wrk:workflowData", workflowCount);

                //subjects
                final QueryOrganizer subjects = new QueryOrganizer("xnat:subjectData", user, ViewManager.ALL);
                subjects.addField("xnat:subjectData/ID");

                final Long subjectCount = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM (" + subjects.buildQuery() + ") SEARCH;", "count", dbName, username);
                readableCounts.put("xnat:subjectData", subjectCount);

                //experiments
                final String query = StringUtils.replaceEach(subjects.buildQuery(),
                                                             new String[]{subjects.translateXMLPath("xnat:subjectData/ID"), "xnat_subjectData", "xnat_projectParticipant", "subject_id"},
                                                             REPLACEMENT_LIST);

                final XFTTable table = XFTTable.Execute("SELECT element_name, COUNT(*) FROM (" + query + ") SEARCH  LEFT JOIN xnat_experimentData expt ON search.id=expt.id LEFT JOIN xdat_meta_element xme ON expt.extension=xme.xdat_meta_element_id GROUP BY element_name", dbName, username);
                readableCounts.putAll(table.convertToHashtable("element_name", "count"));

                log.debug("Found {} readable elements for user {}, caching with ID {}", readableCounts.size(), username, cacheId);
                _cache.put(cacheId, readableCounts);
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
            return safeCastCacheList(_cache.get(cacheId, List.class), ElementDisplay.class);
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
            _cache.put(actionCacheId, actionElementDisplays);
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
        final String groupId = _template.query(QUERY_GET_GROUP_FOR_USER_AND_TAG, parameters, new ResultSetExtractor<String>() {
            @Override
            public String extractData(final ResultSet results) throws DataAccessException, SQLException {
                return results.next() ? results.getString("id") : null;
            }
        });
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
        // Nothing cached for group ID, so let's try to retrieve it.
        log.debug("Initializing group {}", groupId);
        final XdatUsergroup found = XdatUsergroup.getXdatUsergroupsById(groupId, Users.getAdminUser(), false);

        // If we didn't find a group for that ID...
        if (found == null) {
            // Return null.
            log.info("Someone tried to get the user group {}, but a group with that ID doesn't exist.", groupId);
            return null;
        }

        final UserGroupI group = createUserGroupFromXdatUsergroup(found);

        return cacheGroup(group);
    }

    @Override
    public void registerListener(final Listener listener) {
        _listener = listener;
    }

    @Override
    public Listener getListener() {
        return _listener;
    }

    private synchronized UserGroupI initializeGroup(final String groupId) {
        // We may have just checked before coming into this method, but since it's synchronized we may have waited while someone else was caching it so...
        if (has(groupId)) {
            log.info("Group {} was already initialized and in the cache, returning cached instance");
            return _cache.get(groupId, UserGroupI.class);
        }

        return cacheGroup(groupId);
    }

    private UserGroupI cacheGroup(final UserGroupI group) {
        final String groupId = group.getId();
        _cache.put(groupId, group);
        log.debug("Retrieved and cached the group for the ID {}", groupId);
        return group;
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
        return safeCastCacheList(_cache.get(cacheId, List.class), String.class);
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

    private static <T> List<T> safeCastCacheList(final List list, final Class<T> desiredType) {
        return Lists.newArrayList(Iterables.filter(list, desiredType));
    }

    private static String getCacheIdForTag(final String tag) {
        return StringUtils.startsWith(tag, TAG_PREFIX) ? tag : createCacheIdFromElements(TAG_PREFIX, tag);
    }

    private static String getCacheIdForUserElements(final String username, final String elementType) {
        return createCacheIdFromElements(USER_ELEMENT_PREFIX, username, elementType);
    }

    private static String getCacheIdForActionElements(final String username, final String action) {
        return createCacheIdFromElements(ACTION_PREFIX, username, action);
    }

    private static String createCacheIdFromElements(final String... elements) {
        return StringUtils.join(elements, ":");
    }

    private static boolean isGroupsAndPermissionsCacheEvent(final Ehcache cache) {
        return StringUtils.equals(CACHE_NAME, cache.getName());
    }

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
    private static final String QUERY_ALL_GROUPS                 = "SELECT id FROM xdat_usergroup";
    private static final String QUERY_ALL_TAGS                   = "SELECT DISTINCT tag FROM xdat_usergroup WHERE tag IS NOT NULL AND tag <> ''";
    private static final String QUERY_GET_GROUPS_FOR_TAG         = "SELECT id FROM xdat_usergroup WHERE tag = :tag";
    private static final String QUERY_CHECK_USER_EXISTS          = "SELECT EXISTS(SELECT TRUE FROM xdat_user WHERE login = :username) AS exists";

    private static final String ACTION_PREFIX       = "action";
    private static final String TAG_PREFIX          = "tag";
    private static final String USER_ELEMENT_PREFIX = "user";

    private static final NumberFormat FORMATTER = NumberFormat.getNumberInstance(Locale.getDefault());

    private final Cache                      _cache;
    private final NamedParameterJdbcTemplate _template;
    private final JmsTemplate                _jmsTemplate;
    private final DatabaseHelper             _helper;

    private Listener _listener;
    private boolean  _initialized;
}
