/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_active_sessions
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import java.util.Collection;
import java.util.Date;

import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.session.SessionService;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

public class XDATScreen_active_sessions extends AdminScreen {

    @Override
    protected void doBuildTemplate(PipelineData pipelineData, Context context) throws Exception {
        RunData data = pipelineData.getRunData();
        try {
            Collection col = ((SessionService) TurbineServices.getInstance().getService(SessionService.SERVICE_NAME)).getActiveSessions();
            context.put("sessions", col);
            context.put("dateUtil",new LongDateUtil());
        } catch (Throwable e) {
            logger.error("",e);
            String msg = "To enable session tracking, add the following lines:<br><br>";
            msg +="<b>WEB-INF/conf/TurbineResources.properties</b><br>";
            msg +="services.SessionService.classname=org.apache.turbine.services.session.TurbineSessionService<br>";
            msg +="services.SessionService.earlyInit=true<br><br>";
            
            msg +="<b>WEB-INF/web.xml</b><br>";
            msg +="<listener><br>";
            msg +="<listener-class>org.apache.turbine.services.session.SessionListener</listener-class><br>";
            msg +="</listener>";
            context.put("msg", msg);
        }
    }

    public class LongDateUtil {
        
        public String formatDate(long d, String pattern){
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat (pattern);
            return formatter.format(new Date(d));
        }
    }
}
