package org.nrg.xnat.restlet.projectResource.extensions;

import java.util.Hashtable;

import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xft.XFTTable;
import org.nrg.xnat.restlet.resources.ProjectResource;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.resources.SecureResource.FilteredResourceHandlerI;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

@SuppressWarnings("unused")
public class ProjectPermissionsFilter implements FilteredResourceHandlerI{

	@Override
	public boolean canHandle(SecureResource resource) {
		return resource.isQueryVariableTrue("creatableTypes");
	}

	@Override
	public Representation handle(SecureResource resource, Variant variant) throws Exception {
		ProjectResource projResource=(ProjectResource)resource;
		StringBuilder builder=new StringBuilder();
        if(Groups.isMember(resource.user,"ALL_DATA_ADMIN")){
        	 builder.append(String.format("SELECT DISTINCT element_name FROM xdat_element_access xea JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id=xfms.permissions_allow_set_xdat_elem_xdat_element_access_id JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id WHERE create_element=1 AND field_value='%1s' and field !=''", projResource.getProjectId()));
        }else{
        	 builder.append(String.format("SELECT DISTINCT element_name FROM xdat_user_groupID map JOIN xdat_userGroup gp ON map.groupid=gp.id JOIN xdat_element_access xea ON gp.xdat_usergroup_id=xea.xdat_usergroup_xdat_usergroup_id JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id=xfms.permissions_allow_set_xdat_elem_xdat_element_access_id JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id WHERE map.groups_groupid_xdat_user_xdat_user_id=%1s  AND create_element=1 AND field_value='%2s' and field !=''",resource.user.getID(),projResource.getProjectId()));
        }
   
        return resource.representTable(XFTTable.Execute(builder.toString(), resource.user.getDBName(), resource.userName), resource.overrideVariant(variant), new Hashtable<String,Object>()) ;
	}

}
