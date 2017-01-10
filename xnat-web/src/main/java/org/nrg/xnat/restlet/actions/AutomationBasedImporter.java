/*
 * web: org.nrg.xnat.restlet.actions.AutomationBasedImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.automation.entities.ScriptOutput;
import org.nrg.automation.entities.ScriptOutput.Status;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.event.entities.AutomationCompletionEvent;
import org.nrg.automation.event.entities.AutomationEventIdsIds;
import org.nrg.automation.services.AutomationEventIdsIdsService;
import org.nrg.automation.services.AutomationEventIdsService;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.event.Filterable;
import org.nrg.framework.services.NrgEventService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.EventUtils.CATEGORY;
import org.nrg.xft.event.EventUtils.TYPE;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.EventRequirementAbsent;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.event.listeners.AutomationCompletionEventListener;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.files.utils.RestFileUtils;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.zip.ZipOutputStream;

@ImporterHandler(handler = "automation", allowCallsWithoutFiles = true, callPartialUriWrap = false)
public class AutomationBasedImporter extends ImporterHandlerA implements Callable<List<String>> {

	/** The Constant ZIP_EXT. */
	static final String[] ZIP_EXT = { ".zip", ".jar", ".rar", ".ear", ".gar", ".xar" };
	
	/** The Constant STATUS_COMPLETE. */
	private static final String STATUS_COMPLETE = "Complete";

	/** The Constant CACHE_CONSTANT. */
	private static final String CACHE_CONSTANT = "_CACHE_";

	/** The Constant CONFIG_TOOL. */
	private static final String CONFIG_TOOL = "resource_config";

	/** The Constant CONFIG_SCRIPT_PATH. */
	private static final String CONFIG_SCRIPT_PATH = "script";

	/** The Constant EMAIL_SUBJECT. */
	// private static final String EMAIL_SUBJECT = "AutomationBasedImporter
	// results";

	/** The Constant TIMEOUT_SECONDS. */
	private static final int TIMEOUT_SECONDS = 600;

	/** The logger. */
	static Logger logger = Logger.getLogger(AutomationBasedImporter.class);

	/** The fw. */
	private final FileWriterWrapperI fw;

	/** The user. */
	private final UserI user;

	/** The params. */
	final Map<String, Object> params;

	/** The return list. */
	private final List<String> returnList = new ArrayList<String>();;

	/** The configured resource. */
	private String configuredResource;

	/**
	 *  The send admin email.
	 *
	 * @param listenerControl the listener control
	 * @param u the u
	 * @param fw the fw
	 * @param params the params
	 */
	// Is this useful? Do we want it to be configurable?
	// private boolean sendAdminEmail = false;

	/**
	 * Instantiates a new automation based importer.
	 *
	 * @param listenerControl
	 *            the listener control
	 * @param u
	 *            the u
	 * @param fw
	 *            the fw
	 * @param params
	 *            the params
	 */
	public AutomationBasedImporter(Object listenerControl, UserI u, FileWriterWrapperI fw, Map<String, Object> params) {
		super(listenerControl, u, fw, params);
		this.user = u;
		this.fw = fw;
		this.params = params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nrg.xnat.restlet.actions.importer.ImporterHandlerA#call()
	 */
	@SuppressWarnings("deprecation")
	@Override
	public List<String> call() throws ClientException, ServerException {
		try {
			processUpload();
			this.completed("Success");
			return returnList;
		} catch (ClientException e) {
			logger.error("", e);
			this.failed(e.getMessage());
			throw e;
		} catch (ServerException e) {
			logger.error("", e);
			this.failed(e.getMessage());
			throw e;
		} catch (Throwable e) {
			logger.error("", e);
			throw new ServerException(e.getMessage(), new Exception());
		}
	}

	/**
	 * Process upload.
	 *
	 * @throws ClientException
	 *             the client exception
	 * @throws ServerException
	 *             the server exception
	 */
	private void processUpload() throws ClientException, ServerException {

		String cachePath = ArcSpecManager.GetInstance().getGlobalCachePath();

		final Object processParam = params.get("process");
		final Object configuredResourceParam = params.get("configuredResource");
		final Object buildPathParam = params.get("buildPath");
		final Object eventHandlerParam = params.get("eventHandler");
		final Object escapeHtmlParam = params.get("escapeHtml");
		// final Object sendemailParam = params.get("sendemail");
		final boolean doProcess = processParam != null && processParam.toString().equalsIgnoreCase("true");
		final boolean escapeHtml = escapeHtmlParam != null && escapeHtmlParam.toString().equalsIgnoreCase("true");
		configuredResource = (configuredResourceParam != null) ? configuredResourceParam.toString() : CACHE_CONSTANT;

		// Uploads to configured resources are handled on the client side. Only
		// the workflow is generated by this resource
		if (doProcess && !configuredResource.equalsIgnoreCase(CACHE_CONSTANT) && eventHandlerParam == null) {
			createWorkflowEntry(configuredResourceParam.toString());
			return;
		}
		if (doProcess && !configuredResource.equalsIgnoreCase(CACHE_CONSTANT)) {
			doAutomation(escapeHtml);
			return;
		}

		// Multiple uploads are allowed to same space (processing will take
		// place when process parameter=true). Use specified build path when
		// one is given, otherwise create new one
		String specPath = null;
		String buildPath = null;
		if (buildPathParam != null) {
			// If buildpath parameter is specified and valid, use it
			specPath = buildPathParam.toString();
			if (specPath.indexOf(cachePath) >= 0 && specPath.indexOf("user_uploads") >= 0
					&& specPath.indexOf(File.separator + user.getID() + File.separator) >= 0
					&& new File(specPath).isDirectory()) {
				buildPath = specPath;
			} else {
				throw new ClientException("ERROR:  Specified build path is invalid or directory does not exist.");
			}
		} else if (specPath == null && !(fw == null && doProcess)) {
			final Date d = Calendar.getInstance().getTime();
			final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
			final String uploadID = formatter.format(d);
			// Save input files to cache space
			buildPath = cachePath + "user_uploads/" + user.getID() + "/" + uploadID + "/";
		}

		File cacheLoc = null;
		if (buildPath != null) {
			cacheLoc = new File(buildPath);
			cacheLoc.mkdirs();
		}

		// If uploading a file, process it.
		if (fw != null && cacheLoc != null) {
			processFile(cacheLoc, specPath);
		}

		// Conditionally process cache location files, otherwise return cache
		// location
		if (doProcess) {
			doAutomation(escapeHtml);
			/*
			 * if (sendemailParam!=null &&
			 * sendemailParam.toString().equalsIgnoreCase("true")) {
			 * sendUserEmail(sendAdminEmail); } else if (sendAdminEmail) {
			 * sendAdminEmail(); }
			 */
		} else if (buildPath != null) {
			returnList.add(buildPath);
		}

	}

	/**
	 * Return list to html string.
	 *
	 * @param string the string
	 * @return the string
	 */
	/*
	 * private String returnListToHtmlString() { final StringBuilder sb = new
	 * StringBuilder("<br/>"); for (final String s : returnList) {
	 * sb.append(s).append("<br/>\t"); } return sb.toString(); }
	 */

	/**
	 * Send user email.
	 *
	 * @param ccAdmin
	 *            the cc admin
	 */
	/*
	 * private void sendUserEmail(boolean ccAdmin) {
	 * AdminUtils.sendUserHTMLEmail(EMAIL_SUBJECT, returnListToHtmlString(),
	 * ccAdmin, new String[] { user.getEmail() }); }
	 */

	/**
	 * Send admin email.
	 */
	/*
	 * private void sendAdminEmail() {
	 * AdminUtils.sendAdminEmail(user,EMAIL_SUBJECT, returnListToHtmlString());
	 * }
	 */

	private void createWorkflowEntry(String string) {
		
		final Map<String, Object> passMap = Maps.newHashMap();
		final Object projectParam = params.get("project");
		final Object subjectParam = params.get("subject");
		final Object experimentParam = params.get("experiment");
		final Object passedParametersParam = params.get("passedParameters");
		String eventText = null; 

		@SuppressWarnings("rawtypes")
		Class clazz;
		Object eventObj;
		String eventClass = WorkflowStatusEvent.class.getName();
		try {
			clazz = WorkflowStatusEvent.class;
			eventObj = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			returnList.add("ERROR: Could not instantiate event (" + eventClass + "). <br>" + e.toString());
			return;
		}
		if (!(eventObj instanceof AutomationEventImplementerI)) {
			returnList.add("ERROR: Event (" + eventClass + ") is not an AutomationEventImplementer.");
		}
		final AutomationEventImplementerI automationEvent = (AutomationEventImplementerI) eventObj;
		final String entityId;
		final String entityType;
		if (experimentParam == null && subjectParam == null) {
			entityId = projectParam.toString();
			entityType = XnatProjectdata.SCHEMA_ELEMENT_NAME;
		} else if (experimentParam == null && subjectParam != null) {
			entityId = subjectParam.toString();
			entityType = XnatSubjectdata.SCHEMA_ELEMENT_NAME;
		} else if (experimentParam != null) {
			entityId = experimentParam.toString();
			final XnatExperimentdata experimentData = XnatExperimentdata.getXnatExperimentdatasById(experimentParam,
					user, false);
			entityType = experimentData.getXSIType();
		} else {
			entityId = null;
			entityType = null;
		}
		if (entityId == null) {
			returnList.add("ERROR: Entity type could not be determined");
			return;
		}
		automationEvent.setEntityId(entityId);
		automationEvent.setEntityType(entityType);
		automationEvent.setExternalId(projectParam.toString());
		automationEvent.setSrcEventClass(eventClass);
		automationEvent.setUserId(user.getID());
		automationEvent.setEventId(null);
		// Create parameter map
		XnatProjectdata proj = null;
		XnatSubjectdata subj = null;
		XnatExperimentdata exp = null;
		if (projectParam != null && projectParam.toString().length() > 0) {
			proj = XnatProjectdata.getXnatProjectdatasById(projectParam, user, false);
			if (proj != null) {
				passMap.put("project", proj.getId());
			}
		}
		if (subjectParam != null && subjectParam.toString().length() > 0) {
			subj = XnatSubjectdata.getXnatSubjectdatasById(subjectParam.toString(), user, false);
			if (subj == null) {
				subj = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subjectParam.toString(), user,
						false);
			}
			if (subj != null) {
				passMap.put("subject", subj.getId());
			}
		}
		if (experimentParam != null && experimentParam.toString().length() > 0) {
			exp = XnatExperimentdata.getXnatExperimentdatasById(experimentParam.toString(), user, false);
			if (exp == null) {
				exp = XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), experimentParam.toString(), user,
						false);
			}
			if (exp != null) {
				passMap.put("experiment", exp.getId());
			}
		}
		if (passedParametersParam != null && passedParametersParam.toString().length() > 0) {
			String passedParametersJsonStr = null;
			try {
				passedParametersJsonStr = URLDecoder.decode(passedParametersParam.toString(), "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				returnList.add("WARNING: Could not parse passed parameters");
			}
			if (passedParametersJsonStr != null) {
				try {
					Type type = new TypeToken<Map<String, String>>() {
					}.getType();
					Map<String, String> gsonMap = new Gson().fromJson(passedParametersJsonStr, type);
					passMap.putAll(gsonMap);
				} catch (JsonParseException e) {
					returnList.add("WARNING: Could not parse passed parameters");
				}
			}
		}
		if (!(configuredResource == null || configuredResource.equalsIgnoreCase(CACHE_CONSTANT))) {
			eventText = getEventTextFromConfiguredResourceConfig(proj, subj, exp, configuredResource);
			passMap.put("configuredResource", configuredResource);

		}
		if (eventText!=null) {
			buildWorkflow(proj,subj,exp,passMap,eventText);
		}
		
	}

	/**
	 * Process file.
	 *
	 * @param cacheLoc
	 *            the cache loc
	 * @param specPath
	 *            the spec path
	 * @throws ClientException
	 *             the client exception
	 */
	private void processFile(final File cacheLoc, final String specPath) throws ClientException {
		final String fileName;
		try {
			fileName = URLDecoder.decode(fw.getName(), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			throw new ClientException("Could not decode file name.", e1);
		}
		final Object extractParam = params.get("extract");
		if (extractParam != null && extractParam.toString().equalsIgnoreCase("true") && isZipFileName(fileName)) {
			final ZipI zipper = getZipper(fileName);
			try {
				zipper.extract(fw.getInputStream(), cacheLoc.getAbsolutePath());
			} catch (Exception e) {
				throw new ClientException("Archive file is corrupt or not a valid archive file type.");
			}
		} else {
			final File cacheFile = new File(cacheLoc, fileName);
			try {
				fw.write(cacheFile);
				// Uploading directories via linux (and likely Mac) will not
				// fail due to "Everything is a file". In such cases we wind
				// up with a "file" generated. Check for these "files". Remove
				// them, and thrown an exception.
				// Windows uploads of directories should fail before hitting
				// this class.
				removeAndThrowExceptionIfDirectory(cacheFile);
			} catch (ClientException e) {
				throw e;
			} catch (Exception e) {
				throw new ClientException("Could not write uploaded file.", e);
			}
		}
	}

	/**
	 * Removes the and throw exception if directory.
	 *
	 * @param file
	 *            the file
	 * @throws ClientException
	 *             the client exception
	 */
	private void removeAndThrowExceptionIfDirectory(File file) throws ClientException {
		if (RestFileUtils.isFileRepresentationOfDirectory(file)) {
			if (file.delete()) {
				throw new ClientException(
						"Upload of directories is not currently supported.  To upload a directory, please create a zip archive.");
			}
		}
	}

	/**
	 * Do automation.
	 *
	 * @throws ClientException
	 *             the client exception
	 * @throws ServerException
	 *             the server exception
	 */
	@SuppressWarnings({ "rawtypes", "static-access" })
	private void doAutomation(boolean escapeHtml) throws ClientException, ServerException {

		returnList.add("<b>BEGIN PROCESSING UPLOADED FILES</b>");

		final Map<String, Object> passMap = Maps.newHashMap();
		final Object eventHandlerParam = params.get("eventHandler");
		final Object projectParam = params.get("project");
		final Object subjectParam = params.get("subject");
		final Object experimentParam = params.get("experiment");
		final Object buildPathParam = params.get("buildPath");
		final Object passedParametersParam = params.get("passedParameters");
		String eventText = null; 

		final ScriptTriggerService scriptTriggerService = XDAT.getContextService().getBean(ScriptTriggerService.class);
		if (scriptTriggerService == null) {
			returnList.add("ERROR: Could not obtain event handler service.");
			return;
		}
		final ScriptTrigger scriptTrigger = scriptTriggerService.getByTriggerId(eventHandlerParam.toString());
		if (scriptTrigger == null) {
			returnList.add("ERROR: Could not obtain event handler.");
			return;
		}
		final String eventClass = scriptTrigger.getSrcEventClass();
		final Map<String, List<String>> eventFilters = scriptTrigger.getEventFiltersAsMap();
		Class clazz;
		Object eventObj;
		try {
			clazz = Class.forName(eventClass);
			eventObj = clazz.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			returnList.add("ERROR: Could not instantiate event (" + eventClass + "). <br>" + e.toString());
			return;
		}
		if (!(eventObj instanceof AutomationEventImplementerI)) {
			returnList.add("ERROR: Event (" + eventClass + ") is not an AutomationEventImplementer.");
		}
		final AutomationEventImplementerI automationEvent = (AutomationEventImplementerI) eventObj;
		final String entityId;
		final String entityType;
		if (experimentParam == null && subjectParam == null) {
			entityId = projectParam.toString();
			entityType = XnatProjectdata.SCHEMA_ELEMENT_NAME;
		} else if (experimentParam == null && subjectParam != null) {
			entityId = subjectParam.toString();
			entityType = XnatSubjectdata.SCHEMA_ELEMENT_NAME;
		} else if (experimentParam != null) {
			entityId = experimentParam.toString();
			final XnatExperimentdata experimentData = XnatExperimentdata.getXnatExperimentdatasById(experimentParam,
					user, false);
			entityType = experimentData.SCHEMA_ELEMENT_NAME;
		} else {
			entityId = null;
			entityType = null;
		}
		if (entityId == null) {
			returnList.add("ERROR: Entity type could not be determined");
			return;
		}
		automationEvent.setEntityId(entityId);
		automationEvent.setEntityType(entityType);
		automationEvent.setExternalId(projectParam.toString());
		automationEvent.setSrcEventClass(eventClass);
		automationEvent.setUserId(user.getID());
		automationEvent.setEventId(scriptTrigger.getEvent());
		final List<Method> setters = Lists.newArrayList();
		for (final Method method : Arrays.asList(automationEvent.getClass().getMethods())) {
			if (method.isAnnotationPresent(Filterable.class)
					&& method.getName().substring(0, 3).equalsIgnoreCase("get")) {
				final char c[] = method.getName().substring(3).toCharArray();
				c[0] = Character.toLowerCase(c[0]);
				final String column = new String(c);
				for (final String filterKey : eventFilters.keySet()) {
					if (filterKey.equals(column)) {
						Method setter;
						try {
							setter = automationEvent.getClass().getMethod(method.getName().replaceFirst("get", "set"),
									String.class);
						} catch (NoSuchMethodException | SecurityException e) {
							continue;
						}
						if (setter.getName().substring(0, 3).equals("set")) {
							List<String> filterList = eventFilters.get(filterKey);
							setters.add(setter);
							if (filterList != null && filterList.size() > 0) {
								try {
									setter.invoke(automationEvent, filterList.get(0));
								} catch (IllegalAccessException | IllegalArgumentException
										| InvocationTargetException e) {
									returnList.add("ERROR: Could not set values for filters");
									return;
								}
							}
						}
					}
				}

			}
		}
		if (!(setters.size() >= eventFilters.keySet().size())) {
			returnList.add("ERROR: Could not set values for filters");
			return;
		}
		// Create parameter map
		XnatProjectdata proj = null;
		XnatSubjectdata subj = null;
		XnatExperimentdata exp = null;
		if (projectParam != null && projectParam.toString().length() > 0) {
			proj = XnatProjectdata.getXnatProjectdatasById(projectParam, user, false);
			if (proj != null) {
				passMap.put("project", proj.getId());
			}
		}
		if (subjectParam != null && subjectParam.toString().length() > 0) {
			subj = XnatSubjectdata.getXnatSubjectdatasById(subjectParam.toString(), user, false);
			if (subj == null) {
				subj = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subjectParam.toString(), user,
						false);
			}
			if (subj != null) {
				passMap.put("subject", subj.getId());
			}
		}
		if (experimentParam != null && experimentParam.toString().length() > 0) {
			exp = XnatExperimentdata.getXnatExperimentdatasById(experimentParam.toString(), user, false);
			if (exp == null) {
				exp = XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), experimentParam.toString(), user,
						false);
			}
			if (exp != null) {
				passMap.put("experiment", exp.getId());
			}
		}
		if (passedParametersParam != null && passedParametersParam.toString().length() > 0) {
			String passedParametersJsonStr = null;
			try {
				passedParametersJsonStr = URLDecoder.decode(passedParametersParam.toString(), "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				returnList.add("WARNING: Could not parse passed parameters");
			}
			if (passedParametersJsonStr != null) {
				try {
					Type type = new TypeToken<Map<String, String>>() {
					}.getType();
					Map<String, String> gsonMap = new Gson().fromJson(passedParametersJsonStr, type);
					passMap.putAll(gsonMap);
				} catch (JsonParseException e) {
					returnList.add("WARNING: Could not parse passed parameters");
				}
			}
		}
		if (!(configuredResource == null || configuredResource.equalsIgnoreCase(CACHE_CONSTANT))) {
			eventText = getEventTextFromConfiguredResourceConfig(proj, subj, exp, configuredResource);
			passMap.put("configuredResource", configuredResource);

		}
		if (eventText!=null) {
			buildWorkflow(proj,subj,exp,passMap,eventText);
		}
		if (buildPathParam != null) {
			passMap.put("BuildPath", buildPathParam);
		}
		automationEvent.setParameterMap(passMap);
		// Create automationCompletionEvent and launch automation
		final String automationId = String.valueOf(System.currentTimeMillis()).concat("-")
				.concat(String.valueOf(this.hashCode()));
		final AutomationCompletionEvent automationCompletionEvent = new AutomationCompletionEvent(automationId);
		final Object sendemailParam = params.get("sendemail");
		if (sendemailParam != null && sendemailParam.toString().equalsIgnoreCase("true")) {
			automationCompletionEvent.addNotificationEmailAddr(user.getEmail());
		}
		automationEvent.setAutomationCompletionEvent(automationCompletionEvent);
		final NrgEventService eventService = XDAT.getContextService().getBean(NrgEventService.class);
		if (eventService == null) {
			returnList.add("ERROR: Could retrieve event service");
			return;
		}
		eventService.triggerEvent(automationEvent, automationCompletionEvent);
		final AutomationCompletionEventListener completionService = XDAT.getContextService().getBeanSafely(AutomationCompletionEventListener.class);
		List<ScriptOutput> scriptOutputs = null;
		for (int i = 1; i < TIMEOUT_SECONDS; i++) {
			try {
				Thread.sleep(1000);
				final AutomationCompletionEvent ace = completionService.getEvent(automationId);
				if (ace != null) {
					scriptOutputs = ace.getScriptOutputs();
					break;
				} else if (i == TIMEOUT_SECONDS) {
					returnList.add("<br><b>TIMEOUT WAITING FOR SCRIPT TO RETURN.<b></br>");
				}
			} catch (InterruptedException e) {
				// Do nothing for now.
			}
		}
		if (scriptOutputs != null && scriptOutputs.size() > 0) {
			for (ScriptOutput scriptOut : scriptOutputs) {
				returnList.add("<br><b>SCRIPT EXECUTION RESULT</b>");
				returnList.add("<br><b>FINAL STATUS:  " + scriptOut.getStatus() + "</b>");
				if (scriptOut.getStatus().equals(Status.ERROR) && scriptOut.getResults() != null
						&& scriptOut.getResults().toString().length() > 0) {
					returnList.add("<br><b>SCRIPT RESULTS</b><br>");
					try {
						if (escapeHtml) {
							final StringWriter writer = new StringWriter();
							StringEscapeUtils.escapeHtml(writer, scriptOut.getResults().toString());
							returnList.add(writer.toString().replace("\n", "<br>"));
							writer.close();
						} else {
							returnList.add(conditionallyAddHtmlBreaks(scriptOut.getResults().toString()));
						}
					} catch (IOException e) {
						returnList.add(conditionallyAddHtmlBreaks(scriptOut.getResults().toString()));
					}
				}
				if (scriptOut.getOutput() != null && scriptOut.getOutput().length() > 0) {
					returnList.add("<br><b>SCRIPT STDOUT</b><br>");
					try {
						if (escapeHtml) {
							final StringWriter writer = new StringWriter();
							StringEscapeUtils.escapeHtml(writer, scriptOut.getOutput());
							returnList.add(writer.toString().replace("\n", "<br>"));
							writer.close();
						} else {
							returnList.add(conditionallyAddHtmlBreaks(scriptOut.getOutput().toString()));
						}
					} catch (IOException e) {
						returnList.add(conditionallyAddHtmlBreaks(scriptOut.getOutput().toString()));
					}
				}
				if (scriptOut.getErrorOutput() != null && scriptOut.getErrorOutput().length() > 0) {
					returnList.add("<br><b>SCRIPT STDERR/EXCEPTION</b><br>");
					try {
						if (escapeHtml) {
							final StringWriter writer = new StringWriter();
							StringEscapeUtils.escapeHtml(writer, scriptOut.getErrorOutput());
							returnList.add(writer.toString().replace("\n", "<br>"));
							writer.close();
						} else {
							returnList.add(conditionallyAddHtmlBreaks(scriptOut.getErrorOutput().toString()));
						}
					} catch (IOException e) {
						returnList.add(conditionallyAddHtmlBreaks(scriptOut.getErrorOutput().toString()));
					}
				}
			}
		} else {
			returnList.add("<br><b>No output was returned from the script</b>");
		}
		returnList.add("<br><b>PROCESSING COMPLETE");
		
	}
	
 	private String conditionallyAddHtmlBreaks(String output) {
 		// An (overly) simplistic check just trying to see if the output is doing its own line breaking.  Otherwise we'll add them.
 		// I'm not sure we want to do this, but some currently in used scripts plan on the importer doing the breaks and
 		// this could be useful for text output that doesn't request the html to be escaped.
		return output.replace("\n", ((double)((StringUtils.countMatches(output,"<br>")+
										StringUtils.countMatches(output,"<br/>")+
										StringUtils.countMatches(output,"<p>")+
										StringUtils.countMatches(output,"<tr>")+
										StringUtils.countMatches(output,"<ul>")+
										StringUtils.countMatches(output,"<ul>")+
										StringUtils.countMatches(output,"<ol>")+
										StringUtils.countMatches(output,"<li>")+
										StringUtils.countMatches(output,"<dl>")
									))/((double)(StringUtils.countMatches(output,"\n")+1))>0.95) ? "" : "<br>") + "<br>";
	}

	/**
	  * Builds the workflow.
	  *
	  * @param proj the proj
	  * @param subj the subj
	  * @param exp the exp
	  * @param passMap the pass map
	  * @param eventText the event text
	  * @return the persistent workflow i
	  */
	 private PersistentWorkflowI buildWorkflow(XnatProjectdata proj,XnatSubjectdata subj,XnatExperimentdata exp, Map<String, Object> passMap,String eventText) {
		final PersistentWorkflowI wrk;
		try {
			returnList.add("Building workflow entry for configured resource / event handler - " + eventText);
			wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, params.get("xsiType").toString(),
					(exp.getId()!=null) ? exp.getId() : (subj.getId()!=null) ? subj.getId() : proj.getId(), proj.getId(),
			EventUtils.newEventInstance(CATEGORY.DATA, TYPE.WEB_SERVICE, eventText, "Automation-based upload", null));
			wrk.setStatus(STATUS_COMPLETE);
			//wrk.setDetails(JSONObject.fromObject(passMap).toString());
			wrk.setDetails((new JSONObject(passMap)).toString());
			final EventMetaI em = wrk.buildEvent();
			returnList.add("Saving workflow - " + eventText);
			// Make workflow save request, requesting that the workflow not trigger an event.  We want to make
			// the save request here.
			PersistentWorkflowUtils.save(wrk, em, false, false);
			// Update automationEventIds table since we're not triggering an event (allows event to appear in UI).
			try {
				updateAutomationEventIdsIds(proj, WorkflowStatusEvent.class.getCanonicalName(), eventText);
			} catch (Throwable t) {
				logger.debug("Couldn't update hibernate event id table", t);
			}
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



	private void updateAutomationEventIdsIds(XnatProjectdata proj, String canonicalName, String eventText) {
        AutomationEventIdsIdsService _idsIdsService = XDAT.getContextService().getBean(AutomationEventIdsIdsService.class);
        AutomationEventIdsService _idsService = XDAT.getContextService().getBean(AutomationEventIdsService.class);
		final List<AutomationEventIdsIds> autoIds = _idsIdsService.getEventIds(proj.getId(), canonicalName, eventText, true);
        if (autoIds.size() < 1) {
            final AutomationEventIdsIds idsids = new AutomationEventIdsIds(proj.getId(), canonicalName, eventText, _idsService);
            _idsIdsService.saveOrUpdate(idsids);
        } else {
            for (final AutomationEventIdsIds ids : autoIds) {
                if (ids.getEventId().equals(eventText)) {
                	ids.setCounter(ids.getCounter()+1);
                    _idsIdsService.saveOrUpdate(ids);
                }
            }
        }
		
	}

	/**
	 * Gets the event text from configured resource config.
	 *
	 * @param proj
	 *            the proj
	 * @param subj
	 *            the subj
	 * @param exp
	 *            the exp
	 * @param configuredResource
	 *            the configured resource
	 * @return the event text from configured resource config
	 */
	// TODO: Configured Resource configuration should be updated to store EVENT,
	// rather than using this "Uploaded {label}" event.
	private String getEventTextFromConfiguredResourceConfig(XnatProjectdata proj, XnatSubjectdata subj,
			XnatExperimentdata exp, String configuredResource) {
		// Do we need to handle scope differently?  I don't think site configured uploads will pass through this method.
		// It's really only for configured resources, which should always be scoped at the project level, I think.
		final Scope scope = (exp != null) ? Scope.Project : (subj != null) ? Scope.Project : (proj != null ) ? Scope.Project : Scope.Site;
		final String crConfig = XDAT.getConfigService().getConfigContents(CONFIG_TOOL, CONFIG_SCRIPT_PATH, scope, proj.getId());
		if (crConfig != null && crConfig.length() > 0) {
			try {
				final JSONArray jsonArray = new JSONArray(new JSONTokener(crConfig));
				for (int i = 0; i < jsonArray.length(); i++) {
					final JSONObject jsonObj = jsonArray.getJSONObject(i);
					if (jsonObj.getString("name").equals(configuredResource)) {
						return "Uploaded " + jsonObj.getString("label");
					}
				}
			} catch (JSONException e) {
				logger.warn("WARNING:  Could not parse resource_config json results (PROJECT=" + proj.getId()
						+ ",CONFIG_TOOL=" + CONFIG_TOOL + ",CONFIG_PATH=" + CONFIG_SCRIPT_PATH);
			}
		}
		return null;
	}

	/**
	 * Checks if is zip file name.
	 *
	 * @param fileName
	 *            the file name
	 * @return true, if is zip file name
	 */
	private boolean isZipFileName(final String fileName) {
		for (final String ext : ZIP_EXT) {
			if (fileName.toLowerCase().endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the zipper.
	 *
	 * @param fileName
	 *            the file name
	 * @return the zipper
	 */
	private ZipI getZipper(final String fileName) {

		// Assume file name represents correct compression method
		String file_extension = null;
		if (fileName != null && fileName.indexOf(".") != -1) {
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
