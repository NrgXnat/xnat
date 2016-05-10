package org.nrg.xapi.rest.event;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javassist.Modifier;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xapi.model.event.EventClassInfo;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.services.impl.hibernate.HibernateAutomationEventIdsService;
import org.nrg.xdat.services.impl.hibernate.HibernateAutomationFiltersService;
import org.nrg.xft.event.AutomationEventImplementerI;
import org.nrg.xft.event.Filterable;
import org.nrg.xft.event.entities.AutomationEventIds;
import org.nrg.xft.event.entities.AutomationFilters;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.event.conf.EventPackages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Class EventHandlerApi.
 */
@Api(description = "The XNAT Event Handler API")
@XapiRestController
public class EventHandlerApi {

    /**
     * The Constant _log.
     */
    private static final Logger _log = LoggerFactory.getLogger(EventHandlerApi.class);

    /**
     * The event ids service.
     */
    @Autowired
    private HibernateAutomationEventIdsService eventIdsService;

    /**
     * The filters service.
     */
    @Autowired
    private HibernateAutomationFiltersService filtersService;

    /**
     * The event packages.
     */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Autowired
    private EventPackages eventPackages;

    /**
     * Inits the this.
     */
    @PostConstruct
    private void initThis() {
        getEventIdsService();
        getFiltersService();
    }

    /**
     * Automation event classes get.
     *
     * @param project_id the project_id
     * @return the response entity
     */
    @ApiOperation(value = "Get list of event classes.", notes = "Returns a list of classes implementing AutomationEventI.", response = List.class)
    @ApiResponses({@ApiResponse(code = 200, message = "An array of class names"), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/projects/{project_id}/eventHandlers/automationEventClasses"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<EventClassInfo>> automationEventClassesGet(@PathVariable("project_id") String project_id) {
        final HttpStatus status = isPermitted(project_id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        return new ResponseEntity<>(getEventInfoList(project_id), HttpStatus.OK);
    }

    /**
     * Automation event classes get.
     *
     * @return the response entity
     */
    @ApiOperation(value = "Get list of event classes.", notes = "Returns a list of classes implementing AutomationEventI.", response = List.class)
    @ApiResponses({@ApiResponse(code = 200, message = "An array of class names"), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/eventHandlers/automationEventClasses"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<EventClassInfo>> automationEventClassesGet() {
        final HttpStatus status = isPermitted(null);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        return new ResponseEntity<>(getEventInfoList(null), HttpStatus.OK);
    }

    /**
     * Gets the event info list.
     *
     * @param project_id the project_id
     * @return the event info list
     */
    private List<EventClassInfo> getEventInfoList(String project_id) {
        final List<EventClassInfo>     eventInfoList = Lists.newArrayList();
        final List<AutomationEventIds> eventIdsList  = getEventIdsService().getEventIds(project_id, false);
        final List<AutomationFilters>  filtersList   = getFiltersService().getAutomationFilters(project_id, false);
        for (final String className : getEventClassList(eventIdsList)) {
            final EventClassInfo            eci              = new EventClassInfo(className);
            final List<String>              eventIds         = eci.getEventIds();
            final Map<String, List<String>> filterableFields = eci.getFilterableFieldsMap();
            if (eci.getIncludeEventIdsFromDatabase()) {
                for (final AutomationEventIds autoIds : eventIdsList) {
                    if (autoIds.getSrcEventClass().equals(className)) {
                        for (String eId : autoIds.getEventIds()) {
                            if (!eventIds.contains(eId)) {
                                eventIds.add(eId);
                            }
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
                        final String     column = new String(c);
                        final Annotation anno   = AnnotationUtils.findAnnotation(method, Filterable.class);

                        final Object   annoInitialValuesObj = AnnotationUtils.getValue(anno, "initialValues");
                        final String[] annoInitialValues    = (annoInitialValuesObj != null && annoInitialValuesObj instanceof String[]) ? (String[]) annoInitialValuesObj : new String[] {};

                        final Object annoIncludeValuesFromDatabase = AnnotationUtils.getValue(anno, "includeValuesFromDatabase");
                        boolean      includeValuesFromDatabase     = !(annoIncludeValuesFromDatabase != null && annoIncludeValuesFromDatabase instanceof Boolean) || (boolean) annoIncludeValuesFromDatabase;
                        if (!filterableFields.containsKey(column)) {
                            final List<String> newValueList = Lists.newArrayList();
                            filterableFields.put(column, newValueList);
                        }
                        final List<String> valueList = filterableFields.get(column);
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
                    }
                }
            } catch (SecurityException | ClassNotFoundException e) {
                for (final AutomationFilters autoFilters : filtersList) {
                    if (!filterableFields.containsKey(autoFilters.getField())) {
                        final List<String> valueList = Lists.newArrayList(autoFilters.getValues());
                        Collections.sort(valueList);
                        filterableFields.put(autoFilters.getField(), valueList);
                    } else {
                        for (String value : autoFilters.getValues()) {
                            final List<String> values = filterableFields.get(autoFilters.getField());
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
     * @return the event class list
     */
    private List<String> getEventClassList(List<AutomationEventIds> eventIdsList) {
        final List<String> classList = Lists.newArrayList();
        // ClassList should be pulled from available event classes rather than from events
        if (eventPackages != null) {
            for (final String pkg : eventPackages) {
                try {
                    for (final Class<?> clazz : Reflection.getClassesForPackage(pkg)) {
                        if (AutomationEventImplementerI.class.isAssignableFrom(clazz) && !clazz.isInterface() &&
                            !Modifier.isAbstract(clazz.getModifiers())) {
                            if (!classList.contains(clazz.getName())) {
                                classList.add(clazz.getName());
                            }
                        }
                    }
                } catch (ClassNotFoundException | IOException e) {
                    // Do nothing.
                }
            }
        }
        // I think for now we'll not pull from the database if we've found event classes.  If the database
        // contains any thing different, it should only be event classes that are no longer available.
        if (classList.size() < 1) {
            for (final AutomationEventIds auto : eventIdsList) {
                if (!classList.contains(auto.getSrcEventClass())) {
                    classList.add(auto.getSrcEventClass());
                }
            }
        }
        return classList;
    }

    /**
     * Gets the event ids service.
     *
     * @return the event ids service
     */
    private HibernateAutomationEventIdsService getEventIdsService() {
        if (eventIdsService == null) {
            eventIdsService = XDAT.getContextService().getBean(HibernateAutomationEventIdsService.class);
        }
        return eventIdsService;
    }

    /**
     * Gets the filters service.
     *
     * @return the filters service
     */
    private HibernateAutomationFiltersService getFiltersService() {
        if (filtersService == null) {
            filtersService = XDAT.getContextService().getBean(HibernateAutomationFiltersService.class);
        }
        return filtersService;
    }

    /**
     * Gets the session user.
     *
     * @return the session user
     */
    private UserI getSessionUser() {
        final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if ((principal instanceof UserI)) {
            return (UserI) principal;
        }
        return null;
    }

    /**
     * Checks if is permitted.
     *
     * @param projectId the project ID
     * @return the http status
     */
    private HttpStatus isPermitted(String projectId) {
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
                return (Roles.isSiteAdmin(sessionUser)) ? null : HttpStatus.FORBIDDEN;
            }
        }
        _log.error("Error checking read status for project");
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
