/*
 * org.nrg.xdat.turbine.modules.screens.ActionsCompleted
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;

/**
 * @author timo
 *
 */
public class ActionsCompleted extends org.nrg.xdat.turbine.modules.screens.SecureScreen {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocitySecureScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        try {
            if (TurbineUtils.HasPassedParameter("new_session", data))
            {
                String s = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("exception",data));
                if (s !=null)
                    data.setMessage(s);
                this.doRedirect(data, "Index.vm");
            }else{
                String s = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("exception",data));
                if (s !=null)
                    AdminUtils.sendErrorNotification(s, context);
            }
        } catch (RuntimeException e) {
        }
    }

}
