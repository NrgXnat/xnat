/*
 * web: org.nrg.xnat.restlet.extensions.PipelineDetailsRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.extensions;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nrg.pipeline.PipelineDetailsHelper;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XnatRestlet({"/projects/{PROJECT_ID}/pipelines/{PIPELINE_NAME}", "/projects/{PROJECT_ID}/pipelines/{PIPELINE_NAME}/details"})
public class PipelineDetailsRestlet extends SecureResource {
    public static final String PARAM_PROJECT_ID = "PROJECT_ID";
    public static final String PARAM_PIPELINE_NAME = "PIPELINE_NAME";

    private final String            _projectId;
    private final String            _pipelineName;

    private static final Logger _log = LoggerFactory.getLogger(PipelineDetailsRestlet.class);


    public PipelineDetailsRestlet(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);

        _projectId = (String) getRequest().getAttributes().get(PARAM_PROJECT_ID);
        _pipelineName = (String) getRequest().getAttributes().get(PARAM_PIPELINE_NAME);

        if (StringUtils.isBlank(_projectId)) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No project specified");
        }

        getVariants().add(new Variant(MediaType.ALL));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (_log.isDebugEnabled()) {
            _log.debug("Returning pipeline details");
        }

        try {
        	PipelineDetailsHelper pipelineDetailsHelper = new PipelineDetailsHelper(_projectId);
            Map<String,Object> pipelineDetails = pipelineDetailsHelper.getPipelineDetailsMap(_pipelineName);

            // Make a json object from the pipelineDetails map
            String json = getSerializer().toJson(pipelineDetails);
            return new StringRepresentation(json, MediaType.APPLICATION_JSON);

        } catch (Exception exception) {
            _log.error("There was an error rendering the pipeline details", exception);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "There was an error rendering the pipeline details", exception);
        }
    }

}
