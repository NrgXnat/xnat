//Copyright Washington University School of Medicine All Rights Reserved
/*
 * Created on Nov 29, 2006
 *
 */
package org.nrg.xdat.services;

import java.rmi.RemoteException;
import java.sql.SQLException;

import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.log4j.Logger;
import org.nrg.xdat.security.UserCache;
import org.nrg.xdat.security.XDATUser.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;

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
