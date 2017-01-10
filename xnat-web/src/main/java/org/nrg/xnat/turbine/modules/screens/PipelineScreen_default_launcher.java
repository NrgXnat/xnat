/*
 * web: org.nrg.xnat.turbine.modules.screens.PipelineScreen_default_launcher
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.model.ArcPipelinedataI;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.ArcProjectPipeline;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

public class PipelineScreen_default_launcher extends DefaultPipelineScreen {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PipelineScreen_launch_pipeline.class);

    public void finalProcessing(RunData data, Context context) {
    	try {
	        String projectId = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data));
	        String pipelinePath = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("pipeline",data));
	        String schemaType = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("schema_type",data));
	        ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(projectId);
	        if (schemaType.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
	        	ArcProjectPipeline pipelineData = (ArcProjectPipeline)arcProject.getPipelineByPath(pipelinePath);
	        	context.put("pipeline", pipelineData);
	        	setParameters(pipelineData, context);
	        }else {
	        	ArcPipelinedataI pipelineData = arcProject.getPipelineForDescendantByPath(schemaType, pipelinePath);
	        	context.put("pipeline", pipelineData);
	        	setParameters(pipelineData, context);
	        }
    	}catch(Exception e) {
    		e.printStackTrace();
    		logger.debug(e);
    	}
    }
    
}
