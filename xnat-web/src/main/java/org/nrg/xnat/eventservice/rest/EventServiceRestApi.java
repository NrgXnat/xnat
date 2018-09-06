package org.nrg.xnat.eventservice.rest;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.exceptions.UnauthorizedException;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.model.EventPropertyNode;
import org.nrg.xnat.eventservice.model.JsonPathFilterNode;
import org.nrg.xnat.eventservice.model.ProjectSubscriptionCreator;
import org.nrg.xnat.eventservice.model.SimpleEvent;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.model.SubscriptionCreator;
import org.nrg.xnat.eventservice.model.SubscriptionDelivery;
import org.nrg.xnat.eventservice.model.SubscriptionUpdate;
import org.nrg.xnat.eventservice.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authenticated;
import static org.nrg.xdat.security.helpers.AccessLevel.Owner;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Api(description = "API for the XNAT Event Service")
@XapiRestController
public class EventServiceRestApi extends AbstractXapiRestController {
    private static final Logger log = LoggerFactory.getLogger(EventServiceRestApi.class);

    private static final String ID_REGEX = "\\d+";
    private static final String NAME_REGEX = "\\d*[^\\d]+\\d*";

    private static final String JSON = MediaType.APPLICATION_JSON_UTF8_VALUE;
    private static final String TEXT = MediaType.TEXT_PLAIN_VALUE;

    private EventService eventService;

    @Autowired
    public EventServiceRestApi(final EventService eventService,
                               final UserManagementServiceI userManagementService,
                               final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        this.eventService = eventService;
    }

