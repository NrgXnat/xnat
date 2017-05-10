/*
 * web: org.nrg.xnat.turbine.modules.actions.ProjectDownloadAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import com.google.common.collect.Maps;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ProjectDownloadAction extends SecureAction {
    public ProjectDownloadAction() {
        _parameterized = XDAT.getContextService().getBean(NamedParameterJdbcTemplate.class);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        final String projectId = (String) TurbineUtils.GetPassedParameter("project", data);

        if (projectId.contains("\\") || projectId.contains("'")) {
            data.setMessage("Illegal Character");
            data.setScreenTemplate("Index.vm");
            return;
        }

        // Do a first smell test to see if the user is even logged in, legit, etc.
        final boolean isAuthorized = isAuthorized(data);
        if (!isAuthorized) {
            data.setMessage("You must be logged in to gain access to this page.");
            data.setScreenTemplate("Error.vm");
            return;
        }

        if (!Permissions.getAllProjectIds(_parameterized).contains(projectId)) {
            final Exception e = new Exception("Unknown project: " + projectId);
            logger.error("", e);
            this.error(e, data);
            return;
        }

        final UserI user = XDAT.getUserDetails();

        if (!Permissions.canReadProject(user, projectId)) {
            data.setMessage("You are not authorized to access the project " + projectId + ".");
            data.setScreenTemplate("Error.vm");
            return;
        }

        final Map<String, String> attributes = Maps.newHashMap();
        attributes.put("projectId", projectId);
        final List<String> sessions = _parameterized.queryForList(QUERY_IMG_SESSION_IDS, attributes, String.class);
        for (final String session : sessions) {
            data.getParameters().add("sessions", session);
        }

        data.setScreenTemplate("XDATScreen_download_sessions.vm");
    }

    private static final Logger logger = LoggerFactory.getLogger(ProjectDownloadAction.class);
    private static final String QUERY_IMG_SESSION_IDS = "SELECT DISTINCT isd.id FROM xnat_imageSessionData isd LEFT JOIN xnat_experimentData expt ON isd.id=expt.id LEFT JOIN xnat_experimentData_meta_data meta ON expt.experimentData_info=meta.meta_data_id LEFT JOIN xnat_experimentData_share proj ON expt.id=proj.sharing_share_xnat_experimentda_id WHERE (proj.project=:projectId OR expt.project=:projectId) AND (meta.status='active' OR meta.status='locked');";

    private final NamedParameterJdbcTemplate _parameterized;
}
