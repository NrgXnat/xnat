/*
 * web: org.nrg.xnat.turbine.modules.screens.CompressedUploaderPage
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CompressedUploaderPage extends SecureScreen {

	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_hhmmss");
		context.put("uploadID", formatter.format(Calendar.getInstance().getTime()));
		final ArcArchivespecification arc = ArcSpecManager.GetInstance();
		context.put("arc", arc);
	}

}
