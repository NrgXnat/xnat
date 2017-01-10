/*
 * web: org.nrg.xnat.restlet.projectsList.extensions.EditableProjects
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.projectsList.extensions;

import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xft.XFTTable;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.resources.SecureResource.FilteredResourceHandlerI;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.Hashtable;

@SuppressWarnings("unused")
public class EditableProjects implements FilteredResourceHandlerI {

    @Override
    public boolean canHandle(SecureResource resource) {
        return resource.isQueryVariableTrue("creatableTypes");
    }

    @Override
    public Representation handle(SecureResource resource, Variant variant) throws Exception {
        final StringBuilder builder = new StringBuilder();
        final UserI         user    = resource.getUser();
        if (Groups.isMember(user, "ALL_DATA_ADMIN")) {
            builder.append("SELECT proj.ID, proj.name, proj.description,proj.secondary_id FROM xnat_projectData proj;");
        } else {
            builder.append(String.format("SELECT DISTINCT proj.ID, proj.name, proj.description,proj.secondary_id FROM xdat_user_groupID map JOIN xdat_userGroup gp ON map.groupid=gp.id JOIN xdat_element_access xea ON gp.xdat_usergroup_id=xea.xdat_usergroup_xdat_usergroup_id JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id=xfms.permissions_allow_set_xdat_elem_xdat_element_access_id JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND create_element=1 AND field_value!='*' AND field_value!='' and field !='' JOIN xnat_projectData proj ON field_value=proj.ID WHERE map.groups_groupid_xdat_user_xdat_user_id=%s", user.getID()));
            if (resource.hasQueryVariable("data-type")) {
                GenericWrapperElement gwe = GenericWrapperElement.GetElement(resource.getQueryVariable("data-type"));
                if (gwe != null) {
                    builder.append(" AND xea.element_name='").append(gwe.getXSIType()).append("' ");
                }
            }
        }

        return resource.representTable(XFTTable.Execute(builder.toString(), user.getDBName(), resource.userName), resource.overrideVariant(variant), new Hashtable<String, Object>());
    }

}
