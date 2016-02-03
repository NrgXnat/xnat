/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_edit_xnat_pVisitData
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

public class XDATScreen_edit_xnat_pVisitData extends EditSubjectAssessorScreen {

	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_xnat_pVisitData.class);
	
	@Override
	public String getElementName() {
		return "xnat:pvisitdata";
	}
	/* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
    	super.finalProcessing(data,context);
    }
}
