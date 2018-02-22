package org.nrg.xnat.eventservice.rest;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import org.nrg.xnat.eventservice.model.*;
import org.nrg.xnat.eventservice.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authenticated;
import static org.nrg.xdat.security.helpers.AccessLevel.Owner;
import static org.springframework.web.bind.annotation.RequestMethod.*;

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

    @XapiRequestMapping(restrictTo = Admin, value = "/events/subscription", method = POST)
    @ApiOperation(value = "Create a Subscription", code = 201)
    public ResponseEntity<String> createSubscription(final @RequestBody SubscriptionCreator subscription)
            throws NrgServiceRuntimeException, SubscriptionValidationException, JsonProcessingException {
        final UserI userI = XDAT.getUserDetails();
        Subscription toCreate = Subscription.create(subscription, userI.getLogin());
        eventService.throwExceptionIfNameExists(toCreate);
        Subscription created = eventService.createSubscription(toCreate);
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
        checkProjectSubscriptionCreateAccess(subscription ,project,userI);
        Subscription toCreate = Subscription.createOnProject(subscription, userI.getLogin(), project);
        eventService.throwExceptionIfNameExists(toCreate);
        Subscription created = eventService.createSubscription(toCreate);
        if(created == null){
            return new ResponseEntity<>("Failed to create subscription.",HttpStatus.FAILED_DEPENDENCY);
        }
        return new ResponseEntity<>(created.name() + ":" + Long.toString(created.id()), HttpStatus.CREATED);
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/subscription/{id}", method = PUT)
    @ApiOperation(value = "Update an existing Subscription")
    public ResponseEntity<Void> updateSubscription(final @PathVariable long id, final @RequestBody Subscription subscription)
            throws NrgServiceRuntimeException, SubscriptionValidationException, NotFoundException {
        final UserI userI = XDAT.getUserDetails();
        final Subscription toUpdate =
                subscription.id() != null && subscription.id() == id
                        ? subscription
                        : subscription.toBuilder().id(id).build();
        eventService.updateSubscription(toUpdate);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = Owner, value = "/projects/{project}/events/subscription/{id}", method = PUT)
    @ApiOperation(value = "Update an existing Subscription for (project)")
    public ResponseEntity<Void> updateSubscription(final @PathVariable long id,
                                                   final @RequestBody Subscription subscription,
                                                   final @PathVariable String project)
            throws NrgServiceRuntimeException, SubscriptionValidationException, NotFoundException, UnauthorizedException {
        final UserI userI = XDAT.getUserDetails();
        checkProjectSubscriptionModifyAccess(subscription, project, userI);
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
        checkProjectSubscriptionModifyAccess(id, project, userI);
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
        checkProjectSubscriptionModifyAccess(id, project, userI);
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
        checkProjectSubscriptionModifyAccess(id, project, XDAT.getUserDetails());
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

        checkProjectSubscriptionModifyAccess(id, project, XDAT.getUserDetails());
        eventService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/delivered", method = GET)
    @ResponseBody
    public List<SubscriptionDelivery> getDeliveredSubscriptions(
            final @RequestParam(value = "project", required = false) String projectId,
            final @RequestParam(value = "subscriptionid", required = false) Long subscriptionId)
            throws Exception {
        final UserI userI = XDAT.getUserDetails();
        return eventService.getSubscriptionDeliveries(projectId, subscriptionId);
    }

    @XapiRequestMapping(restrictTo = Owner, value = "/projects/{project}/events/delivered", method = GET)
    @ResponseBody
    public List<SubscriptionDelivery> getDeliveredProjectSubscriptions(
            final @PathVariable String project,
            final @RequestParam(value = "subscriptionid", required = false) Long subscriptionId)
            throws Exception {
        return eventService.getSubscriptionDeliveries(project, subscriptionId);
    }

    @XapiRequestMapping(restrictTo = Authenticated, value = "/events/events", method = GET)
    @ResponseBody
    public List<SimpleEvent> getEvents() throws Exception {
        return eventService.getEvents();
    }


    //@XapiRequestMapping(restrictTo = Authenticated, value = "/events/actionproviders", method = GET)
    //@ApiOperation(value = "Get Action Providers and associated Actions")
    //@ResponseBody
    //public List<ActionProvider> getActionProviders()
    //        throws NrgServiceRuntimeException {
    //    return eventService.getActionProviders();
    //}


    @XapiRequestMapping(restrictTo = Admin, value = "/events/actions", method = GET, params = {"!event-id"})
    @ResponseBody
    public List<Action> getActions(final @RequestParam(value = "project", required = false) String projectId,
                                      final @RequestParam(value = "xnattype", required = false) String xnatType)
            throws NrgServiceRuntimeException {
        final UserI user = XDAT.getUserDetails();
        if(projectId != null && xnatType != null)
            return eventService.getActions(projectId, xnatType, user);
        else
            return eventService.getActions(xnatType, user);
    }

    @XapiRequestMapping(restrictTo = Owner, value = "/projects/{project}/events/actions", method = GET, params = {"!event-id"})
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
    public List<Action> getActionsByEvent(final @RequestParam(value = "event-id", required = true) String eventId,
                                          final @RequestParam(value = "project", required = false) String projectId)
            throws NrgServiceRuntimeException {
        final UserI user = XDAT.getUserDetails();
        return eventService.getActionsByEvent(eventId, projectId, user);
    }

    @XapiRequestMapping(restrictTo = Owner, value = "/projects/{project}/events/actionsbyevent", method = GET, params = {"!xnattype"})
    @ApiOperation(value="Get actions that can act on a particular Event type")
    public List<Action> getProjectActionsByEvent(final @RequestParam(value = "event-id", required = true) String eventId,
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
    public Action getAction(final @RequestParam String actionkey)
            throws NrgServiceRuntimeException {
        final UserI user = XDAT.getUserDetails();
        return eventService.getActionByKey(actionkey, user);
    }

    private void checkProjectSubscriptionCreateAccess(ProjectSubscriptionCreator subscription, String project, UserI userI) throws UnauthorizedException {
        if(!Strings.isNullOrEmpty(subscription.projectId()) && project != subscription.projectId()){
            throw new UnauthorizedException(userI.getLogin() + " not authorized to create subscriptions for project: " + subscription.projectId());
        }
    }

    private void checkProjectSubscriptionModifyAccess(Long subscriptionId, String project, UserI userI) throws UnauthorizedException, NotFoundException {
        checkProjectSubscriptionModifyAccess(eventService.getSubscription(subscriptionId), project, userI);
    }

    private void checkProjectSubscriptionModifyAccess(Subscription subscription, String project, UserI userI) throws UnauthorizedException {
        if(Strings.isNullOrEmpty(subscription.projectId()) || project != subscription.projectId()){
            throw new UnauthorizedException(userI.getLogin() + " not authorized to modify subscriptions for project: "
                    + (Strings.isNullOrEmpty(subscription.projectId()) ? "Site" : subscription.projectId()));
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
