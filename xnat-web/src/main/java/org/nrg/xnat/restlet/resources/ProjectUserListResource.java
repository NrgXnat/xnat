/*
 * org.nrg.xnat.restlet.resources.ProjectUserListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang.StringUtils;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.InvalidItemException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

public class ProjectUserListResource extends SecureResource {
    XFTTable table = null;
    XnatProjectdata proj = null;
    boolean displayHiddenUsers = false;

    public ProjectUserListResource(Context context, Request request, Response response) throws Exception {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));

        final String projectId = (String) getParameter(request, "PROJECT_ID");
        if (projectId != null) {
            proj = XnatProjectdata.getProjectByIDorAlias(projectId, user, false);
        }
        if (proj == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "The project ID " + projectId + " does not appear to be a valid project ID. Please verify your information.");
        } else {
                if (!(Roles.isSiteAdmin(user) || Permissions.canEdit(user, proj) || isWhitelisted(projectId))) {
                    logger.error("Unauthorized Access to project-level user resources. User: " + userName);
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Access Denied: Only project owners and site managers can access user resources.");
                }
                displayHiddenUsers = Boolean.parseBoolean((String) getParameter(request, "DISPLAY_HIDDEN_USERS"));
            }
        }

    @Override
    public boolean allowGet() {
        return true;
    }

    @Override
    public Representation represent(Variant variant) {

        if (proj != null) {
            final StringBuilder query = new StringBuilder("SELECT g.id AS \"GROUP_ID\", displayname,login,firstname,lastname,email FROM xdat_userGroup g RIGHT JOIN xdat_user_Groupid map ON g.id=map.groupid RIGHT JOIN xdat_user u ON map.groups_groupid_xdat_user_xdat_user_id=u.xdat_user_id WHERE tag='").append(proj.getId()).append("' ");
            try {
                if(!displayHiddenUsers){
                    query.append(" and enabled = 1 ");
                }
                query.append(" ORDER BY g.id DESC;");
                table = XFTTable.Execute(query.toString(), user.getDBName(), user.getLogin());
            } catch (SQLException | DBPoolException e) {
                logger.warn("An error occurred trying to run the following query: " + query.toString(), e);
            }
        }

        Hashtable<String, Object> params = new Hashtable<>();
        params.put("title", "Projects");

        MediaType mt = overrideVariant(variant);

        if(table!=null)params.put("totalRecords", table.size());
        return representTable(table, mt, params);
    }

    public boolean isWhitelisted() {
        final String projectId = (String) proj.getItem().getProps().get("id");
        final ConfigService configService = XDAT.getConfigService();
        final String config = configService.getConfigContents("user-resource-whitelist", "whitelist.json", Scope.Project, projectId);
        if (!StringUtils.isBlank(config)) {
            try {
                List<String> projectUserResourceWhitelist = OBJECT_MAPPER.readValue(config, TYPE_REFERENCE_LIST_STRING);
                if (projectUserResourceWhitelist != null) {
                    return projectUserResourceWhitelist.contains(user.getUsername());
                }
            } catch (IOException e) {
                logger.error("", e);
            }
        }

        return false;
    }
}