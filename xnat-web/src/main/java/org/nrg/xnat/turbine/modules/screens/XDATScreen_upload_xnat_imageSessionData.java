/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_upload_xnat_imageSessionData
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
import org.nrg.xdat.turbine.modules.screens.SecureReport;

import java.util.Calendar;
import java.util.Date;

public class XDATScreen_upload_xnat_imageSessionData extends SecureReport {
	static Logger logger = Logger.getLogger(XDATScreen_upload_xnat_imageSessionData.class);

    @Override
    public void finalProcessing(RunData data, Context context) {

        Date d = Calendar.getInstance().getTime();
        
        StringBuffer sb = new StringBuffer();
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyyMMdd_hhmmss");
        sb.append(formatter.format(d));
        context.put("uploadID", sb.toString());
        
    }

}
