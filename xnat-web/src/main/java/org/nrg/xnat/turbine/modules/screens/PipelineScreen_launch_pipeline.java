/*
 * org.nrg.xnat.turbine.modules.screens.PipelineScreen_launch_pipeline
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
import org.nrg.xdat.om.*;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.util.ArrayList;
import java.util.List;

public class PipelineScreen_launch_pipeline extends SecureReport {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PipelineScreen_launch_pipeline.class);

    public void finalProcessing(RunData data, Context context) {
    	//Given the XFTItem look for the pipelines that are defined for that Schema Element and fill in the list
        try {
        	String projectId = null;
        	String schema_element_name = null;
        	try { //Is this an experiment belonging to the project
        		XnatExperimentdata experiment = new XnatExperimentdata(item);
        		projectId = (String)context.get("project");
        		schema_element_name = experiment.getXSIType();
        		context.put("isQueuedOrRunning", isQueuedOrRunning(experiment.getId(), data));
        	}catch(ClassCastException cce) {
        		try {
        			XnatProjectdata project = new XnatProjectdata(item);
        			projectId = project.getId();
        			schema_element_name = project.SCHEMA_ELEMENT_NAME;
            		context.put("isQueuedOrRunning", isQueuedOrRunning(project.getId(), data));
        		}catch(ClassCastException cce1) {
        			XnatSubjectdata subject = new XnatSubjectdata(item);
        			projectId = subject.getProject();
        			schema_element_name = subject.SCHEMA_ELEMENT_NAME;
            		context.put("isQueuedOrRunning", isQueuedOrRunning(subject.getId(), data));
        		}
        	}
        	if (projectId != null && schema_element_name  != null) {
        		XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, TurbineUtils.getUser(data), false);
        		//Get the list of associated pipelines for this item
        		List pipelines = new ArrayList();
        		ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(projectId);
        		if (schema_element_name.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME))
        			pipelines = arcProject.getPipelines_pipeline();
        		else	
        			pipelines = arcProject.getPipelinesForDescendant(schema_element_name );
        		context.put("pipelines", pipelines);
        	//	ArrayList additionalPipelines = PipelineRepositoryManager.GetInstance().getAdditionalPipelinesForDatatype(project, schema_element_name);
        	//	context.put("additional_pipelines", additionalPipelines);
        		context.put("project", project.getId());
        	} else {
        		data.setMessage("Couldnt get the project id from the item");
        		throw new Exception ("Couldnt get the project id from the item");
        	}
        }catch(Exception e) {
        	 logger.error("",e);
        }
    }
  
    private boolean isQueuedOrRunning(String id, RunData data) throws Exception{
    	boolean rtn   = false;
        org.nrg.xft.search.CriteriaCollection cc = new CriteriaCollection("AND");
        cc.addClause("wrk:workflowData.ID",id);
        org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(cc,TurbineUtils.getUser(data),false);
        //Sort by Launch Time
        ArrayList workitems = items.getItems("wrk:workflowData.launch_time","DESC");
        if (workitems != null && workitems.size() > 0) {
        	WrkWorkflowdata wrk = new WrkWorkflowdata((XFTItem)workitems.get(0));
        	if (wrk.getStatus().equalsIgnoreCase("QUEUED") || wrk.getStatus().equalsIgnoreCase("RUNNING") || wrk.getStatus().equalsIgnoreCase("HOLD") || wrk.getStatus().equalsIgnoreCase("AWAITING ACTION"))
        		rtn = true;
        }
        return rtn;
    }
    
}
