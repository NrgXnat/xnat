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
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;

public class PipelineScreen_protocolcheck extends DefaultPipelineScreen{

	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PipelineScreen_protocolcheck_add.class);
	
	 public void preProcessing(RunData data, Context context)   {
		super.preProcessing(data, context);
		
		if(context.get("pipelinePath")==null){
			//when we call this report directly (rather then through ManageFiles), the pipelinepath isn't preconfigured.
			//also, we can't pass the full pipeline location in the URL (the slashes break things)
			try {
				final ItemI temp = TurbineUtils.GetItemBySearch(data,preLoad());
				if(temp!=null){
					final XnatImagesessiondata session=(XnatImagesessiondata)BaseElement.GetGeneratedItem(temp);
					
					context.put("pipelinePath", session.canRunPipeline(TurbineUtils.getUser(data), "ProtocolCheck"));
						
					//already queried this from the db, so we'll pass it to the screen through the HTTP session, so that it doesn't need to be queried again.
					//complex objects can't be put into the RunData, and the destination screen is already checking for the getDataItem().
					TurbineUtils.setDataItem(data, temp);
				}
			} catch (Exception e) {
				logger.error("",e);
			}
		}
	}

	
	 public void finalProcessing(RunData data, Context context){
		 	context.put("projectSettings", projectParameters);
	 	   
	 }
		
}
