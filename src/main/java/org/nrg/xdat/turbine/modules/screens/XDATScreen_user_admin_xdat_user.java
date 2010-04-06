//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 25, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTTable;
/**
 * @author Tim
 *
 */
public class XDATScreen_user_admin_xdat_user extends AdminScreen {
	public void doBuildTemplate(RunData data, Context context)
		{
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
						context.put("roles",SecurityManager.getSecurityRoles());
						context.put("userObject",tempUser);
						List<List<Object>> permisionItems = tempUser.getPermissionItems();
						context.put("allElements",permisionItems);

                        XFTTable groups = XFTTable.Execute("SELECT id,displayname FROM xdat_usergroup", tempUser.getDBName(), null);
                        Hashtable groupHash = new Hashtable();
                        
                        groups.resetRowCursor();
                        while (groups.hasMoreRows()){
                            Hashtable row = groups.nextRowHash();
                            
                            if (row.get("id")!=null){
                                if (row.get("displayname")!=null){
                                    groupHash.put(row.get("id"), row.get("displayname"));
                                }else{
                                    groupHash.put(row.get("id"), row.get("id"));
                                }
                            }
                        }
                        context.put("allGroups", groupHash);
                        
                        XFTTable bundles = XFTTable.Execute("SELECT id,description FROM xdat_stored_search WHERE tag IS NULL;", tempUser.getDBName(), null);
                        ArrayList bundle_ids = bundles.toArrayListOfHashtables();
						context.put("allBundles",bundle_ids);
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

