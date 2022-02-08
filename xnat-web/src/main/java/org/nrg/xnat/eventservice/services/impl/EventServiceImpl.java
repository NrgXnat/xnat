package org.nrg.xnat.eventservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.node.XnatNode;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.events.ScheduledEvent;
import org.nrg.xnat.eventservice.exceptions.SubscriptionAccessException;
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.*;
import org.nrg.xnat.eventservice.model.xnat.XnatModelObject;
import org.nrg.xnat.eventservice.services.*;
import org.nrg.xnat.eventservice.sort.SimpleEventComparator;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.*;

@SuppressWarnings("UnstableApiUsage")
@Slf4j
@Service
public class EventServiceImpl implements EventService {
    private final EventSubscriptionEntityService      subscriptionService;
    private final EventBus                            eventBus;
    private final EventServiceComponentManager        componentManager;
    private final ActionManager                       actionManager;
    private final SubscriptionDeliveryEntityService   subscriptionDeliveryEntityService;
    private final UserManagementServiceI              userManagementService;
    private final EventPropertyService                eventPropertyService;
    private final ObjectMapper                        mapper;
    private final EventSchedulingService              schedulingService;
    private final Configuration                       jaywayConf     = Configuration.builder().build().addOptions(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS);
    private final EventServicePrefsBean               prefs;
    private final XnatAppInfo                         xnatAppInfo;


    @Autowired
    public EventServiceImpl(EventSubscriptionEntityService subscriptionService,
                            EventSchedulingService schedulingService,
                            EventBus eventBus,
                            EventServiceComponentManager componentManager,
                            ActionManager actionManager,
                            SubscriptionDeliveryEntityService subscriptionDeliveryEntityService,
                            UserManagementServiceI userManagementService,
                            EventPropertyService eventPropertyService,
                            ObjectMapper mapper,
                            EventServicePrefsBean prefsBean,
                            final XnatAppInfo xnatAppInfo) {
        this.subscriptionService = subscriptionService;
        this.eventBus = eventBus;
        this.componentManager = componentManager;
        this.actionManager = actionManager;
        this.subscriptionDeliveryEntityService = subscriptionDeliveryEntityService;
        this.userManagementService = userManagementService;
        this.eventPropertyService = eventPropertyService;
        this.schedulingService = schedulingService;
        this.mapper = mapper;
        this.prefs = prefsBean;
        this.xnatAppInfo = xnatAppInfo;

    }

    @Override
    public Subscription createSubscription(Subscription subscription) throws SubscriptionValidationException, SubscriptionAccessException {
        throwIfDisabled();
        Subscription created = subscriptionService.createSubscription(subscription);
        scheduleEventsForSubscription(created);
        return created;
    }

    @Override
    public Subscription createSubscription(Subscription subscription, Boolean overpopulateAttributes) throws SubscriptionValidationException, SubscriptionAccessException {
        throwIfDisabled();
        if (overpopulateAttributes != null && overpopulateAttributes) {
            final Map<String, String> current = subscription.attributes();
            final Map<String, String> attributes  = current != null ? new HashMap<>(current) : new HashMap<>();
            try {
                SimpleEvent event = getEvent(subscription.eventFilter().eventType(), true);
                final List<EventPropertyNode> eventPropertyNodes = event.eventProperties();
                if (eventPropertyNodes != null) {
                    attributes.putAll(eventPropertyNodes.stream().filter(Objects::nonNull).collect(Collectors.toMap(EventPropertyNode::name, EventPropertyNode::replacementKey)));
                    subscription = subscription.toBuilder().attributes(attributes).build();
                    log.debug("Overpopulating subscription attributes with: " + eventPropertyNodes.toString());
                }
            } catch (Exception e) {
                log.error("Failed to overpopulate attributes on subscription: {}", subscription.name() != null ? subscription.name() : subscription.id());
            }
        }

        return createSubscription(subscription);
    }

    @Override
    public Subscription updateSubscription(Subscription subscription) throws SubscriptionValidationException, NotFoundException, SubscriptionAccessException {
        throwIfDisabled();
        subscriptionService.validate(subscription);
        final Subscription original = subscriptionService.getSubscription(subscription.id());
        final Subscription updated  = subscriptionService.update(subscription);
        if (updated == null) {
            return null;
        }

        cancelEventsForSubscription(original);

        log.debug("Reactivating updated subscription: {}", subscription.id());
        if(ObjectUtils.defaultIfNull(updated.active(), false)){
            Subscription activated = subscriptionService.activate(updated);
            scheduleEventsForSubscription(activated);
            return activated;
        }else{
            Subscription deactivated = subscriptionService.deactivate(updated);
            return deactivated;
        }
    }

