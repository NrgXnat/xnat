/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_download_sessions
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Slf4j
public class XDATScreen_download_sessions extends SecureScreen {
    private static final int QUERY_GROUP_SIZE = 10000;

    public XDATScreen_download_sessions() {
        _parameterized = XDAT.getContextService().getBean(NamedParameterJdbcTemplate.class);
    }

    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        // Do a first smell test to see if the user is even logged in, legit, etc.
        final boolean isAuthorized = isAuthorized(data);
        if (!isAuthorized) {
            data.setMessage("You must be logged in to gain access to this page.");
            data.setScreenTemplate("Error.vm");
            return;
        }

        final UserI user = XDAT.getUserDetails();
        if (user == null) {
            data.setMessage("An error occurred trying to access your user record. Please try logging back into XNAT or contact your system administrator.");
            data.setScreenTemplate("Error.vm");
            return;
        }

        final String[]     sessions   = ((String[]) TurbineUtils.GetPassedObjects("sessions", data));
        final List<String> sessionIds = new ArrayList<>();

        if (sessions == null) {
            // If the sessions aren't directly embedded in the data, check for a search element.
            final String element = (String) TurbineUtils.GetPassedParameter("search_element", data);
            final String field   = (String) TurbineUtils.GetPassedParameter("search_field", data);
            final String value   = (String) TurbineUtils.GetPassedParameter("search_value", data);

            if (StringUtils.isNotBlank(element) && StringUtils.isNotBlank(field) && StringUtils.isNotBlank(value)) {
                final SchemaElement schemaElement = SchemaElement.GetElement(element);
                if (schemaElement.getGenericXFTElement().instanceOf("xnat:imageSessionData")) {
                    sessionIds.add(value);
                }
            }
        } else {
            sessionIds.addAll(Arrays.asList(sessions));
        }

