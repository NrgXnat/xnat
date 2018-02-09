package org.nrg.xnat.eventservice.rest;


import com.fasterxml.jackson.core.JsonProcessingException;
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
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Api(description = "API for the XNAT Event Event Service")
@XapiRestController
@RequestMapping(value = "/events")
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

//    @XapiRequestMapping(value = "/reactivate", method = POST)
//    @ApiOperation(value = "Reactivate all active subscriptions", code = 201)
//    public ResponseEntity<Void> reactivateSubscriptions() throws UnauthorizedException {
//        checkCreateOrThrow();
//        eventService.reactivateAllSubscriptions();
//        return ResponseEntity.ok().build();
//    }

    @XapiRequestMapping(value = "/subscription", method = POST)
    @ApiOperation(value = "Create a Subscription", code = 201)
    public ResponseEntity<String> createSubscription(final @RequestBody SubscriptionCreator subscription)
            throws NrgServiceRuntimeException, UnauthorizedException, SubscriptionValidationException, JsonProcessingException {
        final UserI userI = XDAT.getUserDetails();
        checkCreateOrThrow(userI);
        Subscription toCreate = Subscription.create(subscription, userI.getLogin());
        eventService.throwExceptionIfNameExists(toCreate);
        Subscription created = eventService.createSubscription(toCreate);
        if(created == null){
            return new ResponseEntity<>("Failed to create subscription.",HttpStatus.FAILED_DEPENDENCY);
        }
        return new ResponseEntity<>(created.name() + ":" + Long.toString(created.id()), HttpStatus.CREATED);

    }

    @XapiRequestMapping(value = "/subscription/{id}", method = PUT)
    @ApiOperation(value = "Update an existing Subscription")
    public ResponseEntity<Void> updateSubscription(final @PathVariable long id, final @RequestBody Subscription subscription)
            throws NrgServiceRuntimeException, UnauthorizedException, SubscriptionValidationException, NotFoundException {
        final UserI userI = XDAT.getUserDetails();
        checkCreateOrThrow(userI);
        final Subscription toUpdate =
                subscription.id() != null && subscription.id() == id
                        ? subscription
                        : subscription.toBuilder().id(id).build();
        eventService.updateSubscription(toUpdate);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(value = "/subscription/{id}/activate", method = POST)
    @ApiOperation(value = "Activate an existing Subscription")
    public ResponseEntity<Void> activateSubscription(final @PathVariable long id)
            throws NrgServiceRuntimeException, UnauthorizedException, NotFoundException {
        final UserI userI = XDAT.getUserDetails();
        checkCreateOrThrow(userI);
        eventService.activateSubscription(id);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(value = "/subscription/{id}/deactivate", method = POST)
    @ApiOperation(value = "deactivate an existing Subscription")
    public ResponseEntity<Void> deactivateSubscription(final @PathVariable long id)
            throws NrgServiceRuntimeException, UnauthorizedException, NotFoundException {
        final UserI userI = XDAT.getUserDetails();
        checkCreateOrThrow(userI);
        eventService.deactivateSubscription(id);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(value = "/subscriptions", method = GET, produces = JSON)
    @ResponseBody
    public List<Subscription> getAllSubscriptions()
            throws NrgServiceRuntimeException, UnauthorizedException {
        final UserI userI = XDAT.getUserDetails();
        checkCreateOrThrow(userI);
        return eventService.getSubscriptions();
    }

    @XapiRequestMapping(value = {"/subscription/{id}"}, method = GET, produces = JSON)
    @ApiOperation(value = "Get a Subscription by ID")
    @ResponseBody
    public Subscription retrieveSubscription(final @PathVariable long id) throws NotFoundException, UnauthorizedException {
        checkCreateOrThrow();
        return eventService.getSubscription(id);
    }

    @XapiRequestMapping(value = "/subscription/{id}", method = DELETE, restrictTo = Admin)
    @ApiOperation(value="Deactivate and delete a subscription by ID", code = 204)
    public ResponseEntity<Void> delete(final @PathVariable long id) throws Exception {
        checkCreateOrThrow();
        eventService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

//    //**  Project Scoped Subscription Endpoints **//
//    @ApiOperation(value="Gets project-specific Event Service subscriptions.")
//    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved project subscriptions."),
//            @ApiResponse(code = 403, message = "Insufficient permissions to view project subscriptions."),
//            @ApiResponse(code = 404, message = "The specified project wasn't found."),
//            @ApiResponse(code = 500, message = "An unexpected error occurred.")})
//    @XapiRequestMapping(value = "/subscriptions/{projectId}", consumes = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.PUT, restrictTo = Delete)
//    @ResponseBody
//    public List<Subscription> getAllSubscriptions(@ApiParam(value = "Indicates the ID of the project to be checked for subscriptions.", required = true)
//                                                  @PathVariable("projectId")  @ProjectId final String projectId)
//            throws NrgServiceRuntimeException, UnauthorizedException{
//        final UserI userI = XDAT.getUserDetails();
//        checkCreateOrThrow(userI);
//        return  eventService.getSubscriptions(projectId, userI);
//    }



    @XapiRequestMapping(value = "/delivered", method = GET)
    @ResponseBody
    public List<SubscriptionDelivery> getDeliveredSubscriptions(
            final @RequestParam(value = "projectid", required = false) String projectId,
            final @RequestParam(value = "subscriptionid", required = false) Long subscriptionId)
            throws Exception {
        final UserI userI = XDAT.getUserDetails();
        checkCreateOrThrow(userI);
        return eventService.getSubscriptionDeliveries(projectId, subscriptionId);
    }



    @XapiRequestMapping(value = "/events", method = GET)
    @ResponseBody
    public List<SimpleEvent> getEvents() throws Exception {
        final UserI userI = XDAT.getUserDetails();
        checkCreateOrThrow(userI);
        return eventService.getEvents();
    }


    @XapiRequestMapping(value = "/actionproviders", method = GET)
    @ApiOperation(value = "Get Action Providers and associated Actions")
    @ResponseBody
    public List<ActionProvider> getActionProviders()
            throws NrgServiceRuntimeException, UnauthorizedException {
        final UserI userI = XDAT.getUserDetails();
        checkCreateOrThrow(userI);
        return eventService.getActionProviders();
    }


    @XapiRequestMapping(value = "/actions", method = GET, params = {"!event-id"})
    @ResponseBody
    public List<Action> getActions(final @RequestParam(value = "projectid", required = false) String projectId,
                                      final @RequestParam(value = "xnattype", required = false) String xnatType)
            throws NrgServiceRuntimeException, UnauthorizedException {
        final UserI user = XDAT.getUserDetails();
        checkCreateOrThrow(user);
        if(projectId != null && xnatType != null)
            return eventService.getActions(projectId, xnatType, user);
        else
            return eventService.getActions(xnatType, user);
    }

    @XapiRequestMapping(value = "/allactions", method = GET, params = {"!projectid", "!xnattype"})
    @ResponseBody
    public List<Action> getAllActions()
            throws NrgServiceRuntimeException, UnauthorizedException {
        final UserI user = XDAT.getUserDetails();
        checkCreateOrThrow(user);
        return eventService.getAllActions();
    }


    @XapiRequestMapping(value = "/actionsbyevent", method = GET, params = {"!xnattype"})
    @ApiOperation(value="Get actions that can act on a particular Event type")
    public List<Action> getActionsByEvent(final @RequestParam(value = "event-id", required = true) String eventId,
                                          final @RequestParam(value = "projectid", required = false) String projectId)
            throws NrgServiceRuntimeException, UnauthorizedException {
        final UserI user = XDAT.getUserDetails();
        checkCreateOrThrow(user);
        return eventService.getActionsByEvent(eventId, projectId, user);
    }


    @XapiRequestMapping(value = "/actions/{provider}", method = GET)
    @ApiOperation(value = "Get a actions by provider")
    @ResponseBody
    public List<Action> getActions(final @PathVariable String provider)
            throws NrgServiceRuntimeException, UnauthorizedException {
        final UserI user = XDAT.getUserDetails();
        checkCreateOrThrow(user);
        return eventService.getActionsByProvider(provider, user);
    }

    @XapiRequestMapping(value = {"/action"}, params = "actionkey", method = GET)
    @ApiOperation(value = "Get a actions by key in the form of \"ProviderID:ActionID\"")
    @ResponseBody
    public Action getAction(final @RequestParam String actionkey)
            throws NrgServiceRuntimeException, UnauthorizedException {
        final UserI user = XDAT.getUserDetails();
        checkCreateOrThrow(user);
        return eventService.getActionByKey(actionkey, user);
    }


    private void checkCreateOrThrow() throws UnauthorizedException {
        checkCreateOrThrow(XDAT.getUserDetails());
    }

    private void checkCreateOrThrow(final UserI userI) throws UnauthorizedException {
        if (!isAdmin(userI)) {
            throw new UnauthorizedException(String.format("User %s not authorize.", userI == null ? "" : userI.getLogin()));
        }
    }

    private boolean isAdmin(final UserI userI) {
        return getRoleHolder().isSiteAdmin(userI);
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
