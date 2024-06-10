package org.nrg.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nrg.pipeline.exception.PipelineNotConfiguredException;
import org.nrg.pipeline.utils.PipelineFileUtils;
import org.nrg.pipeline.xmlbeans.PipelineData;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation.Authors.Author;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation.Authors.Author.Contact;
import org.nrg.pipeline.xmlbeans.PipelineData.ResourceRequirements.Property;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step;
import org.nrg.xdat.model.ArcPipelinedataI;
import org.nrg.xdat.model.ArcPipelineparameterdataI;
import org.nrg.xdat.om.ArcPipelineparameterdata;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.PipePipelinedetails;
import org.nrg.xdat.om.PipePipelinerepository;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

/**
 * @author mohan_000
 *
 */
public class PipelineDetailsHelper {
	
	private String _projectId = null;
	
	public PipelineDetailsHelper(String projectId) {
		_projectId = projectId;
	}
	

    public Map<String,Object> getPipelineDetailsMap(String _pipelineName) throws Exception {
    	return getPipelineDetailsMap(_pipelineName, false);
    }
    
	public Map<String,Object> getPipelineDetailsMap(String _pipelineName,boolean getPipelineFromSite) throws Exception {
    	Map<String, Object> pipelineDetails = new HashMap<String,Object>();
    	ArcProject arcProject = null;
    	if (_projectId == null) {
			arcProject = PipelineRepositoryManager.GetInstance().createNewArcProjectForDummyProject();
    	}else {
    	    arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(_projectId);	
    	}
        try {
        	pipelineDetails = extractDetails(_pipelineName, arcProject);
        }catch(PipelineNotConfiguredException pne) {
        	if (getPipelineFromSite) {
    			arcProject = PipelineRepositoryManager.GetInstance().createNewArcProjectForDummyProject();
    	        try {
    	        	pipelineDetails = extractDetails(_pipelineName, arcProject);
    	        }catch(PipelineNotConfiguredException pne1) {
    	        	throw new Exception("Pipeline " + _pipelineName + " not found at site level");
    	        }        		
        	}
        }
    	return pipelineDetails;
    }

    private Map<String,Object> extractDetails(String _pipelineName, ArcProject arcProject) throws PipelineNotConfiguredException,Exception{
        // Build hash map
        Map<String,Object> pipelineDetails = new HashMap<>();
        if (arcProject == null) 
        	return pipelineDetails;
        
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
            throw new PipelineNotConfiguredException();
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
