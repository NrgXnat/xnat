/*
 * web: org.nrg.xnat.turbine.modules.screens.LaunchUploadApplet
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.om.XnatPvisitdata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xnat.utils.AppletConfig;
import org.nrg.xnat.utils.XnatHttpUtils;

import java.util.List;

/**
 * @author timo
 *
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class LaunchUploadApplet extends UploadAppletScreen {
	
	private static final Log _log = LogFactory.getLog(LaunchUploadApplet.class);

    @Override
	public void doBuildTemplate(RunData data, Context context) {
		context.put("jsessionid", XnatHttpUtils.getJSESSIONID(data));
		
		if(StringUtils.trimToEmpty((String)TurbineUtils.GetPassedParameter("search_field",data)).equals("xnat:subjectData.ID")) {
		    context.put("subject", StringUtils.trimToEmpty((String)TurbineUtils.GetPassedParameter("search_value",data)));
		}

        if(StringUtils.isNotBlank((String) TurbineUtils.GetPassedParameter("pvisit", data))) {
            final XnatPvisitdata visit = XnatPvisitdata.getXnatPvisitdatasById(TurbineUtils.GetPassedParameter("pvisit", data), TurbineUtils.getUser(data), false);
            if (visit != null) {
                context.put("subject", visit.getSubjectId());
                context.put("visit", visit.getId());
            }
        }

        try {
            dynamicContextExpansion(data, context);
        } catch (Exception e) {
            _log.error("",e);
        }

        org.nrg.config.entities.Configuration config = getAppletConfiguration(TurbineUtils.getUser(data), (String)context.get("project"));

		if(config != null) {
			String json = config.getContents();
	    	
	        if (json != null) {
	            try {
	            	//we have JSON, so, create applet parameters from it.
	            	AppletConfig jsonParams = getSerializer().deserializeJson(json, AppletConfig.class);

	            	if(jsonParams.getLaunch() != null){
	            		for(String key:jsonParams.getLaunch().keySet()){
	            			//put EVERYTHING in the context so your VM can use it.
	            			//remember it is all string (no booleans) so your VM has to test for string equality.
	            			context.put(key, jsonParams.getLaunch().get(key));
	            		}
	            	}
	            } catch (Exception exception) {
	                _log.error(exception);
	            }
	        }
		}
	}

    private interface ContextAction {
        void execute(RunData data, Context context);
    }

    private void dynamicContextExpansion(RunData data, Context context) throws Exception {
        List<Class<?>> classes = Reflection.getClassesForPackage("org.nrg.xnat.screens.uploadApplet.context");

        if(classes!=null && classes.size()>0){
            for(Class<?> clazz: classes){
                if(ContextAction.class.isAssignableFrom(clazz)){
                    ContextAction action=(ContextAction)clazz.newInstance();
                    action.execute(data, context);
                }
            }
        }
    }
}
