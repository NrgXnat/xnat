/*
 * org.nrg.xnat.turbine.modules.screens.AlternateImageUpload
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
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author timo
 *
 */
public class AlternateImageUpload extends SecureScreen {
    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    protected void doBuildTemplate(final RunData data, final Context context) throws MalformedURLException {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_hhmmss");
        context.put("uploadID", formatter.format(Calendar.getInstance().getTime()));
        final ArcArchivespecification arc = ArcSpecManager.GetInstance();
        context.put("arc", arc);
    }
}
