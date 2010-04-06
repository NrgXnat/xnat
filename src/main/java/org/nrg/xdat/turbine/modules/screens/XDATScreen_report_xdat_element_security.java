//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Apr 22, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;

/**
 * @author Tim
 *
 */
public class XDATScreen_report_xdat_element_security extends AdminReport {

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
        try {
            if (item.getBooleanProperty("secure"))
            {
//                ArrayList allUsers = new ArrayList();
//                Iterator users = XdatUser.getAllXdatUsers(null,true).iterator();
//                while(users.hasNext())
//                {
//                    try {
//                        XDATUser u = new XDATUser(((XdatUser)users.next()).getItem());
//                        allUsers.add(u);
//                        
//                    } catch (Exception e) {
//                        logger.error("",e);
//                    }
//                }
//                
//                context.put("allUsers",allUsers);
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
    }

}
