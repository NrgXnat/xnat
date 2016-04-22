package org.nrg.xnat.event.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.nrg.automation.entities.EventFilters;
import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptOutput;
import org.nrg.automation.entities.ScriptOutput.Status;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.automation.services.impl.hibernate.HibernateScriptTriggerService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.impl.hibernate.HibernateAutomationEventIdsService;
import org.nrg.xdat.services.impl.hibernate.HibernateAutomationFiltersService;
import org.nrg.xdat.services.impl.hibernate.HibernatePersistentEventService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.nrg.xft.event.AutomationEventImplementerI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.Filterable;
import org.nrg.xft.event.XftEventService;
import org.nrg.xft.event.entities.AutomationCompletionEvent;
import org.nrg.xft.event.entities.AutomationEventIds;
import org.nrg.xft.event.entities.AutomationFilters;
import org.nrg.xft.event.entities.PersistentEvent;
import org.nrg.xft.event.persist.PersistentEventImplementerI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.WorkflowUtils;
import org.nrg.xnat.services.messaging.automation.AutomatedScriptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import static reactor.bus.selector.Selectors.type;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * The Class AutomatedScriptHandler.
 */
@Service
@SuppressWarnings("unused")
public class AutomationEventScriptHandler implements Consumer<Event<AutomationEventImplementerI>> {
	
	private String EMAIL_SUBJECT = "Automation Results";
    
    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(AutomationEventScriptHandler.class);
	
	/** The _service. */
	@Inject ScriptRunnerService _service;
	
	/** The _script trigger service. */
	@Inject ScriptTriggerService _scriptTriggerService;
	
	/** The _data source. */
	@Inject DataSource _dataSource;
	
	/**
	 * Instantiates a new automated script handler.
	 *
	 * @param eventBus the event bus
	 */
	@Inject public AutomationEventScriptHandler( EventBus eventBus ){
		eventBus.on(type(AutomationEventImplementerI.class), this);
	}
	
