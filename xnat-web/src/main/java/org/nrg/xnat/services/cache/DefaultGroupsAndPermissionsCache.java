package org.nrg.xnat.services.cache;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.utilities.LapStopWatch;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.om.XdatElementSecurity;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementAccessManager;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.PermissionCriteriaI;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.UserGroup;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.Initializing;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xdat.servlet.XDATServlet;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.methods.XftItemEventCriteria;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.search.SQLClause.ParamValue;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.cache.extractors.DataExtractor;
import org.nrg.xnat.services.cache.jms.InitializeGroupRequest;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.cache.Cache;
import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Long.max;
import static org.nrg.xdat.security.PermissionCriteria.dumpCriteriaList;
import static org.nrg.xdat.security.helpers.Groups.ALL_DATA_ACCESS_GROUP;
import static org.nrg.xdat.security.helpers.Groups.ALL_DATA_ADMIN_GROUP;
import static org.nrg.xdat.security.helpers.Groups.ALL_DATA_GROUPS;
import static org.nrg.xdat.security.helpers.Groups.USERS;
import static org.nrg.xdat.security.helpers.Users.DEFAULT_GUEST_USERNAME;
import static org.nrg.xnat.services.cache.extractors.DataExtractor.PARAM_DATA_TYPE;
import static org.nrg.xnat.services.cache.extractors.DataExtractor.PARAM_EXPERIMENT_ID;
import static org.nrg.xnat.services.cache.extractors.DataExtractor.PARAM_PROJECT_ID;
import static org.nrg.xnat.services.cache.extractors.DataExtractor.PARAM_USERNAME;

@Service(GroupsAndPermissionsCache.CACHE_NAME)
@Slf4j
public class DefaultGroupsAndPermissionsCache extends AbstractXftItemAndCacheEventHandlerMethod implements GroupsAndPermissionsCache, Initializing, GroupsAndPermissionsCache.Provider {
    public static final int SMALL_SERVER_SUBJ_COUNT = 10000;
    public static final List<String> PERMISSIONS = Arrays.asList(SecurityManager.ACTIVATE, SecurityManager.CREATE, SecurityManager.DELETE, SecurityManager.EDIT, SecurityManager.READ);
    public static final String CACHE_ACCESS_MANAGERS   = "accessManagers";
    public static final String CACHE_ACTIONS           = "actions";
    public static final String CACHE_BROWSEABLES       = "browseables";
    public static final String CACHE_GROUPS            = "groups";
    public static final String CACHE_PROJECT_GROUPS    = "projectGroups";
    public static final String CACHE_PROJECT_MEMBERS   = "projectMembers";
    public static final String CACHE_READABLE_COUNTS   = "readableCounts";
    public static final String CACHE_USER_GROUPS       = "userGroups";
    public static final String CACHE_USER_LAST_UPDATED = "userLastUpdated";

    // These are duplicative but help to distinguish security actions from XftItemEventI actions.
    private static final String ACTION_READ     = SecurityManager.READ;
    private static final String ACTION_EDIT     = SecurityManager.EDIT;
    private static final String ACTION_CREATE   = SecurityManager.CREATE;
    private static final String ACTION_DELETE   = SecurityManager.DELETE;
    private static final String EVENT_UPDATE    = XftItemEventI.UPDATE;
    private static final String EVENT_CREATE    = XftItemEventI.CREATE;
    private static final String EVENT_DELETE    = XftItemEventI.DELETE;
    private static final String EVENT_SHARE     = XftItemEventI.SHARE;
    private static final String EVENT_MOVE      = XftItemEventI.MOVE;
    private static final String EVENT_OPERATION = XftItemEventI.OPERATION;


    private static final List<String> USER_CACHES                          = Arrays.asList(CACHE_ACCESS_MANAGERS,
            CACHE_BROWSEABLES,
            CACHE_READABLE_COUNTS,
            CACHE_USER_GROUPS,
            CACHE_USER_LAST_UPDATED);
    private static final String       COLUMN_ELEMENT_NAME                  = "element_name";
    private static final String       COLUMN_ELEMENT_COUNT                 = "count";
    private static final String       QUERY_EXPT_COUNTS_BY_TYPE            = "SELECT " + COLUMN_ELEMENT_NAME + ", COUNT(id) AS " + COLUMN_ELEMENT_COUNT + " " +
            "FROM xnat_experimentData x " +
            "         LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id " +
            "GROUP BY " + COLUMN_ELEMENT_NAME;
    private static final String       QUERY_PROJECT_COUNTS                 = "SELECT COUNT(*) FROM xnat_projectdata";
    private static final String       QUERY_SUBJECT_COUNTS                 = "SELECT COUNT(*) FROM xnat_subjectdata";
    private static final String       QUERY_SESSION_COUNTS                 = "SELECT COUNT(*) FROM xnat_imagesessiondata";
    private static final String       QUERY_GET_ALL_ROLE_GROUPS            = "SELECT " +
            "  tag AS project_id, " +
            "  id AS group_id " +
            "FROM " +
            "  xdat_usergroup " +
            "WHERE " +
            "  tag IS NOT NULL AND " +
            "  id LIKE '%%_%s' " +
            "ORDER BY project_id, group_id";
    private static final String       QUERY_GET_ALL_MEMBER_GROUPS          = String.format(QUERY_GET_ALL_ROLE_GROUPS, "member");
    private static final String       QUERY_GET_ALL_COLLAB_GROUPS          = String.format(QUERY_GET_ALL_ROLE_GROUPS, "collaborator");
    private static final String       QUERY_ORPHANED_EXPERIMENTS           = "SELECT " +
            "    experiment_id, " +
            "    data_type, " +
            "    coalesce(xdat_meta_element_id, -1) AS xdat_meta_element_id " +
            "FROM " +
            "    data_type_views_experiments_without_data_type";
    private static final String       QUERY_CORRECT_ORPHANED_EXPERIMENTS   = "SELECT " +
            "    orphaned_experiment, " +
            "    original_data_type " +
            "FROM " +
            "    data_type_fns_correct_experiment_extension()";
    private static final String       QUERY_ACCESSIBLE_DATA_PROJECTS       = "SELECT  " +
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
            "     a.element_name = '" + XnatSubjectdata.SCHEMA_ELEMENT_NAME + "' AND  " +
            "     f.%s = 1 AND  " +
            "     u.login IN ('guest', :" + PARAM_USERNAME + ")) projects";
    private static final String       QUERY_READABLE_PROJECTS              = String.format(QUERY_ACCESSIBLE_DATA_PROJECTS, "read_element");
    private static final String       QUERY_EDITABLE_PROJECTS              = String.format(QUERY_ACCESSIBLE_DATA_PROJECTS, "edit_element");
    private static final String       QUERY_OWNED_PROJECTS                 = String.format(QUERY_ACCESSIBLE_DATA_PROJECTS, "delete_element");
    private static final String       QUERY_HAS_ALL_DATA_PRIVILEGES        = "SELECT  " +
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
            "           ea.element_name = '" + XnatProjectdata.SCHEMA_ELEMENT_NAME + "' AND  " +
            "           fm.%s = 1 AND  " +
            "           u.login = :" + PARAM_USERNAME + ")";
    private static final String       QUERY_HAS_ALL_DATA_ACCESS            = String.format(QUERY_HAS_ALL_DATA_PRIVILEGES, "read_element");
    private static final String       QUERY_HAS_ALL_DATA_ADMIN             = String.format(QUERY_HAS_ALL_DATA_PRIVILEGES, "edit_element");
    private static final String       QUERY_ALL_DATA_ACCESS_PROJECTS       = "SELECT id AS project FROM xnat_projectdata ORDER BY project";
    private static final String       QUERY_GET_GROUP_FOR_USER_AND_PROJECT = "SELECT id " +
            "FROM xdat_usergroup xug " +
            "  LEFT JOIN xdat_user_groupid xugid ON xug.id = xugid.groupid " +
            "  LEFT JOIN xdat_user xu ON xugid.groups_groupid_xdat_user_xdat_user_id = xu.xdat_user_id " +
            "WHERE xu.login = :" + PARAM_USERNAME + " AND tag = :" + PARAM_PROJECT_ID + " " +
            "ORDER BY groupid";
    private static final String       QUERY_PROJECT_OWNERS                 = "SELECT DISTINCT u.login AS owner " +
            "FROM xdat_user                     u " +
            "  LEFT JOIN xdat_user_groupid      map ON u.xdat_user_id = map.groups_groupid_xdat_user_xdat_user_id " +
            "  LEFT JOIN xdat_usergroup         g ON map.groupid = g.id " +
            "  LEFT JOIN xdat_element_access    xea ON (g.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id OR u.xdat_user_id = xea.xdat_user_xdat_user_id) " +
            "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
            "  LEFT JOIN xdat_field_mapping     xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
            "WHERE " +
            "  xfm.field_value != '*' AND " +
            "  xea.element_name = '" + XnatProjectdata.SCHEMA_ELEMENT_NAME + "' AND " +
            "  xfm.delete_element = 1 AND " +
            "  g.id LIKE '%_owner' AND " +
            "  g.tag = :" + PARAM_PROJECT_ID + " " +
            "ORDER BY owner";
    private static final String       QUERY_GET_USERS_FOR_PROJECTS         = "SELECT DISTINCT u.login " +
            "FROM xdat_user u " +
            "  LEFT JOIN xdat_user_groupid gid ON u.xdat_user_id = gid.groups_groupid_xdat_user_xdat_user_id " +
            "  LEFT JOIN xdat_usergroup g ON gid.groupid = g.id " +
            "  LEFT JOIN xdat_element_access xea ON g.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
            "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
            "  LEFT JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
            "WHERE " +
            "  tag IN (:projectIds) OR " +
            "  tag IS NULL AND field_value = '*' " +
            "ORDER BY login";
    private static final String       QUERY_GET_GROUPS_FOR_DATATYPE        = "SELECT DISTINCT usergroup.id AS group_name " +
            "FROM xdat_usergroup usergroup " +
            "  LEFT JOIN xdat_element_access xea ON usergroup.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
            "WHERE " +
            "  xea.element_name = :" + PARAM_DATA_TYPE + " " +
            "ORDER BY group_name";
    private static final String       QUERY_GET_SUBJECT_PROJECT            = "SELECT project FROM xnat_subjectdata WHERE id = :subjectId OR label = :subjectId";
    private static final String       QUERY_GET_EXPERIMENT_PROJECT         = "SELECT project FROM xnat_experimentdata WHERE id = :" + PARAM_EXPERIMENT_ID;

