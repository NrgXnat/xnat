/*
 * org.nrg.xdat.security.UserGroupManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.security;

import org.apache.log4j.Logger;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xft.cache.CacheManager;

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
