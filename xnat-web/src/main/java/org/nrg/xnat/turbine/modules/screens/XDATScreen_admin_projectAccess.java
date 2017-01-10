/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_admin_projectAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.AdminScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;

public class XDATScreen_admin_projectAccess extends AdminScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        UserI user = TurbineUtils.getUser(data);
        String query = "SELECT proj.id, CASE WHEN PUB_PROJS.read_element=1 THEN 'public' WHEN PRIV_PROJS.read_element=1 THEN 'protected'  WHEN PRIV_PROJS.read_element=0 THEN 'private' ELSE 'NULL' END AS accessibility" +
                " FROM xnat_projectData proj "+
                " LEFT JOIN xnat_projectData_meta_data meta ON proj.projectData_info=meta.meta_data_id"+
                " LEFT JOIN ( "+
                " SELECT DISTINCT ON (field_value) read_element,field_value  "+
                " FROM xdat_element_access ea  "+
                " LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id=fms.permissions_allow_set_xdat_elem_xdat_element_access_id  "+
                " LEFT JOIN xdat_user u ON ea.xdat_user_xdat_user_id=u.xdat_user_id  "+
                " LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id=fm.xdat_field_mapping_set_xdat_field_mapping_set_id  "+
                " WHERE login='guest' AND element_name='xnat:projectData') PRIV_PROJS ON proj.id=PRIV_PROJS.field_value "+
                " LEFT JOIN ( "+
                " SELECT DISTINCT ON (field_value) read_element,field_value  "+
                " FROM xdat_element_access ea  "+
                " LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id=fms.permissions_allow_set_xdat_elem_xdat_element_access_id  "+
                " LEFT JOIN xdat_user u ON ea.xdat_user_xdat_user_id=u.xdat_user_id  "+
                " LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id=fm.xdat_field_mapping_set_xdat_field_mapping_set_id  "+
                " WHERE login='guest' AND element_name!='xnat:projectData') PUB_PROJS ON proj.id=PUB_PROJS.field_value";
        XFTTable t = XFTTable.Execute(query, user.getDBName(), user.getLogin());
        
        context.put("projectAccessibility", t.toHashtable("id","accessibility"));
        
        query = "SELECT id, name FROM xnat_projectData ORDER BY name;";
        t = XFTTable.Execute(query, user.getDBName(), user.getLogin());
        
        context.put("projects", t.toHashtable("id", "name"));
    }

}
