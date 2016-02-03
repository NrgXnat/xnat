/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_report_xnat_subjectData
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
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.turbine.modules.screens.SecureReport;

/**
 * @author Tim
 *
 */
public class XDATScreen_report_xnat_subjectData extends SecureReport {
	static Logger logger = Logger.getLogger(XDATScreen_report_xnat_mrSessionData.class);

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
        try {
            XnatSubjectdata sub = new XnatSubjectdata(item);
            context.put("subject",sub);
        } catch (Exception e) {
            logger.error("",e);
        }
    }
}
