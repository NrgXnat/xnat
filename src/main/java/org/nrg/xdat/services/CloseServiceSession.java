//Copyright Washington University School of Medicine All Rights Reserved
/*
 * Created on Nov 29, 2006
 *
 */
package org.nrg.xdat.services;

import java.rmi.RemoteException;

import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.log4j.Logger;
import org.nrg.xdat.turbine.utils.AccessLogger;

public class CloseServiceSession {
    static org.apache.log4j.Logger logger = Logger.getLogger(CloseServiceSession.class);
    public String execute(String session) throws RemoteException
    {
        try {
            MessageContext mc = AxisEngine.getCurrentMessageContext();
            mc.getSession().invalidate();
            AccessLogger.LogServiceAccess("","","CloseServiceSession","Closing session: '" + session + "'");
        }catch (Exception e) {
            logger.error("",e);
            throw new RemoteException("",e);
        }
        
        return "CLOSED";
    }

    public static String Execute(String session) throws RemoteException
    {
        return (new CloseServiceSession()).execute(session);
    }
}
