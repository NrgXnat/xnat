/*
 * web: org.nrg.pipeline.utils.PipelineAdder
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.pipeline.utils;

import org.apache.turbine.util.RunData;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.pipeline.PipelineRepositoryManager;
import org.nrg.xdat.om.*;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

public class PipelineAdder {
	
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PipelineAdder.class);

	
    public void prepareScreen(RunData data, Context context) {
	    logger.debug("BEGIN SECURE REPORT :" + this.getClass().getName());
	    String projectId = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data));
	    String pipelinePath = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("pipeline_path",data));
	    String dataType = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("dataType",data));
	    boolean edit = ((Boolean)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedBoolean("edit",data));
	    String templateFile = null;
	    try {
	    	if (edit) {
				ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(projectId);
	    		if (dataType.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME)) { //It's a project level pipeline
					ArcProjectPipeline newPipeline = arcProject.getPipelineEltByPath(pipelinePath);
					templateFile = newPipeline.getCustomwebpage();
	    		}else {
					ArcProjectDescendantPipeline newPipeline = arcProject.getPipelineForDescendantEltByPath(dataType, pipelinePath);
					templateFile = newPipeline.getCustomwebpage();
	    		}
	    	}else {
				    //Get the pipeline identified by the pipeline_path
				    //Get the ArcProject element identified by the projectId
				    //Set the pipeline for the data-type and send it to the screen for the user to add parameters
				    PipePipelinerepository pipelineRepository = PipelineRepositoryManager.GetInstance();
				    PipePipelinedetails pipeline = pipelineRepository.getPipeline(pipelinePath);
					if (dataType.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME)) { //It's a project level pipeline
						if (pipeline.getCustomwebpage() != null) {
							templateFile = pipeline.getCustomwebpage();
						}
					}else {
						//ArcProjectDescendantPipeline newPipeline = new ArcProjectDescendantPipeline();
						if (pipeline.getCustomwebpage() != null) {
							templateFile = pipeline.getCustomwebpage();
						}
					}
	    	}
	    	redirect(data, templateFile);
	    }catch(Exception e) {
	    	logger.error("",e);
	    }
    	
    }

    private void redirect(RunData data, String templateFile) throws Exception {
		if (templateFile != null) {
			String screenName = "";
			int index = templateFile.indexOf(".vm");
			if (index != -1) {
				String prefix = templateFile.substring(0, index); 
				screenName += prefix +"_add"; 
			}else {
				screenName += templateFile +"_add"; 
			}
			if (!screenName.endsWith(".vm")) screenName+=".vm";
			logger.debug("PipelineAdder looking for: " + screenName);
			if (Velocity.resourceExists("/screens/" + screenName)) {
				data.setScreenTemplate(screenName);
			}
		}else {
            String template = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("template",data));
            data.setScreenTemplate(template);
		}
    }
    
    

}
