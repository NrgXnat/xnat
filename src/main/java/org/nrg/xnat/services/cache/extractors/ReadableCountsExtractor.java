package org.nrg.xnat.services.cache.extractors;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.nrg.xnat.services.cache.DefaultGroupsAndPermissionsCache.CACHE_READABLE_COUNTS;

@Component
@Slf4j
public class ReadableCountsExtractor extends AbstractGroupsAndPermissionsCacheDataExtractor<String, Map<String, Long>> {
    private static final String QUERY_USER_READABLE_WORKFLOW_COUNT   = "SELECT greatest(reltuples::bigint, 0) AS COUNT " +
                                                                       "FROM pg_class " +
                                                                       "WHERE oid = 'public.wrk_workflowdata'::regclass";

    private static final String QUERY_USER_READABLE_SUBJECT_COUNT    = "SELECT SUM(subjs.COUNT) AS ELEMENT_COUNT " +
                                                                       "FROM xdat_element_access xea " +
                                                                       "         LEFT JOIN xdat_usergroup grp ON xea.xdat_usergroup_xdat_usergroup_id = grp.xdat_usergroup_id " +
                                                                       "         LEFT JOIN xdat_user_groupid gid ON grp.id = gid.groupid " +
                                                                       "         LEFT JOIN xdat_field_mapping_set fms ON xea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                       "         LEFT JOIN xdat_field_mapping xfm ON fms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND xfm.read_element = 1 AND 'xnat:subjectData/project' = xfm.field " +
                                                                       "         JOIN (SELECT project, COUNT(id) FROM xnat_subjectData GROUP BY project UNION SELECT project, COUNT(subject_id) FROM xnat_projectParticipant GROUP BY project) subjs ON xfm.field_value IN (subjs.project, '*') " +
                                                                       "WHERE gid.groups_groupid_xdat_user_xdat_user_id IN (:userIds) " +
                                                                       "   OR xea.xdat_user_xdat_user_id IN (:userIds)";
    private static final String QUERY_USER_READABLE_EXPERIMENT_COUNT = "SELECT xea.element_name, SUM(expts.SUM) AS ELEMENT_COUNT " +
                                                                       "FROM xdat_element_access xea " +
                                                                       "         LEFT JOIN xdat_usergroup grp ON xea.xdat_usergroup_xdat_usergroup_id = grp.xdat_usergroup_id " +
                                                                       "         LEFT JOIN xdat_user_groupid gid ON grp.id = gid.groupid " +
                                                                       "         LEFT JOIN xdat_field_mapping_set fms ON xea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                       "         LEFT JOIN xdat_field_mapping xfm ON fms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND xfm.read_element = 1 AND xea.element_name || '/project' = xfm.field " +
                                                                       "         JOIN (SELECT project, element_name, SUM(COUNT) " +
                                                                       "               FROM (SELECT project, element_name, COUNT(id) " +
                                                                       "                     FROM xnat_experimentData " +
                                                                       "                              LEFT JOIN xdat_meta_element xme ON xnat_experimentData.extension = xme.xdat_meta_element_id " +
                                                                       "                     GROUP BY project, element_name " +
                                                                       "                     UNION " +
                                                                       "                     SELECT shr.project, element_name, COUNT(expt.id) " +
                                                                       "                     FROM xnat_experimentData_share shr " +
                                                                       "                              LEFT JOIN xnat_experimentData expt ON shr.sharing_share_xnat_experimentda_id = expt.id " +
                                                                       "                              LEFT JOIN xdat_meta_element xme ON expt.extension = xme.xdat_meta_element_id " +
                                                                       "                     GROUP BY shr.project, element_name) SRCH " +
                                                                       "               GROUP BY project, element_name) expts ON xfm.field_value IN (expts.project, '*') AND xea.element_name = expts.element_name " +
                                                                       "WHERE gid.groups_groupid_xdat_user_xdat_user_id IN (:userIds) " +
                                                                       "   OR xea.xdat_user_xdat_user_id IN (:userIds) " +
                                                                       "GROUP BY xea.element_name";
    private static final String QUERY_USER_READABLE_SCAN_COUNT       = "SELECT xea.element_name, SUM(expts.SUM) AS ELEMENT_COUNT " +
                                                                       "FROM xdat_element_access xea " +
                                                                       "         LEFT JOIN xdat_usergroup grp ON xea.xdat_usergroup_xdat_usergroup_id = grp.xdat_usergroup_id " +
                                                                       "         LEFT JOIN xdat_user_groupid gid ON grp.id = gid.groupid " +
                                                                       "         LEFT JOIN xdat_field_mapping_set fms ON xea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                       "         LEFT JOIN xdat_field_mapping xfm ON fms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND xfm.read_element = 1 AND xea.element_name || '/project' = xfm.field " +
                                                                       "         JOIN (SELECT project, element_name, SUM(COUNT) " +
                                                                       "               FROM (SELECT project, element_name, COUNT(id) " +
                                                                       "                     FROM xnat_imageScandata " +
                                                                       "                              LEFT JOIN xdat_meta_element xme ON xnat_imageScandata.extension = xme.xdat_meta_element_id " +
                                                                       "                     GROUP BY project, element_name " +
                                                                       "                     UNION " +
                                                                       "                     SELECT shr.project, element_name, COUNT(expt.id) " +
                                                                       "                     FROM xnat_imageScandata_share shr " +
                                                                       "                              LEFT JOIN xnat_imageScandata expt ON shr.sharing_share_xnat_imagescandat_xnat_imagescandata_id = expt.xnat_imagescandata_id " +
                                                                       "                              LEFT JOIN xdat_meta_element xme ON expt.extension = xme.xdat_meta_element_id " +
                                                                       "                     GROUP BY shr.project, element_name) SRCH " +
                                                                       "               GROUP BY project, element_name) expts ON xfm.field_value IN (expts.project, '*') AND xea.element_name = expts.element_name " +
                                                                       "WHERE gid.groups_groupid_xdat_user_xdat_user_id IN (:userIds) " +
                                                                       "   OR xea.xdat_user_xdat_user_id IN (:userIds) " +
                                                                       "GROUP BY xea.element_name";