    private static final NumberFormat              NUMBER_FORMAT       = NumberFormat.getNumberInstance(Locale.getDefault());
    private static final DateFormat                DATE_FORMAT         = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
    private static final Predicate<ElementDisplay> CONTAINS_MR_SESSION = display -> StringUtils.equalsIgnoreCase(XnatMrsessiondata.SCHEMA_ELEMENT_NAME, display.getElementName());

    private final UserManagementServiceI     _userService;
    private final NamedParameterJdbcTemplate _template;
    private final JmsTemplate                _jmsTemplate;
    private final DatabaseHelper             _helper;
    private  Map<String, Long>          _totalCounts;
    private final AtomicBoolean              _initialized;

    private Listener _listener;
    private XDATUser _guest;

    @Autowired
    public DefaultGroupsAndPermissionsCache(final JCacheHelper cacheHelper, final UserManagementServiceI userService, final NamedParameterJdbcTemplate template, final List<DataExtractor<?, ?>> extractors, final JmsTemplate jmsTemplate) {
        super(cacheHelper,
                extractors.stream().filter(extractor -> StringUtils.equals(extractor.getCacheGroup(), GroupsAndPermissionsCache.CACHE_NAME)).collect(Collectors.toList()),
                XftItemEventCriteria.builder().xsiType(XnatProjectdata.SCHEMA_ELEMENT_NAME).actions(EVENT_CREATE, EVENT_UPDATE, EVENT_DELETE).build(),
                XftItemEventCriteria.builder().xsiType(XnatSubjectdata.SCHEMA_ELEMENT_NAME).xsiType(XnatExperimentdata.SCHEMA_ELEMENT_NAME).actions(EVENT_CREATE, EVENT_DELETE, EVENT_SHARE).build(),
                XftItemEventCriteria.getXsiTypeCriteria(XdatUsergroup.SCHEMA_ELEMENT_NAME),
                XftItemEventCriteria.getXsiTypeCriteria(XdatElementSecurity.SCHEMA_ELEMENT_NAME));
        _userService = userService;
        _template    = template;
        _jmsTemplate = jmsTemplate;
        _helper      = new DatabaseHelper(_template);
        _totalCounts = new ConcurrentHashMap<>();
        _initialized = new AtomicBoolean(false);
    }

    @Override
    public String getCacheName() {
        return GroupsAndPermissionsCache.CACHE_NAME;
    }

