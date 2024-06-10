/*
 * web: org.nrg.xnat.turbine.modules.screens.PipelineScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.app.FieldMethodizer;
import org.apache.velocity.context.Context;
import org.nrg.pipeline.PipelineManager;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.ArcProjectPipelineI;
import org.nrg.xdat.model.WrkAbstractexecutionenvironmentI;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseWrkWorkflowdata;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.search.CriteriaCollection;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class PipelineScreen extends SecureReport {

    static Logger logger = Logger.getLogger(PipelineScreen.class);

    ArrayList skipToList;
    ArrayList<WrkWorkflowdata> workflows;
    
    public PipelineScreen() {
    	skipToList = new ArrayList();
        workflows = new ArrayList<WrkWorkflowdata>();
    }
    

    protected void setWorkflows(RunData data, Context context) {
        String projectId = (String)context.get("project");
        try {
        	org.nrg.xft.search.CriteriaCollection cc = new CriteriaCollection("AND");
            cc.addClause("wrk:workflowData.ID",item.getProperty("ID"));
            if (projectId != null) cc.addClause("wrk:workflowData.ExternalID",projectId);
            org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(cc,TurbineUtils.getUser(data),false);
            //Sort by Launch Time
            ArrayList workitems = items.getItems("wrk:workflowData.launch_time","DESC");
            Iterator iter = workitems.iterator();
            while (iter.hasNext())
            {
                WrkWorkflowdata vrc = new WrkWorkflowdata((XFTItem)iter.next());
                workflows.add(vrc);
            }
            context.put("workflows",workflows);
        }catch(Exception e) {
        	logger.debug(e);
        }
    }
    
    protected void populateSkipToList(RunData data, Context context) {
        String projectId = (String)context.get("project");
    	if (om instanceof XnatProjectdata) {
        	skipToList = PipelineManager.getIndependentPipelinesForProject(projectId, true);
        }else {
        	skipToList = PipelineManager.getIndependentPipelinesForProjectDescendant(projectId, om.getXSIType(), true);
        }
    }
    

    protected void setUpContext(RunData data, Context context) {
        boolean isDescendant = true;
        if (om instanceof XnatProjectdata) {
        	isDescendant = false;
        }	
        context.put("pipelineManager", new PipelineManager());
        context.put("isDescendant", isDescendant);
        context.put( "BaseWrkWorkflowdata", new FieldMethodizer("org.nrg.xdat.om.base.BaseWrkWorkflowdata"));
        context.put("popup", true);
        context.put("skipToList",skipToList);
    }
    
	public void doBuildTemplate(RunData data, Context context)

	{

        preserveVariables(data,context);

        

	    logger.debug("BEGIN SECURE REPORT :" + this.getClass().getName());

	    preProcessing(data,context);

	    

        item = TurbineUtils.getDataItem(data);

	    

	    if (item== null)

		{

		    //System.out.println("No data item passed... looking for item passed by variables");

			try {

				ItemI temp = TurbineUtils.GetItemBySearch(data,preLoad());

			    item = temp;

			} catch (IllegalAccessException e1) {

                logger.error(e1);

			    data.setMessage(e1.getMessage());

				noItemError(data,context);

				return;

			} catch (Exception e1) {

                logger.error(e1);

                data.setMessage(e1.getMessage());

                data.setScreenTemplate("Error.vm");

                noItemError(data,context);

                return;

			}

		}

	    

		if (item == null)

		{

			data.setMessage("Error: No item found.");

			noItemError(data,context);

		}else{

			try {

				if(XFT.VERBOSE)System.out.println("Creating report: " + getClass());;

			    context.put("item",item.getItem());

			    if(XFT.VERBOSE)System.out.println("Loaded item object (org.nrg.xft.ItemI) as context parameter 'item'.");

            	context.put("element",org.nrg.xdat.schema.SchemaElement.GetElement(item.getXSIType()));

            	context.put("search_element",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data)));

            	context.put("search_field",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data)));

            	context.put("search_value",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data)));

			    

            	om = BaseElement.GetGeneratedItem(item);

            	context.put("om",om);
            	
            	 setWorkflows(data,context);
                 populateSkipToList(data,context);
                 setUpContext(data,context);
            	
				 finalProcessing(data,context);

			} catch (Exception e) {

				data.setMessage(e.getMessage());

				logger.error("",e);

			}

		}



	    logger.debug("END SECURE REPORT :" + this.getClass().getName());

	}

    protected <A extends ArcProjectPipelineI> A getCurrentPipeline(ArrayList<A> pipelines, ArrayList<WrkWorkflowdata> workflows,  String pipeline_step) {
        A rtn = null;
        if (pipelines == null || pipelines.size() == 0) {
        	message = "There are no pipelines defined at this time to launch ";
        	return rtn;
        }
        if (pipeline_step != null) { //User wants to jump to a particular pipeline
            return getRequestedPipeline(pipelines,pipeline_step,skipToList);
        }
        if (workflows == null || workflows.size() == 0) { //No workflows are available
            rtn = pipelines.get(0);
        }else if (isAnyQueuedOrRunning(workflows)) {
            message = "An old pipeline is either queued or running. Please wait for the process to complete."; 
            return rtn;
        }else {
            WrkWorkflowdata latestWrkFlow = workflows.get(0);
            if (latestWrkFlow.getStatus().equalsIgnoreCase(BaseWrkWorkflowdata.QUEUED) || latestWrkFlow.getStatus().equalsIgnoreCase(BaseWrkWorkflowdata.RUNNING)) {
                message = "A pipeline is currently " + latestWrkFlow.getStatus() + " please wait for the process to complete."; 
                return rtn;
            }else {
                return getNextPipelineFromWorkflow(pipelines,workflows);
            }
        }
        return rtn;
    }
    
    protected boolean isAnyQueuedOrRunning(ArrayList<WrkWorkflowdata> workflows) {
        boolean rtn = false;
        try {
            for (int i = 1; i <workflows.size(); i++) {
                WrkWorkflowdata wrkFlow = workflows.get(i);
                if (wrkFlow.getStatus().toUpperCase().equals(BaseWrkWorkflowdata.QUEUED) ||wrkFlow.getStatus().toUpperCase().equals(BaseWrkWorkflowdata.RUNNING)) {
                    rtn = true;
                    break;
                }
            }
        }catch(IndexOutOfBoundsException aoe){logger.debug(aoe);}
        return rtn;
    }
    
    protected <A extends ArcProjectPipelineI> A getRequestedPipeline(ArrayList<A> pipelines, String pipeline_step, ArrayList skipToList) {
        A rtn = null;
        for (int i = 0; i <pipelines.size(); i++) {
            A pipeline = pipelines.get(i);
            if (pipeline.getStepid().equals(pipeline_step)) {
                rtn = pipeline;
                break;
            }else {
                skipToList.add(pipeline);
            }
        }        
        if (rtn == null) {
            message = "Pipeline with step id " + pipeline_step + " is not defined. Please contact your site administrator <a href=\"mailto:" + XDAT.getNotificationsPreferences().getHelpContactInfo() + "?subject=Invalid Pipeline Step " + pipeline_step + " for " + item.getXSIType() + "\">Report problem</A>";
        }
        return rtn;
    }
    
    protected <A extends ArcProjectPipelineI> A getNextPipelineFromWorkflow(ArrayList<A> pipelines, ArrayList<WrkWorkflowdata> workflows) {
        A rtn = null;
        WrkWorkflowdata latestWrkFlow = workflows.get(0);
        boolean found = false;
        for (int i = 0; i <pipelines.size(); i++) {
            A pipeline = pipelines.get(i);
            WrkAbstractexecutionenvironmentI absEnv = latestWrkFlow.getExecutionenvironment();
            String matchPipelineName = null;
            String pipelinePath = PipelineManager.getFullPath(pipeline);
           // if (absEnv instanceof WrkXnatexecutionenvironment) {
            //    matchPipelineName = ((WrkXnatexecutionenvironment)absEnv).getPipeline();
            //}else {
                matchPipelineName = latestWrkFlow.getPipelineName();
            //}
            if (pipelinePath.equals(matchPipelineName) && (latestWrkFlow.getStatus().equalsIgnoreCase(BaseWrkWorkflowdata.FAILED) || latestWrkFlow.getStatus().equalsIgnoreCase(BaseWrkWorkflowdata.AWAITING_ACTION))) {
                rtn = pipeline;
                found = true;
                break;
            }else if (pipelinePath.equals(matchPipelineName) && latestWrkFlow.getStatus().equalsIgnoreCase(BaseWrkWorkflowdata.COMPLETE)) {
                skipToList.add(pipeline);
                if ( i != (pipelines.size() - 1)) { //All pipelines are not complete
                    rtn = pipelines.get(i+1);// Next pipeline to launch
                }
                found = true;
                break;
            }else {
                 skipToList.add(pipeline);
            }
        }
        //If no matching pipelines were found from the list of existing workflows for this entity, it will be the first pipeline which will be launched. 
        if (!found) {
        	rtn = pipelines.get(0);
        }
        return rtn;
    }

    protected String message = null;
    
    
}
