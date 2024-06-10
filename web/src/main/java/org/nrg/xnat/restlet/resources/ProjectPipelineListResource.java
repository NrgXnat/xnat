/*
 * web: org.nrg.xnat.restlet.resources.ProjectPipelineListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang3.StringUtils;
import org.nrg.pipeline.PipelineRepositoryManager;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTTable;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

public class ProjectPipelineListResource extends SecureResource  {
	public ProjectPipelineListResource(Context context, Request request, Response response) {
		super(context, request, response);

		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		getVariants().add(new Variant(MediaType.TEXT_XML));

		_projectId = (String) getParameter(request, "PROJECT_ID");
		_project = StringUtils.isNotBlank(_projectId) ? XnatProjectdata.getProjectByIDorAlias(_projectId, getUser(), false) : null;
	}

	@Override
	public boolean allowGet() {
		return true;
	}

	@Override
	public boolean allowDelete() {
		return true;
	}

	public void handleDelete() {
		//Remove the Pipeline identified by the path for the project and the datatype
		if (_project != null) {
			final String pathToPipeline = this.getQueryVariable("path");
            final String datatype = this.getQueryVariable("datatype");
			if (pathToPipeline != null && datatype != null) {
				if (isUserAuthorized()) {
					try {
						final ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(_project.getId());
						final boolean success = PipelineRepositoryManager.GetInstance().delete(arcProject, pathToPipeline, datatype, getUser());
						if (!success) {
                            final String message = "Failed to save project specification for project " + _projectId + " when deleting pipeline " + pathToPipeline;
                            logger.error(message);
                            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, message);
                        } else {
							ArcSpecManager.Reset();
							getResponse().setEntity(represent(getVariants().get(0)));
							setStatusBasedOnConditions();
							//Send a 200 OK message back
							//getResponse().setStatus(Status.SUCCESS_OK,"Pipeline has been removed from project " + _project.getId());
						}
					}catch(Exception e) {
						logger.error("An error occurred try to delete the pipeline " + pathToPipeline, e);
						getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Encountered exception " + e.getMessage());
					}
				}else {
					getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User unauthorized to remove pipeline from project");
				}
			}
		}else {
			getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Expecting path and datatype as query parameters");
		}
	}




	private boolean isUserAuthorized() {
		boolean isUserAuthorized = false;
		try {
			isUserAuthorized = Permissions.canDelete(getUser(), _project);
		}catch(Exception e) {
			e.printStackTrace();
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		return isUserAuthorized;
	}

	@Override
	public Representation represent(Variant variant) {
        if (!isUserAuthorized()) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return null;
        }

        final boolean additional = isQueryVariableTrue("additional");

        //Check to see if the Project already has an entry in the ArcSpec.
        //If yes, then return that entry. If not then construct a new ArcProject element and insert an attribute to say that it's an already existing
        //entry or not
        final ArcProject arcProject;
        try {
            arcProject = getArcProject(additional);
            if (arcProject == null) {
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "No archive spec entry for project " + _projectId);
                return null;
            }
        } catch (Exception e) {
            logger.error("An error occurred trying to retrieve the arc project for project " + _projectId);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }

        final MediaType mediaType = overrideVariant(variant);
        if (mediaType.equals(MediaType.TEXT_XML)) {
            return representItem(arcProject.getItem(), mediaType, null, false, true);
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            final XFTTable table = PipelineRepositoryManager.GetInstance().toTable(arcProject);
            return representTable(table, mediaType, null);
        } else {
            return null;
        }
	}

    private ArcProject getArcProject(final boolean additional) throws Exception {
        final ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(_projectId);
        if (!additional) {
            return arcProject;
        }

        // Check to see if the Project already has an entry in the ArcSpec.
        //If yes, then return that entry. If not then construct a new ArcProject element and insert an attribute to say that it's an already existing
        //entry or not
        return arcProject == null  // No Project pipelines set in the archive specification
               ? PipelineRepositoryManager.GetInstance().createNewArcProject(_project)
               //Return all the pipelines that are applicable to the project but not selected
               : PipelineRepositoryManager.GetInstance().getAdditionalPipelines(_project);
    }

	private final String          _projectId;
	private final XnatProjectdata _project;
}
