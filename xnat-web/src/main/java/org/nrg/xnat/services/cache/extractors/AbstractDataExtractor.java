package org.nrg.xnat.services.cache.extractors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xdat.services.cache.XnatCache;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Getter
@Accessors(prefix = "_")
@Slf4j
public abstract class AbstractDataExtractor<C extends XnatCache, K, V> implements DataExtractor<K, V> {
    private static final Function<Type, Class<?>> TYPE_TO_CLASS = type -> (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);

    private static final String QUERY_ALL_PROJECTS             = "SELECT id FROM xnat_projectdata";
    private static final String QUERY_ALL_USERNAMES            = "SELECT login FROM xdat_user WHERE enabled = 1";
    private static final String QUERY_ACCESSIBLE_DATA_PROJECTS = "SELECT  " +
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
                                                                 "     u.login IN ('guest', :" + PARAM_USERNAME + ")) projects";
    private static final String QUERY_OWNED_PROJECTS           = String.format(QUERY_ACCESSIBLE_DATA_PROJECTS, "delete_element");
    private static final String QUERY_EDITABLE_PROJECTS        = String.format(QUERY_ACCESSIBLE_DATA_PROJECTS, "edit_element");
    private static final String QUERY_READABLE_PROJECTS        = String.format(QUERY_ACCESSIBLE_DATA_PROJECTS, "read_element");
    private static final String QUERY_HAS_ALL_DATA_PRIVILEGES  = "SELECT  " +
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
                                                                 "           u.login = :" + PARAM_USERNAME + ")";
    private static final String QUERY_HAS_ALL_DATA_ACCESS      = String.format(QUERY_HAS_ALL_DATA_PRIVILEGES, "read_element");
    private static final String QUERY_HAS_ALL_DATA_ADMIN       = String.format(QUERY_HAS_ALL_DATA_PRIVILEGES, "edit_element");
    private static final String QUERY_ALL_DATA_ACCESS_PROJECTS = "SELECT id AS project FROM xnat_projectdata ORDER BY project";

    @Getter(AccessLevel.PROTECTED)
    private final C                          _cache;
    @Getter(AccessLevel.PROTECTED)
    private final NamedParameterJdbcTemplate _template;
    @Getter(AccessLevel.PROTECTED)
    private final Map<String, Boolean>       _userChecks;

    private final String   _cacheGroup;
    private final String   _cacheName;
    private final Class<K> _keyType;
    private final Class<V> _valueType;
    private final Class<?> _partitionValueType;
    private final boolean  _partitionedMap;

    @SuppressWarnings("unused")
    protected <T> AbstractDataExtractor(final C cache, final String cacheName, final NamedParameterJdbcTemplate template) {
        this(cache, cacheName, template, null);
    }

    protected <T> AbstractDataExtractor(final C cache, final String cacheName, final NamedParameterJdbcTemplate template, final Class<T> partitionValueType) {
        _cacheGroup = StringUtils.uncapitalize(cache.getClass().getSimpleName());
        _cacheName  = cacheName;
        _cache      = cache;
        _template   = template;
        _userChecks = new HashMap<>();

        final Pair<Class<K>, Class<V>> pair = getKeyAndValueTypes();
        _keyType            = pair.getKey();
        _valueType          = pair.getValue();
        _partitionValueType = partitionValueType;
        _partitionedMap     = _partitionValueType != null;
        if (_partitionValueType != null && (!String.class.isAssignableFrom(_keyType) || !Map.class.isAssignableFrom(_valueType))) {
            throw new IllegalArgumentException("In order for a partitioned map to work properly, the cache key must be a string and the cache value must be a map.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCacheGroup() {
        return _cacheGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCacheName() {
        return _cacheName;
    }

    @SuppressWarnings("unchecked")
    protected Pair<Class<K>, Class<V>> getKeyAndValueTypes() {
        if (log.isTraceEnabled()) {
            log.trace("Trying to get the key and value types for extractor {} here", getClass());
        }
        final Type[]   types      = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
        final Class<K> keyClass   = (Class<K>) TYPE_TO_CLASS.apply(types[0]);
        final Class<V> valueClass = (Class<V>) TYPE_TO_CLASS.apply(types[1]);
        log.debug("Extractor {} has key type {} and value type {}", getClass(), keyClass, valueClass);
        return Pair.of(keyClass, valueClass);
    }

    /**
     * Gets a list of project IDs on the system.
     *
     * @return A list of project IDs.
     */
    protected List<String> getAllProjectIds() {
        return getTemplate().queryForList(QUERY_ALL_PROJECTS, EmptySqlParameterSource.INSTANCE, String.class);
    }

    // CACHING: The "{ @ link" below needs to have spaces removed, but still need the getUserProjects() method.

    /**
     * Retrieves a list of projects where the specified user has read access. This differs from { @ link #getUserProjects(String)} in that it
     * includes protected and public projects and projects to which the user has read access due to all data access privileges.
     *
     * @param username The username to retrieve projects for.
     *
     * @return A list of projects to which the specified user has read access.
     */
    protected List<String> getUserReadableProjects(final String username) {
        return getProjectsByAccessQuery(username, QUERY_READABLE_PROJECTS, false);
    }

    /**
     * Retrieves a list of projects where the specified user has edit access.
     *
     * @param username The username to retrieve projects for.
     *
     * @return A list of projects to which the specified user has edit access.
     */
    @SuppressWarnings("unused")
    protected List<String> getUserEditableProjects(final String username) {
        return getProjectsByAccessQuery(username, QUERY_EDITABLE_PROJECTS, true);
    }

    /**
     * Retrieves a list of projects where the specified user is an owner (i.e. has delete access).
     *
     * @param username The username to retrieve projects for.
     *
     * @return A list of projects to which the specified user has delete access.
     */
    @SuppressWarnings("unused")
    protected List<String> getUserOwnedProjects(final String username) {
        return getProjectsByAccessQuery(username, QUERY_OWNED_PROJECTS, true);
    }

    /**
     * <p>Checks if the cache key and parameters passed to {@link #extract(Object, Object...)} are valid. Invalid indicates
     * that the key is "empty" (determined by calling <a href="https://commons.apache.org/proper/commons-lang/javadocs/api-release/org/apache/commons/lang3/ObjectUtils.html#isEmpty-java.lang.Object-">ObjectUtils.isEmpty()</code>
     * and testing whether the extra parameters array is empty.</p>
     *
     * <p>This method can be overridden if a particular extractor has more complex validation requirements.</p>
     *
     * @param key        The cache key
     * @param parameters Any extra or optional parameters
     *
     * @return Returns <code>true</code> if <code>key</code> is not empty or one or more parameters are specified.
     */
    protected boolean isInvalidExtractRequest(final K key, final Object... parameters) {
        return ObjectUtils.isEmpty(key) && parameters.length == 0;
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
}
