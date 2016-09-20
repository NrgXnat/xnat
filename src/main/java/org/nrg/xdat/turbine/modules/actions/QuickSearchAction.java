/*
 * core: org.nrg.xdat.turbine.modules.actions.QuickSearchAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.TableSearch;

/**
 * @author Tim
 *
 */
public class QuickSearchAction extends SecureAction {
	static Logger logger = Logger.getLogger(QuickSearchAction.class);

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.actions.SearchA#setupSearch(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(RunData data, Context context) {
        preserveVariables(data,context);
        String s = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("searchValue",data));
        if (s==null || s.equalsIgnoreCase(""))
        {
            data.setMessage("Please specify a search value.");
            data.setScreenTemplate("Index.vm");
        }else{
            s = s.toLowerCase().trim();
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
                org.nrg.xft.XFTTable table =TableSearch.Execute("SELECT ID FROM xnat_subjectData s WHERE LOWER(ID) LIKE '%" + s + "%';",TurbineUtils.getUser(data).getDBName(),TurbineUtils.getUser(data).getLogin());

                if (table.size()>0)
                {
                    if (table.size()==1)
                    {
                        DisplayItemAction dia = new DisplayItemAction();
                        String v = table.getFirstObject().toString();
                        data.getParameters().setString("search_value",v);
                        data.getParameters().setString("search_element","xnat:subjectData");
                        data.getParameters().setString("search_field","xnat:subjectData.ID");
                        dia.doPerform(data,context);
                        return;
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
                            DisplayItemAction dia = new DisplayItemAction();
                            Object v = table.getFirstObject();
                            data.getParameters().setString("search_value",v.toString());
                            data.getParameters().setString("search_element","xnat:mrSessionData");
                            data.getParameters().setString("search_field","xnat:mrSessionData.ID");
                            dia.doPerform(data,context);
                            return;
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


    public void doQuickview(RunData data,Context context)
    {

    }
}
