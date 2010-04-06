//Copyright 2007 Washington University School of Medicine All Rights Reserved
/*
 * Created on Sep 11, 2007
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTTable;

public class XDATScreen_manageGroups_xdat_user extends AdminScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        try {
            ItemI item = TurbineUtils.GetItemBySearch(data);
            if (item == null)
            {
                data.setMessage("Invalid Search Parameters: No Data Item Found.");
                data.setScreen("Index");
                TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
            }else{
                try {
                    context.put("item",item);
                    context.put("element",org.nrg.xdat.schema.SchemaElement.GetElement(item.getXSIType()));
                    context.put("search_element",data.getParameters().getString("search_element"));
                    context.put("search_field",data.getParameters().getString("search_field"));
                    context.put("search_value",data.getParameters().getString("search_value"));

                    XDATUser tempUser = new XDATUser(item);
                    context.put("userObject",tempUser);

                    XFTTable groups = XFTTable.Execute("SELECT id,displayname,tag FROM xdat_usergroup ORDER BY tag", tempUser.getDBName(), null);
                    Hashtable groupHash = new Hashtable();
                    Hashtable<Object,Hashtable<Object,Object>> projectGroups = new Hashtable<Object,Hashtable<Object,Object>>();
                    
                    groups.resetRowCursor();
                    while (groups.hasMoreRows()){
                        Hashtable row = groups.nextRowHash();
                        
                        if (row.get("id")!=null){
                            Object id = row.get("id");
                            Object displayname = row.get("displayname");
                            if (row.get("displayname")!=null){
                                displayname=id;
                            }
                            Object tag = row.get("tag");
                            if (tag!=null){
                                Hashtable<Object,Object> projects = projectGroups.get(tag);
                                if (projects==null){
                                    projects = new Hashtable<Object,Object>();
                                }
                                projects.put(id, displayname);
                            }else{
                                groupHash.put(id, displayname);
                            }
                        }
                    }
                    context.put("allGroups", groupHash);
                    context.put("projectGroups", projectGroups);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    data.setMessage("Invalid Search Parameters: No Data Item Found.");
                    data.setScreen("Index");
                    TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            data.setMessage("Invalid Search Parameters: No Data Item Found.");
            data.setScreen("Index");
            TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
        }
    }

}
