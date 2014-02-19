/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_report_xdat_element_security
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
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
