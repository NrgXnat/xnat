/*
 * web: org.nrg.xnat.turbine.modules.screens.Index
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.util.Date;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;


import org.nrg.xnat.turbine.utils.ProjectAccessRequest;

public class Index extends SecureScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        String themedRedirect = themeService.getThemePage("Landing");           // TODO: put all this in a method in the theme service with an optional requested page parameter
        if(themedRedirect != null) {
            context.put("themedRedirect", themedRedirect);
            return;
        }

        UserI user = TurbineUtils.getUser(data);
        
        if(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("node",data))!=null){
        	context.put("node", ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("node",data)));
        }
        
        ProjectAccessRequest.CreatePARTable();
        
        if (user.getEmail()==null)
        {
        	data.setMessage("WARNING: A valid email account is required for many features.  Please use the (edit) link at the top of the page to add a valid email address to your user account.");
        }else{
            Integer parcount=(Integer)PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(par_id)::int4 AS count FROM xs_par_table WHERE approval_date IS NULL AND LOWER(email)='"+ user.getEmail().toLowerCase() + "'", "count", user.getDBName(), user.getLogin());
            context.put("par_count", parcount);
        }
        
        Date lastLogin = UserHelper.getUserHelperService(user).getPreviousLogin();
        if (lastLogin!=null)
        {
            context.put("last_login",lastLogin);
        }
        
        context.put("proj_count", UserHelper.getUserHelperService(user).getTotalCounts().get("xnat:projectData"));
		
		context.put("sub_count", UserHelper.getUserHelperService(user).getTotalCounts().get("xnat:subjectData"));
		
		Long isd_count=(Long)PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM xnat_imageSessionData", "count", TurbineUtils.getUser(data).getDBName(), TurbineUtils.getUser(data).getUsername());
		context.put("isd_count", isd_count);
		
		//count prearc entries
		try {
			context.put("prearc_count",PrearcDatabase.buildRows(user, null).size());
		} catch (Throwable e) {
			logger.error("",e);
		}
    }
}
