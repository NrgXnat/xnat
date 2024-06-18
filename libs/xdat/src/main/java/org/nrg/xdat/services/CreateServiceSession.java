/*
 * core: org.nrg.xdat.services.CreateServiceSession
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.services;

import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.axis.session.Session;
import org.apache.axis.transport.http.AxisHttpSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

import java.rmi.RemoteException;
import java.sql.SQLException;

/**
 * @author timo
 *
 */
public class CreateServiceSession  extends ServiceA{
	static org.apache.log4j.Logger logger = Logger.getLogger(CreateServiceSession.class);
    public String execute() throws RemoteException
    {
        final MessageContext messageContext = AxisEngine.getCurrentMessageContext();
        final String _username= messageContext.getUsername();
        final String _password= messageContext.getPassword();
        
        try {
            UserI user = Authenticator.Authenticate(new Authenticator.Credentials(_username,_password));
            Session session = messageContext.getSession();
            session.set("user", user);
            session.set("state", "maintained");
            messageContext.setMaintainSession(true);
            final String sessionId;
            if (session instanceof AxisHttpSession){
                sessionId = StringUtils.defaultIfBlank(((AxisHttpSession) session).getRep().getId(), "open");
            } else {
                sessionId = "open";
            }
            AccessLogger.LogServiceAccess(_username, messageContext, "CreateServiceSession", "Created session: '" + sessionId + "'");
            return sessionId;
        } catch (XFTInitException e) {
            logger.error("",e);
            throw new RemoteException("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            throw new RemoteException("",e);
        } catch (DBPoolException e) {
            logger.error("",e);
            throw new RemoteException("",e);
        } catch (SQLException e) {
            logger.error("",e);
            throw new RemoteException("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
            throw new RemoteException("",e);
        } catch (FailedLoginException e) {
            logger.error("",e);
            throw new RemoteException("",e);
        } catch (Exception e) {
            logger.error("",e);
            throw new RemoteException("",e);
        }
    }

    public static String Execute() throws RemoteException {
        return (new CreateServiceSession()).execute();
    }
}