        try {
            if (!sessionIds.isEmpty()) {
                final String  projectId    = (String) TurbineUtils.GetPassedParameter("project", data);
                final boolean hasProjectId = StringUtils.isNotBlank(projectId);

                // Get all projects, primary and shared, that contain the specified session IDs.
                final ArrayListMultimap<String, String> projectSessionMap = hasProjectId
                                                                            ? Permissions.verifyAccessToSessions(_parameterized, user, sessionIds, projectId)
                                                                            : Permissions.verifyAccessToSessions(_parameterized, user, sessionIds);
                final Collection<String> sessionsUserCanAccess = hasProjectId ? projectSessionMap.get(projectId) : new HashSet<>(projectSessionMap.values());

                if (projectSessionMap.isEmpty()) {
                    throw new RuntimeException("No accessible projects found for the request by user " + user.getUsername() + " to download the requested session(s): " + Joiner.on(", ").join(sessionIds));
                }

                final ArrayListMultimap<String, String> invertedProjectSessionMap = Multimaps.invertFrom(projectSessionMap, ArrayListMultimap.create());
                final Collection<String> sessionsUserCanDownload = sessionsUserCanAccess.stream()
                        .filter(s -> Features.checkRestrictedFeature(user, hasProjectId ? getPrimaryProject(user, s) : invertedProjectSessionMap.get(s).get(0),"data_download"))
                        .collect(Collectors.toList());

                if (sessionsUserCanDownload.isEmpty()) {
                    context.put("msg","None of the requested data is available for download.");
                    return;
                }else if(sessionsUserCanDownload.size() != sessionsUserCanAccess.size()){
                    context.put("msg","Some of the requested data is unavailable for download and has been excluded from the table below.");
                }

                final Set<String>           projectIds = projectSessionMap.keySet();
                final String accessorsQuery = Groups.hasAllDataAccess(user) || Permissions.isProjectPublic(projectId) ? QUERY_GET_SESSION_ASSESSORS_ADMIN : QUERY_GET_SESSION_ASSESSORS;
                if (sessionsUserCanDownload.size() <= QUERY_GROUP_SIZE) {
                    final MapSqlParameterSource parameters = new MapSqlParameterSource("sessionIds", sessionsUserCanDownload)
                            .addValue("projectIds", projectIds)
                            .addValue("userId", user.getUsername());
                    context.put("projectIds", projectIds);
                    context.put("sessionSummary", _parameterized.query(QUERY_GET_SESSION_ATTRIBUTES, parameters, SESSION_SUMMARY_ROW_MAPPER));
                    context.put("scans", _parameterized.query(QUERY_GET_SESSION_SCANS, parameters, SCAN_ROW_AND_RECON_ROW_MAPPER));
                    context.put("recons", _parameterized.query(QUERY_GET_SESSION_RECONS, parameters, SCAN_ROW_AND_RECON_ROW_MAPPER));
                    context.put("assessors", Lists.transform(_parameterized.query(accessorsQuery, parameters, ASSESSOR_ROW_MAPPER), ASSESSOR_DESCRIPTION_FUNCTION));
                    context.put("scan_formats", _parameterized.query(QUERY_GET_SESSION_SCAN_FORMATS, parameters, SCAN_FORMAT_AND_SESSION_RESOURCE_ROW_MAPPER));
                    context.put("resources", _parameterized.query(QUERY_GET_SESSION_RESOURCES, parameters, SCAN_FORMAT_AND_SESSION_RESOURCE_ROW_MAPPER));
                } else {
                    List<List<String>> sessionSummary = new ArrayList<>();
                    Map<String, Integer> scans = new HashMap<>();
                    Map<String, Integer> recons = new HashMap<>();
                    Map<String, Integer> assessors = new HashMap<>();
                    Map<String, Integer> scan_formats = new HashMap<>();
                    Map<String, Integer> resources = new HashMap<>();
                    for (int i = 0; i < sessionsUserCanDownload.size(); i += QUERY_GROUP_SIZE) {
                        Collection<String> subSessions = sessionsUserCanDownload.stream().skip(i).limit(QUERY_GROUP_SIZE).collect(Collectors.toList());
                        final MapSqlParameterSource parameters = new MapSqlParameterSource("sessionIds", subSessions)
                                .addValue("projectIds", projectIds)
                                .addValue("userId", user.getUsername());
                        sessionSummary.addAll(_parameterized.query(QUERY_GET_SESSION_ATTRIBUTES, parameters, SESSION_SUMMARY_ROW_MAPPER));
                        scans = mergeMap(scans, _parameterized.query(QUERY_GET_SESSION_SCANS, parameters, SCAN_ROW_AND_RECON_ROW_MAPPER));
                        recons = mergeMap(recons, _parameterized.query(QUERY_GET_SESSION_RECONS, parameters, SCAN_ROW_AND_RECON_ROW_MAPPER));
                        assessors = mergeMap(assessors, _parameterized.query(accessorsQuery, parameters, ASSESSOR_ROW_MAPPER));
                        scan_formats = mergeMap(scan_formats, _parameterized.query(QUERY_GET_SESSION_SCAN_FORMATS, parameters, SCAN_FORMAT_AND_SESSION_RESOURCE_ROW_MAPPER));
                        resources = mergeMap(resources, _parameterized.query(QUERY_GET_SESSION_RESOURCES, parameters, SCAN_FORMAT_AND_SESSION_RESOURCE_ROW_MAPPER));
                    }
                    context.put("projectIds", projectIds);
                    context.put("sessionSummary", sessionSummary);
                    context.put("scans", mapToList(scans));
                    context.put("recons", mapToList(recons));
                    context.put("assessors", Lists.transform(mapToList(assessors), ASSESSOR_DESCRIPTION_FUNCTION));
                    context.put("scan_formats", mapToList(scan_formats));
                    context.put("resources", mapToList(resources));
                }
            }
        } catch (InsufficientPrivilegesException e) {
            data.setMessage(e.getMessage());
            data.setScreenTemplate("Error.vm");
        }
    }

    private Map<String, Integer> mergeMap(Map<String, Integer> source, List<List<String>> newResults) {
        Map<String, Integer> newResultsMap = newResults.stream().collect(Collectors.toMap(value -> value.get(0), value -> Integer.valueOf(value.get(1))));
        newResultsMap.forEach((k, v) -> source.merge(k, v, Integer::sum));
        return source;
    }

    private List<List<String>> mapToList(Map<String, Integer> source) {
        return source.entrySet().stream().map(e -> {
            List<String> item = new ArrayList<>();
            item.add(e.getKey());
            item.add(e.getValue().toString());
            return item;
        }).collect(Collectors.toList());
    }

    private String getPrimaryProject(final UserI user, final String experimentId) {
        BaseXnatExperimentdata experimentdata = BaseXnatExperimentdata.getXnatExperimentdatasById(experimentId, user, false);
        return (experimentdata != null) ? experimentdata.getProject() :  null;
    }

    /**
     * Requires two parameters:
     *
     * <ul>
     * <li><b>sessions</b> is a list of session IDs</li>
     * <li><b>project</b> is the project that contains the referenced sessions</li>
     * </ul>
     */
    private static final String QUERY_GET_SESSION_ATTRIBUTES = "SELECT "
                                                               + "  expt.id, "
                                                               + "  COALESCE(pp.label, expt.label, expt.id) AS IDS, "
                                                               + "  modality, "
                                                               + "  subj.label                              AS subject, "
                                                               + "  COALESCE(pp.project, expt.project)      AS project "
                                                               + "FROM xnat_imageSessionData isd "
                                                               + "  LEFT JOIN xnat_experimentData expt ON expt.id = isd.id "
                                                               + "  LEFT JOIN xnat_subjectassessordata sa ON sa.id = expt.id "
                                                               + "  LEFT JOIN xnat_subjectdata subj ON sa.subject_id = subj.id "
                                                               + "  LEFT JOIN xnat_experimentData_share pp "
                                                               + "    ON expt.id = pp.sharing_share_xnat_experimentda_id AND pp.project IN (:projectIds) "
                                                               + "WHERE isd.ID IN (:sessionIds) "
                                                               + "ORDER BY IDS ";

    /**
     * Requires one parameter:
     *
     * <ul>
     * <li><b>sessions</b> is a list of session IDs</li>
     * </ul>
     */
    private static final String QUERY_GET_SESSION_SCANS = "SELECT " +
                                                          "  type, " +
                                                          "  COUNT(*) AS count " +
                                                          "FROM xnat_imagescandata " +
                                                          "WHERE xnat_imagescandata.image_session_id IN (:sessionIds) " +
                                                          "GROUP BY type " +
                                                          "ORDER BY type";

    /**
     * Requires one parameter:
     *
     * <ul>
     * <li><b>sessions</b> is a list of session IDs</li>
     * </ul>
     */
    private static final String QUERY_GET_SESSION_RECONS = "SELECT " +
                                                           "  type, " +
                                                           "  COUNT(*) AS count " +
                                                           "FROM xnat_reconstructedimagedata " +
                                                           "WHERE xnat_reconstructedimagedata.image_session_id IN (:sessionIds) " +
                                                           "GROUP BY type " +
                                                           "ORDER BY type";

    /**
     * Requires two parameters:
     *
     * <ul>
     * <li><b>userId</b> is a the ID of the user whose permissions should be used</li>
     * <li><b>sessions</b> is a list of session IDs</li>
     * </ul>
     */
    private static final String QUERY_GET_SESSION_ASSESSORS = "SELECT element_name, count(*) AS count FROM " +
                                                              "  (SELECT * FROM " +
                                                              "     (SELECT DISTINCT element_name, field, field_value FROM " +
                                                              "        xdat_user u " +
                                                              "          LEFT JOIN xdat_user_groupid i ON u.xdat_user_id = i.groups_groupid_xdat_user_xdat_user_id " +
                                                              "          LEFT JOIN xdat_usergroup g ON i.groupid = g.id " +
                                                              "          LEFT JOIN xdat_element_access a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id OR u.xdat_user_id = a.xdat_user_xdat_user_id " +
                                                              "          LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                              "          LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                              "      WHERE " +
                                                              "        m.read_element = 1 AND " +
                                                              "        a.element_name != 'xnat:projectData' AND " +
                                                              "        field_value IN (:projectIds) AND " +
                                                              "        u.login IN ('guest', :userId)) perms " +
                                                              "       INNER JOIN (SELECT a.id, element_name || '/project' AS field, e.project, e.label FROM " +
                                                              "                     xnat_imageassessordata a " +
                                                              "                       LEFT JOIN xnat_experimentdata e ON a.id = e.id " +
                                                              "                       LEFT JOIN xdat_meta_element m ON e.extension = m.xdat_meta_element_id " +
                                                              "                   WHERE " +
                                                              "                     a.imagesession_id IN (:sessionIds) AND " +
                                                              "                     e.project IN (:projectIds) " +
                                                              "                   UNION " +
                                                              "                   SELECT e.id, m.element_name || '/sharing/share/project', s.project, s.label FROM " +
                                                              "                     xnat_experimentdata_share s " +
                                                              "                       LEFT JOIN xnat_imageassessordata a ON a.id = s.sharing_share_xnat_experimentda_id " +
                                                              "                       LEFT JOIN xnat_experimentdata e ON e.id = a.id " +
                                                              "                       LEFT JOIN xdat_meta_element m ON e.extension = m.xdat_meta_element_id " +
                                                              "                   WHERE " +
                                                              "                     a.imagesession_id IN (:sessionIds) AND " +
                                                              "                     s.project IN (:projectIds)) expts ON perms.field = expts.field AND perms.field_value = expts.project) img_assessors " +
                                                              "GROUP BY element_name " +
                                                              "ORDER BY element_name";

    /**
     * Requires one parameter:
     *
     * <ul>
     * <li><b>sessions</b> is a list of session IDs</li>
     * </ul>
     */
    private static final String QUERY_GET_SESSION_ASSESSORS_ADMIN = "SELECT element_name, Count(*) AS count " +
                                                                    "FROM (SELECT DISTINCT element_name, m.field, m.field_value " +
                                                                    "      FROM xdat_user u " +
                                                                    "           JOIN xdat_user_groupid i ON u.xdat_user_id = i.groups_groupid_xdat_user_xdat_user_id " +
                                                                    "           JOIN xdat_usergroup g ON i.groupid = g.id " +
                                                                    "           JOIN xdat_element_access a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id " +
                                                                    "           JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                    "           JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id AND read_element = 1 " +
                                                                    "      WHERE u.login = 'guest' " +
                                                                    "         OR m.field_value = '*') perms " +
                                                                    "     INNER JOIN (SELECT a.id, element_name || '/project' AS field, e.project, e.label " +
                                                                    "                 FROM xnat_imageassessordata a " +
                                                                    "                      LEFT JOIN xnat_experimentdata e ON a.id = e.id " +
                                                                    "                      LEFT JOIN xdat_meta_element m ON e.extension = m.xdat_meta_element_id " +
                                                                    "                 WHERE a.imagesession_id IN (:sessionIds)) expts ON perms.field = expts.field AND perms.field_value IN (expts.project, '*') " +
                                                                    "GROUP BY element_name " +
                                                                    "ORDER BY element_name";

    /**
     * Requires one parameter:
     *
     * <ul>
     * <li><b>sessions</b> is a list of session IDs</li>
     * </ul>
     */
    private static final String QUERY_GET_SESSION_SCAN_FORMATS = "SELECT " +
                                                                 "  label, " +
                                                                 "  COUNT(*) AS count " +
                                                                 "FROM xnat_imagescandata " +
                                                                 "  JOIN xnat_abstractResource " +
                                                                 "    ON xnat_imagescandata.xnat_imagescandata_id = xnat_abstractResource.xnat_imagescandata_xnat_imagescandata_id " +
                                                                 "WHERE xnat_imagescandata.image_session_id IN (:sessionIds) " +
                                                                 "GROUP BY LABEL";

    /**
     * Requires one parameter:
     *
     * <ul>
     * <li><b>sessions</b> is a list of session IDs</li>
     * </ul>
     */
    private static final String QUERY_GET_SESSION_RESOURCES = "SELECT " +
                                                              "  label, " +
                                                              "  COUNT(*) AS count " +
                                                              "FROM xnat_experimentData_resource expt_res " +
                                                              "  JOIN xnat_abstractResource abst_res ON expt_res.xnat_abstractresource_xnat_abstractresource_id = abst_res.xnat_abstractresource_id " +
                                                              "WHERE expt_res.xnat_experimentdata_id IN (:sessionIds) " +
                                                              "GROUP BY label";

    private static class AttributeAndCountRowMapper implements RowMapper<List<String>> {
        AttributeAndCountRowMapper(final String attribute) {
            _attribute = attribute;
        }

        @Override
        public List<String> mapRow(final ResultSet result, final int index) throws SQLException {
            final List<String> item = new ArrayList<>();
            item.add(result.getString(_attribute));
            item.add(Integer.toString(result.getInt("count")));
            log.debug("Processed row {} with {}: {} and count {}", index, _attribute, item.get(0), item.get(1));
            return item;
        }

        private final String _attribute;
    }

    private static final RowMapper<List<String>> ASSESSOR_ROW_MAPPER                         = new AttributeAndCountRowMapper("element_name");
    private static final RowMapper<List<String>> SCAN_FORMAT_AND_SESSION_RESOURCE_ROW_MAPPER = new AttributeAndCountRowMapper("label");
    private static final RowMapper<List<String>> SCAN_ROW_AND_RECON_ROW_MAPPER               = new AttributeAndCountRowMapper("type");

    private static final RowMapper<List<String>>              SESSION_SUMMARY_ROW_MAPPER    = new RowMapper<List<String>>() {
        @Override
        public List<String> mapRow(final ResultSet result, final int rowNum) throws SQLException {
            final List<String> summaries = new ArrayList<>();
            summaries.add(result.getString("id"));
            summaries.add(result.getString("ids"));
            summaries.add(result.getString("modality"));
            summaries.add(result.getString("subject"));
            summaries.add(result.getString("project"));
            return summaries;
        }
    };
    private static final Function<List<String>, List<String>> ASSESSOR_DESCRIPTION_FUNCTION = new Function<List<String>, List<String>>() {
        @Nullable
        @Override
        public List<String> apply(@Nullable final List<String> assessor) {
            if (assessor == null) {
                return null;
            }
            assessor.add(ElementSecurity.GetPluralDescription(assessor.get(0)));
            return assessor;
        }
    };

    private final NamedParameterJdbcTemplate _parameterized;
}