    @Override
    public void deleteSubscription(Long id) throws Exception {
        throwIfDisabled();
        Subscription toDelete = subscriptionService.getSubscription(id);
        subscriptionService.delete(id);
        cancelEventsForSubscription(toDelete);
    }

    @Override
    public List<Subscription> getSubscriptions() {
        if (!prefs.getEnabled()) {
            return null;
        }
        return subscriptionService.getAllSubscriptions();
    }

    @Override
    public List<Subscription> getSubscriptions(@Nonnull String projectId) {
        return prefs.getEnabled() ? subscriptionService.getSubscriptions(projectId) : null;
    }

    @Override
    public Subscription getSubscription(Long id) throws NotFoundException, SubscriptionAccessException {
        throwIfDisabled();
        return subscriptionService.getSubscription(id);
    }

    @Override
    public Subscription validateSubscription(Subscription subscription) throws SubscriptionValidationException {
        return subscriptionService.validate(subscription);
    }

    @Override
    public List<ActionProvider> getActionProviders() {
        return componentManager.getActionProviders().stream().map(this::toPojo).collect(Collectors.toList());
    }

    @Override
    public List<ActionProvider> getActionProviders(final String xsiType, final String projectId) {
        final List<String> xsiTypes = Collections.singletonList(xsiType);
        return componentManager.getActionProviders().stream().filter(provider -> !provider.getActions(projectId, xsiTypes, null).isEmpty()).map(this::toPojo).collect(Collectors.toList());
    }


    @Override
    public List<Action> getAllActions() {
        return actionManager.getAllActions();
    }

    @Override
    public List<Action> getActions(List<String> xnatTypes, UserI user) {
        return actionManager.getActions(xnatTypes, user);
    }

    @Override
    public List<Action> getActions(String projectId, List<String> xnatTypes, UserI user) {
        return actionManager.getActions(projectId, xnatTypes, user);
    }

    @Override
    public List<Action> getActionsByEvent(String eventId, String projectId, UserI user) {
        return getActionsByEvent(componentManager.getEvent(eventId), projectId, user);
    }

    private List<Action> getActionsByEvent(@Nonnull EventServiceEvent<?> event, String projectId, UserI user) {
        final List<String> xsiTypes = event.getPayloadXnatType() != null ? Collections.singletonList(event.getPayloadXnatType()) : componentManager.getXsiTypes(event.getObjectClass());
        return StringUtils.isBlank(projectId) ? getActions(xsiTypes, user) : getActions(projectId, xsiTypes, user);
    }

    @Override
    public List<Action> getActionsByProvider(String actionProvider, UserI user) {
        return actionManager.getActionsByProvider(actionProvider, user);
    }

    @Override
    public Action getActionByKey(String actionKey, UserI user) {
        return actionManager.getActionByKey(actionKey, user);
    }

    @Override
    public Map<String, JsonPathFilterNode> getEventFilterNodes(String eventId) {
        final EventServiceEvent<?> event = componentManager.getEvent(eventId);
        return event != null && !StringUtils.isBlank(event.getPayloadXnatType()) ? eventPropertyService.generateEventFilterNodes(event) : null;
    }

    @Override
    public List<EventPropertyNode> getEventPropertyNodes(String eventId) {
        final EventServiceEvent<?> event = componentManager.getEvent(eventId);
        return event != null && !StringUtils.isBlank(event.getPayloadXnatType()) ? eventPropertyService.generateEventPropertyKeys(event) : null;
    }

    @Override
    public List<SimpleEvent> getEvents() throws Exception {
        return getEvents(false);
    }

    @Override
    public List<SimpleEvent> getEvents(Boolean loadDetails) throws Exception {
        final List<SimpleEvent> events = new ArrayList<>();
        for (final EventServiceEvent<?> e : componentManager.getInstalledEvents()) {
            // TODO: This is a stupid way to get all the events
            events.add(getEvent(e.getType(), loadDetails));
        }
        events.sort(new SimpleEventComparator());
        return events;
    }

