/*
 * web: org.nrg.xnat.turbine.modules.actions.SubjectDownloadAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2022, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

@SuppressWarnings("unused")
@Slf4j
public class SubjectDownloadAction extends SecureAction {
    public SubjectDownloadAction() {
        _parameterized = XDAT.getContextService().getBean(NamedParameterJdbcTemplate.class);
    }

    @Override
    public void doPerform(final RunData data, final Context context) throws Exception {
        // Do a first smell test to see if the user is even logged in, legit, etc.
        final boolean isAuthorized = isAuthorized(data);
        if (!isAuthorized) {
            data.setMessage("You must be logged in to gain access to this page.");
            data.setScreenTemplate("Error.vm");
            return;
        }

        final String project = (String) TurbineUtils.GetPassedParameter("project", data);
        if (StringUtils.isBlank(project) || StringUtils.containsAny(project, "\\", "'")) {
            data.setMessage("Invalid project ID specified: " + (StringUtils.isBlank(project) ? "can't be blank" : project));
            data.setScreenTemplate("Index.vm");
            return;
        }

        if (!Permissions.getAllProjectIds(_parameterized).contains(project)) {
            final Exception e = new Exception("Unknown project: " + project);
            log.error("The specified download project '{}' is not a valid project ID", project);
            error(e, data);
            return;
        }

        final String                subject    = (String) TurbineUtils.GetPassedParameter("subject", data);
        final MapSqlParameterSource parameters = new MapSqlParameterSource(PARAM_PROJECT, project).addValue(PARAM_SUBJECT, subject);
        if (StringUtils.isBlank(subject) || StringUtils.containsAny(subject, "\\", "'") || !_parameterized.queryForObject(QUERY_SUBJECT_EXISTS, parameters, Boolean.class)) {
            data.setMessage("Invalid subject ID or label specified: " + (StringUtils.isBlank(subject) ? "can't be blank" : subject));
            data.setScreenTemplate("Index.vm");
            return;
        }

        final UserI user = XDAT.getUserDetails();
        if (user == null || !Permissions.canReadProject(user, project)) {
            data.setMessage("You are not authorized to access the project " + project + ".");
            data.setScreenTemplate("Error.vm");
            return;
        }

        parameters.addValue(PARAM_USERNAME, user.getUsername());
        final List<String> sessions = _parameterized.queryForList(QUERY_IMG_SESSION_IDS, parameters, String.class);
        for (final String session : sessions) {
            data.getParameters().add("sessions", session);
        }

        data.setScreenTemplate("XDATScreen_download_sessions.vm");
    }

    private static final String PARAM_PROJECT         = "project";
    private static final String PARAM_SUBJECT         = "subject";
    private static final String PARAM_USERNAME        = "username";
    private static final String QUERY_SUBJECT_EXISTS  = "SELECT exists(SELECT id FROM xnat_subjectdata s LEFT JOIN xnat_projectparticipant p ON s.id = p.subject_id WHERE :" + PARAM_SUBJECT + " IN (s.id, s.label) AND s.project = :" + PARAM_PROJECT + " OR :" + PARAM_SUBJECT + " IN (p.subject_id, p.label) AND p.project = :" + PARAM_PROJECT + ")";
    private static final String QUERY_IMG_SESSION_IDS = "WITH " +
                                                        "    readable_types AS ( " +
                                                        "        SELECT DISTINCT " +
                                                        "            a.element_name " +
                                                        "        FROM " +
                                                        "            xdat_user u " +
                                                        "                LEFT JOIN xdat_user_groupid i ON u.xdat_user_id = i.groups_groupid_xdat_user_xdat_user_id " +
                                                        "                LEFT JOIN xdat_usergroup g ON i.groupid = g.id " +
                                                        "                LEFT JOIN xdat_element_access a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id OR u.xdat_user_id = a.xdat_user_xdat_user_id " +
                                                        "                LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                        "                LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                        "        WHERE " +
                                                        "            m.read_element = 1 AND " +
                                                        "            a.element_name != 'xnat:projectData' AND " +
                                                        "            field_value IN (:" + PARAM_PROJECT + ", '*') AND " +
                                                        "            u.login IN ('guest', :" + PARAM_USERNAME + ")) " +
                                                        "SELECT " +
                                                        "    x.id " +
                                                        "FROM " +
                                                        "    xnat_imagesessiondata i " +
                                                        "        LEFT JOIN xnat_subjectassessordata a ON i.id = a.id " +
                                                        "        LEFT JOIN xnat_experimentdata x ON a.id = x.id " +
                                                        "        LEFT JOIN xnat_experimentdata_meta_data md ON x.experimentdata_info = md.meta_data_id " +
                                                        "        LEFT JOIN xdat_meta_element me ON x.extension = me.xdat_meta_element_id " +
                                                        "        LEFT JOIN xnat_experimentdata_share h ON x.id = h.sharing_share_xnat_experimentda_id " +
                                                        "        LEFT JOIN xnat_subjectdata s ON a.subject_id = s.id " +
                                                        "        LEFT JOIN xnat_projectparticipant p ON s.id = p.subject_id " +
                                                        "WHERE " +
                                                        "    (h.project = :" + PARAM_PROJECT + " OR x.project = :" + PARAM_PROJECT + ") AND " +
                                                        "    (((s.id = :" + PARAM_SUBJECT + " OR s.label = :" + PARAM_SUBJECT + ") AND s.project = :" + PARAM_PROJECT + ") OR ((p.subject_id = :" + PARAM_SUBJECT + " OR p.label = :" + PARAM_SUBJECT + ") AND p.project = :" + PARAM_PROJECT + ")) AND " +
                                                        "    me.element_name IN (SELECT * FROM readable_types) AND " +
                                                        "    md.status IN ('active', 'locked')";

    private final NamedParameterJdbcTemplate _parameterized;
}
