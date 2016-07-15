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
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.services.RoleHolder;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
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
public class EventHandlerApi {

    /**
     * The Constant _log.
     */
    private static final Logger _log = LoggerFactory.getLogger(EventHandlerApi.class);

	/** The maximum number of event IDs to return for each event class in each project. */
	private static final int MAX_EVENT_IDS_LIST = 20;

    /** The _role holder. */
    @Autowired
    private RoleHolder _roleHolder;

    /**
     * The event ids service.
     */
    @Autowired
    private HibernateAutomationEventIdsIdsService eventIdsService;

    /**
     * The filters service.
     */
    @Autowired
    private HibernateAutomationFiltersService filtersService;

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
    public ResponseEntity<List<EventClassInfo>> automationEventClassesGetByProject(@PathVariable("project_id") String project_id) {
        final HttpStatus status = isPermitted(project_id);
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
    @ApiOperation(value = "Get list of event classes.", notes = "Returns a list of classes implementing AutomationEventI.", response = List.class)
    @ApiResponses({@ApiResponse(code = 200, message = "An array of class names"), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/eventHandlers/automationEventClasses"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<EventClassInfo>> automationEventClassesGet() {
        final HttpStatus status = isPermitted(null);
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
     * Gets the event info list.
     *
     * @param project_id the project_id
     * @return the event info list
     */
    private List<EventClassInfo> getEventInfoList(String project_id) {
        final List<EventClassInfo>     eventInfoList = Lists.newArrayList();
        final List<AutomationEventIdsIds> eventIdsList  = getEventIdsService().getEventIds(project_id, false, MAX_EVENT_IDS_LIST);
        final List<AutomationFilters>  filtersList   = getFiltersService().getAutomationFilters(project_id, false);
        for (final String className : getEventClassList(eventIdsList)) {
            final EventClassInfo            eci              = new EventClassInfo(className);
            final List<String>              eventIds         = eci.getEventIds();
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
                        final String     column = new String(c);
                        final Annotation anno   = AnnotationUtils.findAnnotation(method, Filterable.class);

                        final Object   annoInitialValuesObj = AnnotationUtils.getValue(anno, "initialValues");
                        final String[] annoInitialValues    = (annoInitialValuesObj != null && annoInitialValuesObj instanceof String[]) ? (String[]) annoInitialValuesObj : new String[] {};

                        final Object   annoDefaultValueObj = AnnotationUtils.getValue(anno, "defaultValue");
                        final String   annoDefaultValue    = (annoDefaultValueObj != null) ? annoDefaultValueObj.toString() : "";

                        final Object annoFilterRequired = AnnotationUtils.getValue(anno, "filterRequired");
                        boolean      filterRequired     = (annoFilterRequired == null || !(annoFilterRequired instanceof Boolean)) ? false : (boolean) annoFilterRequired;

                        final Object annoIncludeValuesFromDatabase = AnnotationUtils.getValue(anno, "includeValuesFromDatabase");
                        boolean      includeValuesFromDatabase     = (annoIncludeValuesFromDatabase == null || !(annoIncludeValuesFromDatabase instanceof Boolean)) ? true :(boolean) annoIncludeValuesFromDatabase;
                        if (!filterableFields.containsKey(column)) {
                        	EventHandlerFilterInfo filterInfo = new EventHandlerFilterInfo();
                        	filterInfo.setFilterValues(new ArrayList<String>());
                            final List<String> newValueList = Lists.newArrayList();
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
                        	valueList.add(0,"_FILTER_NOT_REQUIRED_");
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
     * Gets the event ids service.
     *
     * @return the event ids service
     */
    private HibernateAutomationEventIdsIdsService getEventIdsService() {
        if (eventIdsService == null) {
            eventIdsService = XDAT.getContextService().getBean(HibernateAutomationEventIdsIdsService.class);
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
                return (_roleHolder.isSiteAdmin(sessionUser)) ? null : HttpStatus.FORBIDDEN;
            }
        }
        _log.error("Error checking read status for project");
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
