//Copyright 2007 Washington University School of Medicine All Rights Reserved
/*
 * Created on Jun 29, 2007
 *
 */
package org.nrg.xdat.security;

import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xft.ItemI;
import org.nrg.xft.cache.CacheManager;
import org.nrg.xft.event.Event;
import org.nrg.xft.event.EventManager;

public class UserGroupManager{
    static Logger logger = Logger.getLogger(UserGroupManager.class);
    
	
    public static UserGroup GetGroup(String id){
    	//reintroduce caching as on-demand 11/09 TO
    	UserGroup g =(UserGroup) CacheManager.GetInstance().retrieve(XdatUsergroup.SCHEMA_ELEMENT_NAME, id);
    	if(g==null){
    		try {
                XdatUsergroup temp =(XdatUsergroup) XdatUsergroup.getXdatUsergroupsById(id, null, true);
                if(temp!=null){
                    g = new UserGroup(id);
                    if(g!=null)CacheManager.GetInstance().put(XdatUsergroup.SCHEMA_ELEMENT_NAME, id, g);
                    try {
                        g.init(temp);
                    } catch (Exception e) {
                        logger.error("",e);
                    }
                }
                return g;
            } catch (Throwable e) {
                logger.error("",e);
                return null;
            }
    	}else{
    		return g;
    	}
    	
        
    }
}
