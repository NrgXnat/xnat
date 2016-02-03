/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_add_pipeline
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
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;

import java.util.ArrayList;

public class XDATScreen_add_pipeline extends AdminEditScreenA {

	
	
	public String getElementName() {
		return PipePipelinedetails.SCHEMA_ELEMENT_NAME;
	}
	
	public void finalProcessing(RunData data, Context context) {
		
	}
	
	public void doBuildTemplate(RunData data, Context context)      {
		try {
			ArrayList<GenericWrapperElement> elements = GenericWrapperElement.GetAllElements(false);
			ArrayList<GenericWrapperElement> myelements = new ArrayList<GenericWrapperElement>();
			//Remove the datatypes which cannt be created or applied to
			for (int i = 0; i < elements.size(); i++) {
				GenericWrapperElement element = elements.get(i);
				if (!element.getFullXMLName().startsWith("cat:") && !element.getFullXMLName().startsWith("pipe:") && !element.getFullXMLName().startsWith("arc:") && !element.getFullXMLName().startsWith("prov:") && !element.getFullXMLName().startsWith("xdat:") && !element.getFullXMLName().startsWith("wrk:"))  {
					myelements.add(element);
				}
			}
			context.put("elements",myelements);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
