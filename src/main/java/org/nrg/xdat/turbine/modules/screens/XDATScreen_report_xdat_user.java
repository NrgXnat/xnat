//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 21, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XDATUserDetails;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.XDATUser;

import java.util.List;

/**
 * @author Tim
 *
 */
public class XDATScreen_report_xdat_user extends AdminReport {
	static Logger logger = Logger.getLogger(DefaultReport.class);
    public void finalProcessing(RunData data,Context context)
    {
        try {            
            XDATUser tempUser = new XDATUser(item);
			context.put("userObject",tempUser);
            context.put("allGroups",XdatUsergroup.getAllXdatUsergroups(null, false,"xdat:userGroup/tag"));
            boolean hasFailedLoginAttempts = false;
            List<XdatUserAuth> auths = XDAT.getXdatUserAuthService().getUsersByName(tempUser.getUsername());
            for (XdatUserAuth auth : auths) {
                if (auth.getFailedLoginAttempts() > 0) {
                    hasFailedLoginAttempts = true;
                }
            }
            context.put("hasFailedLoginAttempts", hasFailedLoginAttempts);
        } catch (Exception e) {
            logger.error("",e);
        }
    }
}

