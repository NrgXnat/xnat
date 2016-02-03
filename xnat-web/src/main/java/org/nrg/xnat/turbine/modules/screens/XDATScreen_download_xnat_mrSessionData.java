/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_download_xnat_mrSessionData
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;


import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xdat.turbine.utils.TurbineUtils;

/**
 * @author timo
 *
 */
public class XDATScreen_download_xnat_mrSessionData extends SecureReport {

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
        XnatMrsessiondata mr = ((XnatMrsessiondata)om);
        context.put("archive",mr.listArchiveToHTML(TurbineUtils.GetRelativeServerPath(data)));
        data.getSession().setAttribute("download_session", mr);
    }

    
    /**
     * Return null to use the defualt settings (which are configured in xdat:element_security).  Otherwise, true will force a pre-load of the item.
     * @return
     */
    public Boolean preLoad()
    {
        return Boolean.TRUE;
    }
}
