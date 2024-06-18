/*
 * core: org.nrg.xdat.services.CloseServiceSession
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services;

import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.log4j.Logger;
import org.nrg.xdat.turbine.utils.AccessLogger;

import java.rmi.RemoteException;

public class CloseServiceSession {
    static org.apache.log4j.Logger logger = Logger.getLogger(CloseServiceSession.class);

    public String execute(String session) throws RemoteException {
        try {
            final MessageContext messageContext = AxisEngine.getCurrentMessageContext();
            final String         username       = messageContext.getUsername();
            messageContext.getSession().invalidate();
            AccessLogger.LogServiceAccess(username, messageContext, "CloseServiceSession", "Closing session: '" + session + "'");
        } catch (Exception e) {
            logger.error("", e);
            throw new RemoteException("", e);
        }

        return "CLOSED";
    }

    public static String Execute(String session) throws RemoteException {
        return (new CloseServiceSession()).execute(session);
    }
}
