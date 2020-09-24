package org.nrg.xnat.eventservice.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.exceptions.SubscriptionAccessException;
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.exceptions.UnauthorizedException;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.model.EventPropertyNode;
import org.nrg.xnat.eventservice.model.EventServicePrefs;
import org.nrg.xnat.eventservice.model.JsonPathFilterNode;
import org.nrg.xnat.eventservice.model.ProjectSubscriptionCreator;
import org.nrg.xnat.eventservice.model.SimpleEvent;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.model.SubscriptionCreator;
import org.nrg.xnat.eventservice.model.SubscriptionDelivery;
import org.nrg.xnat.eventservice.model.SubscriptionDeliverySummary;
import org.nrg.xnat.eventservice.model.SubscriptionDisplay;
import org.nrg.xnat.eventservice.model.SubscriptionUpdate;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityPaginatedRequest;
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

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authenticated;
import static org.nrg.xdat.security.helpers.AccessLevel.Delete;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@SuppressWarnings("deprecation")
@Slf4j
@Api("API for the XNAT Event Service")
@XapiRestController
public class EventServiceRestApi extends AbstractXapiRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_UTF8_VALUE;

    private final EventService eventService;

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
                                                     final @RequestParam(value = "overpopulate-attributes", required = false) Boolean overpopulateAttributes) throws SubscriptionValidationException, SubscriptionAccessException {
        final UserI        userI    = getSessionUser();
        final Subscription toCreate = Subscription.create(subscription, userI.getLogin());
        eventService.throwExceptionIfNameExists(toCreate);
        final Subscription created = eventService.createSubscription(toCreate, overpopulateAttributes);
        if (created == null) {
            return new ResponseEntity<>("Failed to create subscription.", HttpStatus.FAILED_DEPENDENCY);
        }
        return new ResponseEntity<>(created.name() + ":" + created.id(), HttpStatus.CREATED);
    }

    @XapiRequestMapping(restrictTo = Delete, value = "/projects/{project}/events/subscription", method = POST)
    @ApiOperation(value = "Create a Subscription for (project)", code = 201)
    public ResponseEntity<String> createSubscription(final @RequestBody ProjectSubscriptionCreator subscription,
                                                     final @PathVariable @Project String project) throws SubscriptionValidationException, SubscriptionAccessException, UnauthorizedException {
        final UserI  userI    = getSessionUser();
        Subscription toCreate = Subscription.createOnProject(subscription, userI.getLogin());
        checkProjectSubscriptionAccess(toCreate, project, userI);
        eventService.throwExceptionIfNameExists(toCreate);
        Subscription created = eventService.createSubscription(toCreate);
        if (created == null) {
            return new ResponseEntity<>("Failed to create subscription.", HttpStatus.FAILED_DEPENDENCY);
        }
        return new ResponseEntity<>(created.name() + ":" + created.id(), HttpStatus.CREATED);
    }

    @XapiRequestMapping(restrictTo = Admin, value = {"/events/subscription/filter"}, method = GET, produces = JSON, params = {"event-type"})
    @ApiOperation(value = "Get a subscription filter for a given Event ID")
    @ResponseBody
    public Map<String, JsonPathFilterNode> retrieveFilterBuilder(final @RequestParam(name = "event-type") String eventId) {
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
    public String generateFilterRegEx(final @RequestBody Map<String, JsonPathFilterNode> filterNodes) {
        return eventService.generateFilterRegEx(filterNodes);
    }


    @XapiRequestMapping(restrictTo = Admin, value = "/events/subscription/{id}", method = PUT)
    @ApiOperation(value = "Update an existing Subscription")
    public ResponseEntity<Void> updateSubscription(final @PathVariable long id, final @RequestBody SubscriptionUpdate update) throws SubscriptionValidationException, NotFoundException, SubscriptionAccessException {
        final Subscription toUpdate = eventService.getSubscription(id);
        final Subscription updated  = toUpdate.update(update);
        eventService.updateSubscription(updated);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = Delete, value = "/projects/{project}/events/subscription/{id}", method = PUT)
    @ApiOperation(value = "Update an existing Subscription for (project)")
    public ResponseEntity<Void> updateSubscription(final @PathVariable long id,
                                                   final @RequestBody SubscriptionUpdate update,
                                                   final @PathVariable @Project String project)
            throws SubscriptionValidationException, NotFoundException, UnauthorizedException, SubscriptionAccessException {
        final UserI        userI    = getSessionUser();
        final Subscription toUpdate = eventService.getSubscription(id);
        Subscription       updated  = toUpdate.update(update);
        checkProjectSubscriptionAccess(toUpdate, project, userI);
        eventService.updateSubscription(updated);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/subscription/{id}/activate", method = POST)
    @ApiOperation(value = "Activate an existing Subscription")
    public ResponseEntity<Void> activateSubscription(final @PathVariable long id)
            throws NotFoundException {
        eventService.activateSubscription(id);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = Delete, value = "/projects/{project}/events/subscription/{id}/activate", method = POST)
    @ApiOperation(value = "Activate an existing Subscription")
    public ResponseEntity<Void> activateSubscription(final @PathVariable long id,
                                                     final @PathVariable @Project String project)
            throws NotFoundException, UnauthorizedException, SubscriptionAccessException {
        final UserI userI = getSessionUser();
        checkProjectSubscriptionAccess(id, project, userI);
        eventService.activateSubscription(id);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/subscription/{id}/deactivate", method = POST)
    @ApiOperation(value = "deactivate an existing Subscription")
    public ResponseEntity<Void> deactivateSubscription(final @PathVariable long id)
            throws NotFoundException {
        eventService.deactivateSubscription(id);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = Delete, value = "/projects/{project}/events/subscription/{id}/deactivate", method = POST)
    @ApiOperation(value = "Activate an existing Subscription")
    public ResponseEntity<Void> deactivateSubscription(final @PathVariable long id,
                                                      final @PathVariable @Project String project)
            throws NotFoundException, UnauthorizedException, SubscriptionAccessException {
        final UserI userI = getSessionUser();
        checkProjectSubscriptionAccess(id, project, userI);
        eventService.deactivateSubscription(id);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/subscriptions", method = GET, produces = JSON)
    @ResponseBody
    public List<Subscription> getAllSubscriptions()
            throws SubscriptionAccessException {
        return eventService.getSubscriptions();
    }

    @XapiRequestMapping(restrictTo = Delete, value = "/projects/{project}/events/subscriptions", method = GET, produces = JSON)
    @ResponseBody
    public List<SubscriptionDisplay> getAllSubscriptions(final @Nonnull @PathVariable @Project String project) throws SubscriptionAccessException {
        return setSubscriptionDisplayEditFlag(eventService.getSubscriptions(project), project);
    }

    @XapiRequestMapping(restrictTo = Admin, value = {"/events/subscription/{id}"}, method = GET, produces = JSON)
    @ApiOperation(value = "Get a Subscription by ID")
    @ResponseBody
    public Subscription retrieveSubscription(final @PathVariable long id) throws NotFoundException, SubscriptionAccessException {
        return eventService.getSubscription(id);
    }

    @XapiRequestMapping(restrictTo = Delete, value = {"/projects/{project}/events/subscription/{id}"}, method = GET, produces = JSON)
    @ApiOperation(value = "Get a Subscription by ID")
    @ResponseBody
    public SubscriptionDisplay retrieveSubscription(final @PathVariable long id,
                                             final @PathVariable @Project String project) throws NotFoundException, UnauthorizedException, SubscriptionAccessException {
        checkReadProjectSubscriptionAccess(id, project, getSessionUser());
        return setSubscriptionDisplayEditFlag(eventService.getSubscription(id), project);
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/subscription/{id}", method = DELETE)
    @ApiOperation(value = "Deactivate and delete a subscription by ID", code = 204)
    public ResponseEntity<Void> delete(final @PathVariable long id) throws Exception {
        eventService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    @XapiRequestMapping(restrictTo = Delete, value = "/projects/{project}/events/subscription/{id}", method = DELETE)
    @ApiOperation(value = "Deactivate and delete a subscription by ID", code = 204)
    public ResponseEntity<Void> delete(final @PathVariable long id,
                                       final @PathVariable @Project String project) throws Exception {

        checkProjectSubscriptionAccess(id, project, getSessionUser());
        eventService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/delivered/count", method = GET)
    @ResponseBody
    public Integer getDeliveredSubscriptionsCount(
            final @RequestParam(value = "project", required = false) String projectId,
            final @RequestParam(value = "subscription-id", required = false) Long subscriptionId,
            final @RequestParam(value = "include-filter-mismatch", required = false) Boolean includeFilterMismatch) {
        return eventService.getSubscriptionDeliveriesCount(projectId, subscriptionId, includeFilterMismatch);
    }


    @XapiRequestMapping(restrictTo = Admin, value = "/events/delivered/summary", method = GET)
    @ResponseBody
    public List<SubscriptionDeliverySummary> getDeliveredSubscriptionsSummary(
            final @RequestParam(value = "project", required = false) String projectId) {
        return eventService.getSubscriptionDeliverySummary(projectId);
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/delivered/{id}", method = GET)
    @ResponseBody
    public SubscriptionDelivery getDeliveredSubscriptions(final @PathVariable long id)
            throws Exception {
        return eventService.getSubscriptionDelivery(id, null);
    }


    @XapiRequestMapping(restrictTo = Admin, value = "/events/delivered", method = GET)
    @ResponseBody
    public List<SubscriptionDelivery> getDeliveredSubscriptions(
            final @RequestParam(value = "project", required = false) String projectId,
            final @RequestParam(value = "subscription-id", required = false) Long subscriptionId,
            final @RequestParam(value = "include-filter-mismatch", required = false) Boolean includeFilterMismatch,
            final @RequestBody(required = false) SubscriptionDeliveryEntityPaginatedRequest request) {
        return eventService.getSubscriptionDeliveries(projectId, subscriptionId, includeFilterMismatch, request);
    }

    @XapiRequestMapping(restrictTo = Delete, value = "/projects/{project}/events/delivered/count", method = GET)
    @ResponseBody
    public Integer getDeliveredProjectSubscriptionsCount(
            final @PathVariable @Project String project,
            final @RequestParam(value = "subscription-id", required = false) Long subscriptionId,
            final @RequestParam(value = "include-filter-mismatch", required = false) Boolean includeFilterMismatch) {
        return eventService.getSubscriptionDeliveriesCount(project, subscriptionId, includeFilterMismatch);
    }

    @XapiRequestMapping(restrictTo = Delete, value = "/projects/{project}/events/delivered/{id}", method = GET)
    @ResponseBody
    public SubscriptionDelivery getDeliveredProjectSubscriptions(
            final @PathVariable long id,
            final @PathVariable @Project String project)
            throws Exception {
        return eventService.getSubscriptionDelivery(id, project);
    }

    @XapiRequestMapping(restrictTo = Delete, value = "/projects/{project}/events/delivered/summary", method = GET)
    @ResponseBody
    public List<SubscriptionDeliverySummary> getDeliveredProjectSubscriptionsSummary(final @PathVariable @Project String project) {
        return eventService.getSubscriptionDeliverySummary(project);
    }

    @XapiRequestMapping(restrictTo = Delete, value = "/projects/{project}/events/delivered", method = GET, params = {"subscription-id"})
    @ResponseBody
    public List<SubscriptionDelivery> getDeliveredProjectSubscriptions(
            final @PathVariable @Project String project,
            final @RequestParam(value = "subscription-id", required = false) Long subscriptionId,
            final @RequestParam(value = "include-filter-mismatch", required = false) Boolean includeFilterMismatch,
            final @RequestBody(required = false) SubscriptionDeliveryEntityPaginatedRequest request) {
        return eventService.getSubscriptionDeliveries(project, subscriptionId, includeFilterMismatch, request);
    }

    @XapiRequestMapping(restrictTo = Authenticated, value = "/events/events", method = GET)
    @ResponseBody
    public List<SimpleEvent> getEvents(final @RequestParam(value = "load-details", required = false) Boolean loadDetails) throws Exception {
        if (loadDetails != null) {
            return eventService.getEvents(loadDetails);
        } else {
            return eventService.getEvents();
        }
    }

    @XapiRequestMapping(restrictTo = Authenticated, value = "/events/event", method = GET, params = {"event-type"})
    @ResponseBody
    public SimpleEvent getEvent(final @RequestParam(value = "event-type") String eventId,
                                final @RequestParam(value = "load-details", required = false) Boolean loadDetails) throws Exception {
        if (loadDetails != null) {
            return eventService.getEvent(eventId, loadDetails);
        } else {
            return eventService.getEvent(eventId, false);
        }
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/actions", method = GET, params = {"!event-type"})
    @ResponseBody
    public List<Action> getActions(final @RequestParam(value = "project", required = false) String projectId,
                                   final @RequestParam(value = "xnattype", required = false) String xnatType) {
        final UserI user = getSessionUser();
        return StringUtils.isNotBlank(projectId) ? eventService.getActions(projectId, Collections.singletonList(xnatType), user) : eventService.getActions(xnatType, user);
    }

    @XapiRequestMapping(restrictTo = Delete, value = "/projects/{project}/events/actions", method = GET, params = {"!event-type"})
    @ResponseBody
    public List<Action> getProjectActions(
            final @PathVariable @Project String project,
            final @RequestParam(value = "xnattype", required = false) String xnatType) {
        final UserI user = getSessionUser();
        return eventService.getActions(project, xnatType, user);
    }

    @XapiRequestMapping(restrictTo = Authenticated, value = "/events/allactions", method = GET, params = {"!projectid", "!xnattype"})
    @ResponseBody
    public List<Action> getAllActions() {
        return eventService.getAllActions();
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/actionsbyevent", method = GET, params = {"!xnattype"})
    @ApiOperation(value = "Get actions that can act on a particular Event type")
    public List<Action> getActionsByEvent(final @RequestParam(value = "event-type") String eventId,
                                          final @RequestParam(value = "project", required = false) String projectId) {
        final UserI user = getSessionUser();
        return eventService.getActionsByEvent(eventId, projectId, user);
    }

    @XapiRequestMapping(restrictTo = Delete, value = "/projects/{project}/events/actionsbyevent", method = GET, params = {"!xnattype"})
    @ApiOperation(value = "Get actions that can act on a particular Event type")
    public List<Action> getProjectActionsByEvent(final @RequestParam(value = "event-type") String eventId,
                                                 final @PathVariable @Project String project) {
        final UserI user = getSessionUser();
        return eventService.getActionsByEvent(eventId, project, user);
    }

    //@XapiRequestMapping(restrictTo = Admin, value = "/events/actions/{provider}", method = GET)
    //@ApiOperation(value = "Get a actions by provider")
    //@ResponseBody
    //public List<Action> getActions(final @PathVariable String provider)
    //         {
    //    final UserI user = getSessionUser();
    //    return eventService.getActionsByProvider(provider, user);
    //}

    @XapiRequestMapping(restrictTo = Admin, value = "/events/prefs", method = GET)
    @ApiOperation(value = "Get Event Service Preferences")
    public EventServicePrefs getPrefs() {
        return eventService.getPrefsPojo();
    }

    @XapiRequestMapping(restrictTo = Admin, value = "/events/prefs", method = PUT)
    @ApiOperation(value = "Update Event Service Preferences")
    public ResponseEntity<Void> updatePrefs(final @RequestBody EventServicePrefs prefs) {
        eventService.updatePrefs(prefs);
        return ResponseEntity.ok().build();
    }


    @XapiRequestMapping(restrictTo = Authenticated, value = {"/events/action"}, params = "actionkey", method = GET)
    @ApiOperation(value = "Get a actions by key in the form of \"ProviderID:ActionID\"")
    @ResponseBody
    public Action getAction(final @RequestParam String actionkey,
                            final @RequestParam(value = "project", required = false) String projectId,
                            final @RequestParam(value = "xnattype", required = false) String xnatType) {
        final UserI user = getSessionUser();
        return eventService.getActions(projectId, xnatType, user).stream().filter(a -> actionkey.contentEquals(a.actionKey())).findFirst().orElse(null);
    }

    private void checkProjectSubscriptionAccess(@Nonnull Long subscriptionId, @Nonnull String project, @Nonnull UserI userI) throws UnauthorizedException, NotFoundException, SubscriptionAccessException {
        checkProjectSubscriptionAccess(eventService.getSubscription(subscriptionId), project, userI);
    }

    private void checkProjectSubscriptionAccess(@Nonnull Subscription subscription, @Nonnull String project, @Nonnull UserI userI) throws UnauthorizedException {
        if (subscription.eventFilter().projectIds() == null ||
            subscription.eventFilter().projectIds().isEmpty() ||
            !subscription.eventFilter().projectIds().contains(project) ||
            !(isAdmin(userI) || isOwner(userI, subscription.eventFilter().projectIds()))) {

            throw new UnauthorizedException(userI.getLogin() + " not authorized to modify subscriptions for project(s): "
                                            + ((subscription.eventFilter().projectIds() == null || subscription.eventFilter().projectIds().isEmpty()) ? "Site" : StringUtils.join(subscription.eventFilter().projectIds(), ',')));
        }
    }

    private void checkReadProjectSubscriptionAccess(@Nonnull Long subscriptionId, @Nonnull String project, @Nonnull UserI userI) throws UnauthorizedException, NotFoundException, SubscriptionAccessException {
        checkReadProjectSubscriptionAccess(eventService.getSubscription(subscriptionId), project, userI);
    }

    private void checkReadProjectSubscriptionAccess(@Nonnull Subscription subscription, @Nonnull String project, @Nonnull UserI userI) throws UnauthorizedException {
        if (subscription.eventFilter().projectIds() == null ||
                subscription.eventFilter().projectIds().isEmpty() ||
                subscription.eventFilter().projectIds().contains(project)) {
            return;
        }
        throw new UnauthorizedException(userI.getLogin() + " not authorized to read subscriptions for other projects");
    }

    private boolean isAdmin(final UserI user) {
        return getRoleHolder().isSiteAdmin(user);
    }

    private boolean isOwner(final UserI user, final List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return false;
        }
        final UserHelperServiceI userHelperService = UserHelper.getUserHelperService(user);
        for (String pid : projectIds) {
            if (!userHelperService.isOwner(pid)) {
                return false;
            }
        }
        return true;
    }

    // Set editable to false for Site or Multi-project subscriptions
    // Set editable to true only if being accessed from the project page of the only project on the subscription
    private SubscriptionDisplay setSubscriptionDisplayEditFlag(@Nonnull Subscription subscription, @Nonnull String projectIdAccess){
        Boolean editable = false;
        List<String> projectIds = subscription.eventFilter().projectIds();
        if(projectIds != null && projectIds.size() == 1)  {
            editable = projectIds.get(0).contentEquals(projectIdAccess);
        }
        return SubscriptionDisplay.create(subscription, editable);
    }

    private List<SubscriptionDisplay> setSubscriptionDisplayEditFlag(List<Subscription> subscriptions, String projectIdAccess){
        return subscriptions.stream()
                     .map(s -> setSubscriptionDisplayEditFlag(s, projectIdAccess))
                     .collect(Collectors.toList());
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
