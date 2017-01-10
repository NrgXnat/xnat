/*
 * web: org.nrg.xnat.restlet.projectResource.extensions.ProjectPermissionsFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.projectResource.extensions;

import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.resources.ProjectResource;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.resources.SecureResource.FilteredResourceHandlerI;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.Hashtable;

@SuppressWarnings("unused")
public class ProjectPermissionsFilter implements FilteredResourceHandlerI {
    @Override
    public boolean canHandle(SecureResource resource) {
        return resource.isQueryVariableTrue("creatableTypes");
    }

    @Override
    public Representation handle(SecureResource resource, Variant variant) throws Exception {
        final ProjectResource projResource = (ProjectResource) resource;
        final StringBuilder   builder      = new StringBuilder();
        final UserI           user         = resource.getUser();

        if (Groups.isMember(user, "ALL_DATA_ADMIN")) {
            builder.append(String.format("SELECT DISTINCT element_name FROM xdat_element_access xea JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id=xfms.permissions_allow_set_xdat_elem_xdat_element_access_id JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id WHERE create_element=1 AND field_value='%1$s' and field !=''", projResource.getProjectId()));
        } else {
            builder.append(String.format("SELECT DISTINCT element_name FROM xdat_user_groupID map JOIN xdat_userGroup gp ON map.groupid=gp.id JOIN xdat_element_access xea ON gp.xdat_usergroup_id=xea.xdat_usergroup_xdat_usergroup_id JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id=xfms.permissions_allow_set_xdat_elem_xdat_element_access_id JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id WHERE map.groups_groupid_xdat_user_xdat_user_id=%1$s  AND create_element=1 AND field_value='%2$s' and field !=''", user.getID(), projResource.getProjectId()));
        }

        return resource.representTable(XFTTable.Execute(builder.toString(), user.getDBName(), resource.userName), resource.overrideVariant(variant), new Hashtable<String, Object>());
    }
}
