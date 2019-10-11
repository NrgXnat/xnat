/*
 * web: org.nrg.xnat.restlet.extensions.PipelineDetailsRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.extensions;

import org.apache.commons.lang3.StringUtils;
import org.nrg.pipeline.PipelineRepositoryManager;
import org.nrg.pipeline.utils.PipelineFileUtils;
import org.nrg.pipeline.xmlbeans.PipelineData;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation.Authors.Author;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation.Authors.Author.Contact;
import org.nrg.pipeline.xmlbeans.PipelineData.ResourceRequirements.Property;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step;
import org.nrg.xdat.model.ArcPipelinedataI;
import org.nrg.xdat.model.ArcPipelineparameterdataI;
import org.nrg.xdat.om.*;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Map<String,Object> pipelineDetails = getPipelineDetailsMap();

            // Make a json object from the pipelineDetails map
            String json = getSerializer().toJson(pipelineDetails);
            return new StringRepresentation(json, MediaType.APPLICATION_JSON);

        } catch (Exception exception) {
            _log.error("There was an error rendering the pipeline details", exception);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "There was an error rendering the pipeline details", exception);
        }
    }

    public Map<String,Object> getPipelineDetailsMap() throws Exception {
        ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(_projectId);
        PipePipelinerepository pipelineRepository = PipelineRepositoryManager.GetInstance();
        String pipelineDescriptorPath = "";
        for (String[] pipelineProperties : pipelineRepository.listPipelines(arcProject)) {
            if (pipelineProperties[2].equals(_pipelineName) || pipelineProperties[2].equals("AUTO_ARCHIVE_"+_pipelineName) ||
                    pipelineProperties[7].equals(_pipelineName) || pipelineProperties[7].equals("AUTO_ARCHIVE_"+_pipelineName)) {
                pipelineDescriptorPath = pipelineProperties[4];
                break;
            }
        }
        if (StringUtils.isBlank(pipelineDescriptorPath)) {
            throw new Exception("Pipeline does not exist in project");
        }

        PipePipelinedetails pipeline = pipelineRepository.getPipeline(pipelineDescriptorPath);
        PipelineData pipelineData = PipelineFileUtils.GetDocument(pipelineDescriptorPath).getPipeline();

        String xsiTypeAppliesTo = pipeline.getAppliesto();
        ArcPipelinedataI projectPipelineData;
        if (xsiTypeAppliesTo.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
            projectPipelineData = arcProject.getPipelineByPath(pipelineDescriptorPath);
        } else {
            projectPipelineData = arcProject.getPipelineForDescendantByPath(xsiTypeAppliesTo,pipelineDescriptorPath);
        }
        if (projectPipelineData == null) {
            throw new Exception("Could not find project pipeline");
        }

        // Build hash map
        Map<String,Object> pipelineDetails = new HashMap<>();

        // Basic info
        pipelineDetails.put("path",pipelineDescriptorPath);
        if (StringUtils.isNotBlank(pipelineData.getDescription())) {
            pipelineDetails.put("description", pipelineData.getDescription());
        }
        if (StringUtils.isNotBlank(pipelineRepository.getElementsGeneratedBy(pipeline))) {
            pipelineDetails.put("generates", pipelineRepository.getElementsGeneratedBy(pipeline));
        }
        if (StringUtils.isNotBlank(pipelineRepository.getDisplayName(xsiTypeAppliesTo))) {
            pipelineDetails.put("appliesTo", pipelineRepository.getDisplayName(xsiTypeAppliesTo));
        }

        if (pipelineData.isSetResourceRequirements()) {
            String resourceRequirements = "";
            for (Property property : pipelineData.getResourceRequirements().getPropertyArray()) {
                resourceRequirements += property.getStringValue() + ", ";
            }
            if (resourceRequirements.endsWith(", ")) {
                int index = resourceRequirements.lastIndexOf(", ");
                resourceRequirements = resourceRequirements.substring(0, index);
            }

            pipelineDetails.put("resourceRequirements",resourceRequirements);
        }

        if (pipelineData.isSetDocumentation()) {
            Documentation doc = pipelineData.getDocumentation();
            if (doc.isSetWebsite()) {
                pipelineDetails.put("website",doc.getWebsite());
            }
            if (doc.isSetPublications()) {
                pipelineDetails.put("publications",doc.getPublications().getPublicationArray());
            }
            if (doc.isSetAuthors()) {
                List<Map<String,String>> authorInfoList = new ArrayList<>();
                for (Author aAuthor : doc.getAuthors().getAuthorArray()) {
                    Map<String,String> authorInfo = new HashMap<>();
                    if (StringUtils.isNotBlank(aAuthor.getFirstname())) {
                        authorInfo.put("firstname", aAuthor.getFirstname());
                    }
                    if (StringUtils.isNotBlank(aAuthor.getLastname())) {
                        authorInfo.put("lastname", aAuthor.getLastname());
                    }
                    if (aAuthor.isSetContact()) {
                        Contact contact = aAuthor.getContact();
                        if (contact.isSetEmail()) {
                            authorInfo.put("email",contact.getEmail());
                        }
                        if (contact.isSetPhone()) {
                            authorInfo.put("phone",contact.getPhone());
                        }
                    }
                    authorInfoList.add(authorInfo);
                }
                pipelineDetails.put("authors",authorInfoList);
            }
            if (doc.isSetVersion()) {
                pipelineDetails.put("version",doc.getVersion());
            }
        }

        // Step ids and descriptions
        List<Map<String,String>> stepInfoList = new ArrayList<>();
        for (Step aStep : pipelineData.getSteps().getStepArray()) {
            Map<String,String> stepInfo = new HashMap<>();
            stepInfo.put("id",aStep.getId());

            if (StringUtils.isNotBlank(aStep.getDescription())) {
                stepInfo.put("description", aStep.getDescription());
            }
            stepInfoList.add(stepInfo);
        }
        pipelineDetails.put("steps",stepInfoList);

        // Project-level param defaults
        List<Map<String,Object>> paramInfoList = new ArrayList<>();
        for (ArcPipelineparameterdataI aParamI : projectPipelineData.getParameters_parameter()) {
            ArcPipelineparameterdata aParam = (ArcPipelineparameterdata) aParamI;

            Map<String,Object> paramInfo = new HashMap<>();
            paramInfo.put("name",aParam.getName());
            if (StringUtils.isNotBlank(aParam.getDescription())) {
                paramInfo.put("description", aParam.getDescription());
            }

            String csv = aParam.getCsvvalues();
            String schemaLink = aParam.getSchemalink();
            if (StringUtils.isNotBlank(schemaLink) || StringUtils.isNotBlank(csv)) {
                Map<String,String> paramValues = new HashMap<>();
                if (StringUtils.isNotBlank(schemaLink)) {
                    paramValues.put("schemaLink",schemaLink);
                } else {
                    paramValues.put("csv",csv);
                }
                paramInfo.put("values",paramValues);
            }
            paramInfoList.add(paramInfo);
        }
        pipelineDetails.put("inputParameters",paramInfoList);

        return pipelineDetails;
    }
}
