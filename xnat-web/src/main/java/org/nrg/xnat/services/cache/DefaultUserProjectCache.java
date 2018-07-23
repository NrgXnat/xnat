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
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.Initializing;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xdat.servlet.XDATServlet;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.methods.XftItemEventCriteria;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xft.utils.XftStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.nrg.xdat.entities.UserRole.ROLE_ADMINISTRATOR;
import static org.nrg.xdat.security.helpers.AccessLevel.*;
import static org.nrg.xdat.security.helpers.Groups.*;
import static org.nrg.xdat.security.helpers.Roles.*;
import static org.nrg.xft.event.XftItemEventI.*;

@SuppressWarnings("Duplicates")
@Service("userProjectCache")
@Slf4j
public class DefaultUserProjectCache extends AbstractXftItemAndCacheEventHandlerMethod implements UserProjectCache, Initializing {
    @Autowired
    public DefaultUserProjectCache(final CacheManager cacheManager, final GroupsAndPermissionsCache cache, final NamedParameterJdbcTemplate template) {
        super(cacheManager,
              XftItemEventCriteria.getXsiTypeCriteria(XnatProjectdata.SCHEMA_ELEMENT_NAME),
              XftItemEventCriteria.getXsiTypeCriteria(XnatDatatypeprotocol.SCHEMA_ELEMENT_NAME),
              XftItemEventCriteria.builder().xsiType(XnatInvestigatordata.SCHEMA_ELEMENT_NAME).action(XftItemEvent.DELETE).build(),
              XftItemEventCriteria.builder().xsiType(XdatUsergroup.SCHEMA_ELEMENT_NAME).predicate(Predicates.or(XftItemEventCriteria.IS_PROJECT_GROUP, new Predicate<XftItemEventI>() {
                  @Override
                  public boolean apply(final XftItemEventI event) {
                      return StringUtils.equalsAny(event.getId(), Groups.ALL_DATA_ADMIN_GROUP, Groups.ALL_DATA_ACCESS_GROUP);
                  }
              })).build(),
              XftItemEventCriteria.builder().xsiType(XdatUser.SCHEMA_ELEMENT_NAME).action(UPDATE).predicate(PREDICATE_IS_ROLE_OPERATION).build());
        _cache = cache;
        _template = template;
        _helper = new DatabaseHelper((JdbcTemplate) _template.getJdbcOperations());
    }

    @Override
    public boolean canInitialize() {
        try {
            final boolean doesProjectTableExists              = _helper.tableExists("xnat_projectdata");
            final boolean doesAliasTableExists                = _helper.tableExists("xnat_projectdata_alias");
            final boolean isXftManagerComplete                = XFTManager.isComplete();
            final boolean isDatabasePopulateOrUpdateCompleted = XDATServlet.isDatabasePopulateOrUpdateCompleted();
            log.info("Project table {}, Project alias table {}, XFTManager initialization completed {}, database populate or updated completed {}", doesProjectTableExists, doesAliasTableExists, isXftManagerComplete, isDatabasePopulateOrUpdateCompleted);
            return doesProjectTableExists && doesAliasTableExists && isXftManagerComplete && isDatabasePopulateOrUpdateCompleted;
        } catch (SQLException e) {
            log.info("Got an SQL exception checking for xdat_usergroup table", e);
            return false;
        }
    }

    @Async
    @Override
    public Future<Boolean> initialize() {
        _template.query(QUERY_GET_IDS_AND_ALIASES, new RowCallbackHandler() {
            @Override
            public void processRow(final ResultSet resultSet) throws SQLException {
                final String projectId = resultSet.getString("project_id");
                final String idOrAlias = resultSet.getString("id_or_alias");
                _aliasMapping.put(idOrAlias, projectId);
                _projectsAndAliases.put(projectId, idOrAlias);
            }
        });
        _initialized.set(true);
        return new AsyncResult<>(true);
    }

    @Override
    public boolean isInitialized() {
        return _initialized.get();
    }

