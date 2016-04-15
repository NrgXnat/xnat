package org.nrg.xnat.restlet.actions;

/*
 * org.nrg.xnat.restlet.actions.importer.handlers.AutomationBasedImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptOutput;
import org.nrg.automation.entities.ScriptOutput.Status;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.files.utils.RestFileUtils;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.services.messaging.automation.AutomatedScriptRequest;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.WorkflowUtils;
import org.json.JSONArray;
import org.json.JSONException;
//import net.sf.json.JSONObject;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.zip.ZipOutputStream;

import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.EventUtils.CATEGORY;
import org.nrg.xft.event.EventUtils.TYPE;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.EventRequirementAbsent;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;


/**
 * @author Mike Hodge <hodgem@mir.wustl.edu>
 *
 */
@ImporterHandler(handler = "automation", allowCallsWithoutFiles = true, callPartialUriWrap = false)
public class AutomationBasedImporter extends ImporterHandlerA implements Callable<List<String>> {
 	
    private final ScriptRunnerService _service = XDAT.getContextService().getBean(ScriptRunnerService.class);

	static final String[] ZIP_EXT={".zip",".jar",".rar",".ear",".gar",".xar"};

	private static final String STATUS_COMPLETE = "Complete";
	private static final String CACHE_CONSTANT = "_CACHE_";
	private static final String CONFIG_TOOL = "resource_config";
	private static final String CONFIG_SCRIPT_PATH = "script";
	private static final String EMAIL_SUBJECT = "AutomationBasedImporter results";

	static Logger logger = Logger.getLogger(AutomationBasedImporter.class);
	
	private final FileWriterWrapperI fw;
	private final UserI user;
	final Map<String,Object> params;
	private final List<String> returnList = new ArrayList<String>();;
	private String configuredResource; 
	// Is this useful?  Do we want it to be configurable?
	private boolean sendAdminEmail = false;
	
