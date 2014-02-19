/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_edit_xdat_userGroup
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import java.util.List;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatUsergroup;
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
            List<List<Object>> permisionItems = g.getPermissionItems(TurbineUtils.getUser(data).getUsername());
            context.put("allElements",permisionItems);
        } catch (Exception e) {
            logger.error("",e);
        }
    }
}