    @Override
    public SimpleEvent getEvent(@Nonnull final String eventId, Boolean loadDetails) throws Exception {
        for (EventServiceEvent e : componentManager.getInstalledEvents()) {
            if (eventId.contentEquals(e.getType())) {
                SimpleEvent simpleEvent = toPojo(e);
                if (loadDetails) {
                    Map<String, JsonPathFilterNode> eventFilterNodes = getEventFilterNodes(simpleEvent.id());
                    if (eventFilterNodes != null && eventFilterNodes.size() > 0) {
                        simpleEvent = simpleEvent.toBuilder().nodeFilters(eventFilterNodes).build();
                    }
                    List<EventPropertyNode> eventPropertyNodes = getEventPropertyNodes(simpleEvent.id());
                    if (eventPropertyNodes != null && !eventPropertyNodes.isEmpty()) {
                        simpleEvent = simpleEvent.toBuilder().eventProperties(eventPropertyNodes).build();
                    }
                }
                return simpleEvent;
            }
        }
        return null;
    }


    @Override
    public void reactivateAllSubscriptions() {

        List<Subscription> failedReactivations = new ArrayList<>();
        for (Subscription subscription : subscriptionService.getAllSubscriptions()) {
            if (subscription.active()) {
                log.debug("Reactivating subscription: " + Long.toString(subscription.id()));
                try {
                    Subscription active = subscriptionService.activate(subscription);
                    scheduleEventsForSubscription(active);
                    if (active == null || !active.active()) {
                        failedReactivations.add(subscription);
                    }

                } catch (Exception e) {
                    log.error("Failed to reactivate and update subscription: " + Long.toString(subscription.id()));
                    log.error(e.getMessage());
                }
            }
        }
        if (!failedReactivations.isEmpty()) {
            log.error("Failed to re-activate %i event subscriptions.", failedReactivations.size());
            for (Subscription fs : failedReactivations) {
                log.error("Subscription activation: <" + fs.toString() + "> failed.");
            }
        }
    }

    @Async
    @Override
    public void triggerEvent(EventServiceEvent event) {
        if (prefs != null && !prefs.getEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug("Preference: enabled == false. Skipping Event Service triggering");
            }
            return;
        }

