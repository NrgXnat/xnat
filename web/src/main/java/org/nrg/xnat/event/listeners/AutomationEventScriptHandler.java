/*
 * web: org.nrg.xnat.event.listeners.AutomationEventScriptHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners;

import org.apache.commons.lang3.StringUtils;
import org.nrg.automation.entities.Script;
import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.event.entities.PersistentEvent;
import org.nrg.automation.services.PersistentEventService;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.automation.services.impl.AutomationService;
import org.nrg.automation.services.impl.hibernate.HibernateScriptTriggerService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.event.Filterable;
import org.nrg.framework.event.persist.PersistentEventImplementerI;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.messaging.automation.AutomatedScriptRequest;
import org.nrg.xnat.utils.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private static final Map<Class<? extends AutomationEventImplementerI>, Map<String, Method>> FILTERABLE_METHODS = new ConcurrentHashMap<>();

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
     * Persistent event service.
     */
    private PersistentEventService _persistentEventService;

    private final AutomationService automationService;

    /**
     * Instantiates a new automation event script handler.
     *
     * @param eventBus the event bus
     * @param service the service
     * @param scriptTriggerService the script trigger service
     * @param dataSource the data source
     * @param persistentEventService the persistent event service
     */
    @Autowired
    public AutomationEventScriptHandler(EventBus eventBus, ScriptRunnerService service, ScriptTriggerService scriptTriggerService,
                                        DataSource dataSource, PersistentEventService persistentEventService,
                                        final AutomationService automationService) {
        eventBus.on(type(AutomationEventImplementerI.class), this);
        this._service = service;
        this._scriptTriggerService = scriptTriggerService;
        this._dataSource = dataSource;
        this._persistentEventService = persistentEventService;
        this.automationService = automationService;
    }

    public static Map<String, Method> getFilterableMethods(final Class<? extends AutomationEventImplementerI> clazz) {
        return FILTERABLE_METHODS.computeIfAbsent(clazz, AutomationEventScriptHandler::computeFilterableMethods);
    }

    private static Map<String, Method> computeFilterableMethods(final Class<? extends AutomationEventImplementerI> clazz) {
        final String get = "get";
        final int getLength = get.length();
        return Arrays.stream(clazz.getMethods())
                .filter(method -> method.isAnnotationPresent(Filterable.class))
                .filter(method -> method.getName().startsWith(get))
                .collect(Collectors.toMap(method -> StringUtils.uncapitalize(method.getName().substring(getLength)), Function.identity()));
    }

    private static String invokeMethod(final AutomationEventImplementerI eventData, final Method method) {
        Object result;
        try {
            result = method.invoke(eventData);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.error("Error invoking method \"{}\" on eventData", method.getName(), e);
            result = null;
        }
        return result == null ? null : result.toString();
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
        final AutomationEventImplementerI eventData = event.getData();

        if (shouldSkipEvent(eventData)) {
            return;
        }

        handleAsPersistentEventIfMarkedPersistent(eventData);

        final Map<String, String> methodInvocationResults = invokeMethods(eventData);

        automationService.incrementEventId(eventData.getExternalId(), eventData.getSrcEventClass(), eventData.getEventId());

        for (final Map.Entry<String, String> entry : methodInvocationResults.entrySet()) {
            automationService.addValueToStoredFilters(eventData.getExternalId(), eventData.getSrcEventClass(), entry.getKey(), entry.getValue());
        }

        launchScripts(eventData, methodInvocationResults);
    }

    private boolean shouldSkipEvent(final AutomationEventImplementerI eventData) {
        if (eventData == null) {
            logger.debug("Automation script will not be launched because applicationEvent object is null");
            return true;
        }
        try {
            final UserI user = Users.getUser(eventData.getUserId());
        } catch (UserNotFoundException | UserInitException e) {
            // User is required to launch script
            logger.debug("Automation not launching because user object is null");
            return true;
        }
        if (eventData.getSrcEventClass() == null) {
            logger.debug("Automation not launching because eventClass is null");
            return true;
        }
        if (eventData.getEventId() == null) {
            logger.debug("Automation not launching because eventID is null");
            return true;
        }
        return false;
    }

    private Map<String, String> invokeMethods(final AutomationEventImplementerI eventData) {
        return getFilterableMethods(eventData.getClass()).entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), invokeMethod(eventData, entry.getValue())))
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Handle as persistent event if marked persistent.
     *
     * @param eventData the event data
     */
    private void handleAsPersistentEventIfMarkedPersistent(AutomationEventImplementerI eventData) {
        // Persist the event if this is a PersistentEventImplementerI
        if (eventData instanceof PersistentEventImplementerI) {
            try {
                _persistentEventService.create((PersistentEvent) eventData);
            } catch (SecurityException | IllegalArgumentException e) {
                logger.error("Exception persisting event", e);
            }
        }
    }

    /**
     * Handle event.
     *
     * @param eventData the event data
     */
    public void launchScripts(final AutomationEventImplementerI eventData, final Map<String, String> filterMap) {
        logger.debug("Handling event {}", eventData);

        final UserI user;
        try {
            user = Users.getUser(eventData.getUserId());
        } catch (UserNotFoundException | UserInitException e) {
            // This would have been caught earlier
            logger.debug("Automation not launching because user object is null");
            return;
        }

        final String srcEventClass = eventData.getSrcEventClass();

        final String eventID = eventData.getEventId();
        final String eventName = eventID.replaceAll("\\*OPEN\\*", "(").replaceAll("\\*CLOSE\\*", ")");

        // Build justification if possible
        Method justMethod = null;
        try {
            justMethod = eventData.getClass().getMethod("getJustification");
        } catch (NoSuchMethodException | NullPointerException | SecurityException e) {
            // Do nothing for now
        }
        String justification = justMethod != null ? invokeMethod(eventData, justMethod) : null;
        justification = justification != null ? justification : "";

        // Iterate through scripts + launch
        for (final Script script : getScripts(eventData.getExternalId(), srcEventClass, eventID, filterMap)) {
            try {
                final String action = "Executed script " + script.getScriptId();
                final String comment = action + " triggered by event " + eventID;
                final PersistentWorkflowI scriptWrk = PersistentWorkflowUtils.buildOpenWorkflow(user, eventData.getEntityType(), eventData.getEntityId(), eventData.getExternalId(),
                                                                                                EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.PROCESS, action,
                                                                                                                            StringUtils.isNotBlank(justification) ? justification : "Automated execution: " + comment, comment));
                assert scriptWrk != null;
                scriptWrk.setStatus(PersistentWorkflowUtils.QUEUED);
                WorkflowUtils.save(scriptWrk, scriptWrk.buildEvent());

                final AutomatedScriptRequest request = new AutomatedScriptRequest(eventData, eventName, user, script, scriptWrk);
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

        final List<Script> scripts = new ArrayList<>();

        //project level scripts
        if (StringUtils.isNotBlank(projectId)) {
            scripts.addAll(_service.getScripts(Scope.Project, projectId, eventClass, event, filterMap));
        }
        scripts.addAll(_service.getScripts(Scope.Site, null, eventClass, event, filterMap));
        return scripts;
    }

}
