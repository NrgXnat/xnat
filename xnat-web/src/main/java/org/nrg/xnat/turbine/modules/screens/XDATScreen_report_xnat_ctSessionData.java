/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_report_xnat_ctSessionData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatExperimentdataShareI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatCtsessiondata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Tim
 *
 */
public class XDATScreen_report_xnat_ctSessionData extends SecureReport {
	private static final Logger logger = LoggerFactory.getLogger(XDATScreen_report_xnat_ctSessionData.class);

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
        try {
            XnatCtsessiondata ct = new XnatCtsessiondata(item);
            context.put("ct", ct);
            context.put("workflows", ct.getWorkflows());
            if(context.get("project")==null) {
                String proj = ct.getProject();
                if (!Permissions.canReadProject(XDAT.getUserDetails(), proj)) {
                    // If user cannot read that project, look through the projects that session is shared into. If user
                    // can view the data in one of those projects they should view this session from that project's context.
                    List<XnatExperimentdataShareI> list = ct.getSharing_share();
                    for (XnatExperimentdataShareI exptShare : list) {
                        if (Permissions.canReadProject(XDAT.getUserDetails(), exptShare.getProject())) {
                            proj = exptShare.getProject();
                            break;
                        }
                    }
                }
                context.put("project", proj);
            }
            
            for(XnatImagescandataI scan: ct.getSortedScans()){
            	((XnatImagescandata)scan).setImageSessionData(ct);
            }
        } catch (Exception e) {
            logger.error("",e);
        }
    }

    
    /**
     * Return null to use the default settings (which are configured in xdat:element_security).  Otherwise, true will force a pre-load of the item.
     * @return Returns <b>true</b> if the object should be pre-loaded, <b>false</b> if not, <b>null</b> if the default value should be used.
     */
    @Override
    public Boolean preLoad() {
        return Boolean.FALSE;
    }
}