    @Override
    public Map<String, String> getInitializationStatus() {
        return ImmutableMap.of("count", Integer.toString(_aliasMapping.size()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementRemoved(final Ehcache cache, final Element element) throws CacheException {
        log.debug("Cache {} had element '{}' with type '{}' removed", cache.getName(), element.getObjectKey(), element.getObjectValue().getClass());
        handleCacheRemoveEvent(cache, element, "removed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementExpired(final Ehcache cache, final Element element) {
        log.debug("Cache {} had element '{}' with type '{}' expired", cache.getName(), element.getObjectKey(), element.getObjectValue().getClass());
        handleCacheRemoveEvent(cache, element, "expired");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementEvicted(final Ehcache cache, final Element element) {
        log.debug("Cache {} had element '{}' with type '{}' evicted", cache.getName(), element.getObjectKey(), element.getObjectValue().getClass());
        handleCacheRemoveEvent(cache, element, "evicted");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyRemoveAll(final Ehcache cache) {
        log.debug("Cache {} had all elements removed", cache.getName());
        if (isProjectCacheEvent(cache)) {
            _aliasMapping.clear();
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
     * Indicates whether permissions for the specified user in the specified project ID or alias is already cached.
     *
     * @param user      The user object for the user requesting the project.
     * @param idOrAlias The ID or alias of the project to check.
     *
     * @return Returns true if the user is mapped to a cache entry for the cached project ID or alias, false otherwise.
     */
    @Override
    public boolean has(final UserI user, final String idOrAlias) {
        // If it's not in the alias map, we don't know about it.
        if (!_aliasMapping.containsKey(idOrAlias)) {
            return false;
        }

        final String       userId       = user.getUsername();
        final ProjectCache projectCache = getProjectCache(idOrAlias);
        return projectCache != null && projectCache.hasUser(userId);
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
    @Override
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
    @Override
    @SuppressWarnings("unused")
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
    @Override
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
    @Override
    public XnatProjectdata get(final UserI user, final String idOrAlias) {
        final String userId;
        if (user != null) {
            userId = user.getUsername();
            // Check that the project is readable by the user and, if not, return null.
            if (!canRead(userId, idOrAlias)) {
                log.info("User {} attempted to retrieve the project by ID or alias {}, but that project isn't accessible for the user. Returning null.", userId, idOrAlias);
                return null;
            }
        } else {
            userId = "<none>";
        }

        log.debug("Found a cached project with ID or alias '{}' that is accessible by the user {}.", idOrAlias, userId);
        final ProjectCache projectCache = getProjectCache(idOrAlias);
        if (projectCache != null) {
            log.debug("Found a cached project for ID or alias {} for user {}.", idOrAlias, userId);
            final XnatProjectdata project = projectCache.getProject();
            project.setUser(user);
            return project;
        } else {
            log.info("Call to has({}, {}), which returned true, but getProjectCache({}) returned null. This probably means that the cache entry is being initialized but was caught in the thundering herd. This method will return null.", userId, idOrAlias, idOrAlias);
            // If we made it here, the project is either inaccessible to the user or doesn't exist. In either case, return null.
            return null;
        }
    }

    @Override
    public List<XnatProjectdata> getAll(final UserI user) {
        return getProjectsFromIds(user, _cache.getProjectsForUser(user.getUsername(), SecurityManager.READ));
    }

    @Override
    public List<XnatProjectdata> getByField(final UserI user, final String field, final String value) {
        try {
            final String standardized = XftStringUtils.StandardizeXMLPath(field);
            log.debug("User {} requested to retrieve project by field '{}' (standardized: '{}') with value '{}'", user.getUsername(), field, standardized, value);

            final String rootElement = XftStringUtils.GetRootElementName(standardized);
            if (!StringUtils.equalsIgnoreCase(XnatProjectdata.SCHEMA_ELEMENT_NAME, rootElement)) {
                return Collections.emptyList();
            }

            final GenericWrapperField wrapper = GenericWrapperElement.GetFieldForXMLPath(standardized);
            if (wrapper == null) {
                throw new RuntimeException("No field named " + standardized);
            }

            final String column      = wrapper.getSQLName();
            final String query       = StringSubstitutor.replace(QUERY_PROJECTS_BY_FIELD, ImmutableMap.<String, Object>of("column", column));
            final Object mappedValue = convertStringToMappedTypeObject(wrapper, value);

            log.debug("Executing query '{}' with value '{}'", query, mappedValue);
            return getProjectsFromIds(user, _template.queryForList(query, new MapSqlParameterSource("value", mappedValue), String.class));
        } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
            log.error("Got an error trying to retrieve project by field '{}' (standardized: '{}') with value '{}' for user {}", field, value, user.getUsername());
            return null;
        }
    }

    @Override
    public boolean clearProjectCacheEntry(final String idOrAlias) {
        return evictProjectCache(idOrAlias);
    }

    @Override
    protected boolean handleEventImpl(final XftItemEventI event) {
        final String xsiType = event.getXsiType();
        switch (xsiType) {
            case XnatProjectdata.SCHEMA_ELEMENT_NAME:
                return handleProjectEvent(event);

            case XdatUsergroup.SCHEMA_ELEMENT_NAME:
                return handleGroupEvent(event);

            case XdatUser.SCHEMA_ELEMENT_NAME:
                return handleUserEvent(event);

            case XnatInvestigatordata.SCHEMA_ELEMENT_NAME:
                return handleInvestigatorEvent(event);

            case XnatDatatypeprotocol.SCHEMA_ELEMENT_NAME:
                return handleDataTypeProtocolEvent(event);

            default:
                return false;
        }
    }

    private boolean handleProjectEvent(final XftItemEventI event) {
        final String projectId = event.getId();
        if (StringUtils.isBlank(projectId)) {
            log.error("Handled an event that should have had a project ID, but it didn't: {}", event);
            return false;
        }

        final String  action          = event.getAction();
        final boolean isProjectDelete = StringUtils.equals(DELETE, action);

        if (StringUtils.equals(CREATE, action)) {
            _aliasMapping.put(projectId, projectId);
            log.debug("Created new project, cached ID {}", projectId);
        } else if (isProjectDelete) {
            _aliasMapping.remove(projectId);
            final List<String> aliases = _projectsAndAliases.removeAll(projectId);
            for (final String alias : aliases) {
                _aliasMapping.remove(alias);
            }
            log.debug("The project {} was deleted, so skipping cache reinitialization. Removed ID and any aliases from cache: {}", projectId, aliases);
        }

        log.info("Got an XFTItemEvent for project '{}' with action '{}', refreshing now", projectId, action);
        if (isProjectDelete) {
            evictProjectCache(projectId);
        } else {
            refreshProjectCache(projectId);
        }

        return true;
    }

    private boolean handleGroupEvent(final XftItemEventI event) {
        final String eventId = event.getId();
        if (StringUtils.equalsAny(eventId, Groups.ALL_DATA_ADMIN_GROUP, Groups.ALL_DATA_ACCESS_GROUP)) {
            final UserGroupI group = getGroup(eventId);
            switch (group.getId()) {
                case Groups.ALL_DATA_ADMIN_GROUP:
                    _dataAdmins.clear();
                    _dataAdmins.addAll(group.getUsernames());
                    break;

                case Groups.ALL_DATA_ACCESS_GROUP:
                    _dataAccess.clear();
                    _dataAccess.addAll(group.getUsernames());
                    break;
            }
            return true;
        }
        final Pair<String, String> idAndAccess = Groups.getProjectIdAndAccessFromGroupId(eventId);
        if (idAndAccess.equals(ImmutablePair.<String, String>nullPair())) {
            log.info("Got a non-project-related group event, which I'm not supposed to handle: ", event);
            return false;
        }
        final String      projectId = idAndAccess.getLeft();
        final AccessLevel access    = AccessLevel.getAccessLevel(idAndAccess.getRight());
        if (StringUtils.isBlank(projectId)) {
            return false;
        }

        // When groups are created for projects, the groups are created *before* the project. We need to check whether the
        // project is already cached so we don't try to refresh the cache before the project's actually in.
        if (!_aliasMapping.containsKey(projectId)) {
            return true;
        }

        final Map<String, ?> properties = event.getProperties();
        final String         operation  = (String) properties.get(OPERATION);

        // In this case, we don't know what happened or at least don't know how to deal with it, so just update the whole thing.
        if (properties.isEmpty() || !StringUtils.equalsAny(operation, OPERATION_ADD_USERS, OPERATION_REMOVE_USERS)) {
            refreshProjectCache(projectId);
            return true;
        }

        //noinspection unchecked
        final Collection<String> users        = (Collection<String>) properties.get(Groups.USERS);
        final ProjectCache       projectCache = getProjectCache(projectId);
        if (StringUtils.equals(operation, OPERATION_ADD_USERS)) {
            if (projectCache == null) {
                log.debug("Users had access level {} added for project {}, but that project's not in the cache: {}", access, projectId, users);
                return true;
            }
            for (final String username : users) {
                projectCache.addUser(username, access);
            }
        } else {
            if (projectCache == null) {
                log.debug("Users had access level {} revoked for project {}, but that project's not in the cache: {}", access, projectId, users);
                return true;
            }
            for (final String username : users) {
                projectCache.removeUser(username, access);
            }
        }
        return true;
    }

    private boolean handleUserEvent(final XftItemEventI event) {
        final String         username   = event.getId();
        final Map<String, ?> properties = event.getProperties();
        final String         operation  = (String) properties.get(OPERATION);
        final String         role       = (String) properties.get(ROLE);

        switch (operation) {
            case OPERATION_ADD_ROLE:
                if (StringUtils.equals(ROLE_ADMINISTRATOR, role)) {
                    _siteAdmins.add(username);
                }
                break;

            case OPERATION_DELETE_ROLE:
                if (StringUtils.equals(ROLE_ADMINISTRATOR, role)) {
                    _siteAdmins.remove(username);
                }
                break;
        }
        return false;
    }

    private boolean handleInvestigatorEvent(final XftItemEventI event) {
        //noinspection unchecked
        final Set<String> projectIds = new HashSet<>((Collection<? extends String>) event.getProperties().get("projects"));
        if (projectIds.isEmpty()) {
            return false;
        }
        for (final String projectId : projectIds) {
            refreshProjectCache(projectId);
        }
        return true;
    }

    private boolean handleDataTypeProtocolEvent(final XftItemEventI event) {
        log.info("Got the {} event for the data-type protocol {}. This should only happen when changes to the protocol affected field definition groups that are non-project specific. Clearing the cache because all projects need to be updated.", event.getAction(), event.getId());
        clearCache();
        return true;
    }

    private void refreshProjectCache(final String projectId) {
        evictProjectCache(projectId);
        initializeProjectCache(projectId);
    }

    private boolean evictProjectCache(final String projectId) {
        final ProjectCache projectCache = getCachedProjectCache(projectId);
        if (projectCache == null || projectCache.getProject() == null) {
            log.info("No cache found for the project '{}', nothing much to be done.", projectId);
            return false;
        } else {
            log.info("Found project cache for project {}, evicting the project cache.", projectId);
            evict(projectId);
            return true;
        }
    }

    private Object convertStringToMappedTypeObject(final GenericWrapperField field, final String value) {
        final String mappedType = field.getType(XFTItem.JAVA_CONVERTER);
        log.debug("Converting value '{}' to mapped type '{}", value, mappedType);
        switch (mappedType) {
            case "java.lang.Boolean":
                return Boolean.parseBoolean(value);

            case "java.lang.Double":
                return Double.parseDouble(value);

            case "java.lang.Integer":
                return Integer.parseInt(value);

            case "java.lang.Long":
                return Long.parseLong(value);

            case "java.util.Date":
                Date attempt;
                try {
                    attempt = DateUtils.parseDateTime(value);
                } catch (ParseException e) {
                    try {
                        attempt = DateUtils.parseDate(value);
                    } catch (ParseException e1) {
                        try {
                            attempt = DateUtils.parseDate(value);
                        } catch (ParseException e2) {
                            log.error("Can't parse the date value '{}', unknown format!", value);
                            attempt = null;
                        }
                    }
                }
                return attempt;
            default:
                return value;
        }
    }

    /**
     * Converts a list of strings into a list of projects with the corresponding IDs.
     *
     * @param user       The user retrieving the projects.
     * @param projectIds The list of IDs of the projects to be retrieved.
     *
     * @return A list of project objects.
     */
    private List<XnatProjectdata> getProjectsFromIds(final UserI user, final List<String> projectIds) {
        log.debug("User {} is converting a list of {} strings to projects: {}", user.getUsername(), projectIds.size(), projectIds);
        return Lists.transform(projectIds, new Function<String, XnatProjectdata>() {
            @Override
            public XnatProjectdata apply(final String projectId) {
                return get(user, projectId);
            }
        });
    }

    private boolean hasAccess(final String userId, final String idOrAlias, final AccessLevel accessLevel) {
        // If the user is not in the user lists, try to retrieve and cache it.
        final XDATUser user;
        final boolean  isSiteAdmin;
        final boolean  isDataAdmin;
        final boolean  isDataAccess;
        if (!_nonAdmins.contains(userId) && !_dataAdmins.contains(userId) && !_dataAdmins.contains(userId) && !_siteAdmins.contains(userId)) {
            try {
                // Get the user...
                user = new XDATUser(userId);
                // If the user is an admin, add the user ID to the admin list and return true.
                if (user.isSiteAdmin()) {
                    _siteAdmins.add(userId);
                    isSiteAdmin = true;
                    isDataAdmin = false;
                    isDataAccess = false;
                } else if (user.isDataAdmin()) {
                    _dataAdmins.add(userId);
                    isSiteAdmin = false;
                    isDataAdmin = true;
                    isDataAccess = false;
                } else if (user.isDataAccess()) {
                    _dataAccess.add(userId);
                    isSiteAdmin = false;
                    isDataAdmin = false;
                    isDataAccess = true;
                } else {
                    // Not an admin but let's track that we've retrieved the user by adding it to the non-admin list.
                    _nonAdmins.add(userId);
                    isSiteAdmin = false;
                    isDataAdmin = false;
                    isDataAccess = false;
                }
            } catch (UserNotFoundException e) {
                // User doesn't exist, so return false
                log.error("Got a request to test '{}' access to project with ID or alias '{}' for user {}, but that user doesn't exist.", accessLevel, idOrAlias, userId);
                return false;
            } catch (UserInitException e) {
                // Something bad happened so note it and move on.
                log.error("Something bad happened trying to retrieve the user {}", userId, e);
                return false;
            }
        } else {
            // Set the user to null. It will only get initialized later in the initProjectCache() method if required.
            user = null;
            isSiteAdmin = _siteAdmins.contains(userId);
            isDataAdmin = _dataAdmins.contains(userId);
            isDataAccess = _dataAccess.contains(userId);
        }

        try {
            // Check for existing cache for the current project.
            final String projectId = getCanonicalProjectId(idOrAlias, user, userId);
            if (StringUtils.isBlank(projectId)) {
                return false;
            }

            final ProjectCache projectCache = getProjectCache(projectId);

            // If the project cache is null, the project doesn't exist (same as isCachedNonexistentProject() but it wasn't cached previously).
            if (projectCache == null) {
                return false;
            }

            // We don't care about checking the user against the project if it's a site admin: they have access to everything.
            if (isSiteAdmin || isDataAdmin || isDataAccess) {
                return true;
            }

            // If the user isn't already cached...
            if (!projectCache.hasUser(userId)) {
                // Cache the user!
                projectCache.addUser(userId, getUserProjectAccess(ObjectUtils.defaultIfNull(user, new XDATUser(userId)), projectId));
            }
            return CollectionUtils.containsAny(projectCache.getUserAccessLevels(userId), ACCESS_LEVELS.get(accessLevel));
        } catch (UserInitException e) {
            log.error("Something bad happened trying to retrieve the user {}", userId, e);
        } catch (UserNotFoundException e) {
            log.error("A user not found exception occurred searching for the user {}. This really shouldn't happen as I checked for the user's existence earlier.", userId, e);
        } catch (Exception e) {
            log.error("An error occurred trying to test whether the user {} can read the project specified by ID or alias {}", userId, idOrAlias, e);
        }
        return false;
    }

    /**
     * Returns the canonical ID for the submitted ID or alias. If the specified ID or alias can't be found, this method returns null. If the ID or alias is already
     * cached, this method just returns the canonical project ID corresponding to the submitted ID or alias. If it's not already cached, the project is retrieved,
     * the project ID and its aliases are cached in the alias mapping table, and the project object is inserted into the cache under its canonical project ID. This
     * allows the project to be retrieved once on initial reference then just pulled from the cache later.
     *
     * @param idOrAlias The ID or alias to test.
     * @param user      The user object for the user requesting the project.
     * @param userId    The user ID for the user requesting the project. This is used to retrieve the user object if the user parameter is null.
     *
     * @return The ID of the project with the submitted ID or alias.
     */
    private String getCanonicalProjectId(final String idOrAlias, final UserI user, final String userId) {
        // First check for cached ID or alias.
        if (_aliasMapping.containsKey(idOrAlias)) {
            // Found it so return that.
            final String projectId = _aliasMapping.get(idOrAlias);
            log.debug("Found cached project ID {} for the ID or alias {}", projectId, idOrAlias);
            return projectId;
        }

        log.info("User {} requested project by ID or alias {}, not found so starting initialization.", getUserId(user, userId), idOrAlias);
        initializeProjectCache(idOrAlias);

        // Return the mapped value for the ID or alias, which should now be initialized (even if it's initialized to "not a project")
        if (_aliasMapping.containsKey(idOrAlias)) {
            final String projectId = _aliasMapping.get(idOrAlias);
            log.debug("After initialization, found canonical project ID {} for the ID or alias {}", projectId, idOrAlias);
            return projectId;
        } else {
            log.debug("After initialization, no project was found that matches the ID or alias {}", idOrAlias);
            return null;
        }
    }

    /**
     * Gets all aliases for the project with the specified ID.
     *
     * @param projectId The ID of the project to be queried.
     *
     * @return A list of aliases (if any) for the specified project.
     */
    private List<String> getProjectAliases(final String projectId) {
        return _template.queryForList(QUERY_GET_PROJECT_ALIASES, new MapSqlParameterSource("projectId", projectId), String.class);
    }

    /**
     * Gets the project ID from the ID or alias submitted.
     *
     * @param idOrAlias Can be the project ID or an alias.
     *
     * @return The project ID, null if it can't be found.
     */
    private String getProjectIdFromIdOrAlias(final String idOrAlias) {
        if (_aliasMapping.containsKey(idOrAlias)) {
            return _aliasMapping.get(idOrAlias);
        }

        try {
            return _template.queryForObject(QUERY_GET_PROJECT_BY_ID_OR_ALIAS, new MapSqlParameterSource("idOrAlias", idOrAlias), String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException e) {
            log.error("Somehow came back with more than one result searching for the project ID or alias {}: should have gotten {} result and ended up with {}", idOrAlias, e.getExpectedSize(), e.getActualSize());
            return null;
        }
    }

    /**
     * Gets the cache for the project identified by the ID or alias parameter. If the project is cached as non-existent, this method returns null. Otherwise,
     * it checks the project cache and, if the project is already there, just returns the corresponding project cache. If the project isn't already in the cache,
     * this method tries to find the project by ID or alias. If it's not found, it's then cached as non-existent and the method returns null as before. If it is
     * found, the project is cached.
     *
     * @param idOrAlias The ID or alias of the project to retrieve.
     *
     * @return The project cache consisting of the project as the "left" or "key" value and a map of users and their access level to the project as the "right" or "value" value.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private ProjectCache getProjectCache(final String idOrAlias) {
        // If we've already mapped and cached the project...
        if (_aliasMapping.containsKey(idOrAlias)) {
            // Resolve the ID or alias to the mapped project ID.
            final String projectId = _aliasMapping.get(idOrAlias);
            log.debug("Found project ID {} for the ID or alias {}, returning project cache for that ID.", projectId, idOrAlias);

            // And then return the project cache.
            final ProjectCache projectCache = getCachedProjectCache(projectId);
            if (projectCache != null && projectCache.getProject() != null) {
                return projectCache;
            }
            log.info("Found cached project ID '{}' for ID or alias '{}', but there's no project cache entry or the project cache is null. Initializing the cache entry in response.", projectId, idOrAlias);
        }

        return initializeProjectCache(idOrAlias);
    }

    @Nullable
    private synchronized ProjectCache initializeProjectCache(final String idOrAlias) {
        // Get the project ID from the alias map. If there's not an entry presume that it's the ID.
        final String projectId = getProjectIdFromIdOrAlias(idOrAlias);
        if (StringUtils.isBlank(projectId)) {
            log.error("There is no project that has an ID or alias of: {}", idOrAlias);
            return null;
        }

        if (has(projectId)) {
            log.info("Found project ID {} for ID or alias {} and it has a project cache entry, this was probably initialized by another thread.", projectId, idOrAlias);
            return getCachedProjectCache(projectId);
        }

        log.info("Locking and initializing project cache for ID or alias {}, which mapped to project ID {}", idOrAlias, projectId);
        final XnatProjectdata project = AutoXnatProjectdata.getXnatProjectdatasById(projectId, null, false);
        if (project == null) {
            if (StringUtils.equals(idOrAlias, projectId)) {
                log.error("Could not find a project for the ID {}", projectId);
            } else {
                log.error("Could not find a project for the ID {} or alias {}", projectId, idOrAlias);
            }
            return null;
        }

        // Hooray, we found the project, so let's cache the ID and all the aliases.
        cacheProjectIdsAndAliases(projectId);

        // Create the project cache and user list.
        final ProjectCache projectCache = new ProjectCache(project);

        // This caches all of the users from the standard user groups and their permissions ahead of time in the most efficient way possible.
        for (final String accessLevel : USER_GROUP_SUFFIXES.keySet()) {
            final List<AccessLevel> accessLevelPermissions = USER_GROUP_SUFFIXES.get(accessLevel);
            for (final String userIdByAccess : _template.queryForList(QUERY_USERS_BY_GROUP, getProjectAccessParameterSource(projectId, accessLevel), String.class)) {
                log.debug("Caching user {} for access level {} on project {}", userIdByAccess, accessLevel, projectId);
                projectCache.addUser(userIdByAccess, accessLevelPermissions);
            }
        }

        log.debug("Caching project cache for {}", projectId);
        cacheObject(projectId, projectCache);

        return projectCache;
    }

    private ProjectCache getCachedProjectCache(final String cacheId) {
        return getCachedObject(cacheId, ProjectCache.class);
    }

    private void handleCacheRemoveEvent(final Ehcache cache, final Element element, final String event) {
        log.debug("Handling cache remove event in cache {} with element {} and event {}", cache.getName(), element.getObjectKey(), event);
    }

    private void cacheProjectIdsAndAliases(final String projectId) {
        _aliasMapping.put(projectId, projectId);
        final List<String> aliases = getProjectAliases(projectId);
        for (final String alias : aliases) {
            _aliasMapping.put(alias, projectId);
        }
        if (_projectsAndAliases.containsKey(projectId)) {
            _projectsAndAliases.removeAll(projectId);
        }
        _projectsAndAliases.put(projectId, projectId);
        _projectsAndAliases.putAll(projectId, aliases);
        log.debug("Just cached ID and aliases for project {}: {}", projectId, aliases);
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

    @SuppressWarnings("unused")
    private static XnatProjectdata getProjectCacheEventInstance(final Element element) {
        return ((ProjectCache) element.getObjectValue()).getProject();
    }

    private static boolean isProjectCacheEvent(final Ehcache cache) {
        return StringUtils.equals(CACHE_NAME, cache.getName());
    }

    @SuppressWarnings("unused")
    private static boolean isProjectCacheEvent(final Ehcache cache, final Element element) {
        return isProjectCacheEvent(cache) && (element == null || element.getObjectValue() instanceof ProjectCache);
    }

    private static List<AccessLevel> getUserProjectAccess(final UserI user, final String projectId) {
        final List<AccessLevel> levels = new ArrayList<>();
        if (Permissions.canDeleteProject(user, projectId)) {
            levels.add(Delete);
        } else if (Permissions.canEditProject(user, projectId)) {
            levels.add(Edit);
        } else if (Permissions.canReadProject(user, projectId)) {
            levels.add(Read);
        }
        // If no levels have been added, the user CAN'T be an owner, member, or collaborator, as there
        // would be at least one level already added, so just return the list without checking that.
        if (levels.isEmpty()) {
            return levels;
        }
        final String access = Permissions.getUserProjectAccess(user, projectId);
        if (StringUtils.isNotBlank(access)) {
            switch (access) {
                case OWNER_GROUP:
                    levels.add(Owner);
                    break;
                case MEMBER_GROUP:
                    levels.add(Member);
                    break;
                case COLLABORATOR_GROUP:
                    levels.add(Collaborator);
                    break;
                default:
                    log.warn("Unknown access group for user {} and project {}: {}", user.getUsername(), projectId);
            }
        }
        return levels;
    }

    private static MapSqlParameterSource getProjectAccessParameterSource(final String projectId, final String accessLevel) {
        return new MapSqlParameterSource(QUERY_KEY_PROJECT_ID, projectId).addValue(QUERY_KEY_ACCESS_LEVEL, accessLevel);
    }

    private static class ProjectCache {
        ProjectCache(final XnatProjectdata project) {
            _project = project;
        }

        public XnatProjectdata getProject() {
            return _project;
        }

        public void removeUser(final String username, final AccessLevel access) {
            _userCache.remove(username, access);
        }

        @SuppressWarnings("unused")
        public void removeUser(final String username) {
            _userCache.removeAll(username);
        }

        boolean hasUser(final String userId) {
            return _userCache.containsKey(userId);
        }

        Collection<AccessLevel> getUserAccessLevels(final String userId) {
            return _userCache.get(userId);
        }

        void addUser(final String userId, final List<AccessLevel> userProjectAccess) {
            _userCache.putAll(userId, userProjectAccess);
        }

        void addUser(final String userId, final AccessLevel userProjectAccess) {
            _userCache.put(userId, userProjectAccess);
        }

        private final XnatProjectdata _project;

        private final Multimap<String, AccessLevel> _userCache = ArrayListMultimap.create();
    }

    private static final Predicate<XftItemEventI> PREDICATE_IS_ROLE_OPERATION = new Predicate<XftItemEventI>() {
        @Override
        public boolean apply(final XftItemEventI event) {
            final Map<String, ?> properties = event.getProperties();
            return !properties.isEmpty() && StringUtils.equalsAny((String) properties.get(OPERATION), OPERATION_ADD_ROLE, OPERATION_DELETE_ROLE);
        }
    };

    private static final List<AccessLevel>                   DELETABLE_ACCESS                 = Arrays.asList(Owner, Delete, Admin);
    private static final List<AccessLevel>                   WRITABLE_ACCESS                  = Arrays.asList(Member, Edit, Admin, DataAdmin);
    private static final List<AccessLevel>                   READABLE_ACCESS                  = Arrays.asList(Collaborator, Read, Admin, DataAdmin, DataAccess);
    private static final Map<AccessLevel, List<AccessLevel>> ACCESS_LEVELS                    = ImmutableMap.of(Delete, DELETABLE_ACCESS,
                                                                                                                Edit, Lists.newArrayList(Iterables.concat(DELETABLE_ACCESS, WRITABLE_ACCESS)),
                                                                                                                Read, Lists.newArrayList(Iterables.concat(DELETABLE_ACCESS, WRITABLE_ACCESS, READABLE_ACCESS)));
    private static final Map<String, List<AccessLevel>>      USER_GROUP_SUFFIXES              = ImmutableMap.of("owner", DELETABLE_ACCESS, "member", WRITABLE_ACCESS, "collaborator", READABLE_ACCESS);
    private static final String                              QUERY_KEY_PROJECT_ID             = "projectId";
    private static final String                              QUERY_KEY_ACCESS_LEVEL           = "accessLevel";
    private static final String                              CACHE_NAME                       = "UserProjectCacheManagerCache";
    private static final String                              QUERY_GET_PROJECT_BY_ID_OR_ALIAS = "SELECT DISTINCT id " +
                                                                                                "FROM xnat_projectdata " +
                                                                                                "  LEFT JOIN xnat_projectdata_alias a on xnat_projectdata.id = a.aliases_alias_xnat_projectdata_id " +
                                                                                                "WHERE " +
                                                                                                "  id = :idOrAlias OR " +
                                                                                                "  a.alias = :idOrAlias";

    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
    private static final String QUERY_USERS_BY_GROUP      = "SELECT DISTINCT login " +
                                                            "FROM xdat_user " +
                                                            "  RIGHT JOIN xdat_user_groupid xug ON xdat_user.xdat_user_id = xug.groups_groupid_xdat_user_xdat_user_id " +
                                                            "WHERE groupid = :projectId || :accessLevel";
    private static final String QUERY_PROJECTS_BY_FIELD   = "SELECT id " +
                                                            "FROM xnat_projectdata " +
                                                            "WHERE ${column} = :value";
    private static final String QUERY_GET_PROJECT_ALIASES = "SELECT " +
                                                            "  alias " +
                                                            "FROM xnat_projectdata_alias alias " +
                                                            "WHERE aliases_alias_xnat_projectdata_id = :projectId";
    private static final String QUERY_GET_IDS_AND_ALIASES = "SELECT " +
                                                            "  aliases_alias_xnat_projectdata_id AS project_id, " +
                                                            "  alias AS id_or_alias " +
                                                            "FROM xnat_projectdata_alias alias " +
                                                            "LEFT JOIN xnat_projectdata project on alias.aliases_alias_xnat_projectdata_id = project.id " +
                                                            "UNION " +
                                                            "SELECT " +
                                                            "  id AS project_id, " +
                                                            "  id AS id_or_alias " +
                                                            "FROM xnat_projectdata project " +
                                                            "ORDER BY project_id, id_or_alias";

    private final Set<String>                       _siteAdmins         = new HashSet<>();
    private final Set<String>                       _dataAdmins         = new HashSet<>();
    private final Set<String>                       _dataAccess         = new HashSet<>();
    private final Set<String>                       _nonAdmins          = new HashSet<>();
    private final Map<String, String>               _aliasMapping       = new HashMap<>();
    private final ArrayListMultimap<String, String> _projectsAndAliases = ArrayListMultimap.create();
    private final AtomicBoolean                     _initialized        = new AtomicBoolean(false);

    private final GroupsAndPermissionsCache  _cache;
    private final NamedParameterJdbcTemplate _template;
    private final DatabaseHelper             _helper;
}
