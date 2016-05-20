/*
 * org.nrg.xnat.turbine.modules.screens.ReportIssue
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DICOMSCPPage extends SecureScreen {

	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_hhmmss");
		context.put("uploadID", formatter.format(Calendar.getInstance().getTime()));
		final ArcArchivespecification arc = ArcSpecManager.GetInstance();
		context.put("arc", arc);
	}

}
