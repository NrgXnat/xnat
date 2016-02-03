/*
 * org.nrg.xnat.turbine.modules.screens.CSVScreen
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

@SuppressWarnings("unused")
public class CSVScreen extends org.nrg.xdat.turbine.modules.screens.CSVScreen {

	@Override
	public String getContentType(RunData data) {
		return "text/csv";
	}
}
