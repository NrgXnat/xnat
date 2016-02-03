/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_report_xnat_mrSessionData
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xdat.turbine.modules.screens.SecureReport;

/**
 * @author Tim
 *
 */
public class XDATScreen_report_xnat_mrSessionData extends SecureReport {
	static Logger logger = Logger.getLogger(XDATScreen_report_xnat_mrSessionData.class);

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
        try {
            XnatMrsessiondata mr = new XnatMrsessiondata(item);
            context.put("mr",mr);
            
            
            context.put("workflows",mr.getWorkflows());
            
            if(context.get("project")==null){
            	context.put("project", mr.getProject());
            }
            
            for(XnatImagescandataI scan:mr.getSortedScans()){
            	((XnatImagescandata)scan).setImageSessionData(mr);
            }
        } catch (Exception e) {
            logger.error("",e);
        }
    }

    
    /**
     * Return null to use the defualt settings (which are configured in xdat:element_security).  Otherwise, true will force a pre-load of the item.
     * @return
     */
    public Boolean preLoad()
    {
        return Boolean.FALSE;
    }
}
