/*
 * web: org.nrg.xnat.turbine.modules.actions.ConfirmRegister
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

import java.util.List;

public class ConfirmRegister extends SecureAction {

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        try {
            UserI newUser=Users.createUser(TurbineUtils.GetDataParameterHash(data));
            
            UserI current=Users.getUser(newUser.getLogin());
            
    		String nextPage = (String)TurbineUtils.GetPassedParameter("nextPage",data);
    		String nextAction = (String)TurbineUtils.GetPassedParameter("nextAction",data);
    		String par = (String)TurbineUtils.GetPassedParameter("par",data);

            if(!StringUtils.isEmpty(par)){
            	context.put("par", par);
            }
		    if (!StringUtils.isEmpty(nextAction) && !nextAction.contains("XDATLoginUser") && !nextAction.equals(org.apache.turbine.Turbine.getConfiguration().getString("action.login"))){
            	context.put("nextAction", nextAction);
		    }else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(org.apache.turbine.Turbine.getConfiguration().getString("template.home")) ) {
            	context.put("nextPage", nextPage);
			}
            

	        SecureScreen.loadAdditionalVariables(data, context);
            
            if (current==null)
            {
            	//allowed to have multiple accounts with the admin email address
            	if(!StringUtils.equals(newUser.getEmail(), XDAT.getSiteConfigPreferences().getAdminEmail()))
            	{
            		List<? extends UserI> match=Users.getUsersByEmail(newUser.getEmail());
            		if(match.size()>0){
            			current=match.get(0);
            		}
            	}
            	
                if (current==null)
                {
                    context.put("newUser", newUser);
                    data.setScreenTemplate("ConfirmRegistration.vm");
                }else{
                    // OLD USER
                    data.setMessage("Email (" + newUser.getEmail() + ") already exists.");
                    data.setScreenTemplate("ForgotLogin.vm");
                }
            }else{
                // OLD USER
                data.setMessage("Username (" + newUser.getEmail() + ") already exists.");
                data.setScreenTemplate("ForgotLogin.vm");
            }
        } catch (Exception ignored) {
        }
    }

    protected boolean isAuthorized( RunData data )  throws Exception
    {
        return true;
    }
}
