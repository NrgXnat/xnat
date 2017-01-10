/*
 * web: org.nrg.xnat.turbine.modules.actions.ManagePipeline
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.turbine.modules.ScreenLoader;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.PipelineRepositoryManager;
import org.nrg.pipeline.XnatPipelineLauncher;
import org.nrg.pipeline.utils.PipelineAdder;
import org.nrg.pipeline.utils.PipelineFileUtils;
import org.nrg.pipeline.utils.PipelineUtils;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ParameterData.Values;
import org.nrg.pipeline.xmlbeans.ParametersDocument;
import org.nrg.pipeline.xmlbeans.ParametersDocument.Parameters;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.ArcPipelinedataI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.nrg.xnat.exceptions.PipelineNotFoundException;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;


public class ManagePipeline extends SecureAction {

    private static final Logger logger = LoggerFactory.getLogger(ManagePipeline.class);

    public void doPerform(RunData data, Context context) throws Exception {
        String task = ((String) TurbineUtils.GetPassedParameter("task", data));
        if (task != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Got a request to perform a pipeline management task: " + task);
            }
            if (task.equalsIgnoreCase("DELETE")) {
                doDelete(data);
            } else if (task.equalsIgnoreCase("EDIT")) {
                doEdit();
            } else if (task.equalsIgnoreCase("RESET")) {
                PipelineRepositoryManager.Reset();
                data.setMessage("Pipeline Repository Reset");
            } else if (task.equalsIgnoreCase("DELETEFROMPROJECT")) {
                doDeletefromproject(data);
            } else if (task.equalsIgnoreCase("projectpipeline")) {
                doAddpipeline(data, context);
            }
        }
    }

    private void doEdit() {
        logger.debug("I'm in the doEdit() method, which is somewhat odd as there's nothing to be done here.");
    }

    public void doAddpipeline(RunData data, Context context) {
        PipelineAdder pipelineAdder = new PipelineAdder();
        pipelineAdder.prepareScreen(data, context);
    }

    private void doDeletefromproject(RunData data) {
    	UserI user = TurbineUtils.getUser(data);
        String projectId = ((String) TurbineUtils.GetPassedParameter("project", data));
        try {
            XFTItem pline = TurbineUtils.GetItemBySearch(data);
            if (pline != null) {
                PipePipelinedetails pipeline = new PipePipelinedetails(pline);
                if (logger.isDebugEnabled()) {
                    logger.debug("The user {} has requested removal of the pipeline {} from the project {}.", user.getLogin(), pipeline.getPath(), projectId);
                }
                data.setMessage("Item deleted");
                PipelineRepositoryManager.RemoveReferenceToPipelineFromProject(pipeline, user, projectId,getEventType(data),getReason(data));
                PipelineRepositoryManager.Reset();
            }
        } catch (Exception e) {
            logger.error("Error deleting " + TurbineUtils.GetPassedParameter("search_value", data), e);
            data.setMessage("Error Deleting item.");
        }
    }

    private void doDelete(RunData data) {
    	UserI user = TurbineUtils.getUser(data);
        String pipelinePath = null;
        try {
            XFTItem pipeline = TurbineUtils.GetItemBySearch(data);
            if (pipeline != null) {
            	PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow(user, pipeline.getXSIType(), pipeline.getPKValueString(), PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Deleted registered pipeline"));
				PipePipelinerepository pipelines = PipelineRepositoryManager.GetInstance();
                pipelinePath = pipeline.getStringProperty("path");
				try {
                    logger.info("Attempting to delete the pipeline {}", pipelinePath);
					SaveItemHelper.authorizedRemoveChild(pipelines.getCurrentDBVersion(),null,pipeline.getCurrentDBVersion(),user,wrk.buildEvent());
                    logger.info("Deleted {}", pipelinePath);
                    data.setMessage("Pipeline removed from site repository: " + pipelinePath);
                    PipelineRepositoryManager.RemoveReferenceToPipelineFromProjects(pipelinePath, user, wrk.buildEvent());
					PipelineRepositoryManager.Reset();
					ArcSpecManager.Reset();

					WorkflowUtils.complete(wrk, wrk.buildEvent());
				} catch (Exception e) {
                    logger.error("Error occurred deleting the indicated pipeline: " + pipelinePath, e);
					WorkflowUtils.fail(wrk, wrk.buildEvent());
				}
            }
        } catch (Exception e) {
            logger.error("Error deleting " + TurbineUtils.GetPassedParameter("search_value", data), e);
            data.setMessage("Error deleting requested pipeline: " + (pipelinePath == null ? "Couldn't find the pipeline object" : pipelinePath));
        }
        data.setScreenTemplate("ClosePageAndRefresh.vm");
    }

    public void doRedirect(RunData data, Context context) throws Exception {
        try {
            String projectId = ((String) TurbineUtils.GetPassedParameter("project", data));
            String pipelinePath = ((String) TurbineUtils.GetPassedParameter("pipeline", data));
            String schemaType = ((String) TurbineUtils.GetPassedParameter("schema_type", data));
            context.put("pipelinePath", pipelinePath);
            String customWebPage = "PipelineScreen_default_launcher.vm";
            try {
		        ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(projectId);
                if (schemaType.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
                    ArcProjectPipeline pipelineData = (ArcProjectPipeline) arcProject.getPipelineByPath(pipelinePath);
                    if (pipelineData.getCustomwebpage() != null) customWebPage = pipelineData.getCustomwebpage();
                } else {
                    ArcPipelinedataI pipelineData = arcProject.getPipelineForDescendantByPath(schemaType, pipelinePath);
                    if (pipelineData.getCustomwebpage() != null) customWebPage = pipelineData.getCustomwebpage();
                }
            } catch (PipelineNotFoundException pne) {
                // Could be an additional pipeline not yet defined for the
                // project
                PipePipelinedetails pipelineDetails = PipelineRepositoryManager.GetInstance().getPipeline(pipelinePath);
                if (pipelineDetails.getCustomwebpage() != null) {
                    customWebPage = pipelineDetails.getCustomwebpage();
                }
            }
            data.setScreenTemplate(customWebPage);
        } catch (Exception e) {
            data.setMessage("Unknown Error: " + e.getMessage());
            logger.error("Error occurred trying to redirect to the pipeline launch page.", e);
        }

    }

    private String getStepId(String templateSuppliedStepId, boolean launchedAtAutoArchive, String nextStepId, String displayText) {
        if (templateSuppliedStepId != null) {
            if (launchedAtAutoArchive) {
                if (templateSuppliedStepId.startsWith(PipelineUtils.AUTO_ARCHIVE)) {
                    return templateSuppliedStepId;
            }
                return nextStepId;
            } else if (!templateSuppliedStepId.startsWith(PipelineUtils.AUTO_ARCHIVE)) {
                return templateSuppliedStepId;
            }
            return displayText;
        } else  if (launchedAtAutoArchive) {
            return nextStepId;
        }
        return displayText;
    }

    @SuppressWarnings("unused")
    public void doAddprojectpipeline(RunData data, Context context) throws Exception {
    	UserI user = TurbineUtils.getUser(data);
        XFTItem found;
        try {
            String projectId = ((String) TurbineUtils.GetPassedParameter("project",data));
            String pipelinePath = ((String) TurbineUtils.GetPassedParameter("pipeline_path",data));
            String dataType = ((String) TurbineUtils.GetPassedParameter("dataType",data));
            String schemaType = ((String) TurbineUtils.GetPassedParameter("schemaType",data));

            boolean edit = TurbineUtils.GetPassedBoolean("edit",data);

            XFTItem newItem = XFTItem.NewItem(schemaType, TurbineUtils.getUser(data));
            TurbineUtils.OutputDataParameters(data);

            // Get the pipeline identified by the pipeline_path
            // Get the ArcProject element identified by the projectId
            // Set the pipeline for the data-type and send it to the screen for
            // the user to add parameters
            PopulateItem populater = PopulateItem.Populate(data, schemaType, true, newItem);
            found = populater.getItem();

            boolean launchedAtAutoArchive = data.getParameters().getBoolean("auto_archive");

            //boolean launchedAtAutoArchive = ((Boolean)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("auto_archive",data));
            //Boolean autoArchive = (Boolean) org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("auto_archive",data);
			//boolean launchedAtAutoArchive = autoArchive == null ? false : autoArchive.booleanValue();

            String templateSuppliedStepId = found.getStringProperty("stepid");
            boolean saved = false;

	   		PersistentWorkflowI wrk=WorkflowUtils.buildOpenWorkflow(user, XnatProjectdata.SCHEMA_ELEMENT_NAME,projectId,projectId,newEventInstance(data, EventUtils.CATEGORY.PROJECT_ADMIN, EventUtils.MODIFY_CONFIGURED_PIPELINE));
			EventMetaI c=wrk.buildEvent();
            // A set of pipelines can be launched on auto archive.This set will
            // have
            // AUTO_ARCHIVE_<SEQUENTIAL NUMBER> unless site wants to setup the
            // stepid at the template level
            // If the step is provided at the template level, it must be of the
            // kind AUTO_<SOMETHING>
            // It is assumed that the sequence will be independent

    		ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(projectId);
            if (dataType.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME)) { // Its a
                                                                        // project
                                                                        // level
                                                                        // pipeline
                ArcProjectPipeline newPipeline = new ArcProjectPipeline(found);
                if (edit) {
                    ArcProjectPipeline existing = arcProject.getPipelineEltByPath(pipelinePath);
                    copy(data, existing);
                    if (existing.getStepid().startsWith(PipelineUtils.AUTO_ARCHIVE) && !launchedAtAutoArchive) {
                        existing.setStepid(existing.getDisplaytext());
                    }
    		    saved = SaveItemHelper.authorizedSave(existing,user, false, false,c);
                } else {
                    String stepId = getStepId(templateSuppliedStepId, launchedAtAutoArchive, PipelineUtils.getNextAutoArchiveStepId(arcProject), newPipeline.getDisplaytext());
                    newPipeline.setStepid(stepId);
                }
    				arcProject.setPipelines_pipeline(newPipeline.getItem());
            } else {
                ArcProjectDescendant existingDesc = arcProject.getDescendant(dataType);
                ArcProjectDescendant newDesc = new ArcProjectDescendant();
                ArcProjectDescendantPipeline newPipeline = new ArcProjectDescendantPipeline(found);
                if (existingDesc == null) {
                    newDesc.setXsitype(dataType);
                    String stepId = getStepId(templateSuppliedStepId, launchedAtAutoArchive, PipelineUtils.getNextAutoArchiveStepId((ArcProjectDescendant) null), newPipeline.getDisplaytext());
                    newPipeline.setStepid(stepId);
                    newDesc.setPipeline(newPipeline.getItem());
                    arcProject.setPipelines_descendants_descendant(newDesc.getItem());
                } else {
                    if (edit) {
                        ArcProjectDescendantPipeline existingPipeline = existingDesc.getPipeline(pipelinePath);
                        copy(data, existingPipeline);
                        if (existingPipeline.getStepid().startsWith(PipelineUtils.AUTO_ARCHIVE) && !launchedAtAutoArchive) {
                            existingPipeline.setStepid(existingPipeline.getDisplaytext());
                        }
                        saved = SaveItemHelper.authorizedSave(existingPipeline,user, false, false,c);
                    } else {
                        String stepId = getStepId(templateSuppliedStepId, launchedAtAutoArchive, PipelineUtils.getNextAutoArchiveStepId(existingDesc), newPipeline.getDisplaytext());
                        newPipeline.setStepid(stepId);
                        existingDesc.setPipeline(newPipeline.getItem());
                        newDesc.setItem(existingDesc.getItem());
                        arcProject.setPipelines_descendants_descendant(newDesc.getItem());
                    }
                }
            }
            if (!edit) {
    		saved = SaveItemHelper.authorizedSave(arcProject,user, false, false,c);
            }
			PersistentWorkflowUtils.complete(wrk, c);
            ArcSpecManager.Reset();
            String msg = "<p><b>The pipelines for the project could NOT be modified.</b></p>";
            if (saved) {
                msg = "<p><b>The pipelines for the project were successfully modified.</b></p>";
            }
            data.setMessage(msg);
            data.setScreenTemplate("ClosePageAndRefresh.vm");
            // ItemI project = TurbineUtils.GetItemBySearch(data,false);
            // this.redirectToReportScreen((String)TurbineUtils.GetPassedParameter("destination",
            // data), project, data);
            // data.setAction("DisplayAction");
        } catch (Exception e) {
            data.setMessage("Unknown Error.");
            logger.error("", e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void copy(RunData data, ArcProjectDescendantPipeline existingPipeline) throws XFTInitException, ElementNotFoundException, FieldNotFoundException, InvalidValueException {
        Hashtable hash = (Hashtable) TurbineUtils.GetDataParameterHash(data);
        existingPipeline.getItem().setProperties(hash, true);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void copy(RunData data, ArcProjectPipeline existingPipeline) throws XFTInitException, ElementNotFoundException, FieldNotFoundException, InvalidValueException {
        Hashtable hash = (Hashtable) TurbineUtils.GetDataParameterHash(data);
        existingPipeline.getItem().setProperties(hash, true);
    }

    // Final step of adding a pipeline to the Site
    @SuppressWarnings("unused")
    public void doAdd(RunData data, Context context) throws Exception {
    	UserI user = TurbineUtils.getUser(data);
        XFTItem found = null;

		try {
            EditScreenA screen = (EditScreenA) ScreenLoader.getInstance().getInstance("XDATScreen_add_pipeline");
            XFTItem newItem = (XFTItem) screen.getEmptyItem(data);
            TurbineUtils.OutputDataParameters(data);
            PopulateItem populater = PopulateItem.Populate(data, "pipe:pipelineDetails", true, newItem);
            found = populater.getItem();

            PipePipelinedetails pipelineDetails = new PipePipelinedetails(found);
            PipelineRepositoryManager.SetInfo(pipelineDetails);

            ValidationResultsI vr = null;

            ValidationResultsI temp = pipelineDetails.getItem().validate();
            if (!pipelineDetails.getItem().isValid()) {
                vr = temp;
            }

            if (vr != null) {
                TurbineUtils.SetEditItem(pipelineDetails.getItem(), data);
                context.put("vr", vr);
                if (TurbineUtils.GetPassedParameter("edit_screen",data) !=null){
                    data.setScreenTemplate(((String) TurbineUtils.GetPassedParameter("edit_screen",data)));
                }
            } else {
                try {
                    PipePipelinerepository pipelineRepository = PipelineRepositoryManager.GetInstance();
                    pipelineRepository.setPipeline(pipelineDetails);
            	    SaveItemHelper.authorizedSave(pipelineRepository,user, false, true,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Added registered pipeline"));
                    PipelineRepositoryManager.Reset();
                    data.setMessage("Pipeline " + pipelineDetails.getPath() + " has been successfully added to the repository");
                    data.setScreenTemplate("ClosePageAndRefresh.vm");
                } catch (Exception e) {
                    logger.error("Error Storing " + found.getXSIType(), e);
                    data.setMessage("Error Saving item.");
                    TurbineUtils.SetEditItem(found, data);
                    if (TurbineUtils.GetPassedParameter("edit_screen",data) !=null){
                        data.setScreenTemplate(((String) TurbineUtils.GetPassedParameter("edit_screen",data)));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred adding the pipeline", e);
            data.setMessage("Unknown Error: " + e.getMessage());
            TurbineUtils.SetEditItem(found, data);
            if (TurbineUtils.GetPassedParameter("edit_screen",data) !=null){
                data.setScreenTemplate(((String) TurbineUtils.GetPassedParameter("edit_screen",data)));
            }
        }
    }

    @SuppressWarnings("unused")
    public void doLaunchpipeline(RunData data, Context context) throws Exception {
    	UserI user = TurbineUtils.getUser(data);
            XFTItem item = TurbineUtils.GetItemBySearch(data);
        String pipeline_path = ((String) TurbineUtils.GetPassedParameter("pipeline_path",data));
			boolean launch_now = false;
			String launch_nowStr = data.getParameters().get("launch_now");
			if (launch_nowStr != null){
				launch_now = launch_nowStr.equalsIgnoreCase("true");
			}

            XnatPipelineLauncher xnatPipelineLauncher = new XnatPipelineLauncher(user);
            xnatPipelineLauncher.setSupressNotification(true);
            xnatPipelineLauncher.setParameter("useremail", user.getEmail());
            xnatPipelineLauncher.setParameter("userfullname", XnatPipelineLauncher.getUserName(user));
            xnatPipelineLauncher.setParameter("adminemail", XDAT.getSiteConfigPreferences().getAdminEmail());
            xnatPipelineLauncher.setParameter("mailhost", XDAT.getNotificationsPreferences().getSmtpServer().getHostname());
            xnatPipelineLauncher.setParameter("xnatserver", TurbineUtils.GetSystemName());
            xnatPipelineLauncher.setPipelineName(pipeline_path);
            String exptLabel = item.getStringProperty("label");
            String project = item.getStringProperty("project");
            xnatPipelineLauncher.setId(item.getStringProperty("ID"));
            xnatPipelineLauncher.setLabel(exptLabel);
            xnatPipelineLauncher.setExternalId(project);
            xnatPipelineLauncher.setDataType(item.getXSIType());

            boolean runPipelineInProcess = data.getParameters().containsKey("run_pipeline_in_process") ? data.getParameters().getBoolean("run_pipeline_in_process") : XnatPipelineLauncher.DEFAULT_RUN_PIPELINE_IN_PROCESS;
            xnatPipelineLauncher.setRunPipelineInProcess(runPipelineInProcess);

            // To work around the fact that checkbox parameters don't appear
            // when not selected, the meaning of the checkbox
            // is reversed here to allow the default to take over when suppress
            // isn't selected.
            boolean recordWorkflowEntries = data.getParameters().containsKey("suppress_workflow_entries") ? !data.getParameters().getBoolean("suppress_workflow_entries") : XnatPipelineLauncher.DEFAULT_RECORD_WORKFLOW_ENTRIES;
            xnatPipelineLauncher.setRecordWorkflowEntries(recordWorkflowEntries);

        Parameters parameters = extractParameters(data);
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            String s = formatter.format(date);
            String paramFileName = exptLabel + "_params_" + s + ".xml";
            String buildDir = PipelineFileUtils.getBuildDir(project, true);
            xnatPipelineLauncher.setBuildDir(buildDir);
            String paramFilePath = saveParameters(buildDir + File.separator + exptLabel, paramFileName, parameters);
            xnatPipelineLauncher.setParameterFile(paramFilePath);
        if (launch_now) {
		    	xnatPipelineLauncher.launch(null);
        } else {
		    	xnatPipelineLauncher.launch();
        }

            // TODO: We need to get status back for in-process pipeline launching and use that for when runPipelineInProcess is true.
            data.setMessage(runPipelineInProcess ? "<p><b>The requested pipeline has completed.</b></p>" : "<p><b>The pipeline has been scheduled.  Status email will be sent upon its completion.</b></p>");
            data.setScreenTemplate("ClosePage.vm");
        }

    private Parameters extractParameters(RunData data) {
        Parameters parameters = Parameters.Factory.newInstance();
	    int totalParams = TurbineUtils.GetPassedInteger("param_cnt",data);
        for (int i = 0; i < totalParams; i++) {
			String name = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("param[" + i + "].name",data));
			int rowcount = TurbineUtils.GetPassedInteger("param[" + i + "].name.rowcount", data);
            ArrayList<String> formvalues = new ArrayList<>();
            for (int j = 0; j < rowcount; j++) {
                String formfieldname = "param[" + i + "][" + j + "].value";
                if (TurbineUtils.HasPassedParameter(formfieldname, data)) //formvalues.add(data.getParameters().get(formfieldname));
				   formvalues.add(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(formfieldname,data)));
            }

            if (formvalues.size() > 0) {
                ParameterData param = parameters.addNewParameter();
                param.setName(name);
                if (formvalues.size() == 1) {
                    Values values = param.addNewValues();
                    values.setUnique(formvalues.get(0));
                } else {
                    Values values = param.addNewValues();
                    for (String value : formvalues) {
                        values.addList(value);
                    }
                }
            }
        }
        return parameters;
    }


    private String saveParameters(String rootpath, String fileName, Parameters parameters) throws Exception {
        File dir = new File(rootpath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File paramFile = new File(rootpath + File.separator + fileName);
        ParametersDocument paramDoc = ParametersDocument.Factory.newInstance();
        paramDoc.addNewParameters().set(parameters);
        paramDoc.save(paramFile, new XmlOptions().setSavePrettyPrint().setSaveAggressiveNamespaces());
        return paramFile.getAbsolutePath();
    }
}
