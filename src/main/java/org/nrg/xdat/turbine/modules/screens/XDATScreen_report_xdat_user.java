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
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.XDATUser;
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
        } catch (Exception e) {
            logger.error("",e);
        }
    }
}