    private static final ResultSetExtractor<Map<String, Long>> ELEMENT_COUNT_EXTRACTOR = results -> {
        final Map<String, Long> elementCounts = new HashMap<>();
        while (results.next()) {
            final String elementName  = results.getString("element_name");
            final long   elementCount = results.getLong("element_count");
            elementCounts.put(elementName, elementCount);
        }
        return elementCounts;
    };

    @Autowired
    public ReadableCountsExtractor(final @Lazy GroupsAndPermissionsCache cache, final NamedParameterJdbcTemplate template) {
        super(cache, CACHE_READABLE_COUNTS, template);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> extract(final String username, final Object... parameters) {
        if (isInvalidExtractRequest(username, parameters)) {
            return Collections.emptyMap();
        }

        log.info("Extracting readable counts for user '{}'", username);

        try {
            final Map<String, Long> readableCounts   = new HashMap<>();
            final List<String>      readableProjects = getUserReadableProjects(username);
            readableCounts.put(XnatProjectdata.SCHEMA_ELEMENT_NAME, (long) readableProjects.size());
            readableCounts.put(WrkWorkflowdata.SCHEMA_ELEMENT_NAME, getUserReadableWorkflowCount(username));
            readableCounts.putAll(getUserReadableSubjectsAndExperiments(readableProjects, Arrays.asList(Users.getUserId(username), Users.getUserId(Users.DEFAULT_GUEST_USERNAME))));

            if (log.isDebugEnabled()) {
                log.debug("Caching the following readable element counts for user '{}': '{}'", username, getDisplayForReadableCounts(readableCounts));
            }

            return readableCounts;
        } catch (DataAccessException e) {
            log.error("An error occurred in the SQL for retrieving readable counts for the  user {}", username, e);
            return Collections.emptyMap();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getKeys() {
        return Users.getUsernames(getTemplate());
    }

    private Long getUserReadableWorkflowCount(final String username) {
        return getTemplate().queryForObject(QUERY_USER_READABLE_WORKFLOW_COUNT, new MapSqlParameterSource(PARAM_USERNAME, username), Long.class);
    }

    private Map<String, Long> getUserReadableSubjectsAndExperiments(final List<String> readableProjectIds, final List<Integer> userIds) {
        if (readableProjectIds.isEmpty()) {
            return ImmutableMap.of(XnatSubjectdata.SCHEMA_ELEMENT_NAME, 0L);
        }

        final MapSqlParameterSource userIdParameters         = new MapSqlParameterSource(PARAM_USER_IDS, userIds);
        final Map<String, Long> readableExperimentCounts = new HashMap<>(Objects.requireNonNull(getTemplate().query(QUERY_USER_READABLE_EXPERIMENT_COUNT, userIdParameters, ELEMENT_COUNT_EXTRACTOR)));
        final Long readableSubjectCount = getTemplate().queryForObject(QUERY_USER_READABLE_SUBJECT_COUNT, userIdParameters, Long.class);
        readableExperimentCounts.put(XnatSubjectdata.SCHEMA_ELEMENT_NAME, Optional.ofNullable(readableSubjectCount).orElse(0L));
        readableExperimentCounts.putAll(Objects.requireNonNull(getTemplate().query(QUERY_USER_READABLE_SCAN_COUNT, userIdParameters, ELEMENT_COUNT_EXTRACTOR)));
        return readableExperimentCounts;
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
}
