/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_delete_pipeline
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
import org.nrg.xdat.om.PipePipelinedetails;
import org.nrg.xdat.turbine.modules.screens.AdminEditScreenA;

public class XDATScreen_delete_pipeline extends AdminEditScreenA {

	@Override
	public String getElementName() {
		return PipePipelinedetails.SCHEMA_ELEMENT_NAME;
	}

	@Override
	public void finalProcessing(RunData data, Context context) {
		// TODO Auto-generated method stub
	}
}

