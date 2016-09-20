/*
 * core: org.nrg.xdat.services.CreateServiceSession
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.services;

import java.rmi.RemoteException;
import java.sql.SQLException;

import org.apache.axis.AxisEngine;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.session.Session;
import org.apache.axis.transport.http.AxisHttpSession;
import org.apache.log4j.Logger;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

/**
 * @author timo
 *
 */
public class CreateServiceSession  extends ServiceA{
	static org.apache.log4j.Logger logger = Logger.getLogger(CreateServiceSession.class);
    public String execute() throws RemoteException
    {
        MessageContext mc = AxisEngine.getCurrentMessageContext();
        String _username= mc.getUsername();
        String _password= mc.getPassword();
        
        String s=null;
        try {
            Message rspmsg =mc.getResponseMessage();
            UserI user = Authenticator.Authenticate(new Authenticator.Credentials(_username,_password));
            Session session = mc.getSession();
            session.set("user", user);
            session.set("state", "maintained");
            if (session instanceof AxisHttpSession){
                s=((AxisHttpSession)session).getRep().getId();
            }
            mc.setMaintainSession(true);
            if (s==null){
                s="open";
            }
            AccessLogger.LogServiceAccess(_username,"","CreateServiceSession","Created session: '" + s + "'");
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
        
        return s;
    }

    public static String Execute() throws RemoteException
    {
        return (new CreateServiceSession()).execute();
    }
}