	/**
	 * 
	 * @param listenerControl
	 * @param u
	 * @param session
	 * @param overwrite:   'append' means overwrite, but preserve un-modified content (don't delete anything)
	 *                      'delete' means delete the pre-existing content.
	 * @param additionalValues: should include project (subject and experiment are expected to be found in the archive)
	 */
	public AutomationBasedImporter(Object listenerControl, UserI u, FileWriterWrapperI fw, Map<String, Object> params) {
		super(listenerControl, u, fw, params);
		this.user=u;
		this.fw=fw;
		this.params=params;
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<String> call() throws ClientException, ServerException {
		try {
			processUpload();
			this.completed("Success");
			return returnList;
		} catch (ClientException e) {
			logger.error("",e);
			this.failed(e.getMessage());
			throw e;
		} catch (ServerException e) {
			logger.error("",e);
			this.failed(e.getMessage());
			throw e;
		} catch (Throwable e) {
			logger.error("",e);
			throw new ServerException(e.getMessage(),new Exception());
		}
	}

	/*
	@SuppressWarnings("deprecation")
	private void clientFailed(final String fmsg) throws ClientException {
		this.failed(fmsg);
		throw new ClientException(fmsg,new Exception());
	}
	*/

	private void processUpload() throws ClientException,ServerException {
		
		String cachePath = ArcSpecManager.GetInstance().getGlobalCachePath();
		
		final Object processParam = params.get("process");
		final Object configuredResourceParam = params.get("configuredResource");
		final Object buildPathParam = params.get("buildPath");
		final Object sendemailParam = params.get("sendemail");
		final boolean doProcess = processParam!=null && processParam.toString().equalsIgnoreCase("true");
		configuredResource = (configuredResourceParam!=null) ? configuredResourceParam.toString() : CACHE_CONSTANT;
		
		// Uploads to configured resources are handled on the client side.  Only the workflow is generated by this resource
		if (doProcess && !configuredResource.equalsIgnoreCase(CACHE_CONSTANT)) {
			doAutomation();
			return;
		}
		
		// Multiple uploads are allowed to same space (processing will take place when process parameter=true).  Use specified build path when
		// one is given, otherwise create new one
		String specPath=null;
		String buildPath = null;
		if (buildPathParam!=null) {
			// If buildpath parameter is specified and valid, use it
			specPath=buildPathParam.toString();
			if (specPath.indexOf(cachePath)>=0 && specPath.indexOf("user_uploads")>=0 &&
					specPath.indexOf(File.separator + user.getID() + File.separator)>=0 && new File(specPath).isDirectory()) {
				buildPath=specPath;
			} else {
				throw new ClientException("ERROR:  Specified build path is invalid or directory does not exist.");
			}
		} else  if (specPath==null && !(fw==null && doProcess)) {
			final Date d = Calendar.getInstance().getTime();
			final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyyMMdd_HHmmss");
			final String uploadID = formatter.format(d);
			// Save input files to cache space
			buildPath = cachePath + "user_uploads/" + user.getID() + "/" + uploadID + "/";
		} 
		
		File cacheLoc = null;
		if (buildPath!=null) {
			cacheLoc = new File(buildPath);
			cacheLoc.mkdirs();
		}
		
		// If uploading a file, process it.
		if (fw!=null && cacheLoc!=null) {
			processFile(cacheLoc, specPath);
		} 
		
		// Conditionally process cache location files, otherwise return cache location
		if (doProcess) {
			doAutomation();
			if (sendemailParam!=null && sendemailParam.toString().equalsIgnoreCase("true")) {
				sendUserEmail(sendAdminEmail);
			} else if (sendAdminEmail) {
				sendAdminEmail();
			}
		} else if (buildPath!=null) {
			returnList.add(buildPath);
		}
		
	}
	
	private String returnListToHtmlString() {
		final StringBuilder sb = new StringBuilder("<br/>");
		for (final String s : returnList) {
			sb.append(s).append("<br/>\t");
		}
		return sb.toString();
	}
	
	private void sendUserEmail(boolean ccAdmin) {
			AdminUtils.sendUserHTMLEmail(EMAIL_SUBJECT, returnListToHtmlString(), ccAdmin, new String[] { user.getEmail() });
	}
	
	private void sendAdminEmail() {
			AdminUtils.sendAdminEmail(user,EMAIL_SUBJECT, returnListToHtmlString());
	}

	private void processFile(final File cacheLoc,final  String specPath) throws ClientException {
		final String fileName;
		try {
			fileName = URLDecoder.decode(fw.getName(),"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			throw new ClientException("Could not decode file name.",e1);
		}
		final Object extractParam = params.get("extract");
		if (extractParam!=null && extractParam.toString().equalsIgnoreCase("true") && isZipFileName(fileName)) {
			final ZipI zipper = getZipper(fileName);
			try {
				zipper.extract(fw.getInputStream(),cacheLoc.getAbsolutePath());
			} catch (Exception e) {
				throw new ClientException("Archive file is corrupt or not a valid archive file type.");
			}
		} else {
			final File cacheFile = new File(cacheLoc,fileName);
			try {
				fw.write(cacheFile);
				// Uploading directories via linux (and likely Mac) will not fail due to "Everything is a file".  In such cases we wind 
				// up with a "file" generated.  Check for these "files".  Remove them, and thrown an exception.
				// Windows uploads of directories should fail before hitting this class.
				removeAndThrowExceptionIfDirectory(cacheFile);
			} catch (ClientException e) {
				throw e;
			} catch (Exception e) {
				throw new ClientException("Could not write uploaded file.",e);
			}
		}
	}
	
	private void removeAndThrowExceptionIfDirectory(File file) throws ClientException {
		if (RestFileUtils.isFileRepresentationOfDirectory(file)) {
			if (file.delete()) {
				throw new ClientException("Upload of directories is not currently supported.  To upload a directory, please create a zip archive.");
			};
		}
	}

	private void doAutomation() throws ClientException, ServerException {
		
		returnList.add("<b>BEGIN PROCESSING UPLOADED FILES</b><br>");
		
		final Map<String,Object> passMap = Maps.newHashMap();
		final Object eventHandlerParam = params.get("eventHandler");
		final Object projectParam = params.get("project");
		final Object subjectParam = params.get("subject");
		final Object experimentParam = params.get("experiment");
		final Object buildPathParam = params.get("buildPath");
		final Object passedParametersParam = params.get("passedParameters");
		String eventText = null; 
		XnatProjectdata proj = null;
		XnatSubjectdata subj = null;
		XnatExperimentdata exp = null;
		if (projectParam!=null && projectParam.toString().length()>0) {
			proj = XnatProjectdata.getXnatProjectdatasById(projectParam, user, false);
			if (proj != null) {
				passMap.put("project", proj.getId());
			}
		}
		if (subjectParam!=null && subjectParam.toString().length()>0) {
			subj = XnatSubjectdata.getXnatSubjectdatasById(subjectParam.toString(), user, false);
			if (subj == null) {		
				subj = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subjectParam.toString(), user, false);
			}
			if (subj != null) {		
				passMap.put("subject", subj.getId());
			}
		}
		if (experimentParam!=null && experimentParam.toString().length()>0) {
			exp = XnatExperimentdata.getXnatExperimentdatasById(experimentParam.toString(), user, false);
			if (exp == null) {
				exp = XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), experimentParam.toString(), user, false);
			}
			if (exp != null) {
				passMap.put("experiment", exp.getId());
			}
		}
		if (passedParametersParam!=null && passedParametersParam.toString().length()>0) {
			try {
				final String passedParametersJsonStr = URLDecoder.decode(passedParametersParam.toString(),"UTF-8");
				final JSONObject json = new JSONObject(passedParametersJsonStr);
				passMap.put("passedParameters", json);
			} catch(UnsupportedEncodingException | JSONException e) {
				// Do nothing for now.
			}
		}
		if (!(configuredResource==null || configuredResource.equalsIgnoreCase(CACHE_CONSTANT))) {
			eventText = getEventTextFromConfiguredResourceConfig(proj, subj, exp, configuredResource);
			passMap.put("configuredResource", configuredResource);
			
		}
		if (buildPathParam!=null) {
			passMap.put("BuildPath", buildPathParam);
		}
		if (eventHandlerParam!=null && eventHandlerParam.toString().length()>0) {
			eventText = eventHandlerParam.toString();
		}	
		if (eventText!=null) {
			PersistentWorkflowI wrk = buildWorkflow(proj,passMap,eventText);
			// Launch automation script for workflow handler
			if (wrk instanceof WrkWorkflowdata) {
				ScriptOutput scriptOut = launchScript((WrkWorkflowdata)wrk);
				if (scriptOut!=null) {
					returnList.add("<br><b>SCRIPT EXECUTION RESULTS</b>");
					returnList.add("<br><b>FINAL STATUS:  " + scriptOut.getStatus() + "</b>");
					if (scriptOut.getStatus().equals(Status.ERROR) && scriptOut.getResults()!=null && scriptOut.getResults().toString().length()>0) {
						returnList.add("<br><b>SCRIPT RESULTS</b>");
						returnList.add(scriptOut.getResults().toString().replace("\n", "<br>"));
					}
					if (scriptOut.getOutput()!=null && scriptOut.getOutput().length()>0) {
						returnList.add("<br><b>SCRIPT STDOUT</b>");
						returnList.add(scriptOut.getOutput().replace("\n", "<br>"));
					}
					if (scriptOut.getErrorOutput()!=null && scriptOut.getErrorOutput().length()>0) {
						returnList.add("<br><b>SCRIPT STDERR/EXCEPTION</b>");
						returnList.add(scriptOut.getErrorOutput().replace("\n", "<br>"));
					}
				}
			}
		}
		
