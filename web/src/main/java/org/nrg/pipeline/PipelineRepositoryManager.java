/*
 * web: org.nrg.pipeline.PipelineRepositoryManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.pipeline;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.xmlbeans.PipelineData;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation.InputParameters;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation.InputParameters.Parameter;
import org.nrg.pipeline.xmlbeans.PipelineData.XnatInfo.GeneratesElements;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.*;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.EventUtils.CATEGORY;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.EventRequirementAbsent;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PipelineRepositoryManager {

	public static final String ALL_DATA_TYPES="All Datatypes";
	private  static PipePipelinerepository pipelineRepository = null;
    private static final Logger logger = LoggerFactory.getLogger(PipelineRepositoryManager.class);

	public synchronized static PipePipelinerepository GetInstance() {
		if (pipelineRepository == null) {
            logger.info("Initializing PipelineRepository...");
            ArrayList<PipePipelinerepository> pipelineRepos = PipePipelinerepository.getAllPipePipelinerepositorys(null,true);
            if (pipelineRepos.size()>0) {
            	pipelineRepository = pipelineRepos.get(0);
            }else { //No pipelines have been set for the site so far.
                pipelineRepository = new PipePipelinerepository((UserI) null);
            }
		}
		if (pipelineRepository != null) {
            logger.info("Finished loading the pipeline repository!");
        } else {
            logger.warn("Tried to initialize the pipeline repository, but failed somehow (pipelineRepository is null). Please check the logs for failures that may have caused this condition.");
		}
		return pipelineRepository;
	}

    public synchronized static void SetInfo(final PipePipelinedetails pipelineDetails) throws Exception {
		String pipelineXml = pipelineDetails.getPath();
        if (pipelineXml == null) {
            throw new Exception("Path to the pipeline not set");
        }
		File xmlFile = new File(pipelineXml);
		if (!xmlFile.exists() ) throw new Exception("File at " + pipelineXml + " not found");
			   ArrayList errors = new ArrayList();
		        XmlOptions xopt = new XmlOptions();
		        xopt.setErrorListener(errors);
		         XmlObject xo = XmlObject.Factory.parse(xmlFile, xopt);
		         if (errors.size() != 0) {
            throw new XmlException(Arrays.toString(errors.toArray()));
		         }
		         PipelineDocument pipelineDoc = (PipelineDocument)xo;
		         PipelineData pipeline = pipelineDoc.getPipeline();
		         pipelineDetails.setDescription(pipeline.getDescription());
		         boolean hasXnatInfo = pipeline.isSetXnatInfo();
		         if (hasXnatInfo) {
		         String appliesTo = pipeline.getXnatInfo().getAppliesTo();
		         if (appliesTo != null) {
		        	 pipelineDetails.setAppliesto(appliesTo);
		         }else {
		        	 pipelineDetails.setAppliesto(ALL_DATA_TYPES);
		         }
		         if (pipeline.getXnatInfo().isSetGeneratesElements()) {
		        	 GeneratesElements elementsGenerated = pipeline.getXnatInfo().getGeneratesElements();
		        	 for (int i = 0; i < elementsGenerated.sizeOfElementArray(); i++) {
		        		 String elementGenerated = elementsGenerated.getElementArray(i);
		        		 PipePipelinedetailsElement element = new PipePipelinedetailsElement();
		        		 element.setElement(elementGenerated);
		        		 pipelineDetails.setGenerateselements_element(element);
		        	 }
		         }
		         }else {
		        	 pipelineDetails.setAppliesto(ALL_DATA_TYPES);
		         }
		         if (pipeline.isSetDocumentation()) { //Extract the input parameters for the pipeline
		        	 Documentation  documentation = pipeline.getDocumentation();
		        	 if (documentation.isSetInputParameters()) {
		        		 InputParameters inputParameters = documentation.getInputParameters();
		        		 for (int i=0; i < inputParameters.sizeOfParameterArray(); i++) {
		        			 Parameter param = inputParameters.getParameterArray(i);
		        			 PipePipelinedetailsParameter pipeParam = new PipePipelinedetailsParameter();
		        			 pipeParam.setName(param.getName());
		        			 pipeParam.setDescription(param.getDescription());
		        			 if (param.isSetValues()) {
		        				 if (param.getValues().isSetSchemalink()) {
		        					 pipeParam.setValues_schemalink(param.getValues().getSchemalink());
		        				 }else if (param.getValues().isSetCsv()) {
		        					 pipeParam.setValues_csvvalues(param.getValues().getCsv());
		        				 }
		        			 }else {
		        				 pipeParam.setValues_csvvalues("");
		        			 }
		        			 pipelineDetails.setParameters_parameter(pipeParam);
		        		 }
		        	 }
		         }
	}

	   public synchronized static  void Reset(){
		   pipelineRepository=null;
	    }


	 public synchronized static void RemoveReferenceToPipelineFromProject(PipePipelinedetails pipeline, UserI user, String pID, final EventUtils.TYPE type, String reason) {
		 //Check to see if the project has some study protocol data corresponding to the pipeline
		 //If it does do nothing
		 //If no data has been generated so far remove the reference to the study protocol generated by the pipeline
		 XnatProjectdata proj = XnatProjectdata.getXnatProjectdatasById(pID, user, false);
		 
        if (proj != null) {
		 PersistentWorkflowI wrk;
		try {
                wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, XnatProjectdata.SCHEMA_ELEMENT_NAME, proj.getId(), proj.getId(), EventUtils.newEventInstance(CATEGORY.PROJECT_ADMIN, type, EventUtils.MODIFY_CONFIGURED_PIPELINE, reason, null));
		} catch (EventRequirementAbsent e1) {
			throw new NullPointerException(e1.getMessage());
		}
		 EventMetaI c = wrk.buildEvent();
	    	
			 ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(proj.getId());
			 removePipeline(pipeline.getPath(), arcProject, user,c);
		 }
	 }

	 private static void removePipeline(String pipelinePath, ArcProject aProject, UserI user, EventMetaI c) {
		 List<ArcProjectPipelineI> projectPipelines = aProject.getPipelines_pipeline();
        if (logger.isDebugEnabled()) {
            logger.debug("Checking the {} pipelines configured for project {} to remove pipeline {}.", projectPipelines.size(), aProject.getId(), pipelinePath);
        }
        for (ArcProjectPipelineI projectPipeline : projectPipelines) {
            ArcProjectPipeline instance = (ArcProjectPipeline) projectPipeline;
            ArcPipelinedata pipeline = instance.getPipelinedata();
			 String path = pipeline.getLocation() ;
			 if (pipelinePath.equals(path)) {
				 try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Removing the {} pipeline from project {}.", pipelinePath, aProject.getId());
                    }
                    SaveItemHelper.authorizedRemoveChild(instance.getCurrentDBVersion(), null, pipeline.getCurrentDBVersion(), user, c);
                    instance.getItem().removeItem(pipeline.getItem());
					 //DBAction.DeleteItem(pipeline.getItem(), user);
					 break;
				 }catch(Exception e) {
                    logger.error("Couldn't delete pipelines located at " + pipelinePath + " for project " + aProject.getId());
				 }
            } else if (logger.isDebugEnabled()) {
                logger.debug("Found a pipeline {}, but that doesn't match the target pipeline {} so I'm not removing it.", path, pipelinePath);
			 }
		 }

        List<ArcProjectDescendantI> descendants = aProject.getPipelines_descendants_descendant();
        if (logger.isDebugEnabled()) {
            logger.debug("Now processing {} descendants.", descendants.size());
	         }
        for (ArcProjectDescendantI descendant : descendants) {
            ArcProjectDescendant instance = (ArcProjectDescendant) descendant;
            List<ArcProjectDescendantPipelineI> pipelines = instance.getPipeline();
            if (logger.isDebugEnabled()) {
                logger.debug("Now processing {} pipelines related to descendant {}.", pipelines.size(), instance.getArcProjectDescendantId());
           }
            for (ArcProjectDescendantPipelineI pipeline1 : pipelines) {
                ArcProjectDescendantPipeline descPipeline = (ArcProjectDescendantPipeline) pipeline1;
                ArcPipelinedata pipeline = descPipeline.getPipelinedata();
				 String path = pipeline.getLocation() ;
				 if (pipelinePath.equals(path)) {
					 try {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Removing pipeline {} as pipeline descendant.");
                        }
                        SaveItemHelper.authorizedRemoveChild(instance.getCurrentDBVersion(), null, descPipeline.getCurrentDBVersion(), user, c);
                        instance.getItem().removeItem(descPipeline.getItem());
						 break;
					 }catch(Exception e) {
                        logger.error("Couldn't delete pipelines located at " + pipelinePath + " for project " + aProject.getId() + " descendant  " + instance.getXsitype());
					 }
					 }
			 }
		 }
		 /*if (deleted) {
		       try {
				 ValidationResults vr = aProject.getCurrentDBVersion().validate();
		         if (vr.isValid()){
		                 aProject.getCurrentDBVersion().save(user,false,false);
		         }
	           }catch(Exception e) {
	          	 logger.error("",e);
	           }
			 }*/

		 ArcSpecManager.Reset();
	 }

	 public synchronized static void RemoveReferenceToPipelineFromProjects(String pipelinePath, UserI user, EventMetaI c) {
		 ArcArchivespecification arcSpec = ArcSpecManager.GetFreshInstance();
		 List<ArcProjectI> arcProjects = arcSpec.getProjects_project();
        if (logger.isDebugEnabled()) {
            logger.info("I will now attempt to remove the pipeline {} from the {} projects on this system.", pipelinePath, arcProjects.size());
        }
        for (ArcProjectI arcProject : arcProjects) {
            ArcProject aProject = (ArcProject) arcProject;
			 removePipeline(pipelinePath,  aProject,  user,c);
		 }
	 }

    // Reinstated this method that was removed (possibly because it was thought to be unused??)
    // ...because without it CNDA builds breaks due to their reference to it in:
    // src.org.apache.turbine.app.cnda_xnat.modules.screens.PipelineScreen_BoldSeedBasedAnalysis.java (line #42)
    public static String GetPathToPipeline(String partialPipelinePath, ArcProject aProject) {
        String rtn = null;
        List<ArcProjectPipelineI> projectPipelines = aProject.getPipelines_pipeline();
        for (int j = 0; j < projectPipelines.size(); j++) {
            ArcProjectPipeline projectPipeline = (ArcProjectPipeline)projectPipelines.get(j);
            ArcPipelinedata pipeline = (ArcPipelinedata) projectPipeline.getPipelinedata();
            String path = pipeline.getLocation() ;
            if (path.endsWith(partialPipelinePath)) {
                rtn = path;
                break;
            }
        }
        if (rtn == null) {
            List<ArcProjectDescendantI> projectDescs = aProject.getPipelines_descendants_descendant();
            for (int j = 0; j < projectDescs.size(); j++) {
                ArcProjectDescendant projectDesc = (ArcProjectDescendant) projectDescs.get(j);
                List<ArcProjectDescendantPipelineI> pipelines = projectDesc.getPipeline();
                for (int k = 0; k < pipelines.size(); k++) {
                    ArcProjectDescendantPipeline descPipeline = (ArcProjectDescendantPipeline) pipelines.get(k);
                    ArcPipelinedata pipeline = (ArcPipelinedata) descPipeline.getPipelinedata();
                    String path = pipeline.getLocation() ;
                    if (path.endsWith(partialPipelinePath)) {
                        rtn = path;
                        break;
                    }
                }
                if (rtn != null) break;
            }
        }
        return rtn;
    }

}
