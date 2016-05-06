/*
 * org.nrg.xnat.turbine.modules.actions.XDATForgotLogin
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

public class XDATForgotLogin extends org.nrg.xdat.turbine.modules.actions.XDATForgotLogin {


    public void additionalProcessing(RunData data, Context context,UserI user) throws Exception{
		String par = (String)TurbineUtils.GetPassedParameter("par",data);

        if(!StringUtils.isEmpty(par)){
         	AcceptProjectAccess action = new AcceptProjectAccess();
         	context.put("user", user);
         	action.doPerform(data, context);
        }

    }
}