		returnList.add("<br><b>FINISHED PROCESSING");
		
	}

 	private PersistentWorkflowI buildWorkflow(XnatProjectdata proj,Map<String, Object> passMap,String eventText) {
		final PersistentWorkflowI wrk;
		try {
			returnList.add("Building workflow entry for configured resource / event handler - " + eventText);
			wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, params.get("xsiType").toString(), proj.getId(), proj.getId(),
			EventUtils.newEventInstance(CATEGORY.DATA, TYPE.WEB_SERVICE, eventText, "Automation-based upload", null));
			wrk.setStatus(STATUS_COMPLETE);
			//wrk.setDetails(JSONObject.fromObject(passMap).toString());
			wrk.setDetails((new JSONObject(passMap)).toString());
			final EventMetaI em = wrk.buildEvent();
			returnList.add("Saving workflow - " + eventText);
			// Make workflow save request, requesting that the workflow not trigger an event.  We want to make
			// the save request here.
			PersistentWorkflowUtils.save(wrk, em, false, false);
			return wrk;
		} catch (EventRequirementAbsent e1) {
			returnList.add("ERROR:  error generating workflow -" + e1.toString());
			throw new NullPointerException(e1.getMessage());
		} catch (Exception e) {
			returnList.add("ERROR:  error generating workflow -" + e.toString());
			logger.error("",e);
		}
		return null;
	}

    public ScriptOutput launchScript(WrkWorkflowdata wrk) {
        if (StringUtils.equals(PersistentWorkflowUtils.COMPLETE, wrk.getStatus()) && !StringUtils.equals(wrk.getExternalid(), PersistentWorkflowUtils.ADMIN_EXTERNAL_ID)) {
            //check to see if this has been handled before
        	ScriptOutput scriptOut = null;
            for (final Script script : getScripts(wrk.getExternalid(), wrk.getPipelineName())) {
                try {
                    //create a queued workflow to track this script
                    final String action = "Executed script " + script.getScriptId();
                    final String justification = wrk.getJustification();
                    final String comment = "Executed script " + script.getScriptId() + " triggered by event " + wrk.getPipelineName();
                    final PersistentWorkflowI scriptWrk = PersistentWorkflowUtils.buildOpenWorkflow(wrk.getUser(), wrk.getDataType(), wrk.getId(), wrk.getExternalid(),
                    		EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.PROCESS, action, StringUtils.isNotBlank(justification) ? justification : "Automated execution: " + comment, comment));
                    assert scriptWrk != null;
                    scriptWrk.setStatus(PersistentWorkflowUtils.QUEUED);
                    WorkflowUtils.save(scriptWrk, scriptWrk.buildEvent());
                    final AutomatedScriptRequest request = new AutomatedScriptRequest(wrk.getWrkWorkflowdataId().toString(), wrk.getUser(), script.getScriptId(), 
                    		wrk.getPipelineName(), scriptWrk.getWorkflowId().toString(), wrk.getDataType(), wrk.getId(), wrk.getExternalid());
                    scriptOut = executeScriptRequest(request);
                    return scriptOut;	
                } catch (Exception e1) {
                	if (scriptOut == null) {
                		scriptOut = new ScriptOutput();
                	}
                	scriptOut.setErrorOutput((scriptOut.getErrorOutput()!=null) ? scriptOut.getErrorOutput() +
                			ExceptionUtils.getStackTrace(e1) : ExceptionUtils.getStackTrace(e1));
                	scriptOut.setOutput((scriptOut.getOutput()!=null) ? scriptOut.getOutput() : "");
                    logger.error("", e1);
                    return scriptOut;
                }
            }
        }
		return null;
    }

    private ScriptOutput executeScriptRequest(AutomatedScriptRequest request) throws Exception {
    	
        final PersistentWorkflowI workflow = WorkflowUtils.getUniqueWorkflow(request.getUser(), request.getScriptWorkflowId());
        workflow.setStatus(PersistentWorkflowUtils.IN_PROGRESS);
        WorkflowUtils.save(workflow, workflow.buildEvent());

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("user", request.getUser());
        parameters.put("scriptId", request.getScriptId());
        parameters.put("event", request.getEvent());
        parameters.put("srcWorkflowId", request.getSrcWorkflowId());
        parameters.put("scriptWorkflowId", request.getScriptWorkflowId());
        parameters.put("dataType", request.getDataType());
        parameters.put("dataId", request.getDataId());
        parameters.put("externalId", request.getExternalId());
        parameters.put("workflow", workflow);

        ScriptOutput scriptOut = null;
        try {
            scriptOut = _service.runScript(_service.getScript(request.getScriptId()), null, parameters, false);
            if (PersistentWorkflowUtils.IN_PROGRESS.equals(workflow.getStatus())) {
                WorkflowUtils.complete(workflow, workflow.buildEvent());
            }
        } catch (NrgServiceException e) {
            final String message = String.format("Failed running the script %s by user %s for event %s on data type %s instance %s from project %s",
                    request.getScriptId(),
                    request.getUser().getLogin(),
                    request.getEvent(),
                    request.getDataType(),
                    request.getDataId(),
                    request.getExternalId());
            AdminUtils.sendAdminEmail("Script execution failure", message);
            logger.error(message, e);
            if (PersistentWorkflowUtils.IN_PROGRESS.equals(workflow.getStatus())) {
                WorkflowUtils.fail(workflow, workflow.buildEvent());
            }
        }
        return scriptOut;
        
	}

	private List<Script> getScripts(final String projectId, final String event) {
        final List<Script> scripts = Lists.newArrayList();

        //project level scripts
        if (StringUtils.isNotBlank(projectId)) {
            final Script script = _service.getScript(Scope.Project, projectId, event);
            if (script != null) {
                scripts.add(script);
            }
        }

        //site level scripts
        final Script script = _service.getScript(Scope.Site, null, event);
        if (script != null) {
            scripts.add(script);
        }

        return scripts;
    }
 	

	// TODO:  Configured Resource configuration should be updated to store EVENT, rather than using this "Uploaded {label}" event. 
	private String getEventTextFromConfiguredResourceConfig(XnatProjectdata proj, XnatSubjectdata subj, XnatExperimentdata exp, String configuredResource) {
		final Scope scope = (exp!=null) ? Scope.Experiment : (subj!=null) ? Scope.Subject : Scope.Project; 
		final String crConfig = XDAT.getConfigService().getConfigContents(CONFIG_TOOL,CONFIG_SCRIPT_PATH,scope,proj.getId());
		if (crConfig!=null && crConfig.length()>0) {
			try {
				final JSONArray jsonArray = new JSONArray(new JSONTokener(crConfig));
				for (int i=0;i<jsonArray.length();i++) {
					final JSONObject jsonObj = jsonArray.getJSONObject(i);
					if (jsonObj.getString("name").equals(configuredResource)) {
						return "Uploaded " + jsonObj.getString("label");
					}
				}
			} catch (JSONException e) {
				logger.warn("WARNING:  Could not parse resource_config json results (PROJECT=" + proj.getId() + ",CONFIG_TOOL=" + CONFIG_TOOL + ",CONFIG_PATH=" + CONFIG_SCRIPT_PATH);
			}
		}
		return null;
	}

	private boolean isZipFileName(final String fileName) {
		for (final String ext : ZIP_EXT) {
			if (fileName.toLowerCase().endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

	private ZipI getZipper(final String fileName) {
		
		// Assume file name represents correct compression method
		String file_extension = null;
		if (fileName!=null && fileName.indexOf(".")!=-1) {
			file_extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
			if (Arrays.asList(ZIP_EXT).contains(file_extension)) {
				return new ZipUtils();
			} else if (file_extension.equalsIgnoreCase(".tar")) {
				return new TarUtils();
			} else if (file_extension.equalsIgnoreCase(".gz")) {
				TarUtils zipper = new TarUtils();
				zipper.setCompressionMethod(ZipOutputStream.DEFLATED);
				return zipper;
			}
		}
		// Assume zip-compression for unnamed inbody files
		return new ZipUtils();
		
	}

}

