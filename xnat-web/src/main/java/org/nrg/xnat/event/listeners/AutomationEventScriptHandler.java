/*
 * web: org.nrg.xnat.event.listeners.AutomationEventScriptHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptOutput;
import org.nrg.automation.entities.ScriptOutput.Status;
import org.nrg.automation.event.AutomationCompletionEventI;
import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.event.entities.AutomationCompletionEvent;
import org.nrg.automation.event.entities.AutomationEventIds;
import org.nrg.automation.event.entities.AutomationEventIdsIds;
import org.nrg.automation.event.entities.AutomationFilters;
import org.nrg.automation.event.entities.PersistentEvent;
import org.nrg.automation.services.AutomationEventIdsIdsService;
import org.nrg.automation.services.AutomationEventIdsService;
import org.nrg.automation.services.AutomationFiltersService;
import org.nrg.automation.services.PersistentEventService;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.automation.services.impl.hibernate.HibernateScriptTriggerService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.event.Filterable;
import org.nrg.framework.event.persist.PersistentEventImplementerI;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.NrgEventService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.messaging.automation.AutomatedScriptRequest;
import org.nrg.xnat.utils.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static reactor.bus.selector.Selectors.type;

/**
 * The Class AutomatedScriptHandler.
 */
@Service
@SuppressWarnings("unused")
public class AutomationEventScriptHandler implements Consumer<Event<AutomationEventImplementerI>> {

    /**
     * The Constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(AutomationEventScriptHandler.class);

    /**
     * The _service.
     */
    private ScriptRunnerService _service;
    
    /**
     * The _script trigger service.
     */
    private ScriptTriggerService _scriptTriggerService;

    /**
     * The _data source.
     */
    private DataSource _dataSource;

    /**
     * Automation filters service.
     */
    private AutomationFiltersService _filtersService;

    /**
     * Persistent event service.
     */
    private PersistentEventService _persistentEventService;
    
    /** The _ids service. */
    private AutomationEventIdsService _idsService;
    
    /** The _ids ids service. */
    private AutomationEventIdsIdsService _idsIdsService;

    /**
     * Instantiates a new automation event script handler.
     *
     * @param eventBus the event bus
     * @param service the service
     * @param scriptTriggerService the script trigger service
     * @param dataSource the data source
     * @param filtersService the filters service
     * @param persistentEventService the persistent event service
     * @param idsService the ids service
     * @param idsIdsService the ids ids service
     */
    @Autowired
    public AutomationEventScriptHandler(EventBus eventBus, ScriptRunnerService service, ScriptTriggerService scriptTriggerService,
    		DataSource dataSource, AutomationFiltersService filtersService, PersistentEventService persistentEventService,
    		AutomationEventIdsService idsService, AutomationEventIdsIdsService idsIdsService) {
        eventBus.on(type(AutomationEventImplementerI.class), this);
        this._service = service;
        this._scriptTriggerService = scriptTriggerService;
        this._dataSource = dataSource;
        this._filtersService = filtersService;
        this._persistentEventService = persistentEventService;
        this._idsService = idsService;
        this._idsIdsService = idsIdsService;
    }

    /**
     * init - update xhbm_script_trigger table for XNAT 1.7
     */
    @PostConstruct
    public void initUpdateTables() {
        /** Update script trigger table for XNAT 1.7.  Drop constraints on any columns other than id and trigger_id */
        if (_scriptTriggerService instanceof HibernateScriptTriggerService) {

            final List<String> cleanUpQuery = (new JdbcTemplate(_dataSource)).query(
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
                for (final String query : cleanUpQuery) {
                    if (query.contains("xhbm_script_trigger")) {
                        logger.info("Execute clean-up query (" + query + ")");
                        new JdbcTemplate(_dataSource).execute(query);
                    }
                }
            }
            /** Update table rows for pre-XNAT 1.7 tables to fill in missing column values with defaults */
            ((HibernateScriptTriggerService) _scriptTriggerService).updateOldStyleScriptTriggers();
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
        } catch (Throwable t) {
            logger.error("Unexpected error persisting Persistent/Automation event information", t);
        } finally {
            handleEvent(event);
        }
    }

