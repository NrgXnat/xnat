//Copyright 2006 Harvard University / Washington University School of Medicine All Rights Reserved
/*
 * Created on Sep 20, 2006
 *
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
public class Error extends org.nrg.xdat.turbine.modules.screens.SecureScreen {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocitySecureScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        try {
            if (TurbineUtils.HasPassedParameter("new_session", data))
            {
                String s = data.getParameters().get("exception");
                if (s !=null)
                    data.setMessage(s);
                this.doRedirect(data, "Index.vm");
            }else{
                String s = data.getParameters().get("exception");
                if (s !=null)
                    AdminUtils.sendErrorEmail(data, s);
            }
        } catch (RuntimeException e) {
        }
    }

}
