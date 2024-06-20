package org.nrg.xnat.services.cache.extractors;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.ElementAccessManager;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.nrg.xnat.services.cache.DefaultGroupsAndPermissionsCache.CACHE_ACCESS_MANAGERS;

@Component
@Slf4j
public class AccessManagersExtractor extends AbstractGroupsAndPermissionsCacheDataExtractor<String, Map<String, ElementAccessManager>> {
    private static final String QUERY_USER_PERMISSIONS  = "SELECT " +
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
                                                          "  u.login = :" + PARAM_USERNAME;

    @Autowired
    public AccessManagersExtractor(final @Lazy GroupsAndPermissionsCache cache, final NamedParameterJdbcTemplate template) {
        super(cache, CACHE_ACCESS_MANAGERS, template);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ElementAccessManager> extract(final String username, final Object... parameters) {
        if (isInvalidExtractRequest(username, parameters)) {
            return Collections.emptyMap();
        }

        log.debug("Extracting element access managers for user {}", username);

        final Map<String, ElementAccessManager> managers = ElementAccessManager.initialize(getTemplate(),
                                                                                           QUERY_USER_PERMISSIONS,
                                                                                           new MapSqlParameterSource(PARAM_USERNAME, username));
        log.info("Extracted {} element access managers for user '{}'", managers.size(), username);
        return managers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getKeys() {
        return Users.getUsernames(getTemplate());
    }
}
