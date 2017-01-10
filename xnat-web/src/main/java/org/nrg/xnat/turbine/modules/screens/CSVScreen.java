/*
 * web: org.nrg.xnat.turbine.modules.screens.CSVScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;

@SuppressWarnings("unused")
public class CSVScreen extends org.nrg.xdat.turbine.modules.screens.CSVScreen {

	@Override
	public String getContentType(RunData data) {
		return "text/csv";
	}
}
