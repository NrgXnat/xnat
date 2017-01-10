/*
 * web: org.nrg.xnat.services.ArcFindService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.axis.AxisEngine;
import org.apache.log4j.Logger;
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

public class ArcFindService {
    static org.apache.log4j.Logger logger = Logger.getLogger(ArcFindService.class);
    public Object[] execute(String _field,String _comparison,Object _value,String projectId) throws RemoteException
    {
        String _username= AxisEngine.getCurrentMessageContext().getUsername();
        String _password= AxisEngine.getCurrentMessageContext().getPassword();
        AccessLogger.LogServiceAccess(_username,"","ArcFindService",_value.toString());
        try {
            UserI user = Authenticator.Authenticate(new Authenticator.Credentials(_username,_password));
            if (user == null)
            {
                throw new Exception("Invalid User: "+_username);
            }
            return execute(user,_field,_comparison,_value,projectId);
        } catch (RemoteException e) {
            logger.error("",e);
            throw new RemoteException("",e);
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

    public Object[] execute(String session_id,String _field,String _comparison,Object _value,String projectId) throws RemoteException
    {
        AccessLogger.LogServiceAccess(session_id,"","ArcFindService",_value.toString());
        try {
            UserI user = (UserI)AxisEngine.getCurrentMessageContext().getSession().get("user");
            if (user == null)
            {
                throw new Exception("Invalid Session: "+session_id);
            }
            return execute(user,_field,_comparison,_value,projectId);
        } catch (RemoteException e) {
            logger.error("",e);
            throw new RemoteException("",e);
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
    
    public Object[] execute(UserI user, String _field,String _comparison,Object _value,String projectId) throws Exception
    {
        boolean preLoad =true;
        
        XnatProjectdata project = (XnatProjectdata)XnatProjectdata.getXnatProjectdatasById(projectId, user, false);
        
        if (_field.startsWith("xnat:projectData") || _field.startsWith("xnat:Project") || _field.startsWith("Project"))
        {
            preLoad=false;
        }
        ItemCollection items = ItemSearch.GetItems(_field, _comparison, _value, user, preLoad);
        
        ArrayList<String> url = new ArrayList<String>();
        ArrayList<String> relative = new ArrayList<String>();
        ArrayList<Long> size = new ArrayList<Long>();
        
        Iterator iter = items.iterator();
        while (iter.hasNext()){
            XFTItem item = (XFTItem)iter.next();
            Hashtable<String,XFTItem> hash = item.getChildrenOfTypeWithPaths("xnat:abstractResource");
            
            for (String key : hash.keySet()){
                XFTItem resource = hash.get(key);
                ItemI om = BaseElement.GetGeneratedItem((XFTItem)resource);
                if (om instanceof XnatAbstractresource){
                    XnatAbstractresource resourceA = (XnatAbstractresource)om;
                    ArrayList<File> files = resourceA.getCorrespondingFiles(project.getRootArchivePath());
                    for (int i=0;i<files.size();i++){
                        File f = files.get(i);
                        String xPath= item.getXSIType() + "[" + item.getPKString() + "]/" + key;
                        xPath = xPath.replace('/', '.');
                        
                        url.add("project/" + projectId + "/xmlpath/" + xPath + "/file/" + i);
                        
                        String path = f.getAbsolutePath();
                        if (path.indexOf(File.separator + projectId)!=-1){
                            path = path.substring(path.indexOf(File.separator + projectId) + 1);
                        }
                        relative.add(path);
                        
                        size.add(new Long(f.length()));
                    }
                    
                }
            }
        }
        
        Object[] al = new Object[]{url,relative,size};
        
        return al;
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
