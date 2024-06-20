/*
 * web: org.nrg.xnat.services.cache.DefaultUserProjectCache
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.om.XnatDatatypeprotocol;
import org.nrg.xdat.om.XnatInvestigatordata;
import org.nrg.xdat.om.XnatProjectdata;
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
import org.nrg.xnat.services.cache.extractors.DataExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.nrg.xdat.entities.UserRole.ROLE_ADMINISTRATOR;
import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Collaborator;
import static org.nrg.xdat.security.helpers.AccessLevel.DataAccess;
import static org.nrg.xdat.security.helpers.AccessLevel.DataAdmin;
import static org.nrg.xdat.security.helpers.AccessLevel.Delete;
import static org.nrg.xdat.security.helpers.AccessLevel.Edit;
import static org.nrg.xdat.security.helpers.AccessLevel.Member;
import static org.nrg.xdat.security.helpers.AccessLevel.Owner;
import static org.nrg.xdat.security.helpers.AccessLevel.Read;
import static org.nrg.xdat.security.helpers.Groups.OPERATION_ADD_USERS;
import static org.nrg.xdat.security.helpers.Groups.OPERATION_REMOVE_USERS;
import static org.nrg.xdat.security.helpers.Groups.getGroup;
import static org.nrg.xdat.security.helpers.Roles.ADDED_ROLES;
import static org.nrg.xdat.security.helpers.Roles.DELETED_ROLES;
import static org.nrg.xdat.security.helpers.Roles.OPERATION_ADD_ROLE;
import static org.nrg.xdat.security.helpers.Roles.OPERATION_ADD_ROLES;
import static org.nrg.xdat.security.helpers.Roles.OPERATION_DELETE_ROLE;
import static org.nrg.xdat.security.helpers.Roles.OPERATION_DELETE_ROLES;
import static org.nrg.xdat.security.helpers.Roles.OPERATION_MODIFIED_ROLES;
import static org.nrg.xdat.security.helpers.Roles.ROLE;
import static org.nrg.xdat.security.helpers.Roles.ROLES;
import static org.nrg.xft.event.XftItemEventI.CREATE;
import static org.nrg.xft.event.XftItemEventI.DELETE;
import static org.nrg.xft.event.XftItemEventI.OPERATION;
import static org.nrg.xft.event.XftItemEventI.UPDATE;

@SuppressWarnings({"Duplicates", "deprecation"})
@Service("userProjectCache")
@Slf4j
public class DefaultUserProjectCache extends AbstractXftItemAndCacheEventHandlerMethod implements UserProjectCache, Initializing {
    public static final String CACHE_PROJECTS            = "projects";
    public static final String CACHE_PROJECT_USER_ACCESS = "projectUserAccess";

    @Autowired
    public DefaultUserProjectCache(final JCacheHelper helper, final GroupsAndPermissionsCache cache, final SerializerService serializer, final NamedParameterJdbcTemplate template, final List<DataExtractor<?, ?>> extractors) {
        super(helper,
              extractors.stream().filter(extractor -> StringUtils.equals(extractor.getCacheGroup(), UserProjectCache.CACHE_NAME)).collect(Collectors.toList()),
              XftItemEventCriteria.getXsiTypeCriteria(XnatProjectdata.SCHEMA_ELEMENT_NAME),
              XftItemEventCriteria.getXsiTypeCriteria(XnatDatatypeprotocol.SCHEMA_ELEMENT_NAME),
              XftItemEventCriteria.getXsiTypeCriteria(XnatInvestigatordata.SCHEMA_ELEMENT_NAME),
              XftItemEventCriteria.builder().xsiType(XdatUsergroup.SCHEMA_ELEMENT_NAME).predicate(XftItemEventCriteria.IS_PROJECT_GROUP.or(XftItemEventCriteria.IS_ALL_DATA_ADMIN_OR_ACCESS)).build(),
              XftItemEventCriteria.builder().xsiType(XdatUser.SCHEMA_ELEMENT_NAME).action(UPDATE).predicate(PREDICATE_IS_ROLE_OPERATION).build());
        _cache      = cache;
        _serializer = serializer;
        _template   = template;
        _helper     = new DatabaseHelper((JdbcTemplate) _template.getJdbcOperations());
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
        _template.query(QUERY_GET_IDS_AND_ALIASES, resultSet -> {
            final String projectId = resultSet.getString("project_id");
            final String idOrAlias = resultSet.getString("id_or_alias");
            _aliasMapping.put(idOrAlias, projectId);
            _projectsAndAliases.put(projectId, idOrAlias);
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
    public String getCacheName() {
        return CACHE_NAME;
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
        final String projectId = getCanonicalProjectId(idOrAlias);
        if (StringUtils.isBlank(projectId)) {
            log.info("Checking delete privileges for user '{}' for project ID or alias '{}' but that doesn't seem to be a valid project ID or alias, returning false", userId, idOrAlias);
            return false;
        }
        return hasAccess(userId, projectId, Delete);
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
        final String projectId = getCanonicalProjectId(idOrAlias);
        if (StringUtils.isBlank(projectId)) {
            log.info("Checking edit privileges for user '{}' for project ID or alias '{}' but that doesn't seem to be a valid project ID or alias, returning false", userId, idOrAlias);
            return false;
        }
        return hasAccess(userId, projectId, Edit);
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
        final String projectId = getCanonicalProjectId(idOrAlias);
        if (StringUtils.isBlank(projectId)) {
            log.info("Checking read privileges for user '{}' for project ID or alias '{}' but that doesn't seem to be a valid project ID or alias, returning false", userId, idOrAlias);
            return false;
        }
        return hasAccess(userId, projectId, Read);
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
        final String projectId = getCanonicalProjectId(idOrAlias);
        if (StringUtils.isBlank(projectId)) {
            if (user != null) {
                log.debug("User '{}' requested a project with ID or alias '{}' but that's not in the alias mapping cache or xnat_projectdata table, so doesn't seem to exist, returning null", user.getUsername(), idOrAlias);
            } else {
                log.debug("A system call (no user specified) requested a project with ID or alias '{}' but that's not in the alias mapping cache or xnat_projectdata table, so doesn't seem to exist, returning null", idOrAlias);
            }
            return null;
        }
        final String userId;
        if (user != null) {
            userId = user.getUsername();
            // Check that the project is readable by the user and, if not, return null.
            if (!hasAccess(userId, projectId, Read)) {
                log.info("User '{}' attempted to retrieve the project '{}' with ID or alias '{}', but the user doesn't have at least read access to that project. Returning null.", userId, projectId, idOrAlias);
                return null;
            }
        }

        XnatProjectdata project = getCacheItem(CACHE_PROJECTS, projectId, XnatProjectdata.class);
        if (user != null) {
            project.setUser(user);
        }
        return project;
    }

    @Override
    public List<XnatProjectdata> getAll(final UserI user) {
        return getProjectsFromIds(user, _cache.getProjectsForUser(user.getUsername(), SecurityManager.READ));
    }

    @Override
    public List<XnatProjectdata> getByField(final UserI user, final String field, final String value) {
        final String standardized = XftStringUtils.StandardizeXMLPath(field);
        log.debug("User {} requested to retrieve project by field '{}' (standardized: '{}') with value '{}'", user.getUsername(), field, standardized, value);

        try {
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
            log.error("Got an error trying to retrieve project by field '{}' (standardized: '{}') with value '{}' for user {}", field, standardized, value, user.getUsername());
            return null;
        }
    }

    @Override
    public void clearProjectCacheEntry(final String idOrAlias) {
        evictProjectCache(idOrAlias);
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

        final String action = event.getAction();
        log.info("Got an XFTItemEvent for project '{}' with action '{}', handling now", projectId, action);
        switch (action) {
            case CREATE:
                log.debug("Created new project, caching ID '{}' and initializing project cache entry", projectId);
                _aliasMapping.put(projectId, projectId);
                getCacheItem(CACHE_PROJECTS, projectId, XnatProjectdata.class);
                break;

            case DELETE:
                final List<String> aliases = _projectsAndAliases.removeAll(projectId);
                log.debug("The project {} was deleted, so evicting cache entry and removing ID and any aliases: {}", projectId, aliases);
                _aliasMapping.remove(projectId);
                for (final String alias : aliases) {
                    _aliasMapping.remove(alias);
                }
                evictProjectCache(projectId);
                break;

            case UPDATE:
                log.debug("The project '{}' was updated, refreshing cache entry", projectId);
                refreshProjectCache(projectId);
                break;

            default:
                log.warn("Got the action '{}' for project '{}', I don't know what to do with this action.", action, projectId);
                return false;
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
            log.info("Got a non-project-related group event, which I'm not supposed to handle: {}", event);
            return false;
        }

        final String projectId = idAndAccess.getLeft();
        if (StringUtils.isBlank(projectId)) {
            return false;
        }

        final AccessLevel access = AccessLevel.getAccessLevel(idAndAccess.getRight());

        // When groups are created for projects, the groups are created *before* the project. We need to check whether the
        // project is already cached, so we don't try to refresh the cache before the project's actually in.
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
        final Collection<String> users          = (Collection<String>) properties.get(Groups.USERS);
        final boolean            isAddOperation = StringUtils.equals(operation, OPERATION_ADD_USERS);
        final XnatProjectdata    project        = getCacheItem(CACHE_PROJECTS, projectId, XnatProjectdata.class);
        if (project == null) {
            if (isAddOperation) {
                log.debug("Users had access level {} added for project {}, but that project's not in the cache: {}", access, projectId, users);
            } else {
                log.debug("Users had access level {} revoked for project {}, but that project's not in the cache: {}", access, projectId, users);
            }
            return true;
        }

        final Map<String, ArrayList<AccessLevel>> userAccessLevels = getUserAccessLevelsForProject(projectId);
        for (final String username : users) {
            if (isAddOperation) {
                userAccessLevels.computeIfAbsent(username, (key) -> new ArrayList<>()).add(access);
            } else {
                final List<AccessLevel> levels = userAccessLevels.get(username);
                if (levels != null) {
                    levels.remove(access);
                }
            }
        }

        try {
            cacheObject(CACHE_PROJECT_USER_ACCESS, projectId, _serializer.toJson(userAccessLevels));
        } catch (IOException e) {
            throw new NrgServiceRuntimeException("An error occurred trying to serialize user access levels for the project " + projectId, e);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean handleUserEvent(final XftItemEventI event) {
        final String         username   = event.getId();
        final Map<String, ?> properties = event.getProperties();
        final String         operation  = (String) properties.get(OPERATION);
        final boolean        addedAdmin;
        final boolean        deletedAdmin;

        switch (operation) {
            case OPERATION_ADD_ROLE:
                addedAdmin = StringUtils.equals(ROLE_ADMINISTRATOR, (String) properties.get(ROLE));
                deletedAdmin = false;
                break;

            case OPERATION_ADD_ROLES:
                addedAdmin = ((List<String>) properties.get(ROLES)).contains(ROLE_ADMINISTRATOR);
                deletedAdmin = false;
                break;

            case OPERATION_DELETE_ROLE:
                addedAdmin = false;
                deletedAdmin = StringUtils.equals(ROLE_ADMINISTRATOR, (String) properties.get(ROLE));
                break;

            case OPERATION_DELETE_ROLES:
                addedAdmin = false;
                deletedAdmin = ((List<String>) properties.get(ROLES)).contains(ROLE_ADMINISTRATOR);
                break;

            case OPERATION_MODIFIED_ROLES:
                final List<String> addedRoles = (List<String>) properties.get(ADDED_ROLES);
                final List<String> deletedRoles = (List<String>) properties.get(DELETED_ROLES);
                addedAdmin = addedRoles != null && addedRoles.contains(ROLE_ADMINISTRATOR);
                deletedAdmin = deletedRoles != null && deletedRoles.contains(ROLE_ADMINISTRATOR);
                break;

            default:
                addedAdmin = false;
                deletedAdmin = false;
        }
        if (addedAdmin) {
            _siteAdmins.add(username);
        }
        if (deletedAdmin) {
            _siteAdmins.remove(username);
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
        log.info("Got the {} event for the data-type protocol {}. This should only happen when changes to the protocol affected field definition groups that are non-project specific.", event.getAction(), event.getId());
        return true;
    }

    private Map<String, ArrayList<AccessLevel>> getUserAccessLevelsForProject(final String projectId) {
        final String serialized = getCacheItem(CACHE_PROJECT_USER_ACCESS, projectId, String.class);
        if (StringUtils.isBlank(serialized)) {
            return Collections.emptyMap();
        }
        try {
            return _serializer.deserializeJson(serialized, USER_ACCESS_LEVEL_TYPE_REF);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        return projectIds.stream().map(projectId -> get(user, projectId)).collect(Collectors.toList());
    }

    /**
     * Tests whether the indicated user can access the project with the given ID at the requested level. Note that this
     * method expects the project ID, not an alias, so it should be resolved to project ID before calling this method.
     *
     * @param userId      The username of the user to test.
     * @param projectId   The ID of the project to be queried.
     * @param accessLevel The access level requested.
     *
     * @return Returns true if the user has <i>at least</i> the specified access level to the project.
     */
    private boolean hasAccess(@Nonnull final String userId, @Nonnull final String projectId, @Nonnull final AccessLevel accessLevel) {
        if (StringUtils.isAnyBlank(userId, projectId)) {
            log.error("You must specify a value for both user ID and project ID.");
            return false;
        }

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
                    isSiteAdmin  = true;
                    isDataAdmin  = false;
                    isDataAccess = false;
                } else if (user.isDataAdmin()) {
                    _dataAdmins.add(userId);
                    isSiteAdmin  = false;
                    isDataAdmin  = true;
                    isDataAccess = false;
                } else if (user.isDataAccess()) {
                    _dataAccess.add(userId);
                    isSiteAdmin  = false;
                    isDataAdmin  = false;
                    isDataAccess = true;
                } else {
                    // Not an admin but let's track that we've retrieved the user by adding it to the non-admin list.
                    _nonAdmins.add(userId);
                    isSiteAdmin  = false;
                    isDataAdmin  = false;
                    isDataAccess = false;
                }
            } catch (UserNotFoundException e) {
                // User doesn't exist, so return false
                log.error("Got a request to test '{}' access to project with ID or alias '{}' for user {}, but that user doesn't exist.", accessLevel, projectId, userId);
                return false;
            } catch (UserInitException e) {
                // Something bad happened so note it and move on.
                log.error("Something bad happened trying to retrieve the user {}", userId, e);
                return false;
            }
        } else {
            // Set the user to null. It will only get initialized later in the initProjectCache() method if required.
            user         = null;
            isSiteAdmin  = _siteAdmins.contains(userId);
            isDataAdmin  = _dataAdmins.contains(userId);
            isDataAccess = _dataAccess.contains(userId);
        }

        try {
            final XnatProjectdata project = get(user, projectId);

            // If the project cache is null, the project doesn't exist.
            if (project == null) {
                return false;
            }

            // We don't care about checking the user against the project if it's a site admin: they have access to everything.
            if (isSiteAdmin || isDataAdmin || isDataAccess) {
                return true;
            }

            final Map<String, ArrayList<AccessLevel>> userAccessLevels = getUserAccessLevelsForProject(projectId);

            // If the user isn't already cached...
            if (!userAccessLevels.containsKey(userId)) {
                // Cache the user!
                userAccessLevels.put(userId, new ArrayList<>(Permissions.getAllUserProjectAccess(ObjectUtils.defaultIfNull(user, new XDATUser(userId)), projectId)));
                try {
                    cacheObject(CACHE_PROJECT_USER_ACCESS, projectId, _serializer.toJson(userAccessLevels));
                } catch (IOException e) {
                    throw new NrgServiceRuntimeException("An error occurred trying to serialize user access levels for the project " + projectId, e);
                }
            }
            return CollectionUtils.containsAny(userAccessLevels.get(userId), ACCESS_LEVELS.get(accessLevel));
        } catch (UserInitException e) {
            log.error("Something bad happened trying to retrieve the user {}", userId, e);
        } catch (UserNotFoundException e) {
            log.error("A user not found exception occurred searching for the user {}. This really shouldn't happen as I checked for the user's existence earlier.", userId, e);
        } catch (Exception e) {
            log.error("An error occurred trying to test whether the user {} can read the project specified by ID or alias {}", userId, projectId, e);
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
     *
     * @return The ID of the project with the submitted ID or alias.
     */
    private String getCanonicalProjectId(final String idOrAlias) {
        // First check for cached ID or alias.
        if (_aliasMapping.containsKey(idOrAlias)) {
            // Found it so return that.
            final String projectId = _aliasMapping.get(idOrAlias);
            log.debug("Found cached project ID {} for the ID or alias {}", projectId, idOrAlias);
            return projectId;
        }

        log.debug("Couldn't find project by ID or alias {}, querying database.", idOrAlias);
        try {
            final String projectId = _template.queryForObject(QUERY_GET_PROJECT_BY_ID_OR_ALIAS, new MapSqlParameterSource("idOrAlias", idOrAlias), String.class);
            log.debug("After querying, found canonical project ID {} for the ID or alias {}", projectId, idOrAlias);
            return projectId;
        } catch (EmptyResultDataAccessException e) {
            log.debug("After querying, no project was found that matches the ID or alias {}", idOrAlias);
            return null;
        } catch (IncorrectResultSizeDataAccessException e) {
            log.error("Somehow came back with more than one result searching for the project ID or alias {}: should have gotten {} result and ended up with {}", idOrAlias, e.getExpectedSize(), e.getActualSize());
            throw e;
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

    private void refreshProjectCache(final String projectId) {
        evictProjectCache(projectId);
        getCacheItem(CACHE_PROJECTS, projectId, XnatProjectdata.class);
    }

    private void evictProjectCache(final String projectId) {
        final XnatProjectdata project = getCacheItem(CACHE_PROJECTS, projectId, XnatProjectdata.class);
        if (project == null) {
            log.info("No cache found for the project '{}', nothing much to be done.", projectId);
        } else {
            log.info("Found project cache for project {}, evicting the project cache.", projectId);
            evict(CACHE_PROJECTS, projectId);
        }
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

    private static boolean isProjectCacheEvent(final Cache cache) {
        return StringUtils.equals(CACHE_NAME, cache.getName());
    }

    @SuppressWarnings("unused")
    private static boolean isProjectCacheEvent(final Cache cache, final Object element) {
        return isProjectCacheEvent(cache) && (element == null || element instanceof XnatProjectdata);
    }


    private static MapSqlParameterSource getProjectAccessParameterSource(final String projectId, final String accessLevel) {
        return new MapSqlParameterSource(QUERY_KEY_PROJECT_ID, projectId).addValue(QUERY_KEY_ACCESS_LEVEL, accessLevel);
    }

    private static final Predicate<XftItemEventI> PREDICATE_IS_ROLE_OPERATION = event -> {
        final Map<String, ?> properties = event.getProperties();
        return !properties.isEmpty() && StringUtils.equalsAny((String) properties.get(OPERATION), OPERATION_ADD_ROLE, OPERATION_DELETE_ROLE);
    };

    private static final TypeReference<HashMap<String, ArrayList<AccessLevel>>> USER_ACCESS_LEVEL_TYPE_REF = new TypeReference<HashMap<String, ArrayList<AccessLevel>>>() {
    };

    private static final List<AccessLevel>                   DELETABLE_ACCESS                 = Arrays.asList(Owner, Delete, Admin);
    private static final List<AccessLevel>                   WRITABLE_ACCESS                  = Arrays.asList(Member, Edit, Admin, DataAdmin);
    private static final List<AccessLevel>                   READABLE_ACCESS                  = Arrays.asList(Collaborator, Read, Admin, DataAdmin, DataAccess);
    private static final Map<AccessLevel, List<AccessLevel>> ACCESS_LEVELS                    = ImmutableMap.<AccessLevel, List<AccessLevel>>builder()
                                                                                                            .put(Delete, DELETABLE_ACCESS)
                                                                                                            .put(Edit, Stream.of(DELETABLE_ACCESS, WRITABLE_ACCESS).flatMap(Collection::stream).collect(Collectors.toList()))
                                                                                                            .put(Read, Stream.of(DELETABLE_ACCESS, WRITABLE_ACCESS, READABLE_ACCESS).flatMap(Collection::stream).collect(Collectors.toList())).build();
    private static final Map<String, List<AccessLevel>>      USER_GROUP_SUFFIXES              = ImmutableMap.of("owner", DELETABLE_ACCESS, "member", WRITABLE_ACCESS, "collaborator", READABLE_ACCESS);
    private static final String                              QUERY_KEY_PROJECT_ID             = "projectId";
    private static final String                              QUERY_KEY_ACCESS_LEVEL           = "accessLevel";
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
    private final SerializerService          _serializer;
    private final NamedParameterJdbcTemplate _template;
    private final DatabaseHelper             _helper;
}
