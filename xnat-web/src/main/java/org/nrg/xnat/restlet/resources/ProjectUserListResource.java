/*
 * web: org.nrg.xnat.restlet.resources.ProjectUserListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang3.StringUtils;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

public class ProjectUserListResource extends SecureResource {
    private final XnatProjectdata _project;
    private final boolean         _displayHiddenUsers;

    public ProjectUserListResource(Context context, Request request, Response response) throws Exception {
        super(context, request, response);

        setReadable(true);
        setModifiable(false);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));

        final String projectId = (String) getParameter(request, "PROJECT_ID");
        final UserI  user      = getUser();
        _project = org.apache.commons.lang3.StringUtils.isNotBlank(projectId) ? XnatProjectdata.getProjectByIDorAlias(projectId, user, false) : null;
        if (_project == null) {
            _displayHiddenUsers = false;
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "The project ID " + projectId + " does not appear to be a valid project ID. Please verify your information.");
        } else {
            if (!(Roles.isSiteAdmin(user) || Permissions.canEdit(user, _project) || isWhitelisted(projectId))) {
                logger.error("Unauthorized Access to project-level user resources. User: " + userName);
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Access Denied: Only project owners and site managers can access user resources.");
            }
            _displayHiddenUsers = Boolean.parseBoolean((String) getParameter(request, "DISPLAY_HIDDEN_USERS"));
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {

        final XFTTable table;
        if (_project != null) {
            final StringBuilder query = new StringBuilder("SELECT g.id AS \"GROUP_ID\", displayname,login,firstname,lastname,email FROM xdat_userGroup g RIGHT JOIN xdat_user_Groupid map ON g.id=map.groupid RIGHT JOIN xdat_user u ON map.groups_groupid_xdat_user_xdat_user_id=u.xdat_user_id WHERE tag='").append(_project.getId()).append("' ");
            if(this.getQueryVariable("includeAllDataAccess")!=null && this.getQueryVariable("includeAllDataAccess").equalsIgnoreCase("true")){
                query.append(" OR g.id ='ALL_DATA_ADMIN' ");
            }
            try {
                if(!_displayHiddenUsers){
                    query.append(" and enabled = 1 ");
                }
                query.append(" ORDER BY g.id DESC;");
                final UserI user = getUser();
                table = XFTTable.Execute(query.toString(), user.getDBName(), user.getLogin());
            } catch (SQLException | DBPoolException e) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred trying to run the following query: " + query.toString(), e);
            }
        } else {
            table = null;
        }

        Hashtable<String, Object> params = new Hashtable<>();
        params.put("title", "Projects");

        MediaType mt = overrideVariant(variant);

        if(table!=null)params.put("totalRecords", table.size());
        return representTable(table, mt, params);
    }

    public boolean isWhitelisted() {
        final String projectId = (String) _project.getItem().getProps().get("id");
        final ConfigService configService = XDAT.getConfigService();
        final String config = configService.getConfigContents("user-resource-whitelist", "whitelist.json", Scope.Project, projectId);
        if (!StringUtils.isBlank(config)) {
            try {
                List<String> projectUserResourceWhitelist = getSerializer().deserializeJson(config, SerializerService.TYPE_REF_LIST_STRING);
                if (projectUserResourceWhitelist != null) {
                    return projectUserResourceWhitelist.contains(getUser().getUsername());
                }
            } catch (IOException e) {
                logger.error("", e);
            }
        }

        return false;
    }
}
