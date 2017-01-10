/*
 * web: org.nrg.xapi.rest.event.EventHandlerApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.event;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javassist.Modifier;

import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.event.entities.AutomationEventIdsIds;
import org.nrg.automation.event.entities.AutomationFilters;
import org.nrg.automation.services.impl.hibernate.HibernateAutomationEventIdsIdsService;
import org.nrg.automation.services.impl.hibernate.HibernateAutomationFiltersService;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.event.EventClass;
import org.nrg.framework.event.Filterable;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xapi.model.event.EventClassInfo;
import org.nrg.xapi.model.event.EventHandlerFilterInfo;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The Class EventHandlerApi.
 */
@Api(description = "The XNAT Event Handler API")
@XapiRestController
public class EventHandlerApi extends AbstractXapiRestController {
    @Autowired
    public EventHandlerApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final HibernateAutomationFiltersService filtersService, final HibernateAutomationEventIdsIdsService eventIdsService) {
        super(userManagementService, roleHolder);
        _filtersService = filtersService;
        _eventIdsService = eventIdsService;
    }

    /**
     * Automation event classes get.
     *
     * @param project_id the project_id
     *
     * @return the response entity
     */
    @ApiOperation(value = "Get list of event classes.", notes = "Returns a list of classes implementing AutomationEventI.", response = EventClassInfo.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "An array of class names"), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/projects/{project_id}/eventHandlers/automationEventClasses"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<EventClassInfo>> automationEventClassesGetByProject(@PathVariable("project_id") String project_id) {
        final HttpStatus status = canEditProject(project_id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            return new ResponseEntity<>(getEventInfoList(project_id), HttpStatus.OK);
        } catch (Throwable t) {
            _log.error("EventHandlerApi exception:  " + t.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Automation event classes get.
     *
     * @return the response entity
     */
    @ApiOperation(value = "Get list of event classes.", notes = "Returns a list of classes implementing AutomationEventI.", response = EventClassInfo.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "An array of class names"), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/eventHandlers/automationEventClasses"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<EventClassInfo>> automationEventClassesGet() {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            return new ResponseEntity<>(getEventInfoList(null), HttpStatus.OK);
        } catch (Throwable t) {
            _log.error("EventHandlerApi exception:  " + t.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Checks if is permitted.
     *
     * @param projectId the project ID
     *
     * @return the http status
     */
    // TODO: Migrate this to the abstract superclass. Can't right now because XDAT doesn't know about XnatProjectdata, etc.
    protected HttpStatus canEditProject(String projectId) {
        final UserI sessionUser = getSessionUser();
        if ((sessionUser instanceof XDATUser)) {
            if (projectId != null) {
                final XnatProjectdata project = AutoXnatProjectdata.getXnatProjectdatasById(projectId, sessionUser, false);
                try {
                    return Permissions.canEdit(sessionUser, project) ? null : HttpStatus.FORBIDDEN;
                } catch (Exception e) {
                    _log.error("Error checking read status for project", e);
                    return HttpStatus.INTERNAL_SERVER_ERROR;
                }
            } else {
                return isPermitted() == null ? null : HttpStatus.FORBIDDEN;
            }
        }
        _log.error("Error checking read status for project");
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Gets the event info list.
     *
     * @param project_id the project_id
     *
     * @return the event info list
     */
    private List<EventClassInfo> getEventInfoList(String project_id) {
        final List<EventClassInfo> eventInfoList = Lists.newArrayList();
        final List<AutomationEventIdsIds> eventIdsList = _eventIdsService.getEventIds(project_id, false, MAX_EVENT_IDS_LIST);
        final List<AutomationFilters> filtersList = _filtersService.getAutomationFilters(project_id, false);
        for (final String className : getEventClassList(eventIdsList)) {
            final EventClassInfo eci = new EventClassInfo(className);
            final List<String> eventIds = eci.getEventIds();
            final Map<String, EventHandlerFilterInfo> filterableFields = eci.getFilterableFieldsMap();
            if (eci.getIncludeEventIdsFromDatabase()) {
                for (final AutomationEventIdsIds autoIds : eventIdsList) {
                    if (autoIds.getParentAutomationEventIds().getSrcEventClass().equals(className)) {
                        if (!eventIds.contains(autoIds.getEventId())) {
                            eventIds.add(autoIds.getEventId());
                        }
                    }
                }
            }
            Collections.sort(eventIds);
            try {
                for (final Method method : Arrays.asList(Class.forName(className).getMethods())) {
                    if (method.isAnnotationPresent(Filterable.class) && method.getName().substring(0, 3).equalsIgnoreCase("get")) {
                        final char c[] = method.getName().substring(3).toCharArray();
                        c[0] = Character.toLowerCase(c[0]);
                        final String column = new String(c);
                        final Annotation anno = AnnotationUtils.findAnnotation(method, Filterable.class);

                        final Object annoInitialValuesObj = AnnotationUtils.getValue(anno, "initialValues");
                        final String[] annoInitialValues = (annoInitialValuesObj != null && annoInitialValuesObj instanceof String[]) ? (String[]) annoInitialValuesObj : new String[]{};

                        final Object annoDefaultValueObj = AnnotationUtils.getValue(anno, "defaultValue");
                        final String annoDefaultValue = (annoDefaultValueObj != null) ? annoDefaultValueObj.toString() : "";

                        final Object annoFilterRequired = AnnotationUtils.getValue(anno, "filterRequired");
                        boolean filterRequired = !(annoFilterRequired == null || !(annoFilterRequired instanceof Boolean)) && (boolean) annoFilterRequired;

                        final Object annoIncludeValuesFromDatabase = AnnotationUtils.getValue(anno, "includeValuesFromDatabase");
                        boolean includeValuesFromDatabase = (annoIncludeValuesFromDatabase == null || !(annoIncludeValuesFromDatabase instanceof Boolean)) || (boolean) annoIncludeValuesFromDatabase;
                        if (!filterableFields.containsKey(column)) {
                            EventHandlerFilterInfo filterInfo = new EventHandlerFilterInfo();
                            filterInfo.setFilterValues(new ArrayList<String>());
                            filterableFields.put(column, filterInfo);
                        }
                        final EventHandlerFilterInfo filterInfo = filterableFields.get(column);
                        filterInfo.setDefaultValue(annoDefaultValue);
                        filterInfo.setFilterRequired(filterRequired);
                        filterInfo.setIncludeValuesFromDatabase(includeValuesFromDatabase);
                        final List<String> valueList = filterInfo.getFilterValues();
                        valueList.addAll(Arrays.asList(annoInitialValues));
                        if (includeValuesFromDatabase) {
                            for (final AutomationFilters autoFilters : filtersList) {
                                if (autoFilters.getField().equals(column)) {
                                    valueList.addAll(autoFilters.getValues());
                                    break;
                                }
                            }
                        }
                        Collections.sort(valueList);

                        if (!filterRequired) {
                            // TODO:  Should probably add an EventFilterInfo class and keep the list of filter values there, along with a
                            // a boolean required value and a default value.  
                            // NOTE:  This is handled in JavaScript as non-required filter
                            valueList.add(0, "_FILTER_NOT_REQUIRED_");
                        }
                    }
                }
            } catch (SecurityException | ClassNotFoundException e) {
                for (final AutomationFilters autoFilters : filtersList) {
                    if (!filterableFields.containsKey(autoFilters.getField())) {
                        EventHandlerFilterInfo filterInfo = new EventHandlerFilterInfo();
                        final List<String> valueList = Lists.newArrayList(autoFilters.getValues());
                        filterInfo.setFilterValues(valueList);
                        filterInfo.setFilterRequired(false);
                        filterInfo.setIncludeValuesFromDatabase(true);
                        Collections.sort(valueList);
                        filterableFields.put(autoFilters.getField(), filterInfo);
                    } else {
                        for (final String value : autoFilters.getValues()) {
                            final List<String> values = filterableFields.get(autoFilters.getField()).getFilterValues();
                            if (!values.contains(value)) {
                                values.add(value);
                            }
                            Collections.sort(values);
                        }
                    }
                }
            }
            eventInfoList.add(eci);
        }
        return eventInfoList;
    }

    /**
     * Gets the event class list.
     *
     * @param eventIdsList the event ids list
     *
     * @return the event class list
     */
    private List<String> getEventClassList(List<AutomationEventIdsIds> eventIdsList) {
        final List<String> classList = Lists.newArrayList();
        try {
            for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/event/*-event.properties")) {
                final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                if (!properties.containsKey(EventClass.EVENT_CLASS)) {
                    continue;
                }
                final String clssStr = properties.get(EventClass.EVENT_CLASS).toString();
                try {
                    final Class<?> clazz = Class.forName(clssStr);
                    if (AutomationEventImplementerI.class.isAssignableFrom(clazz) && !clazz.isInterface() &&
                        !Modifier.isAbstract(clazz.getModifiers())) {
                        if (!classList.contains(clazz.getName())) {
                            classList.add(clazz.getName());
                        }
                    }
                } catch (ClassNotFoundException cex) {
                    _log.debug("Could not load class for class name (" + clssStr + ")");
                }
            }
        } catch (IOException e) {
            _log.debug("Could not load event class properties resources (META-INF/xnat/event/*-event.properties)");
        }
        // I think for now we'll not pull from the database if we've found event classes.  If the database
        // contains any thing different, it should only be event classes that are no longer available.
        if (classList.size() < 1) {
            for (final AutomationEventIdsIds auto : eventIdsList) {
                if (!classList.contains(auto.getParentAutomationEventIds().getSrcEventClass())) {
                    classList.add(auto.getParentAutomationEventIds().getSrcEventClass());
                }
            }
        }
        return classList;
    }

    /**
     * The Constant _log.
     */
    private static final Logger _log = LoggerFactory.getLogger(EventHandlerApi.class);

    /**
     * The maximum number of event IDs to return for each event class in each project.
     */
    private static final int MAX_EVENT_IDS_LIST = 20;

    private final HibernateAutomationEventIdsIdsService _eventIdsService;
    private final HibernateAutomationFiltersService     _filtersService;
}
