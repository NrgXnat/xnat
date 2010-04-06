//Copyright 2007 Washington University School of Medicine All Rights Reserved
/*
 * Created on Mar 20, 2008
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import java.util.Collection;
import java.util.Date;

import org.apache.turbine.services.session.TurbineSession;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

public class XDATScreen_active_sessions extends AdminScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        try {
            Collection col = TurbineSession.getActiveSessions();
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
