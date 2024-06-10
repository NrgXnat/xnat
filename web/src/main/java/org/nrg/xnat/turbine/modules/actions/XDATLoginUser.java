/*
 * web: org.nrg.xnat.turbine.modules.actions.XDATLoginUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

public class XDATLoginUser extends
		org.nrg.xdat.turbine.modules.actions.XDATLoginUser {

	
	public void doRedirect(RunData data, Context context,UserI user) throws Exception{
		String nextPage = (String)TurbineUtils.GetPassedParameter("nextPage",data);
		String nextAction = (String)TurbineUtils.GetPassedParameter("nextAction",data);
		String par = (String)TurbineUtils.GetPassedParameter("par",data);
		String rest = (String)TurbineUtils.GetPassedParameter("rest_uri",data);
		/*
		 * If the setPage("template.vm") method has not
		 * been used in the template to authenticate the
		 * user (usually Login.vm), then the user will
		 * be forwarded to the template that is specified
		 * by the "template.home" property as listed in
		 * TR.props for the webapp.
		 */
		 if (!StringUtils.isEmpty(nextAction) && nextAction.indexOf("XDATLoginUser")==-1 && !nextAction.equals(org.apache.turbine.Turbine.getConfiguration().getString("action.login"))){
			data.setAction(nextAction);
            VelocityAction action = (VelocityAction) ActionLoader.getInstance().getInstance(nextAction);
            action.doPerform(data, context);
		 }else if (!StringUtils.isEmpty(par)){
	         	AcceptProjectAccess action = new AcceptProjectAccess();
	         	context.put("user", user);
	         	action.doPerform(data, context);
		 }else if (!StringUtils.isEmpty(rest)){
	         	data.setRedirectURI(rest);
	         	data.setStatusCode(302);
	         	return;
		 }else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(org.apache.turbine.Turbine.getConfiguration().getString("template.home")) ) {
			data.setScreenTemplate(nextPage);
		 }

         if (data.getScreenTemplate().indexOf("Error.vm")!=-1)
         {
             data.setMessage("<b>Previous session expired.</b><br>If you have bookmarked this page, please redirect your bookmark to: " + TurbineUtils.GetFullServerPath());
             data.setScreenTemplate("Index.vm");
         }
	}
}