        try {
            log.debug("Firing EventService Event for Label: " + event.getDisplayName() + " : " + event.toString());
            eventBus.notify(event, Event.wrap(event));
        } catch (Throwable e) {
            log.error("Exception trigger event: " + e.getMessage());
        }
    }


    @Override
    public void processEvent(EventServiceListener listener, Event event) {
        if (prefs != null && !prefs.getEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug("Preference: enabled == false. Skipping Event Service response");
            }
            return;
        }


        try {
            log.debug("Event noticed by EventService: " + event.getData().getClass().getSimpleName());
            String          jsonObject  = null;
            XnatModelObject modelObject = null;
            if (event.getData() instanceof EventServiceEvent) {
                EventServiceEvent esEvent = (EventServiceEvent) event.getData();
                for (Subscription subscription : subscriptionService.getSubscriptionsByListenerId(listener.getInstanceId())) {
                    log.debug("RegKey matched for " + listener.getInstanceId() + "  " + subscription.name());
                    // Create subscription delivery entry
                    Long deliveryId = subscriptionDeliveryEntityService.create(
                            SubscriptionEntity.fromPojoWithTemplate(subscription, subscriptionService.retrieve(subscription.id())),
                            esEvent,
                            listener,
                            subscription.actAsEventUser() ? esEvent.getUser() : subscription.subscriptionOwner(),
                            esEvent.getProjectId() == null ? "" : esEvent.getProjectId(),
                            subscription.attributes() == null ? "" : subscription.attributes().toString());
                    try {
                        subscriptionDeliveryEntityService.addStatus(deliveryId, SUBSCRIPTION_TRIGGERED, new Date(), "Subscription Service process started.");
                        // Is subscription enabled
                        if (!subscription.active()) {
                            subscriptionDeliveryEntityService.addStatus(deliveryId, SUBSCRIPTION_DISABLED_HALT, new Date(), "Inactive subscription skipped.");
                            log.debug("Inactive subscription: " + subscription.name() != null ? subscription.name() : "" + " skipped.");
                            return;
                        }
                        log.debug("Resolving action user (subscription owner or event user).");
                        UserI actionUser = subscription.actAsEventUser() ?
                                           userManagementService.getUser(esEvent.getUser()) :
                                           userManagementService.getUser(subscription.subscriptionOwner());
                        log.debug("Action User: " + actionUser.getUsername());

                        // ** Serialized event object ** //
                        try {
                            Object eventPayloadObject = esEvent.getObject(actionUser);
                            try {
                                modelObject = componentManager.getModelObject(eventPayloadObject, actionUser);
                                if (modelObject != null && mapper.canSerialize(modelObject.getClass())) {
                                    // Serialize data object
                                    log.debug("Serializing event object as known Model Object.");
                                    jsonObject = mapper.writeValueAsString(modelObject);
                                } else if (eventPayloadObject != null && mapper.canDeserialize(mapper.getTypeFactory().constructType(eventPayloadObject.getClass()))) {
                                    log.debug("Serializing event object as unknown object type.");
                                    jsonObject = mapper.writeValueAsString(eventPayloadObject);
                                } else {
                                    log.debug("Could not serialize event object in: " + esEvent.getType());
                                }
                            } catch (JsonProcessingException e) {
                                log.error("Exception attempting to serialize: {}", eventPayloadObject != null ? eventPayloadObject.getClass().getCanonicalName() : "null", e);
                            }

                            if (!Strings.isNullOrEmpty(jsonObject)) {
                                String objectSubString = StringUtils.substring(jsonObject, 0, 200);
                                log.debug("Serialized Object: " + objectSubString + "...");
                                subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_SERIALIZED, new Date(), "Payload Object Serialized.");
                            }
                        } catch (NullPointerException e) {
                            log.error("Aborting Event Service object serialization. Exception serializing event object: " + esEvent.getObjectClass().getName());
                            log.error(e.getMessage());
                            subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_SERIALIZATION_FAULT, new Date(), Strings.isNullOrEmpty(e.getMessage()) ? e.getStackTrace().toString() : e.getMessage());
                            return;
                        }

                        try {
                            //Filter on data object (if filter and object exist)
                            if (subscription.eventFilter() != null && !Strings.isNullOrEmpty(subscription.eventFilter().jsonPathFilter())) {
                                // ** Attempt to filter event if serialization was successful ** //
                                if (Strings.isNullOrEmpty(jsonObject)) {
                                    log.debug("Aborting event pipeline - Event: {} has no object that can be serialized and filtered.", esEvent.getType());
                                    subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_FILTER_MISMATCH_HALT, new Date(), "Event has no object that can be serialized and filtered.");
                                    return;
                                } else {
                                    String jsonFilter = "$[?(" + subscription.eventFilter().jsonPathFilter() + ")]";
                                    jsonFilter = jsonFilter.contains("'") ? jsonFilter.replace("'", "\"") : jsonFilter;
                                    List<String> filterResult    = JsonPath.using(jaywayConf).parse(jsonObject).read(jsonFilter);
                                    String       objectSubString = StringUtils.substring(jsonObject, 0, 200);
                                    if (filterResult.isEmpty()) {
                                        log.debug("Aborting event pipeline - Serialized event:\n" + objectSubString + "..." + "\ndidn't match JSONPath Filter:\n" + jsonFilter);
                                        subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_FILTER_MISMATCH_HALT, new Date(), "Event objected failed filter test.");
                                        return;
                                    } else {
                                        log.debug("JSONPath Filter Match - Serialized event:\n" + objectSubString + "..." + "\nJSONPath Filter:\n" + jsonFilter);
                                        subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_FILTERED, new Date(), "Event objected passed filter test.");
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            log.error("Aborting Event Service object filtering. Exception: " + e.getMessage());
                            subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_FILTERING_FAULT, new Date(), Strings.isNullOrEmpty(e.getMessage()) ? e.getStackTrace().toString() : e.getMessage());
                            return;
                        }
                        try {
                            // ** Extract triggering event details and save to delivery entity ** //
                            String xsiUri      = null;
                            String objectLabel = null;
                            if (modelObject != null) {
                                xsiUri = modelObject.getUri();
                                objectLabel = !Strings.isNullOrEmpty(modelObject.getLabel()) ? modelObject.getLabel() : modelObject.getId();
                            } else if (esEvent.getObjectClass() != null) {
                                objectLabel = esEvent.getObjectClass().getSimpleName();
                                // TODO: Handle other object types
                            }
                            subscriptionDeliveryEntityService.setTriggeringEvent(
                                    deliveryId, esEvent.getDisplayName(), esEvent.getCurrentStatus().name(), esEvent.isPayloadXsiType(), esEvent.getPayloadXnatType(), xsiUri, objectLabel);
                        } catch (Throwable e){
                            log.error("Could not build TriggeringEventEntity ", e.getMessage(), e);

                        }

                       try {
                           validateSubscription(subscription);
                       } catch (SubscriptionValidationException e) {
                           subscription.toBuilder().valid(false).validationMessage(e.getMessage());
                           subscriptionDeliveryEntityService.addStatus(deliveryId, FAILED, new Date(), "Invalid subscription.\n" + e.getMessage());
                           return;
                       }
                       actionManager.processEvent(subscription, esEvent, actionUser, deliveryId);
                    } catch (UserNotFoundException | UserInitException e) {
                        log.error("Failed to process subscription:" + subscription.name());
                        log.error(e.getMessage());
                        e.printStackTrace();
                        if (deliveryId != null) {
                            subscriptionDeliveryEntityService.addStatus(deliveryId, FAILED, new Date(), e.getMessage());
                        }
                    }
                }
            }
        } catch (NotFoundException e) {
            log.error("Failed to processEvent with subscription service.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Subscription activateSubscription(long id) throws NotFoundException {
        Subscription subscription = subscriptionService.getSubscription(id);
        Subscription activated =  subscriptionService.activate(subscription);
        scheduleEventsForSubscription(activated);
        return activated;
    }


    @Override
    public Subscription deactivateSubscription(long id) throws NotFoundException {
        Subscription subscription = subscriptionService.getSubscription(id);
        Subscription deactivated = subscriptionService.deactivate(subscription);
        cancelEventsForSubscription(deactivated);
        return deactivated;
    }

    private void scheduleEventsForSubscription(Subscription subscription){
        final String schedule = subscription.eventFilter().schedule();
        final String status = subscription.eventFilter().status();
        if(ScheduledEvent.Status.CRON.name().equals(status) && StringUtils.isNotEmpty(schedule)){
            schedulingService.scheduleEvent(() -> this.triggerEvent(new ScheduledEvent(schedule)), schedule);
        }
    }

    private void cancelEventsForSubscription(Subscription subscription){
        final String schedule = subscription.eventFilter().schedule();
        final String status = subscription.eventFilter().status();

        if(ScheduledEvent.Status.CRON.name().equals(status) && StringUtils.isNotEmpty(schedule)){
            final List<Subscription> subscriptions = subscriptionService.findActiveSubscriptionsBySchedule(schedule);
            if(subscriptions.isEmpty()){
                // If there are no active subscriptions using this cron trigger, cancel the event.
                schedulingService.cancelScheduledEvent(schedule);
            }
        }
    }

    @Override
    public Integer getSubscriptionDeliveriesCount(String projectId, Long subscriptionId, Boolean includeFilterMismatches) {
        return subscriptionDeliveryEntityService.count(projectId, subscriptionId, includeFilterMismatches);
    }

    @Deprecated
    @Override
    public List<SubscriptionDeliverySummary> getSubscriptionDeliverySummary(String projectId) {
        return subscriptionDeliveryEntityService.getSummaries(projectId);
    }

    @Override
    public SubscriptionDelivery getSubscriptionDelivery(Long id, String projectId) throws NotFoundException {
        return subscriptionDeliveryEntityService.get(id, projectId);
    }

    @Override
    public List<SubscriptionDelivery> getSubscriptionDeliveries(String projectId, Long subscriptionId, Boolean includeFilterMismatches, Boolean loadChildren) {
        return getSubscriptionDeliveries(projectId, subscriptionId, includeFilterMismatches, null, loadChildren);
    }

    @Override
    public List<SubscriptionDelivery> getSubscriptionDeliveries(final String projectId, final Long subscriptionId, final Boolean includeFilterMismatches, final SubscriptionDeliveryEntityPaginatedRequest request, Boolean loadChildren) {
        return subscriptionDeliveryEntityService.get(projectId, subscriptionId, (includeFilterMismatches != null ? includeFilterMismatches : false), request, loadChildren == null ? false : loadChildren);
    }

    @Override
    public void deleteSubscriptionDeliveryPayloads(Integer keepRecentCount){
        subscriptionDeliveryEntityService.deletePayloads(keepRecentCount);
    }

    @Override
    public String generateFilterRegEx(Map<String, JsonPathFilterNode> nodeFilters) {
        return eventPropertyService.generateJsonPathFilter(nodeFilters);
    }

    @Override
    public void validateFilterJsonPathPredicate(String jsonPathPredicate) throws InvalidPathException {
        subscriptionService.compileJsonPathFilter(jsonPathPredicate);
    }

    @Override
    public EventServiceComponentManager getComponentManager() {
        return componentManager;
    }

    @Override
    public EventServicePrefsBean getPrefs() {
        return prefs;
    }

    @Override
    public EventServicePrefs getPrefsPojo() {
        return prefs.toPojo();
    }

    @Override
    public void updatePrefs(EventServicePrefs prefs) {
        Boolean wasEnabled = this.prefs.getEnabled();
        this.prefs.update(prefs);
        Boolean isEnabled = this.prefs.getEnabled();
        if (!wasEnabled && isEnabled) {
            if (log.isDebugEnabled()) {
                log.debug("Enabling Event Service - Refreshing active subscriptions.");
            }
            reactivateAllSubscriptions();
        }
    }

    @Override
    @Async
    @Scheduled(cron = "*/30 * * * * *")
    public void syncReactorRegistrations()
    {
        XnatNode node = xnatAppInfo.getNode();
        if (node == null || node.getNodeId() == null || node.getNodeId().isEmpty()) {
            // Skip reactor sync, since this is not a multi-node XNAT
            return;
        }

        List<Subscription> allSubscriptions = subscriptionService.getAllSubscriptions();
        Set activeRegistrationSubscriptionIds = subscriptionService.getActiveRegistrationSubscriptionIds();

        // Deactivate disabled active subscriptions
        allSubscriptions.stream()
                           .filter(s -> !s.active())
                           .filter(s -> activeRegistrationSubscriptionIds.contains(s.id()))
                           .forEach(s -> subscriptionService.removeActiveRegistration(s.id()));

        // Deactivate deleted active subscriptions
        List<Long> enabledSubscriptionIds = allSubscriptions.stream()
                                                            .filter(s -> s.active())
                                                            .map(Subscription::id).collect(Collectors.toList());
        activeRegistrationSubscriptionIds.stream()
                                         .filter(arsid -> !enabledSubscriptionIds.contains(arsid))
                                         .forEach(arsid -> subscriptionService.removeActiveRegistration((Long) arsid));

        // Update Reactor subscription (reactivate) if subscription filter has been updated
        allSubscriptions.stream()
                .filter(s -> s.active())
                .filter(s -> activeRegistrationSubscriptionIds.contains(s.id()))
                .filter(s -> !s.eventFilter().getReactorCriteriaHash().equals(subscriptionService.getActiveRegistrationCriteriaHash(s.id())))
                .forEach(s -> subscriptionService.activate(s));

        // Activate non-active enabled subscriptions
        allSubscriptions.stream()
                .filter(s -> s.active())
                .filter(s -> !activeRegistrationSubscriptionIds.contains(s.id()))
                .forEach(s -> subscriptionService.activate(s));
    }

    private void throwIfDisabled() throws SubscriptionAccessException{
        if (!prefs.getEnabled()){
            throw new SubscriptionAccessException("Event Service disabled.");
        }
    }

    public static SimpleEvent toPojo(@Nonnull EventServiceEvent<?> event) {
        return SimpleEvent.builder()
                          .id(event.getType() == null ? "" : event.getType())
                          .statuses(event.getStatiStates())
                          .listenerService(
                                  event instanceof EventServiceListener
                                  ? ((EventServiceListener<?>) event).getClass().getName()
                                  : "")
                          .displayName(event.getDisplayName() == null ? "" : event.getDisplayName())
                          .description(event.getDescription() == null ? "" : event.getDescription())
                          .payloadClass(event.getObjectClass() == null ? "" : event.getObjectClass().getName())
                          .xnatType(event.getPayloadXnatType() == null ? "" : event.getPayloadXnatType())
                          .isXsiType(event.isPayloadXsiType() != null && event.isPayloadXsiType())
                          .eventScope(event.getEventScope() == null || event.getEventScope().isEmpty() ?
                                  Collections.emptyList() : event.getEventScope().stream().map(Enum::name).collect(Collectors.toList()))
                          .payloadSignature(event.filterablePayload() ? event.getPayloadSignatureObject() : null)
                          .build();
    }

    private ActionProvider toPojo(@Nonnull EventServiceActionProvider actionProvider) {
        return ActionProvider.builder()
                             .className(actionProvider.getName())
                             .displayName((actionProvider.getDisplayName()))
                             .description(actionProvider.getDescription())
                             .actions(actionProvider.getAllActions())
                             .build();
    }


}
