/*
 * org.nrg.xnat.turbine.modules.screens.PipelineScreen_protocolcheck
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

public class PipelineScreen_protocolcheck extends DefaultPipelineScreen{

	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PipelineScreen_protocolcheck_add.class);
	
	 public void preProcessing(RunData data, Context context)   {
	     super.preProcessing(data, context);
  	 }

	
	 public void finalProcessing(RunData data, Context context){
	     	context.put("projectSettings", projectParameters);
	 	   
	 }
		
}
