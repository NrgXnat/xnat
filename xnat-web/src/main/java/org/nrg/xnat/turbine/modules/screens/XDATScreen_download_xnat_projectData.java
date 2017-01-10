/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_download_xnat_projectData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xnat.utils.XnatHttpUtils;

public class XDATScreen_download_xnat_projectData extends SecureReport {

    @Override
    public void finalProcessing(RunData data, Context context) {

        context.put("appletPath",TurbineUtils.GetRelativeServerPath(data) + "/applet");
        context.put("serverRoot",TurbineUtils.GetRelativeServerPath(data));
		context.put("jsessionid", XnatHttpUtils.getJSESSIONID(data));
    }

}
