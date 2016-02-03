/*
 * org.nrg.xnat.turbine.modules.actions.ShowViewerAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.actions.DisplaySearchAction;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.TableSearch;

public class ShowViewerAction extends SecureAction {
	static org.apache.log4j.Logger logger = Logger.getLogger(ShowViewerAction.class);

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(RunData data, Context context) throws Exception {
        if (data.getParameters().containsKey("searchValue"))
        {
            String s = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("searchValue",data));
            if (s==null || s.equalsIgnoreCase(""))
            {
                data.setMessage("Please specify a search value.");
                data.setScreenTemplate("Index.vm");
            }else{
                s = s.toLowerCase();
                if(s.indexOf("'")>-1){
                	data.setMessage("Invalid character '");
                    data.setScreenTemplate("Index.vm");
                    return;
                }
                if(s.indexOf("\\")>-1){
                	data.setMessage("Invalid character \\");
                    data.setScreenTemplate("Index.vm");
                    return;
                }
                try {
                    org.nrg.xft.XFTTable table =TableSearch.Execute("SELECT ID from xnat_subjectdata WHERE LOWER(ID) LIKE '%" + s + "%';",TurbineUtils.getUser(data).getDBName(),TurbineUtils.getUser(data).getLogin());

                    if (table.size()>0)
                    {
                        if (table.size()==1)
                        {
                            String v = table.getFirstObject().toString();
                            data.getParameters().setString("skipq","true");
                            data.getParameters().setString("id",v);
                        }else{
                            DisplaySearchAction dsa = new DisplaySearchAction();
                            data.getParameters().setString("ELEMENT_0","xnat:subjectData");
                            data.getParameters().setString("xnat:subjectData.COMBO0_FIELDS","xnat:subjectData.SUBJECTID_equals,xnat:subjectData/label_equals,xnat:subjectData/sharing/share/label_equals");
                            data.getParameters().setString("xnat:subjectData.COMBO0",s);
                            dsa.doPerform(data,context);
                            return;
                        }
                    }else{
                        table =TableSearch.Execute("SELECT ID FROM xnat_mrSessionData WHERE LOWER(ID) LIKE '%" + s + "%';",TurbineUtils.getUser(data).getDBName(),TurbineUtils.getUser(data).getLogin());
                        
                        if (table.size()>0)
                        {
                            if (table.size()==1)
                            {
                                Object v = table.getFirstObject();
                                data.getParameters().setString("search_value",v.toString());
                                data.getParameters().setString("search_element","xnat:mrSessionData");
                                data.getParameters().setString("search_field","xnat:mrSessionData.ID");
                            }else{
                                DisplaySearchAction dsa = new DisplaySearchAction();
                                data.getParameters().setString("ELEMENT_0","xnat:mrSessionData");
                                data.getParameters().setString("xnat:mrSessionData.SESSION_ID_equals",s);
                                dsa.doPerform(data,context);
                                return;
                            }
                        }else{
                            data.setMessage("No matching items found.");
                        }
                    }
                } catch (ElementNotFoundException e1) {
                    logger.error("",e1);
                    data.setMessage(e1.getMessage());
                } catch (XFTInitException e1) {
                    logger.error("",e1);
                    data.setMessage(e1.getMessage());
                } catch (FieldNotFoundException e1) {
                    logger.error("",e1);
                    data.setMessage(e1.getMessage());
                } catch (Exception e1) {
                    logger.error("",e1);
                    data.setMessage(e1.getMessage());
                }
            }
        }
        
        if (data.getParameters().containsKey("skipq")) {
			context.put("skipq", "true");
			context.put("id",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("id",data)));
			data.getParameters().remove("skipq");
		}else {
			String sessionId = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("session",data));
			context.put("sessionId",sessionId);
			if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("startDisplayWith",data))!=null && !((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("startDisplayWith",data)).equalsIgnoreCase(""))
				context.put("startDisplayWith",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("startDisplayWith",data)));
		}
	
		data.setScreenTemplate("Viewer.vm");
    }

}
