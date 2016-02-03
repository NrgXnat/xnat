/*
 * org.nrg.xnat.turbine.modules.screens.PipelineScreen_protocolcheck_add
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/19/13 3:01 PM
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.ElementSecurity;

import java.util.ArrayList;
import java.util.List;

public class PipelineScreen_protocolcheck_add extends PipelineScreen_add_project_pipeline{

	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PipelineScreen_protocolcheck_add.class);
	

	
	 public void finalProcessing(RunData data, Context context){
		 try {
			 List<String> elementsAll = ElementSecurity.GetElementNames();
			 List<String> elements = new ArrayList<String>();
			 for (int i=0; i< elementsAll.size(); i++) {
				 if (!((String)elementsAll.get(i)).startsWith("xdat:") && !((String)elementsAll.get(i)).startsWith("prov:") && !((String)elementsAll.get(i)).startsWith("wrk:") && !((String)elementsAll.get(i)).startsWith("val:")) {
					elements.add(elementsAll.get(i)); 
				 }
			 }
			 context.put("elements",elements);

		 }catch(Exception e) {
			 logger.error("Encountered error", e);
			 e.printStackTrace();
		 }
	 }
		
}
