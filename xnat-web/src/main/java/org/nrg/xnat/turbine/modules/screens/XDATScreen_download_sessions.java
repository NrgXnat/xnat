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
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings("unused")
public class XDATScreen_download_sessions extends SecureScreen {
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

        final String[] sessions = ((String[]) TurbineUtils.GetPassedObjects("sessions", data));
        final List<String> sessionIds = new ArrayList<>();

        if (sessions == null) {
            // If the sessions aren't directly embedded in the data, check for a search element.
            final String element = (String) TurbineUtils.GetPassedParameter("search_element", data);
            final String field = (String) TurbineUtils.GetPassedParameter("search_field", data);
            final String value = (String) TurbineUtils.GetPassedParameter("search_value", data);

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
                final String login = user.getUsername();

                final String submittedProjectId = (String) TurbineUtils.GetPassedParameter("project", data);

                // Get all projects, primary and shared, that contain the specified session IDs.
                final Multimap<String, String> projectSessionMap = StringUtils.isNotBlank(submittedProjectId)
                                                                   ? Permissions.verifyAccessToSessions(_parameterized, user, sessionIds, submittedProjectId)
                                                                   : Permissions.verifyAccessToSessions(_parameterized, user, sessionIds);
                final List<String> sessionsUserCanAccess = projectSessionMap!=null
                        ? (StringUtils.isNotBlank(submittedProjectId)? (List<String>)projectSessionMap.get(submittedProjectId): new ArrayList<>(projectSessionMap.values()))
                        : new ArrayList<String>();


                if (projectSessionMap.isEmpty()) {
                    throw new RuntimeException("No accessible projects found for the request by user " + user.getUsername() + " to download the requested session(s): " + Joiner.on(", ").join(sessionIds));
                }

                final Set<String> projectIds = projectSessionMap.keySet();

                context.put("projectIds", projectIds);
                context.put("sessionSummary", _parameterized.query(QUERY_GET_SESSION_ATTRIBUTES, new HashMap<String, Object>() {{
                    put("sessionIds", sessionsUserCanAccess);
                    put("projectIds", projectIds);
                }}, new RowMapper<List<String>>() {
                    @Override
                    public List<String> mapRow(final ResultSet result, final int rowNum) throws SQLException {
                        return new ArrayList<String>() {{
                            add(result.getString("id"));
                            add(result.getString("ids"));
                            add(result.getString("modality"));
                            add(result.getString("subject"));
                            add(result.getString("project"));
                        }};
                    }
                }));
                context.put("scans", _parameterized.query(QUERY_GET_SESSION_SCANS, new HashMap<String, Object>() {{
                    put("sessionIds", sessionsUserCanAccess);
                }}, new RowMapper<List<String>>() {
                    @Override
                    public List<String> mapRow(final ResultSet result, final int rowNum) throws SQLException {
                        return new ArrayList<String>() {{
                            add(result.getString("type"));
                            add(Integer.toString(result.getInt("count")));
                        }};
                    }
                }));
                context.put("recons", _parameterized.query(QUERY_GET_SESSION_RECONS, new HashMap<String, Object>() {{
                    put("sessionIds", sessionsUserCanAccess);
                }}, new RowMapper<List<String>>() {
                    @Override
                    public List<String> mapRow(final ResultSet result, final int rowNum) throws SQLException {
                        return new ArrayList<String>() {{
                            add(result.getString("type"));
                            add(Integer.toString(result.getInt("count")));
                        }};
                    }
                }));

                context.put("assessors", Lists.transform(_parameterized.query(QUERY_GET_SESSION_ASSESSORS, new HashMap<String, Object>() {{
                    put("sessionIds", sessionsUserCanAccess);
                    put("userId", user.getID());
                }}, new RowMapper<List<String>>() {
                    @Override
                    public List<String> mapRow(final ResultSet result, final int rowNum) throws SQLException {
                        return new ArrayList<String>() {{
                            add(result.getString("element_name"));
                            add(Integer.toString(result.getInt("count")));
                        }};
                    }
                }), new Function<List<String>, List<String>>() {
                    @Nullable
                    @Override
                    public List<String> apply(@Nullable final List<String> assessor) {
                        if (assessor == null) {
                            return null;
                        }
                        assessor.add(ElementSecurity.GetPluralDescription(assessor.get(0)));
                        return assessor;
                    }
                }));
                context.put("scan_formats", _parameterized.query(QUERY_GET_SESSION_SCAN_FORMATS, new HashMap<String, Object>() {{
                    put("sessionIds", sessionsUserCanAccess);
                }}, new RowMapper<List<String>>() {
                    @Override
                    public List<String> mapRow(final ResultSet result, final int rowNum) throws SQLException {
                        return new ArrayList<String>() {{
                            add(result.getString("label"));
                            add(Integer.toString(result.getInt("count")));
                        }};
                    }
                }));
                context.put("resources", _parameterized.query(QUERY_GET_SESSION_RESOURCES, new HashMap<String, Object>() {{
                    put("sessionIds", sessionsUserCanAccess);
                }}, new RowMapper<List<String>>() {
                    @Override
                    public List<String> mapRow(final ResultSet result, final int rowNum) throws SQLException {
                        return new ArrayList<String>() {{
                            add(result.getString("label"));
                            add(Integer.toString(result.getInt("count")));
                        }};
                    }
                }));
            }
        } catch (InsufficientPrivilegesException e) {
            data.setMessage(e.getMessage());
            data.setScreenTemplate("Error.vm");
        }
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
     * Requires one parameter:
     *
     * <ul>
     * <li><b>sessions</b> is a list of session IDs</li>
     * </ul>
     */
    private static final String QUERY_GET_SESSION_ASSESSORS = "SELECT element_name, " +
                                                              "        Count(*) AS count " +
                                                              " FROM   (SELECT xea.element_name, " +
                                                              "                xfm.field, " +
                                                              "                xfm.field_value " +
                                                              "         FROM   xdat_user u " +
                                                              "                JOIN xdat_user_groupid map " +
                                                              "                  ON u.xdat_user_id = map.groups_groupid_xdat_user_xdat_user_id " +
                                                              "                JOIN xdat_usergroup gp " +
                                                              "                  ON map.groupid = gp.id " +
                                                              "                JOIN xdat_element_access xea " +
                                                              "                  ON gp.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
                                                              "                JOIN xdat_field_mapping_set xfms " +
                                                              "                  ON " +
                                                              " xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                              " JOIN xdat_field_mapping xfm " +
                                                              "   ON " +
                                                              " xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                              " AND read_element = 1 " +
                                                              " AND field_value != '' " +
                                                              " AND field != '' " +
                                                              " WHERE  u.login = 'guest' " +
                                                              "  UNION " +
                                                              "  SELECT xea.element_name, " +
                                                              "         xfm.field, " +
                                                              "         xfm.field_value " +
                                                              "  FROM   xdat_user_groupid map " +
                                                              "         JOIN xdat_usergroup gp " +
                                                              "           ON map.groupid = gp.id " +
                                                              "         JOIN xdat_element_access xea " +
                                                              "           ON gp.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
                                                              "         JOIN xdat_field_mapping_set xfms " +
                                                              "           ON " +
                                                              " xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                              " JOIN xdat_field_mapping xfm " +
                                                              "   ON " +
                                                              " xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                              " AND read_element = 1 " +
                                                              " AND field_value != '' " +
                                                              " AND field != '' " +
                                                              " WHERE  map.groups_groupid_xdat_user_xdat_user_id = ( :userId ) " +
                                                              " OR xfm.field_value IN (SELECT proj.id " +
                                                              "         FROM   xnat_projectdata proj " +
                                                              "         JOIN (SELECT field_value, " +
                                                              "                        read_element AS " +
                                                              "                                                        project_read " +
                                                              "                                FROM   xdat_element_access " +
                                                              "                                ea " +
                                                              "                                LEFT JOIN xdat_field_mapping_set fms " +
                                                              "                                ON ea.xdat_element_access_id = " +
                                                              "                                fms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                              "                                LEFT JOIN xdat_user u " +
                                                              "                                ON ea.xdat_user_xdat_user_id = u.xdat_user_id " +
                                                              "                                LEFT JOIN xdat_field_mapping fm " +
                                                              "                                ON fms.xdat_field_mapping_set_id = " +
                                                              "                                fm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                              "                                WHERE  login = 'guest' " +
                                                              "                                AND read_element = 1 " +
                                                              "                                AND element_name = 'xnat:projectData')project_read " +
                                                              " ON proj.id = project_read.field_value " +
                                                              " JOIN (SELECT field_value, " +
                                                              "       read_element AS subject_read " +
                                                              "               FROM   xdat_element_access ea " +
                                                              "               LEFT JOIN xdat_field_mapping_set fms " +
                                                              "               ON ea.xdat_element_access_id = " +
                                                              "               fms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                              "               LEFT JOIN xdat_user u " +
                                                              "               ON ea.xdat_user_xdat_user_id = u.xdat_user_id " +
                                                              "               LEFT JOIN xdat_field_mapping fm " +
                                                              "               ON fms.xdat_field_mapping_set_id = " +
                                                              "               fm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                              "               WHERE  login = 'guest' " +
                                                              "               AND read_element = 1 " +
                                                              "               AND field = 'xnat:subjectData/project')subject_read " +
                                                              " ON proj.id = subject_read.field_value)) perms             " +
                                                              " INNER JOIN (SELECT iad.id, " +
                                                              "                    element_name " +
                                                              "                    || '/project' AS field, " +
                                                              "                    expt.project, " +
                                                              "                    expt.label " +
                                                              "             FROM   xnat_imageassessordata iad " +
                                                              "                    LEFT JOIN xnat_experimentdata expt " +
                                                              "                           ON iad.id = expt.id " +
                                                              "                    LEFT JOIN xdat_meta_element xme " +
                                                              "                           ON expt.extension = xme.xdat_meta_element_id " +
                                                              "             WHERE  iad.imagesession_id IN ( :sessionIds ) " +
                                                              "             ) expts " +
                                                              "         ON perms.field = expts.field " +
                                                              "            AND perms.field_value = expts.project " +
                                                              " GROUP  BY element_name " +
                                                              " ORDER  BY element_name;   ";

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

    private final NamedParameterJdbcTemplate _parameterized;
}
