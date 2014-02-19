/*
 * org.nrg.xdat.services.CloseServiceSession
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
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
