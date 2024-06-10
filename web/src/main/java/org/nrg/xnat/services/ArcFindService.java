/*
 * web: org.nrg.xnat.services.ArcFindService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

@Slf4j
public class ArcFindService {
    public Object[] execute(String _field,String _comparison,Object _value,String projectId) throws RemoteException
    {
        final MessageContext messageContext = AxisEngine.getCurrentMessageContext();
        String               _username             = messageContext.getUsername();
        String               _password             = messageContext.getPassword();
        AccessLogger.LogServiceAccess(_username, messageContext, "ArcFindService", _value.toString());
        try {
            UserI user = Authenticator.Authenticate(new Authenticator.Credentials(_username,_password));
            if (user == null)
            {
                throw new Exception("Invalid User: "+_username);
            }
            return execute(user,_field,_comparison,_value,projectId);
        } catch (RemoteException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (XFTInitException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (ElementNotFoundException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (DBPoolException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (SQLException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (FieldNotFoundException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (FailedLoginException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (Exception e) {
            log.error("",e);
            throw new RemoteException("",e);
        }
    }

    public Object[] execute(String sessionId, String _field, String _comparison, Object _value, String projectId) throws RemoteException
    {
        final MessageContext messageContext = AxisEngine.getCurrentMessageContext();
        final UserI          user           = (UserI) messageContext.getSession().get("user");
        if (user == null) {
            throw new RemoteException("Invalid Session: " + sessionId);
        }
        AccessLogger.LogServiceAccess(user.getUsername(), messageContext, "ArcFindService", _value.toString());
        try {
            return execute(user,_field,_comparison,_value,projectId);
        } catch (RemoteException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (XFTInitException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (ElementNotFoundException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (DBPoolException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (SQLException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (FieldNotFoundException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (FailedLoginException e) {
            log.error("",e);
            throw new RemoteException("",e);
        } catch (Exception e) {
            log.error("",e);
            throw new RemoteException("",e);
        }
    }
    
    public Object[] execute(UserI user, String _field,String _comparison,Object _value,String projectId) throws Exception
    {
        boolean preLoad =true;
        
        XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);
        
        if (_field.startsWith("xnat:projectData") || _field.startsWith("xnat:Project") || _field.startsWith("Project"))
        {
            preLoad=false;
        }
        ItemCollection items = ItemSearch.GetItems(_field, _comparison, _value, user, preLoad);
        
        ArrayList<String> url = new ArrayList<>();
        ArrayList<String> relative = new ArrayList<>();
        ArrayList<Long> size = new ArrayList<>();
        
        Iterator iter = items.iterator();
        while (iter.hasNext()){
            XFTItem item = (XFTItem)iter.next();
            Hashtable<String,XFTItem> hash = item.getChildrenOfTypeWithPaths("xnat:abstractResource");
            
            for (String key : hash.keySet()){
                XFTItem resource = hash.get(key);
                ItemI om = BaseElement.GetGeneratedItem(resource);
                if (om instanceof XnatAbstractresource){
                    XnatAbstractresource resourceA = (XnatAbstractresource)om;
                    ArrayList<File> files = resourceA.getCorrespondingFiles(project.getRootArchivePath());
                    for (int i=0;i<files.size();i++){
                        File f = files.get(i);
                        String xPath= item.getXSIType() + "[" + item.getPKString() + "]/" + key;
                        xPath = xPath.replace('/', '.');
                        
                        url.add("project/" + projectId + "/xmlpath/" + xPath + "/file/" + i);
                        
                        String path = f.getAbsolutePath();
                        if (path.contains(File.separator + projectId)){
                            path = path.substring(path.indexOf(File.separator + projectId) + 1);
                        }
                        relative.add(path);
                        
                        size.add(f.length());
                    }
                    
                }
            }
        }
        
        return new Object[]{url,relative,size};
    }
    
    public static Object[] Execute(String _field,String _comparison,Object _value,String projectId) throws java.rmi.RemoteException 
    {
        return (new ArcFindService()).execute(_field,_comparison,_value,projectId);
    }
    
    public static Object[] Execute(String _session,String _field,String _comparison,Object _value,String projectId) throws java.rmi.RemoteException 
    {
        return (new ArcFindService()).execute(_session,_field,_comparison,_value,projectId);
    }
}
