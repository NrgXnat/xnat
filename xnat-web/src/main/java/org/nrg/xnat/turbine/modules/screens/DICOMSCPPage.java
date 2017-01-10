/*
 * web: org.nrg.xnat.turbine.modules.screens.DICOMSCPPage
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.dcm.DicomSCPManager;
import org.nrg.dcm.preferences.DicomSCPInstance;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DICOMSCPPage extends SecureScreen {

	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_hhmmss");
		context.put("uploadID", formatter.format(Calendar.getInstance().getTime()));
		List<DicomSCPInstance> scps = XDAT.getContextService().getBean(DicomSCPManager.class).getDicomSCPInstances();
		context.put("host", XDAT.getSiteConfigPreferences().getSiteUrl());
		context.put("scps", scps);
	}
}
