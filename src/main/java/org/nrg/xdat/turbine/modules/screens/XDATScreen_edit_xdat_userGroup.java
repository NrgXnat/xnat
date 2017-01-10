/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_edit_xdat_userGroup
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;

public class XDATScreen_edit_xdat_userGroup extends EditScreenA {
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_xdat_userGroup.class);
    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
     */
    public String getElementName() {
        return "xdat:userGroup";
    }
    
    public ItemI getEmptyItem(RunData data) throws Exception
    {
        String s = getElementName();
        ItemI temp =  XFTItem.NewItem(s,TurbineUtils.getUser(data));
        return temp;
    }
    
    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {

        try {
        	XdatUsergroup g = new XdatUsergroup(item);
        	if(TurbineUtils.HasPassedParameter("tag", data) && g.getTag()==null){
        		g.setTag((String)TurbineUtils.GetPassedParameter("tag", data));
        	}
        	            
            UserGroupI storedGroup=null;
            if(g.getId()!=null){
            	storedGroup =Groups.getGroup(g.getId());
            }
            
            if(storedGroup==null){
            	UserGroupI passedGroup=Groups.createGroup(TurbineUtils.GetDataParameterHash(data));
                if(TurbineUtils.HasPassedParameter("tag", data) && passedGroup.getTag()==null){
                	passedGroup.setTag((String)TurbineUtils.GetPassedParameter("tag", data));
                }
            	storedGroup=passedGroup;
            }
            
            context.put("allElements",storedGroup.getPermissionItems(TurbineUtils.getUser(data).getUsername())); 
            context.put("ug",storedGroup);
        } catch (Exception e) {
            logger.error("",e);
        }
    }
}
