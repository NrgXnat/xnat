/*
 * org.nrg.xnat.turbine.modules.screens.UploadApplet
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.AppletConfig;
import org.nrg.xnat.utils.XnatHttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class UploadApplet extends UploadAppletScreen {
	private static final Logger logger = LoggerFactory.getLogger(UploadApplet.class);
	
	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		context.put("jsessionid", XnatHttpUtils.getJSESSIONID(data));
        storeParameterIfPresent(data, context, "project");
        storeParameterIfPresent(data, context, "subject_id", "part", "part_id");
        storeParameterIfPresent(data, context, "subject_label");
        storeParameterIfPresent(data, context, "session_id", "expt_id");
        storeParameterIfPresent(data, context, "visit_id");
        storeParameterIfPresent(data, context, "visit");
        storeParameterIfPresent(data, context, "protocol");
        storeParameterIfPresent(data, context, "expectedModality");
        storeParameterIfPresent(data, context, "scan_type");
        if (TurbineUtils.HasPassedParameter("session_date", data)) {
        	//build up the full date/time expected by the upload applet. If the upload applet receives a date/time in the format
        	// mm/dd/yyyy HH:MM it will verify the scan time is within 61 minutes of HH:MM (24 hour time). If it does not receive
        	// HH:MM or if HH:MM == 00:00 the applet will only verify the scan is on the same day as mm/dd/yyyy. If it receives no
        	// session_date, it will prompt the user for one.  With that said, we want to build up the mm/dd/yyyy string if we can.
        	// we'll do it here.
        	String time = " 00:00";
        	if (TurbineUtils.HasPassedParameter("session_time_h", data) && TurbineUtils.HasPassedParameter("session_time_m", data)) {
        		// parameters are set with drop-downs so no need to validate here.
        		String hr = (String)TurbineUtils.GetPassedParameter("session_time_h", data);
        		String mm =(String)TurbineUtils.GetPassedParameter("session_time_m", data);
        		try{
        			//poor man's validation
        			time =  Integer.parseInt(hr) + ":" + Integer.parseInt(mm);
        		} catch (Exception e){
        			//if one or both aren't an integer, we'll get here, which is fine. it just means they didn't select a time. 
        		}
        	}
            context.put("session_date", ((String)TurbineUtils.GetPassedParameter("session_date", data)).replace('.', '/') + " " + time);
        } else if (TurbineUtils.HasPassedParameter("no_session_date", data)) {
            context.put("session_date", "no_session_date");
        }
		context.put("arc", ArcSpecManager.GetInstance());
		
        org.nrg.config.entities.Configuration config = getAppletConfiguration(TurbineUtils.getUser(data), (String)context.get("project"));
		
		if(config != null) {
			String json = config.getContents();
	    	
	        if (json != null) {
	            try {
	            	//we have JSON, so, create applet parameters from it.
	            	AppletConfig jsonParams = getSerializer().deserializeJson(json, AppletConfig.class);
	            	StringBuilder sb = new StringBuilder();
	            	if(jsonParams.getParameters() != null) {
	            		for(String key:jsonParams.getParameters().keySet()) {
	            			sb.append("parameters['").append(key).append("'] = '").append(jsonParams.getParameters().get(key)).append("';\n");
	            		}
	            	}
	
	            	context.put("appletParams", sb.toString());
	            } catch (Exception exception) {
	                logger.error("Error processing applet parameters", exception);
	            }
	        }
		}
	}
}