    @Override
    public boolean canInitialize() {
        // If it's already initialized, you can't re-initialize.
        if (_initialized.get()) {
            return false;
        }
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

    @Override
    public Future<Boolean> initialize() {
        final LapStopWatch stopWatch = LapStopWatch.createStarted(log, Level.INFO);

        final int tags = getProjectGroupsExtractor().initialize(getProjectGroupsCache());
        stopWatch.lap("Processed {} tags", tags);

        final List<String> groupIds = Groups.getAllGroupIds(_template);
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
            if (stopWatch.isStarted()) {
                stopWatch.stop();
            }
            log.info("Total time to queue {} groups was {} ms", groupIds.size(), NUMBER_FORMAT.format(stopWatch.getTime()));
            if (log.isInfoEnabled()) {
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

        final Date completed = _listener.getCompleted();

        if (unprocessed.isEmpty() && completed != null) {
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

    @Nullable
    @Override
    public UserGroupI get(final String groupId) {
        final UserGroupI group = ObjectUtils.getIfNull(getCachedGroup(groupId), () -> null);
        if (group != null) {
            log.debug("Found group for ID '{}'", groupId);
        } else {
            log.info("Someone requested group with ID '{}' but I can't find that group", groupId);
        }
        return group;
    }

    @Override
    public Map<String, Long> getReadableCounts(final UserI user) {
        return getReadableCounts(user.getUsername());
    }

    @Override
    public Map<String, Long> getReadableCounts(final String username) {
        updateUserLastUpdateCacheIfEmpty(CACHE_READABLE_COUNTS, username);
        return getCacheMap(CACHE_READABLE_COUNTS, username, String.class, Long.class);
    }

    @Override
    public Map<String, ElementDisplay> getBrowseableElementDisplays(final UserI user) {
        return getBrowseableElementDisplays(user.getUsername());
    }

    @Override
    public Map<String, ElementDisplay> getBrowseableElementDisplays(final String username) {
        updateUserLastUpdateCacheIfEmpty(CACHE_BROWSEABLES, username);
        return getCacheMap(CACHE_BROWSEABLES, username, String.class, ElementDisplay.class);
    }

    @Override
    public List<ElementDisplay> getSearchableElementDisplays(final UserI user) {
        return getSearchableElementDisplays(user.getUsername());
    }

    private boolean isSmallServer(){
        return getTotalCounts().getOrDefault(XnatSubjectdata.SCHEMA_ELEMENT_NAME, 0L) < SMALL_SERVER_SUBJ_COUNT;
    }

    @Override
    public List<ElementDisplay> getSearchableElementDisplays(final String username) {
        log.debug("Retrieving searchable element displays for user {}", username);

        final Map<String, Long> counts = getReadableCounts(username);
        // CACHING: Extended logging
        if (log.isDebugEnabled()) {
            log.debug("Retrieved {} readable counts for user {}:\n{}", counts.size(), username, counts.entrySet().stream().map(entry -> " * " + entry.getKey() + ": " + entry.getValue()).collect(Collectors.joining("\n")));
        } else {
            log.info("Retrieved {} readable counts for user {}", counts.size(), username);
        }
        try {
            return getActionElementDisplays(username, ACTION_READ).stream().filter(display -> {
                if (display == null) {
                    log.info("Null display found for user {} action read", username);
                    return false;
                }
                final String name = display.getElementName();
                try {
                    final boolean isDisplaySearchable = ElementSecurity.IsSearchable(name);
                    final long    displayCount        = counts.getOrDefault(name, 0L);
                    // CACHING: Extended logging
                    log.debug("User {} action read display {} {} searchable: current display count is {}, {} be included", username, name, isDisplaySearchable ? "is" : "is not", displayCount, isDisplaySearchable && displayCount > 0 ? "will" : "won't");
                    return isDisplaySearchable && displayCount > 0;
                } catch (Exception e) {
                    log.error("An error occurred trying to test if display {} is searchable", name, e);
                    return false;
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("An unknown error occurred while trying to retrieve action element displays for user {}", username, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<ElementDisplay> getActionElementDisplays(final UserI user, final String action) {
        return getActionElementDisplays(user.getUsername(), action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ElementDisplay> getActionElementDisplays(final String username, final String action) {
        // CACHING: This is a very awkward construction, it would be nice to reduce it to getActionsCache().get(username).get(action)
        return GenericUtils.convertToTypedList(getCacheMapPartitionItem(CACHE_ACTIONS, action, username, List.class), ElementDisplay.class);
    }

    @Override
    public List<PermissionCriteriaI> getPermissionCriteria(final UserI user, final String dataType) {
        return getPermissionCriteria(user.getUsername(), dataType);
    }

    @Override
    public List<PermissionCriteriaI> getPermissionCriteria(final String username, final String dataType) {
        try {
            PoolDBUtils.CheckSpecialSQLChars(dataType);
        } catch (Exception e) {
            throw new IllegalArgumentException("The specified data type \"" + dataType + "\" includes one or more reserved characters", e);
        }

        try {
            final List<PermissionCriteriaI> criteria = new ArrayList<>();

            final Map<String, ElementAccessManager> managers = getElementAccessManagers(username);
            if (managers.isEmpty()) {
                log.info("Couldn't find element access managers for user {} trying to retrieve permissions for data type {}", username, dataType);
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

            final Map<String, UserGroupI> userGroups = getMutableGroupsForUser(username);
            log.debug("Found {} user groups for the user {}{}", userGroups.size(), username, userGroups.isEmpty() ? "" : ": " + StringUtils.join(userGroups.keySet(), ", "));
            final Set<String> groups = userGroups.keySet();
            if (CollectionUtils.containsAny(groups, ALL_DATA_GROUPS)) {
                if (groups.contains(ALL_DATA_ADMIN_GROUP)) {
                    _template.query(QUERY_GET_ALL_MEMBER_GROUPS, resultSet -> {
                        final String projectId = resultSet.getString("project_id");
                        final String groupId   = resultSet.getString("group_id");

                        // If the user is a collaborator on a project, we're going to upgrade them to member,
                        // so remove that collaborator nonsense, this is the big time.
                        userGroups.remove(projectId + "_collaborator");

                        // If the user is already a member of owner of a project, then don't bother: they already have
                        // sufficient access to the project.
                        if (!userGroups.containsKey(projectId + "_owner") && !userGroups.containsKey(projectId + "_member")) {
                            userGroups.put(groupId, get(groupId));
                        }
                    });
                } else if (userGroups.containsKey(ALL_DATA_ACCESS_GROUP)) {
                    _template.query(QUERY_GET_ALL_COLLAB_GROUPS, resultSet -> {
                        final String projectId = resultSet.getString("project_id");
                        final String groupId   = resultSet.getString("group_id");

                        // If the user has no group membership, then add as a collaborator.
                        if (!CollectionUtils.containsAny(groups, Arrays.asList(groupId, projectId + "_member", projectId + "_owner"))) {
                            userGroups.put(groupId, get(groupId));
                        }
                    });
                }
            }

            for (final UserGroupI group : userGroups.values()) {
                final List<PermissionCriteriaI> permissions = group.getPermissionsByDataType(dataType);
                if (permissions != null) {
                    if (log.isTraceEnabled()) {
                        log.trace("Searched for permission criteria for user {} on type {} in group {}: {}", username, dataType, group.getId(), dumpCriteriaList(permissions));
                    } else {
                        log.debug("Searched for permission criteria for user {} on type {} in group {}: {} permissions found", username, dataType, group.getId(), permissions.size());
                    }
                    criteria.addAll(permissions);
                } else {
                    log.warn("Tried to retrieve permissions for data type {} for user {} in group {}, but this returned null.", dataType, username, group.getId());
                }
            }

            if (!isGuest(username)) {
                try {
                    final List<PermissionCriteriaI> permissions = getPermissionCriteria(getGuest().getUsername(), dataType);
                    if (permissions != null) {
                        criteria.addAll(permissions);
                    } else {
                        log.warn("Tried to retrieve permissions for data type {} for the guest user, but this returned null.", dataType);
                    }
                } catch (Exception e) {
                    log.error("An error occurred trying to retrieve the guest user", e);
                }
            }

            if (log.isTraceEnabled()) {
                log.trace("Retrieved permission criteria for user {} on the data type {}: {}", username, dataType, dumpCriteriaList(criteria));
            } else {
                log.debug("Retrieved permission criteria for user {} on the data type {}: {} criteria found", username, dataType, criteria.size());
            }

            return ImmutableList.copyOf(criteria);
        } catch (UserNotFoundException e) {
            log.error("Couldn't find the indicated user {}", username, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Long> getTotalCounts() {
        if (_totalCounts.isEmpty()) {
            resetTotalCounts();
        }
        return ImmutableMap.copyOf(_totalCounts);
    }

    @NotNull
    @Override
    public List<String> getProjectsForUser(final String username, final String access) {
        log.info("Getting projects with {} access for user {}", access, username);
        switch (access) {
            case ACTION_READ:
                return getUserReadableProjects(username);

            case ACTION_EDIT:
                return getUserEditableProjects(username);

            case ACTION_DELETE:
                return getUserOwnedProjects(username);

            default:
                throw new IllegalArgumentException("Invalid access level '" + access + "', valid values are: '" + ACTION_READ + "', '" + ACTION_EDIT + "', and '" + ACTION_DELETE + "'.");
        }
    }

    @NotNull
    @Override
    public List<UserGroupI> getGroupsForProject(final String projectId) {
        return getCacheList(CACHE_PROJECT_GROUPS, projectId, String.class).stream().map(this::get).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Map<String, UserGroupI> getGroupsForUser(final String username) throws UserNotFoundException {
        return ImmutableMap.copyOf(getMutableGroupsForUser(username));
    }

    @Override
    public void refreshGroupsForUser(final String username) throws UserNotFoundException {
        forceCacheList(CACHE_USER_GROUPS, username, String.class);
        getUserLastUpdateCache().put(username, new Date());
    }

    @Override
    public UserGroupI getGroupForUserAndProject(final String username, final String projectId) throws UserNotFoundException {
        if (!_userService.exists(username)) {
            throw new UserNotFoundException(username);
        }
        final String groupId = _template.query(QUERY_GET_GROUP_FOR_USER_AND_PROJECT, new MapSqlParameterSource(PARAM_USERNAME, username).addValue(PARAM_PROJECT_ID, projectId), results -> results.next() ? results.getString("id") : null);
        return StringUtils.isNotBlank(groupId) ? get(groupId) : null;
    }

    @Override
    public List<String> getUserIdsForGroup(final String groupId) {
        final UserGroupI userGroup = get(groupId);
        if (userGroup == null) {
            return Collections.emptyList();
        }
        return ImmutableList.copyOf(userGroup.getUsernames());
    }

    @Override
    public void refreshGroup(final String groupId) throws ItemNotFoundException {
        evict(CACHE_GROUPS, groupId);
        final UserGroupI group = Optional.ofNullable(get(groupId)).orElseThrow(() -> new ItemNotFoundException("No group with ID " + groupId + " found"));
        for (final String username : group.getUsernames()) {
            try {
                if (!getGroupIdsForUser(username).contains(groupId)) {
                    refreshGroupsForUser(username);
                }
            } catch (UserNotFoundException e) {
                // CACHING: Extended logging
                log.debug("Retrieving users for group {}, couldn't find user {}; this isn't necessarily bad but is curious", groupId, username);
            }
        }
    }

    @Override
    public Date getUserLastUpdateTime(final UserI user) {
        return getUserLastUpdateTime(user.getUsername());
    }

    @Override
    public Date getUserLastUpdateTime(final String username) {
        return getUserLastUpdateCache().get(username);
    }

    @Override
    public void clearUserCache(final UserI user) {
        clearUserCache(user.getUsername());
    }

    @Override
    public void clearUserCache(final String username) {
        USER_CACHES.forEach(cacheName -> evict(cacheName, username));
        getUserLastUpdateCache().put(username, new Date());
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

    private boolean handleProjectEvents(final XftItemEventI event) {
        final String         xsiType    = event.getXsiType();
        final String         id         = event.getId();
        final String         action     = event.getAction();
        final Map<String, ?> properties = event.getProperties();

        try {
            switch (action) {
                case EVENT_CREATE:
                    log.debug("New project created with ID {}, caching new instance", id);
                    for (final String owner : getProjectOwners(id)) {
                        if (getActionElementDisplays(owner, ACTION_CREATE).stream().noneMatch(CONTAINS_MR_SESSION)) {
                            initActionElementDisplays(owner, true);
                        }
                    }

                    final boolean created = !getGroups(xsiType, id).isEmpty();
                    final String access = Permissions.getProjectAccess(_template, id);
                    if (StringUtils.isNotBlank(access)) {
                        switch (access) {
                            case "public":
                                log.debug("Handling project create event for {}, this project is public so requires updating guest access", id);
                                if (getActionElementDisplays(DEFAULT_GUEST_USERNAME, ACTION_CREATE).stream().noneMatch(CONTAINS_MR_SESSION)) {
                                    initActionElementDisplays(DEFAULT_GUEST_USERNAME, true);
                                }

                            case "protected":
                                log.debug("Handling project create event for {}, this project is protected so requires updating project-related caches", id);
                                updateProjectRelatedCaches(xsiType, id, false);
                                break;

                            case "private":
                                log.debug("Handling project create event for {}, this project is private so requires updating project-related caches", id);
                                break;

                            default:
                                log.warn("The project {}'s accessibility setting was updated to an invalid value: {}. Must be one of private, protected, or public.", id, access);
                        }
                    }
                    resetProjectCount(_totalCounts);
                    return created;

                case EVENT_UPDATE:
                    log.debug("The {} object {} was updated, caching updated instance", xsiType, id);
                    if (properties.containsKey("accessibility")) {
                        final String accessibility = (String) properties.get("accessibility");
                        switch (accessibility) {
                            case "private":
                            case "protected":
                                log.debug("Handling project update event for {}, this project is {} so requires updating project-related caches", id, accessibility);
                                return updateProjectRelatedCaches(xsiType, id, true);

                            case "public":
                                log.debug("Handling project update event for {}, this project is public so requires updating guest access", id);
                                // CACHING: Why is this filtering on MR session rather than image sessions or just experiments?
                                if (getActionElementDisplays(DEFAULT_GUEST_USERNAME, ACTION_CREATE).stream().noneMatch(CONTAINS_MR_SESSION)) {
                                    initActionElementDisplays(DEFAULT_GUEST_USERNAME, true);
                                }
                                return true;

                            default:
                                log.warn("The project {}'s accessibility setting was updated to an invalid value: {}. Must be one of private, protected, or public.", id, accessibility);
                                return false;
                        }
                    }
                    break;

                case EVENT_DELETE:
                    log.debug("The {} {} was deleted, removing related instances from cache", xsiType, id);
                    List<String> members = GenericUtils.convertToTypedList(ObjectUtils.getIfNull((List<?>) evict(CACHE_PROJECT_MEMBERS, id), Collections::emptyList), String.class);
                    evict(CACHE_PROJECT_GROUPS, id);
                    resetGuestBrowseableElementDisplays();
                    members.forEach(this::getReadableCounts);
                    resetTotalCounts(); //adding this because project deletes often affect other data types too (subjects, experiments)
                    return true;

                default:
                    log.warn("I was informed that the '{}' action happened to the project with ID '{}'. I don't know what to do with this action.", action, id);
                    break;
            }
        } catch (ItemNotFoundException e) {
            log.warn("While handling action {}, I couldn't find a group for type {} ID {}.", action, xsiType, id, e);
        }

        return false;
    }

    private boolean handleSubjectEvents(final XftItemEventI event) {
        final String action = event.getAction();
        log.debug("Handling subject {} event for {} {}", XftItemEventI.ACTIONS.get(action), event.getXsiType(), event.getId());
        final Set<String> projectIds = new HashSet<>();
        switch (action) {
            case EVENT_CREATE:
                projectIds.add(_template.queryForObject(QUERY_GET_SUBJECT_PROJECT, new MapSqlParameterSource("subjectId", event.getId()), String.class));
                resetTotalCounts(() -> incrementCount(XnatSubjectdata.SCHEMA_ELEMENT_NAME));
                break;

            case EVENT_SHARE:
                projectIds.add((String) event.getProperties().get("target"));
                break;

            case EVENT_MOVE:
                projectIds.add((String) event.getProperties().get("origin"));
                projectIds.add((String) event.getProperties().get("target"));
                break;

            case EVENT_DELETE:
                projectIds.add((String) event.getProperties().get("target"));
                handleGroupRelatedEvents(event);
                resetTotalCounts(() -> decrementCount(XnatSubjectdata.SCHEMA_ELEMENT_NAME));
                break;

            default:
                log.warn("I was informed that the '{}' action happened to subject '{}'. I don't know what to do with this action.", action, event.getId());
        }
        if (projectIds.isEmpty()) {
            return false;
        }

        initReadableCountsForProjectUsers(projectIds, action, event.getXsiType());
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
                case EVENT_CREATE:
                    log.debug("New {} created with ID {}, caching new instance", xsiType, id);
                    for (final UserGroupI group : groups) {
                        usernames.addAll(group.getUsernames());
                    }
                    log.debug("Handling create group event with ID '{}' for users: {}", id, StringUtils.join(usernames, ", "));
                    return !groups.isEmpty();

                case EVENT_UPDATE:
                    log.debug("The {} object {} was updated, caching updated instance", xsiType, id);
                    final boolean hasOperation = properties.containsKey(EVENT_OPERATION);
                    for (final UserGroupI group : groups) {
                        // If there's no operation, we have no way of knowing which users will be affected by the update and have to update all of them.
                        if (!hasOperation) {
                            usernames.addAll(group.getUsernames());
                        }
                        evict(CACHE_GROUPS, group.getId());
                    }
                    if (hasOperation && properties.containsKey(USERS)) {
                        //noinspection unchecked
                        usernames.addAll((Collection<? extends String>) properties.get(USERS));
                    }
                    log.debug("Handling update group event with ID '{}' for users: {}", id, StringUtils.join(usernames, ", "));
                    return !groups.isEmpty();

                case EVENT_DELETE:
                    if (StringUtils.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME, xsiType)) {
                        final List<String> groupIds = getCachedProjectGroups(id);
                        if (CollectionUtils.isNotEmpty(groupIds)) {
                            log.info("Found {} groups cached for deleted project {}", groupIds.size(), id);
                            for (final String groupId : groupIds) {
                                usernames.addAll(evictGroup(groupId));
                            }
                        }
                    } else {
                        usernames.addAll(evictGroup(id));
                    }
                    return true;

                default:
                    log.warn("I was informed that the '{}' action happened to the {} object with ID '{}'. I don't know what to do with this action.", action, xsiType, id);
            }
        } catch (ItemNotFoundException e) {
            log.warn("While handling action {}, I couldn't find a group for type {} ID {}.", action, xsiType, id, e);
        } finally {
            USER_CACHES.forEach(a -> evict(a, usernames));
            for (final String username : usernames) {
                clearUserCache(username);
                ACTIONS.forEach(a -> evictCacheMapPartition(CACHE_ACTIONS, a, username));
                log.info("Initializing user group IDs cache entry for user '{}'", username);
                updateUserLastUpdateCacheIfEmpty(CACHE_USER_GROUPS, username);
                final List<String> groupIds = getCacheList(CACHE_USER_GROUPS, username, String.class);
                log.debug("Found {} user group IDs cache entry for user '{}'", groupIds.size(), username);
                ACTIONS.forEach(a -> getActionElementDisplays(username, a));
                getBrowseableElementDisplays(username);
            }
        }
        return false;
    }

    private boolean handleElementSecurityEvents(final XftItemEventI event) {
        log.debug("Handling {} event for '{}' IDs {}. Updating guest browseable element displays...", event.getAction(), event.getXsiType(), event.getIds());
        final Map<String, ElementDisplay> displays = resetGuestBrowseableElementDisplays();

        if (log.isTraceEnabled()) {
            log.trace("Got back {} browseable element displays for guest user after refresh: {}", displays.size(), StringUtils.join(displays.keySet(), ", "));
        }

        clearAllUserProjectAccessCaches();

        for (final String dataType : event.getIds()) {
            final List<String> groupIds = getGroupIdsForDataType(dataType);
            log.debug("Found {} groups that reference the '{}' data type, updating cache entries for: {}", groupIds.size(), dataType, String.join(", ", groupIds));
            groupIds.forEach(groupId -> {
                log.trace("Evicting group '{}' due to change in element securities for data type {}", groupId, dataType);
                evict(CACHE_GROUPS, groupId);
            });
        }

        return true;
    }

    private boolean handleExperimentEvents(final XftItemEventI event) {
        final String action  = event.getAction();
        final String xsiType = event.getXsiType();
        log.debug("Handling experiment {} event for {} {}", XftItemEventI.ACTIONS.get(action), xsiType, event.getId());
        final String      target, origin;
        final Set<String> projectIds = new HashSet<>();
        switch (action) {
            case EVENT_CREATE:
                target = _template.queryForObject(QUERY_GET_EXPERIMENT_PROJECT, new MapSqlParameterSource("experimentId", event.getId()), String.class);
                origin = null;
                projectIds.add(target);
                resetTotalCounts(() -> incrementCount(xsiType));
                break;

            case EVENT_SHARE:
                target = (String) event.getProperties().get("target");
                origin = null;
                projectIds.add(target);
                break;

            case EVENT_DELETE:
                target = (String) event.getProperties().get("target");
                origin = null;
                projectIds.add(target);
                resetTotalCounts(() -> decrementCount(xsiType));
                break;

            case EVENT_MOVE:
                origin = (String) event.getProperties().get("origin");
                target = (String) event.getProperties().get("target");
                projectIds.add(target);
                projectIds.add(origin);
                break;

            default:
                log.warn("I was informed that the '{}' action happened to experiment '{}' with ID '{}'. I don't know what to do with this action.", action, xsiType, event.getId());
                return false;
        }

        final Map<String, ElementDisplay> displays = getBrowseableElementDisplays(getGuest());
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
            resetGuestBrowseableElementDisplays();
        } else {
            log.debug("Not updating guest browseable element displays: guest {} '{}' and {}",
                    hasEventXsiType ? "already has the event XSI type " : "doesn't have the event XSI type",
                    xsiType,
                    isTargetProjectPublic ? "target project is public" : "target project is not public");
        }

        initReadableCountsForProjectUsers(projectIds, action, event.getXsiType());
        return true;
    }

    private boolean updateProjectRelatedCaches(final String xsiType, final String id, final boolean affectsOtherDataTypes) throws ItemNotFoundException {
        final boolean cachedRelatedGroups = !getGroups(xsiType, id).isEmpty();

        evict(CACHE_BROWSEABLES, DEFAULT_GUEST_USERNAME);
        evict(CACHE_READABLE_COUNTS, DEFAULT_GUEST_USERNAME);
        resetGuestBrowseableElementDisplays();
        initActionElementDisplays(DEFAULT_GUEST_USERNAME, true);

        final Cache<String, Map<String, Long>> readableCountsCache = getReadableCountsCache();
        if (affectsOtherDataTypes) {
            readableCountsCache.clear();
        } else {
            // Update existing user element displays
            clearAllUserProjectAccessCaches();
            // CACHING: This would previously refresh cache for users that were already in the cache. Consider just leaving it and re-caching on reference.
            // initReadableCountsForUsers(cacheIds.stream().map(DefaultGroupsAndPermissionsCache::getUsernameFromCacheId).filter(StringUtils::isNotBlank).collect(Collectors.toSet()));
        }

        return cachedRelatedGroups;
    }

    private Map<String, List<ElementDisplay>> initActionElementDisplays(final String username) {
        return initActionElementDisplays(username, false);
    }

    private synchronized Map<String, List<ElementDisplay>> initActionElementDisplays(final String username, final boolean evict) {
        log.info("Initializing action element displays for user '{}', evict is {}", username, evict);

        // If they want to evict the cache entry, then do that and proceed. They explicitly don't want any cached entry to be returned.
        if (evict) {
            evict(CACHE_ACTIONS, username);
        }

        return ACTIONS.stream().collect(Collectors.toMap(Function.identity(), action -> ObjectUtils.getIfNull(getActionElementDisplays(username, action), Collections::emptyList)));
    }

    private void initReadableCountsForProjectUsers(final Set<String> projectIds, final String action, final String dataType) {
        final Set<String> users = getProjectUsers(projectIds);
        for (final String username : users) {
            final Map<String, Long> cachedCounts = getReadableCountsCache().get(username);
            if (cachedCounts == null || cachedCounts.isEmpty()) {
                initReadableCountsForUser(username);
            } else if (StringUtils.equals(EVENT_CREATE, action) && (!cachedCounts.containsKey(dataType) || cachedCounts.get(dataType) == 0)) {
                //we only need to worry about this if it is null or 0
                initReadableCountsForUser(username);
            } else if (StringUtils.equals(EVENT_DELETE, action)) {
                //do we need this?  Only if its going from 1 to 0, but we don't know that if counts aren't refreshed as they get closer to 1
                //leaving it, but if it becomes an issue in profiling, then we could consider removing it.
                initReadableCountsForUser(username);
            }
        }
    }

    private void initReadableCountsForUser(final String username) {
        evict(CACHE_BROWSEABLES, username);
        evict(CACHE_READABLE_COUNTS, username);
        final Map<String, Long> readableCounts = getReadableCounts(username);
        getUserLastUpdateCache().put(username, new Date());
        log.debug("Initialized readable counts for user {}, finding {} different counts", username, readableCounts.size());
    }

    private List<String> getProjectOwners(final String projectId) {
        return _template.queryForList(QUERY_PROJECT_OWNERS, new MapSqlParameterSource("projectId", projectId), String.class);
    }

    private Set<String> getProjectUsers(final String... projectIds) {
        return getProjectUsers(Arrays.asList(projectIds));
    }

    private Set<String> getProjectUsers(final Collection<String> projectIds) {
        return projectIds.isEmpty() ? Collections.emptySet() : new HashSet<>(_template.queryForList(QUERY_GET_USERS_FOR_PROJECTS, new MapSqlParameterSource("projectIds", projectIds), String.class));
    }

    private void clearAllUserProjectAccessCaches() {
        log.debug("Evicting all user caches");
        getAccessManagersCache().clear();
        getActionsCache().clear();
        getBrowseablesCache().clear();
        getReadableCountsCache().clear();
        getUserGroupsCache().clear();
        getUserLastUpdateCache().clear();
    }

    private boolean isImageSession(String xsiType) {
        try {
            return SchemaElement.GetElement(xsiType).instanceOf(XnatImagesessiondata.SCHEMA_ELEMENT_NAME);
        } catch (XFTInitException | ElementNotFoundException e) {
            log.error("Failed to parse passed XSI type {} in event handler", xsiType, e);
            return false;
        }
    }

    private void incrementCount(final String xsiType) {
        _totalCounts.merge(isImageSession(xsiType) ? XnatImagesessiondata.SCHEMA_ELEMENT_NAME : xsiType, 1L, Long::sum);
    }

    private void decrementCount(final String xsiType) {
        // decrement, but don't go below zero
        _totalCounts.merge(isImageSession(xsiType) ? XnatImagesessiondata.SCHEMA_ELEMENT_NAME : xsiType, 1L, (a, b) -> max(a - b, 0L));
    }

    private List<UserGroupI> getGroups(final String type, final String id) throws ItemNotFoundException {
        switch (type) {
            case XnatProjectdata.SCHEMA_ELEMENT_NAME:
                return getGroupsForProject(id);

            case XdatUsergroup.SCHEMA_ELEMENT_NAME:
                final UserGroupI group = ObjectUtils.getIfNull(getCachedGroup(id), () -> {
                    try {
                        return new UserGroup(id, _template);
                    } catch (ItemNotFoundException e) {
                        log.warn("User group with ID {} was not found in the cache nor does it seem to exist on the system", id);
                        return null;
                    }
                });
                return group != null ? Collections.singletonList(group) : Collections.emptyList();

            case XdatElementSecurity.SCHEMA_ELEMENT_NAME:
                return getGroupIdsForDataType(id).stream()
                        .map(this::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private List<String> getGroupIdsForDataType(final String dataType) {
        return _template.queryForList(QUERY_GET_GROUPS_FOR_DATATYPE, new MapSqlParameterSource("dataType", dataType), String.class);
    }

    @SuppressWarnings("UnusedReturnValue")
    private Map<String, ElementDisplay> resetGuestBrowseableElementDisplays() {
        return resetBrowseableElementDisplays(getGuest());
    }

    private Map<String, ElementDisplay> resetBrowseableElementDisplays(final String username) {
        try {
            return resetBrowseableElementDisplays(Users.getUser(username));
        } catch (UserInitException | UserNotFoundException e) {
            throw new NrgServiceRuntimeException("An error occurred trying to retrieve the user " + username, e);
        }
    }

    private Map<String, ElementDisplay> resetBrowseableElementDisplays(final UserI user) {
        final String username = user.getUsername();
        log.debug("Updating browseable element displays for user {}", username);
        final Map<String, ElementDisplay> browseables = getBrowseablesExtractor().extract(username);
        getBrowseablesCache().put(username, browseables);
        getUserLastUpdateCache().put(username, new Date());
        return browseables;
    }

    private Map<String, ElementAccessManager> getElementAccessManagers(final String username) {
        updateUserLastUpdateCacheIfEmpty(CACHE_ACCESS_MANAGERS, username);
        return getCacheMap(CACHE_ACCESS_MANAGERS, username, String.class, ElementAccessManager.class);
    }

    @Nonnull
    private Map<String, UserGroupI> getMutableGroupsForUser(final String username) throws UserNotFoundException {
        // XNAT-8309 Calling updateUserLastUpdateCacheIfEmpty() then returning the result of this next call inline
        // causes the setup page to fail to load. This is a workaround to get the cached/retrieved value first then
        // updating the last-update cache before returning it.
        final Map<String, UserGroupI> groups = getCacheList(CACHE_USER_GROUPS, username, String.class).stream()
                                                                                                      .map(this::get)
                                                                                                      .filter(Objects::nonNull)
                                                                                                      .collect(Collectors.toMap(UserGroupI::getId, Function.identity()));
        updateUserLastUpdateCacheIfEmpty(CACHE_USER_GROUPS, username);
        return groups;
    }

    private List<String> getGroupIdsForUser(final String username) throws UserNotFoundException {
        updateUserLastUpdateCacheIfEmpty(CACHE_USER_GROUPS, username);
        final List<String> groupIds = getCacheList(CACHE_USER_GROUPS, username, String.class);
        if (log.isTraceEnabled()) {
            log.trace("Found {} groups for user '{}': {}", groupIds.size(), username, StringUtils.join(groupIds, ", "));
        } else {
            log.info("Found {} groups for user '{}'", groupIds.size(), username);
        }
        return groupIds;
    }

    private void updateUserLastUpdateCacheIfEmpty(String cacheId, String username) {
        if (isCacheEmpty(cacheId, username)) {
            getUserLastUpdateCache().put(username, new Date());
        }
    }

    private Set<String> evictGroup(final String groupId) {
        final UserGroupI group = get(groupId);
        if (group == null) {
            log.info("Requested to evict group with ID '{}', but I couldn't find that actual group", groupId);
            return Collections.emptySet();
        }
        final Set<String> usernames = new HashSet<>(group.getUsernames());
        log.debug("Found group for ID '{}' with {} associated users", groupId, usernames.size());
        evict(CACHE_GROUPS, groupId);
        return usernames;
    }

    /**
     * Retrieves a list of projects where the specified user has read access, including protected and public projects and projects
     * to which the user has read access due to all data access privileges.
     *
     * @param username The username to retrieve projects for.
     *
     * @return A list of projects to which the specified user has read access.
     */
    private List<String> getUserReadableProjects(final String username) {
        return getProjectsByAccessQuery(username, QUERY_READABLE_PROJECTS, false);
    }

    /**
     * Retrieves a list of projects where the specified user has edit access.
     *
     * @param username The username to retrieve projects for.
     *
     * @return A list of projects to which the specified user has edit access.
     */
    private List<String> getUserEditableProjects(final String username) {
        return getProjectsByAccessQuery(username, QUERY_EDITABLE_PROJECTS, true);
    }

    /**
     * Retrieves a list of projects where the specified user is an owner (i.e. has delete access).
     *
     * @param username The username to retrieve projects for.
     *
     * @return A list of projects to which the specified user has delete access.
     */
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
        return _template.queryForObject(QUERY_HAS_ALL_DATA_ACCESS, new MapSqlParameterSource(PARAM_USERNAME, username), Boolean.class);
    }

    /**
     * Cheap test to see if the user is in a group that has data admin access on all projects.
     *
     * @param username The username to be checked.
     *
     * @return Returns true if the user has all-data-admin, false otherwise.
     */
    private boolean hasAllDataAdmin(final String username) {
        return _template.queryForObject(QUERY_HAS_ALL_DATA_ADMIN, new MapSqlParameterSource(PARAM_USERNAME, username), Boolean.class);
    }

    private void resetTotalCounts(Runnable runnable) {
        if (isSmallServer()) {
            resetTotalCounts();
        } else {
            runnable.run();
        }
    }
    public synchronized void resetTotalCounts() {
        //modified this to be a bit more safe to being re-run.  i.e. don't clear the _totalCounts until the new values have been calculated.
        final Map<String, Long> tempCounts = new ConcurrentHashMap<>();
        resetProjectCount(tempCounts);
        resetSubjectCount(tempCounts);
        resetImageSessionCount(tempCounts);
        final List<Map<String, Object>> elementCounts = _template.queryForList(QUERY_EXPT_COUNTS_BY_TYPE, EmptySqlParameterSource.INSTANCE);
        for (final Map<String, Object> elementCount : elementCounts) {
            final String elementName = (String) elementCount.get(COLUMN_ELEMENT_NAME);
            final Long   count       = (Long) elementCount.get(COLUMN_ELEMENT_COUNT);
            if (StringUtils.isBlank(elementName)) {
                final String orphaned = _template.queryForList(QUERY_ORPHANED_EXPERIMENTS, EmptySqlParameterSource.INSTANCE).stream().map(experiment -> {
                    final String experimentId = (String) experiment.get("experiment_id");
                    final String dataType     = (String) experiment.get("data_type");
                    final int    extensionId  = (Integer) experiment.get("xdat_meta_element_id");
                    return " * " + experimentId + " was a " + dataType + ", " + (extensionId >= 0 ? "should be extension ID " + extensionId : "data type doesn't appear in xdat_meta_element table");
                }).collect(Collectors.joining("\n"));
                log.warn("Found {} elements that are not associated with a valid data type:\n\n{}\n\nYou can correct some of these orphaned experiments by running the query:\n\n{}\n\nAny experiment IDs and data types returned from that query indicate data types that can not be resolved on the system (i.e. they don't exist in the primary data-type table).",
                        count, orphaned, QUERY_CORRECT_ORPHANED_EXPERIMENTS);
            } else {
                tempCounts.put(elementName, count);
            }
        }
        _totalCounts = tempCounts;
    }


    public XFTTable getProjectsForDatatypeAction(final UserI user, final String dataType, final String action) throws Exception {
        final ParamValue userId = new ParamValue(Users.getUserId(user.getUsername()), Types.INTEGER);
        final ParamValue guestId = new ParamValue(Users.getUserId("guest"), Types.INTEGER);
        if (StringUtils.isBlank(action)) {
            throw new Exception("You must specify a value for the permissions parameter.");
        } else if (!DefaultGroupsAndPermissionsCache.PERMISSIONS.contains(action)) {
            //this is very important since we are injecting it straight into the sql
            throw new Exception("You must specify one of the following values for the permissions parameter: " + Joiner.on(", ").join(DefaultGroupsAndPermissionsCache.PERMISSIONS));
        }
        try {
            final ParamValue dataTypeParam = new ParamValue(SchemaElement.GetElement(dataType).getFullXMLName(),Types.VARCHAR);
            XFTTable t;
            if(Groups.isDataAdmin(user)){
                t= XFTTable.Execute("SELECT ID,secondary_ID FROM xnat_projectData ",null,null);
            }else{
                t= XFTTable.ExecutePS(String.format(PERMS_SQL, action),dataTypeParam,userId,guestId);
            }
            t.sort("secondary_id", "ASC");
            return t;
        } catch (XFTInitException | SQLException | DBPoolException e) {
            log.error("Error checking permissions for datatype",e);
            return null;
        }
    }
    private static final String PERMS_SQL = "WITH PERMS AS ("
            + " SELECT "
            + " xfm.field_value "
            + " FROM xdat_element_access xea "
            + " LEFT JOIN xdat_usergroup grp ON xea.xdat_usergroup_xdat_usergroup_id=grp.xdat_usergroup_id "
            + " LEFT JOIN xdat_user_groupid gid ON grp.id=gid.groupid "
            + " LEFT JOIN xdat_field_mapping_set fms ON xea.xdat_element_access_id=fms.permissions_allow_set_xdat_elem_xdat_element_access_id "
            + " LEFT JOIN xdat_field_mapping xfm ON fms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND xfm.%s_element=1 AND ? || '/project'=xfm.field "
            + " WHERE  (field_value IS NOT NULL AND field_value NOT IN ('','*')) AND (gid.groups_groupid_xdat_user_xdat_user_id IN (?) OR xea.xdat_user_xdat_user_id IN (?)) "
            + " GROUP BY xfm.field_value) "
            + " SELECT ID,secondary_ID FROM xnat_projectData "
            + " WHERE ID IN (SELECT field_value FROM PERMS)";

    private void resetProjectCount(final Map<String, Long> totalCounts) {
        totalCounts.put(XnatProjectdata.SCHEMA_ELEMENT_NAME, _template.queryForObject(QUERY_PROJECT_COUNTS, EmptySqlParameterSource.INSTANCE, Long.class));
    }

    private void resetSubjectCount(final Map<String, Long> totalCounts) {
        totalCounts.put(XnatSubjectdata.SCHEMA_ELEMENT_NAME, _template.queryForObject(QUERY_SUBJECT_COUNTS, EmptySqlParameterSource.INSTANCE, Long.class));
    }

    private void resetImageSessionCount(final Map<String, Long> totalCounts) {
        totalCounts.put(XnatImagesessiondata.SCHEMA_ELEMENT_NAME, _template.queryForObject(QUERY_SESSION_COUNTS, EmptySqlParameterSource.INSTANCE, Long.class));
    }

    private XDATUser getGuest() {
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
        return getGuest() != null ? StringUtils.equalsIgnoreCase(getGuest().getUsername(), username) : StringUtils.equalsIgnoreCase(DEFAULT_GUEST_USERNAME, username);
    }

    private DataExtractor<String, Map<String, ElementAccessManager>> getAccessManagersExtractor() {
        //noinspection unchecked
        return (DataExtractor<String, Map<String, ElementAccessManager>>) getExtractors().get(CACHE_ACCESS_MANAGERS);
    }

    private DataExtractor<String, Map<String, List<ElementDisplay>>> getActionsExtractor() {
        //noinspection unchecked
        return (DataExtractor<String, Map<String, List<ElementDisplay>>>) getExtractors().get(CACHE_ACTIONS);

    }

    private DataExtractor<String, Map<String, ElementDisplay>> getBrowseablesExtractor() {
        //noinspection unchecked
        return (DataExtractor<String, Map<String, ElementDisplay>>) getExtractors().get(CACHE_BROWSEABLES);
    }

    private DataExtractor<String, UserGroupI> getGroupsExtractor() {
        //noinspection unchecked
        return (DataExtractor<String, UserGroupI>) getExtractors().get(CACHE_GROUPS);
    }

    private DataExtractor<String, List<String>> getProjectGroupsExtractor() {
        //noinspection unchecked
        return (DataExtractor<String, List<String>>) getExtractors().get(CACHE_PROJECT_GROUPS);
    }

    private DataExtractor<String, List<String>> getProjectMembersExtractor() {
        //noinspection unchecked
        return (DataExtractor<String, List<String>>) getExtractors().get(CACHE_PROJECT_MEMBERS);
    }

    private DataExtractor<String, Map<String, Long>> getReadableCountsExtractor() {
        //noinspection unchecked
        return (DataExtractor<String, Map<String, Long>>) getExtractors().get(CACHE_READABLE_COUNTS);
    }

    private DataExtractor<String, List<String>> getUserGroupsExtractor() {
        //noinspection unchecked
        return (DataExtractor<String, List<String>>) getExtractors().get(CACHE_USER_GROUPS);
    }

    private DataExtractor<String, Date> getUserLastUpdateExtractor() {
        //noinspection unchecked
        return (DataExtractor<String, Date>) getExtractors().get(CACHE_USER_LAST_UPDATED);
    }

    private Map<String, ElementAccessManager> getCachedAccessManagers(final String username) {
        return getAccessManagersCache().get(username);
    }

    private Map<String, List<ElementDisplay>> getCachedActions(final String username) {
        return getActionsExtractor().getPartitionKeys().stream().collect(Collectors.toMap(Function.identity(), key -> getActionsCache().get(createActionElementsDisplaySubkey(username, key))));
    }

    private Map<String, ElementDisplay> getCachedBrowseables(final String username) {
        return getBrowseablesCache().get(username);
    }

    private UserGroupI getCachedGroup(final String groupId) {
        return ObjectUtils.getIfNull(getGroupsCache().get(groupId), new CacheItemSupplier<>(CACHE_GROUPS, groupId, UserGroupI.class));
    }

    private List<String> getCachedProjectGroups(final String projectId) {
        return getProjectGroupsCache().get(projectId);
    }

    private List<String> getCachedProjectMembers(final String projectId) {
        return getProjectMembersCache().get(projectId);
    }

    private Map<String, Long> getCachedReadableCounts(final String username) {
        return getReadableCountsCache().get(username);
    }

    private List<String> getCachedUserGroups(final String username) {
        return getUserGroupsCache().get(username);
    }

    private Date getCachedUserLastUpdate(final String username) {
        return getUserLastUpdateCache().get(username);
    }

    private Cache<String, Map<String, ElementAccessManager>> getAccessManagersCache() {
        return getCache(CACHE_ACCESS_MANAGERS, String.class, getAccessManagersExtractor().getValueType());
    }

    private Cache<String, List<ElementDisplay>> getActionsCache() {
        return getCache(CACHE_ACTIONS, String.class, getActionsExtractor().getPartitionValueType());
    }

    private Cache<String, Map<String, ElementDisplay>> getBrowseablesCache() {
        return getCache(CACHE_BROWSEABLES, String.class, getBrowseablesExtractor().getValueType());
    }

    private Cache<String, UserGroupI> getGroupsCache() {
        return getCache(CACHE_GROUPS, String.class, getGroupsExtractor().getValueType());
    }

    private Cache<String, List<String>> getProjectGroupsCache() {
        return getCache(CACHE_PROJECT_GROUPS, String.class, getProjectGroupsExtractor().getValueType());
    }

    private Cache<String, List<String>> getProjectMembersCache() {
        return getCache(CACHE_PROJECT_MEMBERS, String.class, getProjectMembersExtractor().getValueType());
    }

    private Cache<String, Map<String, Long>> getReadableCountsCache() {
        return getCache(CACHE_READABLE_COUNTS, String.class, getReadableCountsExtractor().getValueType());
    }

    private Cache<String, List<String>> getUserGroupsCache() {
        return getCache(CACHE_USER_GROUPS, String.class, getUserGroupsExtractor().getValueType());
    }

    private Cache<String, Date> getUserLastUpdateCache() {
        return getCache(CACHE_USER_LAST_UPDATED, String.class, getUserLastUpdateExtractor().getValueType());
    }

    private static String createActionElementsDisplaySubkey(final String username, final String action) {
        return createCompoundCacheKeyFromElements(username, action);
    }
}
