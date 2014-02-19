/*
 * org.nrg.xdat.turbine.modules.actions.ModifyStoredSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/9/13 1:06 PM
 */


package org.nrg.xdat.turbine.modules.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatSearchField;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.design.SchemaElementI;

public class ModifyStoredSearch extends ModifyItem {

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.actions.ModifyItem#postProcessing(org.nrg.xft.XFTItem, org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    public void postProcessing(XFTItem item, RunData data, Context context) throws Exception {
        SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
                
        XdatStoredSearch xss = new XdatStoredSearch(item);
        //XdatStoredSearch.ReplacePreLoadedSearch(xss);
        
        TurbineUtils.getUser(data).replacePreLoadedSearch(xss);
        
        //UserGroupManager.ReplacePreLoadedSearch(xss);
        
        if (TurbineUtils.HasPassedParameter("destination", data)){
            this.redirectToReportScreen((String)TurbineUtils.GetPassedParameter("destination", data), item, data);
        }else{
            this.redirectToReportScreen(DisplayItemAction.GetReportScreen(se), item, data);
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.actions.ModifyItem#preSave(org.nrg.xft.XFTItem, org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    public void preSave(XFTItem item, RunData data, Context context) throws Exception {
        XdatStoredSearch xss = new XdatStoredSearch(item);
        ArrayList al = xss.getSearchField();
        Iterator iter = al.iterator();
        while(iter.hasNext()){
            XdatSearchField sf = (XdatSearchField)iter.next();
            if (sf.getValue()!=null){
                String value = sf.getValue();
                String f = sf.getFieldId();
                if (f.indexOf(value)==-1){
                    sf.setFieldId(f +"=" +value);
                }
            }
        }
    }

}
