package org.nrg.xapi.rest.pipeline;

import static org.nrg.xdat.security.helpers.AccessLevel.Edit;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.pipeline.PipelineLaunchHandler;
import org.nrg.pipeline.PipelineLaunchReport;
import org.nrg.pipeline.PipelineLaunchStatus;
import org.nrg.pipeline.ProcessLauncher;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.ProjectId;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.ResourceData;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Mohana Ramaratnam
 *
 */
@Api(description = "XNAT Traditional Pipeline Management API")
@XapiRestController
@RequestMapping(value = "/pipelines")
@Slf4j
public class PipelineApi extends AbstractXapiRestController {
	
    private static final org.slf4j.Logger launchLogger = LoggerFactory.getLogger("org.nrg.pipeline.launch");

	
	    
		@Autowired
	    public PipelineApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder,final CatalogService catalogService,final JdbcTemplate jdbcTemplate) {
	        super(userManagementService, roleHolder);
	        this.catalogService = catalogService;
	        _jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		}
	  
	    @XapiRequestMapping(value = {"/launch/{pipelineNameOrStep}/{projectId}"}, method = POST, restrictTo = Edit, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	    @ApiOperation(value = "Resolve the parameters and launch the pipeline")
	    public ResponseEntity<PipelineLaunchReport> launchPipelineWQueryParams(@PathVariable("projectId") @ProjectId final String projectId,
	    																			 @PathVariable("pipelineNameOrStep") final String pipelineNameOrStep, 
	    																			 final @RequestBody Map<String, String> allRequestParams) {
			UserI user = getSessionUser();
			XnatProjectdata project = XnatProjectdata.getProjectByIDorAlias(projectId, user, false);
	    	if (project == null) {
				return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
			}
	    	PipelineLaunchReport launchReport = new PipelineLaunchReport(); 
	        _log.info("Launch requested for project id " + projectId);
	        
	        List<PipelineLaunchStatus> pipelineLaunch  = new ArrayList<PipelineLaunchStatus>();
	        int successCount = 0;
	        int failureCount = 0;

	        	//This JSON may look like
	        	//{"Experiments":"[\"/archive/experiments/XNAT_E00001\",\"/archive/experiments/XNAT_E00003\"]","create_nii":"Y","overwrite":"Y"}
	        	String experimentIdOrUri = allRequestParams.get(EXPERIMENTS);
	        	if (experimentIdOrUri == null) {
	        		experimentIdOrUri = allRequestParams.get(EXPERIMENTS.toLowerCase());
	        	}
	        	if (experimentIdOrUri == null) {
	        		experimentIdOrUri = allRequestParams.get(EXPERIMENTS.toUpperCase());
	        	}

	        	if (experimentIdOrUri == null) {
	        		launchReport.setSuccesses(0);
	        		launchReport.setFailures(0);
	    	        return  new ResponseEntity<>(launchReport, HttpStatus.BAD_REQUEST);
	        	}
	        	Map<String,String> queryParams = extractJustParameters(allRequestParams);
	        	Map<String,String> schemaLinkParams = extractSchemaLinkParameters(allRequestParams);
	        	
	        	String[] splits =  experimentIdOrUri.replace("[","").replace("]","").split(",");
	        	ArrayList<String> uriAsList = new ArrayList<>(Arrays.asList(splits));
	        
	        	for (String expIdOrUri : uriAsList) {
	        		try {
				        String experimentId = getExperimentIdFromUri(expIdOrUri.replace("\"", ""));
				        XnatExperimentdata exp = XnatExperimentdata.getXnatExperimentdatasById(experimentId, user, false);
				        if (exp == null) {
			        		pipelineLaunch.add(markLaunchFailure(projectId,experimentId, ""));
			        		failureCount++;
				        }else {
				        	Map<String,String> qParams = new HashMap<String,String>();
				        	Map<String,String> bodyParams = new HashMap<String,String>();
				        	Map<String,String> xmlDocumentParams = new HashMap<String,String>();
				        	String XMLbody = "";
				        	qParams.putAll(queryParams);
				        	Map<String, String> resolvedSchemaLink = resolveSchemaLink(exp,schemaLinkParams);
				        	qParams.putAll(resolvedSchemaLink);
				        	
				        	PipelineLaunchHandler pipelineLaunchHandler = new PipelineLaunchHandler(project,exp,pipelineNameOrStep);
				        	boolean status = pipelineLaunchHandler.handleLaunch(bodyParams, qParams, xmlDocumentParams, XMLbody, user);
				        	if (status) {
				        		successCount++;
				        		pipelineLaunch.add(markLaunchSuccess(projectId,experimentId,exp.getLabel()));
				        	}else {
				        		failureCount++;
				        		pipelineLaunch.add(markLaunchFailure(projectId,experimentId,exp.getLabel()));
				        	}
				        }
	        		}catch(Exception ce) {
	        			failureCount++;
		        		pipelineLaunch.add(markLaunchFailure(projectId,expIdOrUri,""));
	        		}
				        
				  }
	        	launchReport.setSuccesses(successCount);
	        	launchReport.setFailures(failureCount);
	        	launchReport.setExperimentLaunchStatuses(pipelineLaunch);
	        	launchReport.setParams(queryParams);
	        	
	        return  new ResponseEntity<>(launchReport, HttpStatus.OK);
	    }

	    
	    @XapiRequestMapping(value = {"/terminate/{pipelineNameOrStep}/{projectId}"}, method = POST, restrictTo = Edit, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	    @ApiOperation(value = "Resolve the parameters and terminate the pipeline")
	    public ResponseEntity<PipelineLaunchReport> terminate(@PathVariable("pipelineNameOrStep") final String pipelineNameOrStep, 
	    													  @PathVariable("projectId") @ProjectId final String projectId,											  
	    													  final @RequestBody Map<String, String> allRequestParams) {
			UserI user = getSessionUser();
	    	PipelineLaunchReport launchReport = new PipelineLaunchReport(); 
	        int successCount = 0;
	        int failureCount = 0;
	        List<PipelineLaunchStatus> pipelineLaunch  = new ArrayList<PipelineLaunchStatus>();
	        Map<String,String> idToLabel = new HashMap<String,String>();
	        
        	//This JSON may look like
        	//{"experiments":"[\"/archive/experiments/XNAT_E00001\",\"/archive/experiments/XNAT_E00003\"]", "pipelinePath":"/data/pipeline/catalog/dicom/DicomToNifti.xml"}
	        	String experimentIdOrUri = allRequestParams.get(EXPERIMENTS);
	        	String pipelinePath = allRequestParams.get("pipelinePath");
	        	
	        	
	        	if (experimentIdOrUri == null) {
	        		experimentIdOrUri = allRequestParams.get(EXPERIMENTS.toLowerCase());
	        	}
	        	if (experimentIdOrUri == null) {
	        		experimentIdOrUri = allRequestParams.get(EXPERIMENTS.toUpperCase());
	        	}

	        	if (experimentIdOrUri == null || pipelinePath == null) {
	        		launchReport.setSuccesses(0);
	        		launchReport.setFailures(0);
	    	        return  new ResponseEntity<>(launchReport, HttpStatus.BAD_REQUEST);
	        	}
        	
	        	String[] splits =  experimentIdOrUri.replace("[","").replace("]","").split(",");
	        	ArrayList<String> uriAsList = new ArrayList<>(Arrays.asList(splits));
	        	ArrayList<String> experimentIds = new ArrayList<String>();
	        	for (String expIdOrUri : uriAsList) {
			        try {
			        	String experimentId = getExperimentIdFromUri(expIdOrUri.replace("\"", ""));
				        XnatExperimentdata exp = XnatExperimentdata.getXnatExperimentdatasById(experimentId, user, false);
				        if (exp == null) {
			        		pipelineLaunch.add(markLaunchFailure(projectId,experimentId, ""));
			        		failureCount++;
				        }else {
				        	experimentIds.add(experimentId);
				        	idToLabel.put(experimentId, exp.getLabel());
				        }
			        }catch(ClientException ce) {
	        			failureCount++;
		        		pipelineLaunch.add(markLaunchFailure(projectId,expIdOrUri,""));
			        }
	        	}

	        List<Map<String,Object>> dbWorkflowRows = 	geLatestWorkflow(user,pipelinePath,experimentIds,projectId);
	        for (Map row : dbWorkflowRows) {
	        	Integer wId = (Integer) row.get("wrk_workflowdata_id");
	        	String jId = (String) row.get("jobId");
	        	String experimentId = (String) row.get("id");
	        	String label = idToLabel.get(experimentId);
	        	boolean successStatus = triggerTerminate(user,experimentId,wId.toString(),jId,label,projectId,pipelinePath);
	        	if (successStatus) {
        			successCount++;
	        		pipelineLaunch.add(markLaunchSuccess(projectId,experimentId,""));
	        	}else {
        			failureCount++;
	        		pipelineLaunch.add(markLaunchFailure(projectId,experimentId,""));
	        	}
	        }
	        
	        idToLabel.put("pipelinePath", pipelinePath);
        	launchReport.setSuccesses(successCount);
        	launchReport.setFailures(failureCount);
        	launchReport.setParams(idToLabel);
        	return  new ResponseEntity<>(launchReport, HttpStatus.OK);

	    }
	    
	    private boolean terminateFileExists(String command) {
	    	File f = new File(command);
	    	return (f.exists() && f.canExecute());
	    }
	    
		private boolean triggerTerminate(UserI user,String xnatId, String workflowId, String jobId,String label, String projectId, String pipelinePath) {
		    boolean success = false;
			String   command = Paths.get(XDAT.getSiteConfigPreferences().getPipelinePath(), "bin", "killxnatpipeline").toString();
		        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS")) {
		            command += ".bat";
		        }
		       
		        boolean fileExists = terminateFileExists(command);
		        if (!fileExists) {
		        	return false;
		        }
		        
		        final String pipelineUrl = XDAT.safeSiteConfigProperty("processingUrl", "");
		        String host = StringUtils.isNotBlank(pipelineUrl) ? pipelineUrl : TurbineUtils.GetFullServerPath();


		        AliasToken token = XDAT.getContextService().getBean(AliasTokenService.class).issueTokenForUser(user);
		        List<String> arguments = new ArrayList<>();
		        arguments.add("-id");
		        arguments.add(xnatId);
		        arguments.add("-host");
		        arguments.add(host);
		        arguments.add("-u");
		        arguments.add(token.getAlias());
		        arguments.add("-pwd");
		        arguments.add(token.getSecret());
		        arguments.add("-workflowId");
		        arguments.add(workflowId);
		        arguments.add("-jobId");
		        arguments.add(jobId==null?"null":jobId);
		        arguments.add("-pipelinePath");
		        arguments.add(pipelinePath);
		        arguments.add("-project");
		        arguments.add(projectId);
		        arguments.add("-label");
		        arguments.add(label);

			try {

	            if (launchLogger.isInfoEnabled()) {
	                launchLogger.info("Terminating pipeline with command: " + command);
	            }

	            ProcessLauncher processLauncher = new ProcessLauncher();
	            processLauncher.setCommand(command);
	            processLauncher.start();
	        } catch (Exception e) {
	            launchLogger.error(e.getMessage() + " for command " + command, e);
	            success = false;
	        }
			return success;
		}

	    
	    
		private   HashMap<String,String> extractJustParameters(Map<String, String> allRequestParams) {
	    	 HashMap<String,String> params = new HashMap<String,String>();
	    	 for (Map.Entry<String,String> entry : allRequestParams.entrySet())  {
	    		 String key = entry.getKey();
	    		 String value = entry.getValue();
	    		 if (!EXPERIMENTS.equalsIgnoreCase(key) && !key.startsWith(SCHEMALINK_NAME_START)) {
	    			 params.put(key, value);
	    		 }
	    	 }
	    	 return params;
	    }

		private   HashMap<String,String> extractSchemaLinkParameters(Map<String, String> allRequestParams) {
	    	 HashMap<String,String> params = new HashMap<String,String>();
	    	 for (Map.Entry<String,String> entry : allRequestParams.entrySet())  {
	    		 String key = entry.getKey();
	    		 String value = entry.getValue();
	    		 if (!EXPERIMENTS.equalsIgnoreCase(key) && key.startsWith(SCHEMALINK_NAME_START)) {
	    			 params.put(key.substring(SCHEMALINK_NAME_START.length()), value);
	    		 }
	    	 }
	    	 return params;
	    }
		
		private Map<String,String> resolveSchemaLink(ItemI om, Map<String,String>schemaLinkParam) throws Exception {
			Map<String,String> resolvedValues = new HashMap<String,String>();
			for (Map.Entry<String,String> entry : schemaLinkParam.entrySet()) {
				String schemaLink = entry.getKey();
				String resolvedValue = resolveValues(om, schemaLink);
			}
			return resolvedValues;
		}
		
		private String resolveValues(ItemI om, String schemaLink) throws Exception {
			String rtn = ""; 
			Object o = om.getItem().getProperty(schemaLink, true);
             if (o != null) {
                 try {
                     ArrayList<? extends Class> matches = (ArrayList<? extends Class>) o;
                     if (matches.size() == 1) {
                          rtn = "" + matches.get(0);
                     } else {
                         rtn = "[" + StringUtils.join(matches.toArray(), ",") + "]";
                     }
                 } catch (ClassCastException cce) {
                     rtn = "" + o;
                 }
             }
             return rtn;
		}

	    private PipelineLaunchStatus markLaunchSuccess(String projectId, String expId,String label) {
	    	PipelineLaunchStatus pStatus = new PipelineLaunchStatus();
	    	pStatus.setStatus("SUCCESS");
	    	pStatus.setProject(projectId);
	    	pStatus.setExptId(expId);
	    	pStatus.setExptLabel(label);
	    	return pStatus;
	    }

	    private PipelineLaunchStatus markLaunchFailure(String projectId, String expId, String label) {
	    	PipelineLaunchStatus pStatus = new PipelineLaunchStatus();
	    	pStatus.setStatus("FAILURE");
	    	pStatus.setProject(projectId);
	    	pStatus.setExptId(expId);
	    	pStatus.setExptLabel(label);
	    	return pStatus;
	    }

	    
		private String getExperimentIdFromUri(String xnatIdOrUri) throws ClientException {
			String xnatId = null;
			ResourceData resourceData = catalogService.getResourceDataFromUri(xnatIdOrUri);
            ArchivableItem item = resourceData.getItem();
            xnatId = item.getId(); 
            return xnatId;
		}
		
		private List<Map<String,Object>> geLatestWorkflow(final UserI user, final String pipeline_name, List<String> experimentIds, final String projectId ){
			
			String query = "SELECT inn.wrk_workflowdata_id, inn.jobid, inn.launch_time, inn.externalid,  inn.id";
			query +=       " FROM ";
			query +=        "  ( ";
			query +=              " SELECT t.wrk_workflowdata_id, t.jobid, t.launch_time, t.externalid,  t.id, ";
			query +=               " ROW_NUMBER() OVER (PARTITION BY t.id ORDER BY t.launch_time desc) num";
			query +=               " FROM wrk_workflowdata t where pipeline_name=:PIPELINE_NAME and externalid=:PROJECT and id in (:IDS)";
			query +=        "  ) inn ";
			query +=       " WHERE inn.num = 1";
			
			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("PIPELINE_NAME", pipeline_name);
			parameters.addValue("PROJECT", projectId);
			parameters.addValue("IDS", experimentIds);
			
			return _jdbcTemplate.queryForList(query,parameters);
		}

	    private static final Logger _log = LoggerFactory.getLogger(PipelineApi.class);
	    private final String EXPERIMENTS = "Experiments";
	    private final String SCHEMALINK_NAME_START = "xnatschemaLink-";
	    private final CatalogService catalogService;
	    private final NamedParameterJdbcTemplate _jdbcTemplate;

}