	/**
	 *  init - update xhbm_script_trigger table for XNAT 1.7
	 */
	@PostConstruct
	public void initUpdateTables() {
        /** Update script trigger table for XNAT 1.7.  Drop constraints on any columns other than id and trigger_id */ 
        if (_scriptTriggerService instanceof HibernateScriptTriggerService) {
        	
            List<String> cleanUpQuery = (new JdbcTemplate(_dataSource)).query(
            			"SELECT DISTINCT 'ALTER TABLE '||tc.table_name||' DROP CONSTRAINT '||tc.constraint_name||';'" +
                    	"  FROM information_schema.table_constraints tc " + 
                    	"  LEFT JOIN information_schema.constraint_column_usage cu " +
                    	"    ON cu.constraint_name = tc.constraint_name " + 
                    	" WHERE (tc.table_name='xhbm_script_trigger' AND cu.column_name NOT IN ('id', 'trigger_id')) "           		
            		, new RowMapper<String>() {
            			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            				return rs.getString(1);
            			}
            		});
            if (!cleanUpQuery.isEmpty()) {
            	logger.info("Cleaning up pre XNAT 1.7 constraints on the xhbm_script_trigger and xhbm_event tables");
            	for (String query : cleanUpQuery) {
            		if (query.contains("xhbm_script_trigger")) {
            			logger.info("Execute clean-up query (" + query + ")");
            			new JdbcTemplate(_dataSource).execute(query);
            		}
            	}
            }
            /** Update table rows for pre-XNAT 1.7 tables to fill in missing column values with defaults */
        	((HibernateScriptTriggerService)_scriptTriggerService).updateOldStyleScriptTriggers();
        }
	}
	
	/* (non-Javadoc)
	 * @see reactor.fn.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(Event<AutomationEventImplementerI> event) {
		
		try {
			handleAsPersistentEventIfMarkedPersistent(event);
			updateAutomationTables(event);
		} catch  (Throwable t) {
			logger.error("Unexpected error persisting Persistent/Automation event information",t);
		} finally {
			handleEvent(event);
		}
		
	}

    /**
     * Update automation tables.
     *
     * @param event the event
     */
    private void updateAutomationTables(Event<AutomationEventImplementerI> event) {
		final HibernateAutomationEventIdsService idsService = XDAT.getContextService().getBean(HibernateAutomationEventIdsService.class);
		final AutomationEventImplementerI eventData = event.getData();
		if (eventData.getEventId()==null || eventData.getClass()==null) {
			return;
		}
		List<AutomationEventIds> autoIds = idsService.getEventIds(eventData.getExternalId(), eventData.getSrcEventClass(), true);
		if (autoIds.size()<1) {
			final AutomationEventIds ids = new AutomationEventIds(eventData);
			idsService.saveOrUpdate(ids);
		} else {
			for (final AutomationEventIds ids : autoIds) {
				if (!ids.getEventIds().contains(eventData.getEventId())) {
					ids.getEventIds().add(eventData.getEventId());
					idsService.saveOrUpdate(ids);
				}
			}
		}
		final HibernateAutomationFiltersService filtersService = XDAT.getContextService().getBean(HibernateAutomationFiltersService.class);
		final Class<? extends AutomationEventImplementerI> clazz = eventData.getClass();
		for (final Method method : Arrays.asList(clazz.getMethods())) {
			if (method.isAnnotationPresent(Filterable.class) && method.getName().substring(0,3).equalsIgnoreCase("get")) {
				final char c[] = method.getName().substring(3).toCharArray();
				c[0] = Character.toLowerCase(c[0]);
				final String column = new String(c);
				AutomationFilters filters = filtersService.getAutomationFilters(eventData.getExternalId(), eventData.getSrcEventClass(), column, true);
				if (filters == null) {
					filters = new AutomationFilters(eventData, column);
					filtersService.saveOrUpdate(filters);
				} else {
					try {
						final String value = method.invoke(eventData).toString();
						final List<String> values = filters.getValues();
						if (!values.contains(value)) {
						values.add(value);
							filters.setValues(values);
							filtersService.saveOrUpdate(filters);
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						logger.error("Error invoking method on eventData",e);
					}
				}
			}
		}
		
	}

	/**
	 * Handle as persistent event if marked persistent.
	 *
	 * @param event the event
	 */
	private void handleAsPersistentEventIfMarkedPersistent(Event<AutomationEventImplementerI> event) {
	    // Persist the event if this is a PersistentEventImplementerI
		if (event.getData() instanceof PersistentEventImplementerI) {
			try {
				final HibernatePersistentEventService service = XDAT.getContextService().getBean(HibernatePersistentEventService.class);
				service.create((PersistentEvent)event.getData());
			} catch (SecurityException | IllegalArgumentException e) {
				logger.error("Exception persisting event",e);
			}
		}
	}

	/**
	 * Handle event.
	 *
	 * @param event the event
	 */
	/* (non-Javadoc)
     * @see org.nrg.xnat.event.listeners.WorkflowStatusEventHandlerAbst#handleEvent(org.nrg.xft.event.WorkflowStatusEvent)
     */
    public void handleEvent(Event<AutomationEventImplementerI> event) {
    	final AutomationEventImplementerI automationEvent = event.getData();
    	if (automationEvent == null) {
        	logger.debug("Automation script will not be launched because applicationEvent object is null");
    		return;
    	}
    	final UserI user;
    	try {
			user = Users.getUser(automationEvent.getUserId());
		} catch (UserNotFoundException | UserInitException e) {
			// User is required to launch script
        	logger.debug("Automation not launching because user object is null");
			return;
		}
        final String eventClass = automationEvent.getSrcEventClass();
        if (eventClass == null) {
        	logger.debug("Automation not launching because eventClass is null");
        	return;
        }
        final String eventID = automationEvent.getEventId();
        if (eventID == null) {
        	logger.debug("Automation not launching because eventID is null");
        	return;
        }
        final Map<String,String> filterMap = Maps.newHashMap();
		final Class<? extends AutomationEventImplementerI> clazz = automationEvent.getClass();
		for (final Method method : Arrays.asList(clazz.getMethods())) {
			if (method.isAnnotationPresent(Filterable.class) && method.getName().substring(0,3).equalsIgnoreCase("get")) {
				final char c[] = method.getName().substring(3).toCharArray();
				c[0] = Character.toLowerCase(c[0]);
				final String column = new String(c);
				String value;
				try {
					final Object rtValue = method.invoke(automationEvent);
					if (rtValue != null) {
						value = rtValue.toString(); 
						filterMap.put(column, value);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					logger.error("ERROR calling method on filterable field in event object", e);
					// Let's let this pass for now.
				}
			}
		}
        if (eventID == null) {
        	logger.debug("Automation not launching because eventID is null");
        	return;
        }
        final String eventName = eventID.replaceAll("\\*OPEN\\*", "(").replaceAll("\\*CLOSE\\*", ")");
        //check to see if this has been handled before
        final AutomationCompletionEvent automationCompletionEvent = automationEvent.getAutomationCompletionEvent();
        for (final Script script : getScripts(automationEvent.getExternalId(), eventClass, eventID, filterMap)) {
            try {
                final String action = "Executed script " + script.getScriptId();
				Method justMethod = null;
				try {
					justMethod = automationEvent.getClass().getMethod("getJustification");
				} catch (NoSuchMethodException | NullPointerException | SecurityException e) {
					// Do nothing for now
				}
				final Object justObject = (justMethod!=null) ? justMethod.invoke(automationEvent) : null;
				final String justification = (justObject!=null) ? justObject.toString() : "";
                final String comment = "Executed script " + script.getScriptId() + " triggered by event " + eventID;
                final PersistentWorkflowI scriptWrk = PersistentWorkflowUtils.buildOpenWorkflow(user, automationEvent.getEntityType(), automationEvent.getEntityId(), automationEvent.getExternalId(),
                				EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.PROCESS, action,
                						StringUtils.isNotBlank(justification) ? justification : "Automated execution: " + comment, comment));
                assert scriptWrk != null;
                scriptWrk.setStatus(PersistentWorkflowUtils.QUEUED);
                WorkflowUtils.save(scriptWrk, scriptWrk.buildEvent());
                
                final AutomatedScriptRequest request = new AutomatedScriptRequest(automationEvent.getSrcStringifiedId(), automationEvent.getSrcEventClass(), user, script.getScriptId(), eventName,
                			scriptWrk.getWorkflowId().toString(), automationEvent.getEntityType(), automationEvent.getSrcStringifiedId(), automationEvent.getExternalId(), automationEvent.getParameterMap());
                
                // We're running this here now, so we can return script output
                //XDAT.sendJmsRequest(request);
                final ScriptOutput scriptOut = executeScriptRequest(request);
                if (automationCompletionEvent != null && scriptOut != null) {
                	automationCompletionEvent.getScriptOutputs().add(scriptOut);
                }
            } catch (Exception e1) {
                logger.error("Script launch exception", e1);
            }
        }
        if (automationCompletionEvent!= null && automationCompletionEvent.getScriptOutputs().size()>0) {
        	XftEventService eventService = XftEventService.getService();
        	if (eventService != null) {
        		automationCompletionEvent.setEventCompletionTime(System.currentTimeMillis());
        		eventService.triggerEvent(automationCompletionEvent);
        		List<String> notifyList = automationCompletionEvent.getNotificationList();
        		if (notifyList != null && !notifyList.isEmpty()) {
        			AdminUtils.sendUserHTMLEmail(EMAIL_SUBJECT, scriptOutputToHtmlString(automationCompletionEvent.getScriptOutputs()), false, notifyList.toArray(new String[0]));
        		}
        	}
        }
    }

	private String scriptOutputToHtmlString(List<ScriptOutput> scriptOutputs) {
		if (scriptOutputs == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (ScriptOutput scriptOut : scriptOutputs) {
			sb.append("<br><b>SCRIPT EXECUTION RESULTS</b><br>");
			sb.append("<br><b>FINAL STATUS:  " + scriptOut.getStatus() + "</b><br>");
			if (scriptOut.getStatus().equals(Status.ERROR) && scriptOut.getResults()!=null && scriptOut.getResults().toString().length()>0) {
				sb.append("<br><b>SCRIPT RESULTS</b><br>");
				sb.append(scriptOut.getResults().toString().replace("\n", "<br>"));
			}
			if (scriptOut.getOutput()!=null && scriptOut.getOutput().length()>0) {
				sb.append("<br><b>SCRIPT STDOUT</b><br>");
				sb.append(scriptOut.getOutput().replace("\n", "<br>"));
			}
			if (scriptOut.getErrorOutput()!=null && scriptOut.getErrorOutput().length()>0) {
				sb.append("<br><b>SCRIPT STDERR/EXCEPTION</b><br>");
				sb.append(scriptOut.getErrorOutput().replace("\n", "<br>"));
			}
		}
		return sb.toString();
	}

	/**
     * Gets the scripts.
     *
     * @param projectId the project id
     * @param eventClass the event class
     * @param event the event
     * @param filterMap the filter map
     * @return the scripts
     */
    private List<Script> getScripts(final String projectId, String eventClass, String event, Map<String,String> filterMap) {

        final List<Script> scripts = Lists.newArrayList();

        //project level scripts
        if (StringUtils.isNotBlank(projectId)) {
            
            final Script script = _service.getScript(Scope.Project, projectId, eventClass, event, filterMap);
            if (script != null) {
                scripts.add(script);
            }
        }

        //site level scripts
        final Script script = _service.getScript(Scope.Site, null, eventClass, event, filterMap);
        if (script != null) {
            scripts.add(script);
        }

        return scripts;
    }
    
    /**
     * Execute script request.
     *
     * @param request the request
     * @return the script output
     * @throws Exception the exception
     */
    private ScriptOutput executeScriptRequest(AutomatedScriptRequest request) throws Exception {
    	
        final PersistentWorkflowI workflow = WorkflowUtils.getUniqueWorkflow(request.getUser(), request.getScriptWorkflowId());
        workflow.setStatus(PersistentWorkflowUtils.IN_PROGRESS);
        WorkflowUtils.save(workflow, workflow.buildEvent());

        final Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("user", request.getUser());
        parameters.put("scriptId", request.getScriptId());
        parameters.put("event", request.getEvent());
        parameters.put("srcEventId", request.getSrcEventId());
        parameters.put("srcEventClass", request.getSrcEventClass());
        parameters.put("srcWorkflowId", request.getSrcEventId());
        parameters.put("scriptWorkflowId", request.getScriptWorkflowId());
        parameters.put("dataType", request.getDataType());
        parameters.put("dataId", request.getDataId());
        parameters.put("externalId", request.getExternalId());
        parameters.put("workflow", workflow);
        if (request.getArgumentMap()!=null && !request.getArgumentMap().isEmpty()) {
        	parameters.putAll(request.getArgumentMap());
        }

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
    
}