    /**
     * Update automation tables.
     *
     * @param event the event
     */
    // Made this method synchronized to avoid some constraint violation exceptions that were occasionally being thrown.
    private synchronized void updateAutomationTables(Event<AutomationEventImplementerI> event) {
        final AutomationEventImplementerI eventData = event.getData();
        if (eventData.getEventId() == null || eventData.getClass() == null) {
            return;
        }
        final List<AutomationEventIdsIds> autoIds = _idsIdsService.getEventIds(eventData.getExternalId(), eventData.getSrcEventClass(), eventData.getEventId(), true);
        if (autoIds.size() < 1) {
            final AutomationEventIdsIds idsids = new AutomationEventIdsIds(eventData, _idsService);
            _idsIdsService.saveOrUpdate(idsids);
        } else {
            for (final AutomationEventIdsIds ids : autoIds) {
                if (ids.getEventId().equals(eventData.getEventId())) {
                	ids.setCounter(ids.getCounter()+1);
                    _idsIdsService.saveOrUpdate(ids);
                }
            }
        }
        final Class<? extends AutomationEventImplementerI> clazz = eventData.getClass();
        for (final Method method : Arrays.asList(clazz.getMethods())) {
            if (method.isAnnotationPresent(Filterable.class) && method.getName().substring(0, 3).equalsIgnoreCase("get")) {
                final char c[] = method.getName().substring(3).toCharArray();
                c[0] = Character.toLowerCase(c[0]);
                final String column = new String(c);
                AutomationFilters filters = _filtersService.getAutomationFilters(eventData.getExternalId(), eventData.getSrcEventClass(), column, true);
                if (filters == null) {
                    filters = new AutomationFilters(eventData, column);
                    try {
                        _filtersService.saveOrUpdate(filters);
                    } catch (ConstraintViolationException e) {
                        logger.warn("A constraint violation error on {} occurred saving a filters entity: {}\n{}", e.getConstraintName(), filters.toString(), e.getMessage());
                    }
                } else {
                    try {
                        final String value = method.invoke(eventData).toString();
                        final List<String> values = filters.getValues();
                        if (!values.contains(value)) {
                            values.add(value);
                            filters.setValues(values);
                            _filtersService.saveOrUpdate(filters);
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        logger.error("Error invoking method on eventData", e);
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
                _persistentEventService.create((PersistentEvent) event.getData());
            } catch (SecurityException | IllegalArgumentException e) {
                logger.error("Exception persisting event", e);
            }
        }
    }

    /**
     * Handle event.
     *
     * @param event the event
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
        final Map<String, String> filterMap = Maps.newHashMap();
        final Class<? extends AutomationEventImplementerI> clazz = automationEvent.getClass();
        for (final Method method : Arrays.asList(clazz.getMethods())) {
            if (method.isAnnotationPresent(Filterable.class) && method.getName().substring(0, 3).equalsIgnoreCase("get")) {
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
        final String eventName = eventID.replaceAll("\\*OPEN\\*", "(").replaceAll("\\*CLOSE\\*", ")");
        //check to see if this has been handled before
        for (final Script script : getScripts(automationEvent.getExternalId(), eventClass, eventID, filterMap)) {
            try {
                final String action = "Executed script " + script.getScriptId();
                Method justMethod = null;
                try {
                    justMethod = automationEvent.getClass().getMethod("getJustification");
                } catch (NoSuchMethodException | NullPointerException | SecurityException e) {
                    // Do nothing for now
                }
                final Object justObject = (justMethod != null) ? justMethod.invoke(automationEvent) : null;
                final String justification = (justObject != null) ? justObject.toString() : "";
                final String comment = "Executed script " + script.getScriptId() + " triggered by event " + eventID;
                final PersistentWorkflowI scriptWrk = PersistentWorkflowUtils.buildOpenWorkflow(user, automationEvent.getEntityType(), automationEvent.getEntityId(), automationEvent.getExternalId(),
                                                                                                EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.PROCESS, action,
                                                                                                                            StringUtils.isNotBlank(justification) ? justification : "Automated execution: " + comment, comment));
                assert scriptWrk != null;
                scriptWrk.setStatus(PersistentWorkflowUtils.QUEUED);
                WorkflowUtils.save(scriptWrk, scriptWrk.buildEvent());

                final AutomatedScriptRequest request = new AutomatedScriptRequest(automationEvent, eventName, user, script, scriptWrk);
                XDAT.sendJmsRequest(request);
            } catch (Exception e1) {
                logger.error("Script launch exception", e1);
            }
        }
    }

    /**
     * Gets the scripts.
     *
     * @param projectId  the project id
     * @param eventClass the event class
     * @param event      the event
     * @param filterMap  the filter map
     *
     * @return the scripts
     */
    private List<Script> getScripts(final String projectId, String eventClass, String event, Map<String, String> filterMap) {

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

}
