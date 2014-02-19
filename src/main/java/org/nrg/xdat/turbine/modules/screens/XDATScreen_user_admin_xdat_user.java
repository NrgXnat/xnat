/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_user_admin_xdat_user
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
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
						context.put("search_element",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data)));
						context.put("search_field",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data)));
						context.put("search_value",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data)));

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

