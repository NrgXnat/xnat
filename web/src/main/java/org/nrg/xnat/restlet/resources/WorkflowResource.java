/*
 * web: org.nrg.xnat.restlet.resources.WorkflowResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;


public class WorkflowResource extends ItemResource {

    private final String workflowId;

    private static final Logger log = LoggerFactory.getLogger(WorkflowResource.class);

    public WorkflowResource(Context context, Request request, Response response) {
        super(context, request, response);
        workflowId = (String) getParameter(request, "WORKFLOW_ID");
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override
    public boolean allowDelete() {
        return false;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public boolean allowGet() {
        return true;
    }

    @Override
    public void handlePut() {
        try {
            final UserI user = getUser();

            // Create the new workflow item based on information from the user.
            final XFTItem item = loadItem("wrk:workflowData", true);

            final WrkWorkflowdata workflow;
            if (StringUtils.isNotBlank(workflowId)) {
                // Lookup the workflow by the ID provided by the user.
                workflow = (WrkWorkflowdata) WorkflowUtils.getUniqueWorkflow(user, workflowId);
                if (workflow == null) {
                    // If we couldn't find the workflow, 404
                    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find the specified workflow.");
                    return;
                }
            } else {
                // Lookup the workflow by pipeline_name, launch_time, and ID
                workflow = getWorkflow(item);
            }

            // If the workflow exists, Make sure the user has permission to edit an existing workflow.
            if (workflow != null && !canUserEditWorkflow(user, workflow)) {
                // If the user is not allow to modify this workflow, 403
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "You are not allowed to make changes to this workflow.");
                return;
            }

            // Id, launch_time, data_type, and pipeline_name are all required in order to save a new workflow
            if (workflow == null && StringUtils.isAnyBlank(item.getStringProperty("id"), item.getStringProperty("launch_time"), item.getStringProperty("pipeline_name"), item.getStringProperty("data_type"))) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Id, launch_time, data_type and pipeline_name are all required (scan_id may be specified when appropriate but is not required).");
                return;
            }

            // If the workflow exists, set the workflow id on the new item.
            if (workflow != null && workflow.getWrkWorkflowdataId() != null) {
                item.setProperty("wrk_workflowData_id", workflow.getWrkWorkflowdataId());
            }

            // Save the workflow
            SaveItemHelper.authorizedSave(item, user, false, false, EventUtils.DEFAULT_EVENT(user, "Workflow Update"));
        } catch (ActionException e) {
            getResponse().setStatus(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            log.error("Unable to save Workflow.", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
        }
    }

    @Override
    public Representation represent(final Variant variant) {
        final UserI           user = getUser();
        final WrkWorkflowdata workflow;
        if (StringUtils.isNotBlank(workflowId)) {
            // Lookup the workflow by the ID provided by the user.
            workflow = (WrkWorkflowdata) WorkflowUtils.getUniqueWorkflow(user, workflowId);
        } else {
            try {
                workflow = getWorkflow(loadItem("wrk:workflowData", true));
            } catch (ActionException e) {
                getResponse().setStatus(e.getStatus(), e.getMessage());
                return null;
            } catch (Exception e) {
                log.error("An error occurred trying to find the requested workflow.", e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "An error occurred trying to find the requested workflow.");
                return null;
            }
        }

        if (workflow != null) {
            // If we found the workflow, represent it with the requested media type
            return representItem(workflow.getItem(), ObjectUtils.defaultIfNull(getRequestedMediaType(), MediaType.TEXT_XML));
        }

        // If we couldn't find the workflow, 404
        getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find the specified workflow.");
        return null;
    }

    private boolean canUserEditWorkflow(final UserI user, final WrkWorkflowdata workflow) {
        return workflow.getInsertUser().getID().equals(user.getID()) || Roles.isSiteAdmin(user);
    }

    private WrkWorkflowdata getWorkflow(final XFTItem item) throws ElementNotFoundException, FieldNotFoundException, XFTInitException, ParseException {
        final String pipelineName = item.getStringProperty("pipeline_name");
        final Date   launchTime   = item.getDateProperty("launch_time");
        final String id           = item.getStringProperty("id");
        final String scanId       = item.getStringProperty("scan_id");
        return (WrkWorkflowdata) WorkflowUtils.getUniqueWorkflow(getUser(), pipelineName, id, scanId, launchTime);
    }
}