    @XapiRequestMapping(restrictTo = Admin, value = {"/events/triggers/{count}"}, method = GET, produces = JSON)
    @ApiOperation(value = "Get recent Event Service triggers.")
    @ResponseBody
    public List<String> getRecentTriggers(final @PathVariable Integer count) {
        return eventService.getRecentTriggers(count);
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/subscription", method = POST)
    @ApiOperation(value = "Create a Subscription", code = 201)
    public ResponseEntity<String> createSubscription(final @RequestBody SubscriptionCreator subscription,
                                                     final @RequestParam(value = "overpopulate-attributes", required = false) Boolean overpopulateAttributes)

            throws NrgServiceRuntimeException, SubscriptionValidationException, JsonProcessingException {
        final UserI userI = XDAT.getUserDetails();
        Subscription toCreate = Subscription.create(subscription, userI.getLogin());
        eventService.throwExceptionIfNameExists(toCreate);
        Subscription created = eventService.createSubscription(toCreate, overpopulateAttributes);
        if(created == null){
            return new ResponseEntity<>("Failed to create subscription.",HttpStatus.FAILED_DEPENDENCY);
        }
        return new ResponseEntity<>(created.name() + ":" + Long.toString(created.id()), HttpStatus.CREATED);
    }

    @XapiRequestMapping(restrictTo = Owner, value = "/projects/{project}/events/subscription", method = POST)
    @ApiOperation(value = "Create a Subscription for (project)", code = 201)
    public ResponseEntity<String> createSubscription(final @RequestBody ProjectSubscriptionCreator subscription,
                                                            final @PathVariable String project)
            throws NrgServiceRuntimeException, SubscriptionValidationException, JsonProcessingException, UnauthorizedException {
        final UserI userI = XDAT.getUserDetails();
        Subscription toCreate = Subscription.createOnProject(subscription, userI.getLogin());
        checkProjectSubscriptionAccess(toCreate ,project,userI);
        eventService.throwExceptionIfNameExists(toCreate);
        Subscription created = eventService.createSubscription(toCreate);
        if(created == null){
            return new ResponseEntity<>("Failed to create subscription.",HttpStatus.FAILED_DEPENDENCY);
        }
        return new ResponseEntity<>(created.name() + ":" + Long.toString(created.id()), HttpStatus.CREATED);
    }

    @XapiRequestMapping(restrictTo = Admin, value = {"/events/subscription/filter"}, method = GET, produces = JSON, params = {"event-type"})
    @ApiOperation(value = "Get a subscription filter for a given Event ID")
    @ResponseBody
    public Map<String, JsonPathFilterNode> retrieveFilterBuilder(final @RequestParam(name="event-type") String eventId) {
        return eventService.getEventFilterNodes(eventId);
    }

    @XapiRequestMapping(restrictTo = Admin, value = {"/events/event/properties"}, method = GET, produces = JSON, params = {"event-type"})
    @ApiOperation(value = "Get a event properties for a given Event ID")
    @ResponseBody
    public List<EventPropertyNode> retrieveEventProperties(final @RequestParam(value = "event-type") String eventId) {
        return eventService.getEventPropertyNodes(eventId);
    }

    @XapiRequestMapping(restrictTo = Admin, value = {"/events/subscription/filter"}, method = GET, produces = JSON)
    @ApiOperation(value = "Generate a subscription RegEx filter string from filter node set")
    public String generateFilterRegEx(final @RequestBody  Map<String, JsonPathFilterNode> filterNodes) {
        return eventService.generateFilterRegEx(filterNodes);
    }


    @XapiRequestMapping(restrictTo = Admin, value = "/events/subscription/{id}", method = PUT)
    @ApiOperation(value = "Update an existing Subscription")
    public ResponseEntity<Void> updateSubscription(final @PathVariable long id, final @RequestBody SubscriptionUpdate update)
            throws NrgServiceRuntimeException, SubscriptionValidationException, NotFoundException {
        final UserI userI = XDAT.getUserDetails();
        final Subscription toUpdate = eventService.getSubscription(id);
        Subscription updated = toUpdate.update(update);
        eventService.updateSubscription(updated);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = Owner, value = "/projects/{project}/events/subscription/{id}", method = PUT)
    @ApiOperation(value = "Update an existing Subscription for (project)")
    public ResponseEntity<Void> updateSubscription(final @PathVariable long id,
                                                   final @RequestBody Subscription subscription,
                                                   final @PathVariable String project)
            throws NrgServiceRuntimeException, SubscriptionValidationException, NotFoundException, UnauthorizedException {
        final UserI userI = XDAT.getUserDetails();
        checkProjectSubscriptionAccess(subscription, project, userI);
        final Subscription toUpdate =
                subscription.id() != null && subscription.id() == id
                        ? subscription
                        : subscription.toBuilder().id(id).actAsEventUser(false).build();
        eventService.updateSubscription(toUpdate);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/subscription/{id}/activate", method = POST)
    @ApiOperation(value = "Activate an existing Subscription")
    public ResponseEntity<Void> activateSubscription(final @PathVariable long id)
            throws NrgServiceRuntimeException, NotFoundException {
        eventService.activateSubscription(id);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = Owner, value = "/projects/{project}/events/subscription/{id}/activate", method = POST)
    @ApiOperation(value = "Activate an existing Subscription")
    public ResponseEntity<Void> activateSubscription(final @PathVariable long id,
                                                     final @PathVariable String project)
            throws NrgServiceRuntimeException, NotFoundException, UnauthorizedException {
        final UserI userI = XDAT.getUserDetails();
        checkProjectSubscriptionAccess(id, project, userI);
        eventService.activateSubscription(id);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/subscription/{id}/deactivate", method = POST)
    @ApiOperation(value = "deactivate an existing Subscription")
    public ResponseEntity<Void> deactivateSubscription(final @PathVariable long id)
            throws NrgServiceRuntimeException, NotFoundException {
        eventService.deactivateSubscription(id);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = Owner, value = "/projects/{project}/events/subscription/{id}/deactivate", method = POST)
    @ApiOperation(value = "Activate an existing Subscription")
    public ResponseEntity<Void> deactivateSubscription(final @PathVariable long id,
                                                     final @PathVariable String project)
            throws NrgServiceRuntimeException, NotFoundException, UnauthorizedException {
        final UserI userI = XDAT.getUserDetails();
        checkProjectSubscriptionAccess(id, project, userI);
        eventService.deactivateSubscription(id);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/subscriptions", method = GET, produces = JSON)
    @ResponseBody
    public List<Subscription> getAllSubscriptions()
            throws NrgServiceRuntimeException {
        return eventService.getSubscriptions();
    }

    @XapiRequestMapping(restrictTo = Owner, value = "/projects/{project}/events/subscriptions", method = GET, produces = JSON)
    @ResponseBody
    public List<Subscription> getAllSubscriptions(final @PathVariable String project)
            throws NrgServiceRuntimeException {
        return eventService.getSubscriptions(project);
    }

    @XapiRequestMapping(restrictTo = Admin, value = {"/events/subscription/{id}"}, method = GET, produces = JSON)
    @ApiOperation(value = "Get a Subscription by ID")
    @ResponseBody
    public Subscription retrieveSubscription(final @PathVariable long id) throws NotFoundException {
        return eventService.getSubscription(id);
    }

    @XapiRequestMapping(restrictTo = Owner, value = {"/projects/{project}/events/subscription/{id}"}, method = GET, produces = JSON)
    @ApiOperation(value = "Get a Subscription by ID")
    @ResponseBody
    public Subscription retrieveSubscription(final @PathVariable long id,
                                             final @PathVariable String project) throws NotFoundException, UnauthorizedException {
        checkProjectSubscriptionAccess(id, project, XDAT.getUserDetails());
        return eventService.getSubscription(id);
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/subscription/{id}", method = DELETE)
    @ApiOperation(value="Deactivate and delete a subscription by ID", code = 204)
    public ResponseEntity<Void> delete(final @PathVariable long id) throws Exception {
        eventService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    @XapiRequestMapping(restrictTo = Owner, value = "/projects/{project}/events/subscription/{id}", method = DELETE)
    @ApiOperation(value="Deactivate and delete a subscription by ID", code = 204)
    public ResponseEntity<Void> delete(final @PathVariable long id,
                                       final @PathVariable String project) throws Exception {

        checkProjectSubscriptionAccess(id, project, XDAT.getUserDetails());
        eventService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/delivered", method = GET)
    @ResponseBody
    public List<SubscriptionDelivery> getDeliveredSubscriptions(
            final @RequestParam(value = "project", required = false) String projectId,
            final @RequestParam(value = "subscription-id", required = false) Long subscriptionId,
            final @RequestParam(value = "include-filter-mismatch", required = false) Boolean includeFilterMismatch)
            throws Exception {
        final UserI userI = XDAT.getUserDetails();
        return eventService.getSubscriptionDeliveries(projectId, subscriptionId, includeFilterMismatch);
    }

    @XapiRequestMapping(restrictTo = Owner, value = "/projects/{project}/events/delivered", method = GET, params = {"subscription-id"})
    @ResponseBody
    public List<SubscriptionDelivery> getDeliveredProjectSubscriptions(
            final @PathVariable String project,
            final @RequestParam(value = "subscription-id", required = false) Long subscriptionId,
            final @RequestParam(value = "include-filter-mismatch", required = false) Boolean includeFilterMismatch)
            throws Exception {
        return eventService.getSubscriptionDeliveries(project, subscriptionId, includeFilterMismatch);
    }

    @XapiRequestMapping(restrictTo = Authenticated, value = "/events/events", method = GET)
    @ResponseBody
    public List<SimpleEvent> getEvents(final @RequestParam(value = "load-details", required = false) Boolean loadDetails) throws Exception {
        if(loadDetails != null) {
            return eventService.getEvents(loadDetails);
        }else{
            return eventService.getEvents();
        }
    }

    @XapiRequestMapping(restrictTo = Authenticated, value = "/events/event", method = GET, params = {"event-type"})
    @ResponseBody
    public SimpleEvent getEvent(final @RequestParam(value = "event-type") String eventId,
                                final @RequestParam(value = "load-details", required = false) Boolean loadDetails) throws Exception {
        if(loadDetails != null) {
            return eventService.getEvent(eventId, loadDetails);
        }else{
            return eventService.getEvent(eventId, false);
        }
    }


    //@XapiRequestMapping(restrictTo = Authenticated, value = "/events/actionproviders", method = GET)
    //@ApiOperation(value = "Get Action Providers and associated Actions")
    //@ResponseBody
    //public List<ActionProvider> getActionProviders()
    //        throws NrgServiceRuntimeException {
    //    return eventService.getActionProviders();
    //}


    @XapiRequestMapping(restrictTo = Admin, value = "/events/actions", method = GET, params = {"!event-type"})
    @ResponseBody
    public List<Action> getActions(final @RequestParam(value = "project", required = false) String projectId,
                                      final @RequestParam(value = "xnattype", required = false) String xnatType)
            throws NrgServiceRuntimeException {
        final UserI user = XDAT.getUserDetails();
        if(projectId != null)
            return eventService.getActions(projectId, xnatType, user);
        else
            return eventService.getActions(xnatType, user);
    }

    @XapiRequestMapping(restrictTo = Owner, value = "/projects/{project}/events/actions", method = GET, params = {"!event-type"})
    @ResponseBody
    public List<Action> getProjectActions(
            final @PathVariable String project,
            final @RequestParam(value = "xnattype", required = false) String xnatType)
            throws NrgServiceRuntimeException {
        final UserI user = XDAT.getUserDetails();
        return eventService.getActions(project, xnatType, user);
    }

    @XapiRequestMapping(restrictTo = Authenticated, value = "/events/allactions", method = GET, params = {"!projectid", "!xnattype"})
    @ResponseBody
    public List<Action> getAllActions()
            throws NrgServiceRuntimeException {
        final UserI user = XDAT.getUserDetails();
        return eventService.getAllActions();
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/actionsbyevent", method = GET, params = {"!xnattype"})
    @ApiOperation(value="Get actions that can act on a particular Event type")
    public List<Action> getActionsByEvent(final @RequestParam(value = "event-type", required = true) String eventId,
                                          final @RequestParam(value = "project", required = false) String projectId)
            throws NrgServiceRuntimeException {
        final UserI user = XDAT.getUserDetails();
        return eventService.getActionsByEvent(eventId, projectId, user);
    }

    @XapiRequestMapping(restrictTo = Owner, value = "/projects/{project}/events/actionsbyevent", method = GET, params = {"!xnattype"})
    @ApiOperation(value="Get actions that can act on a particular Event type")
    public List<Action> getProjectActionsByEvent(final @RequestParam(value = "event-type", required = true) String eventId,
                                          final @PathVariable String project)
            throws NrgServiceRuntimeException {
        final UserI user = XDAT.getUserDetails();
        return eventService.getActionsByEvent(eventId, project, user);
    }

    //@XapiRequestMapping(restrictTo = Admin, value = "/events/actions/{provider}", method = GET)
    //@ApiOperation(value = "Get a actions by provider")
    //@ResponseBody
    //public List<Action> getActions(final @PathVariable String provider)
    //        throws NrgServiceRuntimeException {
    //    final UserI user = XDAT.getUserDetails();
    //    return eventService.getActionsByProvider(provider, user);
    //}

    @XapiRequestMapping(restrictTo = Authenticated, value = {"/events/action"}, params = "actionkey", method = GET)
    @ApiOperation(value = "Get a actions by key in the form of \"ProviderID:ActionID\"")
    @ResponseBody
    public Action getAction(final @RequestParam String actionkey,
                            final @RequestParam(value = "project", required = false) String projectId,
                            final @RequestParam(value = "xnattype", required = false) String xnatType)
            throws NrgServiceRuntimeException {
        final UserI user = XDAT.getUserDetails();
        List<Action> actions = eventService.getActions(projectId, xnatType, user);
        Optional<Action> action = actions.stream().filter(a -> actionkey.contentEquals(a.actionKey())).findFirst();
        return action.isPresent() ? action.get() : null;
    }


    private void checkProjectSubscriptionAccess(Long subscriptionId, String project, UserI userI) throws UnauthorizedException, NotFoundException {
        checkProjectSubscriptionAccess(eventService.getSubscription(subscriptionId), project, userI);
    }

    private void checkProjectSubscriptionAccess(Subscription subscription, String project, UserI userI) throws UnauthorizedException {
        if(subscription.eventFilter().projectIds() == null || subscription.eventFilter().projectIds().isEmpty() || Arrays.asList(project) != subscription.eventFilter().projectIds()){
            throw new UnauthorizedException(userI.getLogin() + " not authorized to modify subscriptions for project(s): "
                    + ((subscription.eventFilter().projectIds() == null || subscription.eventFilter().projectIds().isEmpty()) ? "Site" : StringUtils.join(subscription.eventFilter().projectIds(),',')));
        }
    }

    @ResponseStatus(value = HttpStatus.FAILED_DEPENDENCY)
    @ExceptionHandler(value = {SubscriptionValidationException.class})
    public String handleFailedSubscriptionValidation(SubscriptionValidationException e) {
        return "Subscription format failed to validate.\n" + e.getMessage();
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = {UnauthorizedException.class})
    public String handleUnauthorized(final Exception e) {
        return "Unauthorized.\n" + e.getMessage();
    }

}
