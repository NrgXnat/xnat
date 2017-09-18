/*
 * web: org.nrg.xnat.restlet.resources.ScriptTriggerResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.automation.entities.EventFilters;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.event.util.ImportEventHandlerResults;
import org.nrg.xnat.event.util.JsonResults;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The Class ScriptTriggerResource.
 */
public class ScriptTriggerResource extends AutomationResource {

	/**
	 * Instantiates a new script trigger resource.
	 *
	 * @param context
	 *            the context
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @throws ResourceException
	 *             the resource exception
	 */
	public ScriptTriggerResource(Context context, Request request, Response response) throws ResourceException {
		super(context, request, response);

		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		getVariants().add(new Variant(MediaType.TEXT_XML));
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));

		_scriptTriggerService = XDAT.getContextService().getBean(ScriptTriggerService.class);

		_eventId = (String) getRequest().getAttributes().get(EVENT_ID);
		final String triggerId = (String) getRequest().getAttributes().get(TRIGGER_ID);
		final String Id = (String) getRequest().getAttributes().get(ID);

		final boolean hasEvent = StringUtils.isNotBlank(_eventId);
		final boolean hasTriggerId = StringUtils.isNotBlank(triggerId);
		final boolean hasId = StringUtils.isNotBlank(Id);

		final String projectId;
		if (!hasTriggerId && !hasEvent && !hasId) {
			projectId = getProjectId();
			_trigger = null;
		} else {
			if (hasId || hasTriggerId) {
				_trigger = (hasId) ? _scriptTriggerService.getById(Id)
						: _scriptTriggerService.getByTriggerId(triggerId);
				if (_trigger == null) {
					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
							"Can't find script trigger with ID: " + triggerId);
				}
				if (StringUtils.isNotBlank(_trigger.getAssociation())) {
					setAssociation(_trigger.getAssociation());
					projectId = getScope() == Scope.Site ? null : getProjectId();
				} else {
					projectId = null;
				}
			} else if (hasProjectId()) {
				projectId = getProjectId();
				_trigger = null;
				// TODO: For these to make sense now, we need to pass at minimum an event class,
				// but really we would need filters
				// _trigger =
				// _scriptTriggerService.getByAssociationAndEvent(Scope.encode(Scope.Project,
				// projectId), _eventId);
			} else {
				projectId = null;
				_trigger = null;
				// TODO: For these to make sense now, we need to pass at minimum an event class,
				// but really we would need filters
				// _trigger = _scriptTriggerService.getByAssociationAndEvent(Scope.Site.code(),
				// _eventId);
			}
		}

		final Method method = request.getMethod();
		final UserI user = getUser();

		if (StringUtils.isNotBlank(projectId)) {
			validateProjectAccess(projectId);
			setProjectId(projectId);
		} else if (!Roles.isSiteAdmin(user) && !method.equals(Method.GET)) {
			final String message = "User " + user.getLogin()
					+ " attempted to access forbidden script trigger resource at the site level.";
			_log.warn(message);
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
		}

		// You can't delete a trigger that you can't find.
		if (method.equals(Method.DELETE) && _trigger == null) {
			if (hasEvent) {
				throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED,
						"You specified an invalid specific scope and event when trying to delete a script trigger: "
								+ formatScopeEntityIdAndEvent());
			}
			throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED,
					"You must specify a specific scope and event or trigger ID to delete a script trigger.");
		}
		// You can't get a trigger when you requested a specific trigger but that
		// doesn't exist.
		if (method.equals(Method.GET) && _trigger == null && hasEvent) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Can't find script trigger for "
					+ formatScopeEntityIdAndEvent() + " to perform the " + method.toString() + " operation. ");
		}

		if (_log.isDebugEnabled()) {
			_log.debug("Servicing script trigger request " + formatScopeEntityIdAndEvent() + " for user "
					+ user.getLogin());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nrg.xnat.restlet.resources.AutomationResource#getResourceType()
	 */
	@Override
	protected String getResourceType() {
		return ScriptTrigger.class.getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nrg.xnat.restlet.resources.AutomationResource#getResourceId()
	 */
	@Override
	protected String getResourceId() {
		return _trigger == null ? null : _trigger.getEvent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.Resource#allowPut()
	 */
	@Override
	public boolean allowPut() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.Resource#allowDelete()
	 */
	@Override
	public boolean allowDelete() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.Resource#represent(org.restlet.resource.Variant)
	 */
	@Override
	public Representation represent(Variant variant) throws ResourceException {
		final MediaType mediaType = overrideVariant(variant);

		if (_trigger != null) {
			try {
				// They're requesting a specific trigger, so return that to them.
				return new StringRepresentation(toJson(mapTrigger(_trigger)), mediaType);
			} catch (IOException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
						"An error occurred marshalling the script trigger data to JSON", e);
			}
		} else {
			// They're asking for list of existing script triggers, so give them that.
			return listScriptTriggers(mediaType);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.Resource#handlePut()
	 */
	@Override
	public void handlePut() {
		try {
			putScriptTrigger();
		} catch (ClientException e) {
			getResponse().setStatus(e.getStatus(), e.getMessage());
		} catch (ServerException e) {
			_log.error("Server error occurred trying to store a script trigger resource", e);
			getResponse().setStatus(e.getStatus(), e.getMessage());
		} catch (ConfigServiceException e) {
			_log.error("Server error occurred trying to store a script trigger resource", e);
			getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.Resource#handleDelete()
	 */
	@Override
	public void handleDelete() {
		if (_log.isDebugEnabled()) {
			_log.debug("Preparing to delete script trigger for " + formatScopeEntityIdAndEvent()
					+ " and its associated triggers.");
		}
		final String triggerId = _trigger.getTriggerId();
		final String containerId = _trigger.getAssociation();
		_scriptTriggerService.delete(_trigger);
		recordAutomationEvent(triggerId, containerId, "Delete", ScriptTrigger.class);
	}

	/**
	 * Map trigger.
	 *
	 * @param trigger
	 *            the trigger
	 * @return the map
	 */
	private Map<String, String> mapTrigger(final ScriptTrigger trigger) {
		final Map<String, String> association = Scope.decode(trigger.getAssociation());

		final Map<String, String> properties = new HashMap<>();
		properties.put("id", String.valueOf(trigger.getId()));
		properties.put("triggerId", trigger.getTriggerId());
		properties.put("scope", association.get("scope"));
		properties.put("entityId", association.get("entityId"));
		properties.put(EVENT, trigger.getEvent());
		properties.put("scriptId", trigger.getScriptId());
		properties.put("description", trigger.getDescription());

		return properties;
	}

	/**
	 * Lists the script triggers with the specified scope and entity ID and event.
	 *
	 * @param mediaType
	 *            the media type
	 * @return A representation of the script triggers available for the specified
	 *         scope, entity ID (if specified), and event.
	 */
	private Representation listScriptTriggers(final MediaType mediaType) {
		Hashtable<String, Object> params = new Hashtable<>();
		final boolean restrictToScope;
		if (getScope() == Scope.Site) {
			final List<String> segments = getRequest().getResourceRef().getSegments();
			final String function = segments.get(segments.size() - 1);
			if (!function.equals("triggers")) {
				params.put("scope", Scope.Site);
				restrictToScope = true;
			} else {
				restrictToScope = false;
			}
		} else {
			params.put("scope", getScope());
			params.put("projectId", getProjectId());
			restrictToScope = true;
		}

		ArrayList<String> columns = new ArrayList<>();
		columns.add("id");
		columns.add("triggerId");
		columns.add("scope");
		columns.add("entityId");
		columns.add("srcEventClass");
		columns.add(EVENT);
		columns.add("eventFilters");
		columns.add("scriptId");
		columns.add("description");

		XFTTable table = new XFTTable();
		table.initTable(columns);

		final List<ScriptTrigger> triggers;
		final List<ScriptTrigger> filteredtriggers = new ArrayList<ScriptTrigger>();
		if (!restrictToScope) {
			triggers = _scriptTriggerService.getAll();
		} else if (getScope() == Scope.Site) {
			triggers = _scriptTriggerService.getSiteTriggers();
		} else {
			triggers = _scriptTriggerService.getByScope(getScope(), getProjectId());
		}
		if (getRequest().getResourceRef().hasQuery()
				&& getRequest().getResourceRef().getQueryAsForm().getFirst("targetProjectName") != null) {
			String sourceProject = getRequest().getResourceRef().getQueryAsForm().getFirst("targetProjectName")
					.getValue();
			final List<ScriptTrigger> sourcetriggers = _scriptTriggerService.getByScope(Scope.Project, sourceProject);

			if (sourcetriggers != null && sourcetriggers.size() < 1) {
				for (final ScriptTrigger trigger : triggers) {
					addEventRow(table, trigger);
				}
			} else {
				//Match all attributes except description to find duplicate event.
				for (final ScriptTrigger trigger : triggers) {
					for (final ScriptTrigger srctrigger : sourcetriggers) {
						if (srctrigger.getEvent().replace(EXECUTED_SCRIPT, "")
								.equals(trigger.getEvent().replace(EXECUTED_SCRIPT, ""))
								&& srctrigger.getSrcEventClass().equals(trigger.getSrcEventClass())
								&& srctrigger.getScriptId().equals(trigger.getScriptId())
								//&& srctrigger.getDescription().equals(trigger.getDescription())
								&& srctrigger.getEventFiltersAsMap().toString()
										.equals(trigger.getEventFiltersAsMap().toString())) {
							filteredtriggers.add(trigger);
						}
					}
				}
				Collection<ScriptTrigger> eventsToDisplay = CollectionUtils.subtract(triggers, filteredtriggers);
				for (Iterator<ScriptTrigger> iterator = eventsToDisplay.iterator(); iterator.hasNext();) {
					ScriptTrigger trigger = iterator.next();
					addEventRow(table, trigger);

				}
			}
		} else {
			for (final ScriptTrigger trigger : triggers) {
				addEventRow(table, trigger);
			}
		}
		table.sort(EVENT, "ASC");

		return representTable(table, mediaType, params);
	}

	private void addEventRow(XFTTable table, final ScriptTrigger trigger) {
		final Map<String, String> atoms = Scope.decode(trigger.getAssociation());
		final String scope = atoms.get("scope");
		final String entityId = scope.equals(Scope.Site.code()) ? "" : atoms.get("entityId");
		table.insertRowItems(String.valueOf(trigger.getId()), trigger.getTriggerId(), scope, entityId,
				trigger.getSrcEventClass(), trigger.getEvent(), trigger.getEventFiltersAsMap(), trigger.getScriptId(),
				trigger.getDescription());
	}

	/**
	 * Put script trigger.
	 *
	 * @throws ClientException
	 *             the client exception
	 * @throws ServerException
	 *             the server exception
	 */
	private void putScriptTrigger() throws ClientException, ServerException, ConfigServiceException {
		// TODO: this needs to properly handle a PUT to an existing script as well as an
		// existing but disabled script.
		final Representation entity = getRequest().getEntity();
		if (entity.getSize() == 0) {
			logger.warn("Unable to find script trigger parameters: no data sent?");
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					"Unable to find script trigger parameters: no data sent?");
			return;
		}

		MediaType mediaType = entity.getMediaType();
		if (!mediaType.equals(MediaType.APPLICATION_JSON)) {
			throw new ClientException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
					"This function currently only supports " + MediaType.APPLICATION_JSON);
		}

		// final Properties properties;
		ImportEventHandlerResults results = new ImportEventHandlerResults();
		try {
			results = results.getObjectFromJSON(entity.getText());
		} catch (IOException e) {
			throw new ServerException(Status.SERVER_ERROR_INTERNAL,
					"An error occurred processing the script properties", e);
		}
		Map<String, String> triggerIdMap = new HashMap<String, String>();
		/**
		 * Add all the selected event handlers from source project to target project.
		 */
		for (int i = 0; i < results.getJsonResults().length; i++) {
			addEventHandler(results.getJsonResults()[i], triggerIdMap);
		}
		/**
		 * This block of code will check if the selected events have custom
		 * configuration(configure uploader option in UI). If source project has custom
		 * configuration then copy the configuration and add it to existing
		 * configuration. If there is no existing configuration then create a new
		 * configuration record. If source project does not have custom configuration
		 * then skip the code and target project will also use default configuration for
		 * events.
		 */
		if (results.getSourceProjectId() != null) {
			ConfigService configService = XDAT.getConfigService();

			String currentConfigJson = configService.getConfigContents(TOOL_NAME_AUTOMATION_UPLOADER,
					PATH_CONFIGURATION, Scope.Project, getProjectId());
			JSONArray currentConfig = null;
			if (currentConfigJson != null)
				currentConfig = new JSONArray(currentConfigJson);
			else
				currentConfig = new JSONArray();

			String sourceConfingJson = configService.getConfigContents(TOOL_NAME_AUTOMATION_UPLOADER,
					PATH_CONFIGURATION, getScope(), results.getSourceProjectId());
			JSONArray srcConfig = null;
			if (sourceConfingJson != null)
				srcConfig = new JSONArray(sourceConfingJson);
			if (srcConfig != null) {
				for (Iterator<String> iterator = triggerIdMap.keySet().iterator(); iterator.hasNext();) {
					String key = iterator.next();
					for (int i = 0; i < srcConfig.length(); i++) {
						if (key.equals(srcConfig.getJSONObject(i).getString(EVENT))) {
							srcConfig.getJSONObject(i).put(EVENT_TRIGGER_ID, triggerIdMap.get(key));
							currentConfig.put(srcConfig.getJSONObject(i));
						}
					}
				}
				configService.replaceConfig(getUser().getLogin(), null, TOOL_NAME_AUTOMATION_UPLOADER,
						PATH_CONFIGURATION, currentConfig.toString(), getScope(), getProjectId());
			}
		}

	}

	private void addEventHandler(JsonResults jsonResults, Map<String, String> triggerIdMap) throws ClientException {
		if (_trigger == null) {
			if (jsonResults.getEvent() == null || jsonResults.getEvent().length() < 1) {
				throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,
						"You must specify the event for your new script trigger.");
			}
			if (jsonResults.getScriptId() == null || jsonResults.getScriptId().length() < 1) {
				throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,
						"You must specify the script ID for your new script trigger.");
			}
			if (_log.isDebugEnabled()) {
				_log.debug("Creating new script trigger");
			}
			final String scriptId = jsonResults.getScriptId();
			final String event = jsonResults.getEvent();
			final String description = jsonResults.getDescription();
			final String eventClass = jsonResults.getEventClass();
			final Map<String, List<String>> eventFilters = jsonResults.getFilters();
			final String triggerId = _scriptTriggerService.getDefaultTriggerName(scriptId, getScope(), getProjectId(),
					eventClass, event, eventFilters);
			final ScriptTrigger trigger = _scriptTriggerService.newEntity(triggerId, description, scriptId,
					getAssociation(), eventClass, event, eventFilters);
			if (_log.isInfoEnabled()) {
				_log.info("Created a new trigger: " + trigger.toString());
			}
			recordAutomationEvent(triggerId, getAssociation(), "Create", ScriptTrigger.class);
			// Return the trigger ID in the response test. The upload UI needs it
			this.getResponse().setEntity(new StringRepresentation(triggerId));
			triggerIdMap.put(event, triggerId);
		} else {
			final String scriptId = jsonResults.getScriptId();
			final String event = jsonResults.getEvent();
			final String description = jsonResults.getDescription();
			final String eventClass = jsonResults.getEventClass();
			final Map<String, List<String>> eventFilters = jsonResults.getFilters();
			final String triggerId = _scriptTriggerService.getDefaultTriggerName(scriptId, getScope(), getProjectId(),
					eventClass, event, eventFilters);
			boolean isDirty = false;
			if (StringUtils.isNotBlank(scriptId) && !scriptId.equals(_trigger.getScriptId())) {
				_trigger.setScriptId(scriptId);
				isDirty = true;
			}
			if (StringUtils.isNotBlank(event) && !event.equals(_trigger.getEvent())) {
				_trigger.setEvent(event);
				isDirty = true;
			}
			if (StringUtils.isNotBlank(triggerId) && !triggerId.equals(_trigger.getTriggerId())) {
				_trigger.setTriggerId(triggerId);
				isDirty = true;
			}
			if (StringUtils.isNotBlank(eventClass) && !eventClass.equals(_trigger.getSrcEventClass())) {
				_trigger.setSrcEventClass(eventClass);
				isDirty = true;
			}
			if (eventFilters != null && !eventFilters.equals(_trigger.getEventFiltersAsMap())) {
				_trigger.setEventFiltersAsMap(eventFilters);
				isDirty = true;
			}
			// Description is a little different because you could specify an empty
			// description.
			if (description != null && !description.equals(_trigger.getDescription())) {
				_trigger.setDescription(description);
				isDirty = true;
			}
			if (!getAssociation().equals(getAssociation())) {
				_trigger.setAssociation(getAssociation());
				isDirty = true;
			}
			if (isDirty) {
				_scriptTriggerService.update(_trigger);
				recordAutomationEvent(triggerId, getAssociation(), "Update", ScriptTrigger.class);
				// Return thie trigger ID in the response test. The upload UI needs it
				this.getResponse().setEntity(new StringRepresentation(triggerId));
			}
			triggerIdMap.put(event, triggerId);
		}
	}

	/**
	 * Gets the event filters.
	 *
	 * @param filters
	 *            the filters
	 * @return the event filters
	 */
	@SuppressWarnings("unused")
	private Set<EventFilters> getEventFilters(Map<String, List<String>> filters) {
		final Set<EventFilters> eventSet = Sets.newHashSet();
		for (final String filterKey : filters.keySet()) {
			EventFilters ef = new EventFilters(filterKey, filters.get(filterKey));
			eventSet.add(ef);
		}
		return eventSet;
	}

	/**
	 * Format scope entity id and event.
	 *
	 * @return the string
	 */
	private String formatScopeEntityIdAndEvent() {
		final StringBuilder buffer = new StringBuilder();
		if (_trigger != null) {
			final Map<String, String> atoms = Scope.decode(_trigger.getAssociation());
			if (atoms.get("scope").equals(Scope.Site.code())) {
				buffer.append("site");
			} else {
				buffer.append("project ").append(atoms.get("entityId"));
			}
			if (_trigger.getEvent() != null) {
				buffer.append(" and event ").append(_trigger.getEvent());
			} else {
				buffer.append(", no event");
			}
		} else {
			if (getScope() == Scope.Site) {
				buffer.append("site");
			} else {
				buffer.append("project ").append(getProjectId());
			}
			if (StringUtils.isNotBlank(_eventId)) {
				buffer.append(" and event ").append(_eventId);
			} else {
				buffer.append(", no event");
			}
		}
		return buffer.toString();
	}

	/** The Constant _log. */
	private static final Logger _log = LoggerFactory.getLogger(ScriptTriggerResource.class);

	/** The Constant EVENT_ID. */
	private static final String EVENT_ID = "EVENT_ID";

	/** The Constant TRIGGER_ID. */
	private static final String TRIGGER_ID = "TRIGGER_ID";

	/** The Constant ID. */
	private static final String ID = "ID";

	/** The tool name automation uploader. */
	private static final String TOOL_NAME_AUTOMATION_UPLOADER = "automation_uploader";

	/** The path configuration. */
	private static final String PATH_CONFIGURATION = "configuration";

	/** The Constant EVENT_TRIGGER_ID. */
	private static final String EVENT_TRIGGER_ID = "eventTriggerId";
	
	/** The Constant EXECUTED_SCRIPT. */
	private static final String EXECUTED_SCRIPT = "Executed script ";

	/** The Constant EVENT. */
	private static final String EVENT = "event";

	/** The _script trigger service. */
	private final ScriptTriggerService _scriptTriggerService;

	/** The _event id. */
	private final String _eventId;

	/** The _trigger. */
	private final ScriptTrigger _trigger;
}
