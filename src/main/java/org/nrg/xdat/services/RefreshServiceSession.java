/*
 * org.nrg.xdat.services.RefreshServiceSession
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
import java.sql.SQLException;

import org.apache.axis.AxisEngine;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.session.Session;
import org.apache.axis.transport.http.AxisHttpSession;
import org.apache.log4j.Logger;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XDATUser.FailedLoginException;
import org.nrg.xft.XFT;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;

public class RefreshServiceSession extends ServiceA {

    static org.apache.log4j.Logger logger = Logger.getLogger(CreateServiceSession.class);
    /**
     * @param session
     * @return
     * @throws RemoteException
     */
    public String execute(String session) throws RemoteException
    {
        MessageContext mc = AxisEngine.getCurrentMessageContext();
        mc.setMaintainSession(true);
        String _username= AxisEngine.getCurrentMessageContext().getUsername();
        String _password= AxisEngine.getCurrentMessageContext().getPassword();
        String s=null;
        try {
            Message rspmsg =mc.getRequestMessage();
            Session sess = mc.getSession();
            if (sess instanceof AxisHttpSession){
                s=((AxisHttpSession)sess).getRep().getId();
            }
            if (sess.get("user")==null){
                if (_password==null || _username==null){
                    throw new Exception("Session Timeout");
                }
                if(XFT.VERBOSE)System.out.println("New User Session:" + s);
                XDATUser user = Authenticator.Authenticate(new Authenticator.Credentials(_username,_password));
                sess.set("user", user);
                sess.set("state", "maintained");
            }
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

    /**
     * @param session
     * @return
     * @throws RemoteException
     */
    public static String Execute(String session) throws RemoteException
    {
        return (new RefreshServiceSession()).execute(session);
    }
}
