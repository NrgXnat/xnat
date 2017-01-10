/*
 * web: org.nrg.xnat.turbine.modules.screens.PipelineScreen_add_project_pipeline
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.pipeline.PipelineRepositoryManager;
import org.nrg.pipeline.utils.PipelineUtils;
import org.nrg.xdat.model.PipePipelinedetailsParameterI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.util.List;

public class PipelineScreen_add_project_pipeline extends SecureReport {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PipelineScreen_add_project_pipeline.class);

    public void finalProcessing(RunData data, Context context) {
    	
    }

    public void doBuildTemplate(RunData data, Context context) {
	    logger.debug("BEGIN SECURE REPORT :" + this.getClass().getName());
	    String projectId = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data));
	    String pipelinePath = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("pipeline_path",data));
	    String dataType = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("dataType",data));
	    boolean edit = org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedBoolean("edit",data);
	    // String templateFile = null;
	    context.put("pipeline_path", pipelinePath);
	    context.put("dataType", dataType);
	    try {
	    	context.put("edit", edit);
	    	if (edit) {
				ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(projectId);
	    		if (dataType.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME)) { //Its a project level pipeline
					ArcProjectPipeline newPipeline = arcProject.getPipelineEltByPath(pipelinePath);
					// templateFile = newPipeline.getCustomwebpage();
					context.put("newpipeline", newPipeline);
					context.put("isAutoArchive",isAutoArchive(newPipeline));
//					redirect(data, templateFile);
	    		}else {
					ArcProjectDescendantPipeline newPipeline = arcProject.getPipelineForDescendantEltByPath(dataType, pipelinePath);
					// templateFile = newPipeline.getCustomwebpage();
					context.put("newpipeline", newPipeline);
					context.put("isAutoArchive",isAutoArchive(newPipeline));
//					redirect(data, templateFile);
	    		}
	    	}else {
				    //Get the pipeline identified by the pipeline_path
				    //Get the ArcProject element identified by the projectId
				    //Set the pipeline for the data-type and send it to the screen for the user to add parameters
				    PipePipelinerepository pipelineRepository = PipelineRepositoryManager.GetInstance();
				    PipePipelinedetails pipeline = pipelineRepository.getPipeline(pipelinePath);
					if (dataType.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME)) { //Its a project level pipeline
						ArcProjectPipeline newPipeline = new ArcProjectPipeline();
						String pipelineName = getName(pipeline.getPath());
						newPipeline.setStepid(pipelineName);
						newPipeline.setDisplaytext(pipelineName);
						newPipeline.setLocation(pipeline.getPath());
						newPipeline.setName(pipelineName);
						newPipeline.setDescription(pipeline.getDescription());
						final List<PipePipelinedetailsParameterI> parameter = pipeline.getParameters_parameter();
						for (final PipePipelinedetailsParameterI aParameter : parameter) {
							ArcPipelineparameterdata arcParam = extractArcPipelineParameter((PipePipelinedetailsParameter) aParameter);
							newPipeline.setParameters_parameter(arcParam.getItem());
						}
						if (pipeline.getCustomwebpage() != null) {
							newPipeline.setCustomwebpage(pipeline.getCustomwebpage());
							// templateFile = newPipeline.getCustomwebpage();
						}
						context.put("newpipeline", newPipeline);
						context.put("isAutoArchive",isAutoArchive(newPipeline));
//						redirect(data, templateFile);
					}else {
						ArcProjectDescendantPipeline newPipeline = new ArcProjectDescendantPipeline();
						String pipelineName = getName(pipeline.getPath());
						newPipeline.setStepid(pipelineName);
						newPipeline.setDisplaytext(pipelineName);
						newPipeline.setLocation(pipeline.getPath());
						newPipeline.setName(pipelineName);
						newPipeline.setDescription(pipeline.getDescription());
						List<PipePipelinedetailsParameterI> parameter = pipeline.getParameters_parameter();
						for (final PipePipelinedetailsParameterI aParameter : parameter) {
							ArcPipelineparameterdata arcParam = extractArcPipelineParameter((PipePipelinedetailsParameter) aParameter);
							newPipeline.setParameters_parameter(arcParam.getItem());
						}
						if (pipeline.getCustomwebpage() != null) {
							newPipeline.setCustomwebpage(pipeline.getCustomwebpage());
							// templateFile = newPipeline.getCustomwebpage();
						}
						context.put("newpipeline", newPipeline);
						context.put("isAutoArchive",isAutoArchive(newPipeline));
//						redirect(data, templateFile);
					}
	    	}
	    	finalProcessing(data,context);
	    }catch(Exception e) {
	    	logger.error("",e);
	    }
    	
    }

    
    
    private boolean isAutoArchive(ArcProjectDescendantPipeline newPipeline) {
        return newPipeline != null && newPipeline.getStepid() != null && newPipeline.getStepid().startsWith(PipelineUtils.AUTO_ARCHIVE);
    }
    
    private boolean isAutoArchive(ArcProjectPipeline newPipeline) {
        return newPipeline != null && newPipeline.getStepid() != null && newPipeline.getStepid().startsWith(PipelineUtils.AUTO_ARCHIVE);
    }

    
    private String getName(String path) {
    	String rtn = path;
    	//int index = path.lastIndexOf(File.separator);
    	int index = path.lastIndexOf("/");
    	if (index != -1) {
    		rtn = path.substring(index + 1);
        	index = rtn.lastIndexOf(".xml");
        	if (index != -1) {
        		rtn = rtn.substring(0, index);
        	}
    	}
    	return rtn;
    }

//    private void redirect(RunData data, String templateFile) throws Exception {
//		if (templateFile != null) {
//			String templateName = "";
//			int index = templateFile.indexOf(".vm");
//			if (index != -1) {
//				String prefix = templateFile.substring(0, index);
//				templateName += prefix +"_add.vm";
//			}else {
//				templateName += templateFile +"_add.vm";
//			}
//			logger.debug("PipelineScreen_add_pipeline looking for: " + templateName);
//			if (Velocity.resourceExists("/screens/" + templateName)) {
//				//doRedirect(data,templateName);
//				data.setScreenTemplate(templateName );
//			}
//		}
//    }
    
    
    private ArcPipelineparameterdata extractArcPipelineParameter(PipePipelinedetailsParameter pipeParameter) {
    	ArcPipelineparameterdata rtn = new ArcPipelineparameterdata();
    	rtn.setName(pipeParameter.getName());
    	rtn.setDescription(pipeParameter.getDescription());
    	String schemaLink = pipeParameter.getValues_schemalink();
    	String csvValue = pipeParameter.getValues_csvvalues();
    	if (schemaLink != null) {
    		rtn.setSchemalink(schemaLink);
    	}else {
    		rtn.setCsvvalues(csvValue);
    	}
    	return rtn;
    }
    
}
