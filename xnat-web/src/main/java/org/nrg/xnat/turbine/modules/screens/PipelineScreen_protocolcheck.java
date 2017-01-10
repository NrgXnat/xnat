/*
 * web: org.nrg.xnat.turbine.modules.screens.PipelineScreen_protocolcheck
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
