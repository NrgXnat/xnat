//Copyright 2007 Washington University School of Medicine All Rights Reserved
/*
 * Created on Jun 29, 2007
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
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

    }
}