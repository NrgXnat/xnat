//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jun 23, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import java.util.ArrayList;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.search.ItemSearch;

/**
 * @author Tim
 *
 */
public class XDATScreen_email extends SecureScreen {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        ArrayList al = ItemSearch.GetAllItems("xdat:user",null,false).items();
        context.put("users",al);
        
        String s= (String) TurbineUtils.GetPassedParameter("emailTo",data,"");
        context.put("emailTo",s);
    }

}
