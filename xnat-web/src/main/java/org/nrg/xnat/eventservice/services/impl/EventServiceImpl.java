package org.nrg.xnat.eventservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.EvictingQueue;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.extern.slf4j.Slf4j;
import org.h2.util.StringUtils;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.exceptions.SubscriptionAccessException;
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.model.ActionProvider;
import org.nrg.xnat.eventservice.model.EventPropertyNode;
import org.nrg.xnat.eventservice.model.EventServicePrefs;
import org.nrg.xnat.eventservice.model.EventSignature;
import org.nrg.xnat.eventservice.model.JsonPathFilterNode;
import org.nrg.xnat.eventservice.model.Listener;
import org.nrg.xnat.eventservice.model.SimpleEvent;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.model.SubscriptionDelivery;
import org.nrg.xnat.eventservice.model.SubscriptionDeliverySummary;
import org.nrg.xnat.eventservice.model.xnat.XnatModelObject;
import org.nrg.xnat.eventservice.services.ActionManager;
import org.nrg.xnat.eventservice.services.EventPropertyService;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.EventServiceActionProvider;
import org.nrg.xnat.eventservice.services.EventServiceComponentManager;
import org.nrg.xnat.eventservice.services.EventServicePrefsBean;
import org.nrg.xnat.eventservice.services.EventSubscriptionEntityService;
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.FAILED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.OBJECT_FILTERED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.OBJECT_FILTERING_FAULT;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.OBJECT_FILTER_MISMATCH_HALT;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.OBJECT_SERIALIZATION_FAULT;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.OBJECT_SERIALIZED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.SUBSCRIPTION_DISABLED_HALT;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.SUBSCRIPTION_TRIGGERED;

@Slf4j
@Service
@EnableAsync
@EnableScheduling
public class EventServiceImpl implements EventService {

    private ContextService contextService;
    private EventSubscriptionEntityService subscriptionService;
    private EventBus eventBus;
    private EventServiceComponentManager componentManager;
    private ActionManager actionManager;
    private SubscriptionDeliveryEntityService subscriptionDeliveryEntityService;
    private UserManagementServiceI userManagementService;
    private EventPropertyService eventPropertyService;
    private ObjectMapper mapper;
    private Configuration jaywayConf = Configuration.defaultConfiguration().builder().build().addOptions(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS);
    private EvictingQueue<EventServiceEvent> recentTriggers = EvictingQueue.create(100);
    private EventServicePrefsBean prefs;

    @Autowired
    public EventServiceImpl(ContextService contextService,
                            EventSubscriptionEntityService subscriptionService, EventBus eventBus,
                            EventServiceComponentManager componentManager,
                            ActionManager actionManager,
                            SubscriptionDeliveryEntityService subscriptionDeliveryEntityService,
                            UserManagementServiceI userManagementService,
                            EventPropertyService eventPropertyService,
                            ObjectMapper mapper,
                            EventServicePrefsBean prefsBean) {
        this.contextService = contextService;
        this.subscriptionService = subscriptionService;
        this.eventBus = eventBus;
        this.componentManager = componentManager;
        this.actionManager = actionManager;
        this.subscriptionDeliveryEntityService = subscriptionDeliveryEntityService;
        this.userManagementService = userManagementService;
        this.eventPropertyService = eventPropertyService;
        this.mapper = mapper;
        this.prefs = prefsBean;
    }

    @Override
    public Subscription createSubscription(Subscription subscription) throws SubscriptionValidationException, SubscriptionAccessException {
        throwIfDisabled();
        return subscriptionService.createSubscription(subscription);
    }

    @Override
    public Subscription createSubscription(Subscription subscription, Boolean overpopulateAttributes) throws SubscriptionValidationException , SubscriptionAccessException {
        throwIfDisabled();
        if(overpopulateAttributes != null && overpopulateAttributes == true){
            Map<String, String> attributes = new HashMap<>(subscription.attributes());
            try {
                SimpleEvent event = getEvent(subscription.eventFilter().eventType(), true);
                event.eventProperties().forEach(node -> attributes.put(node.name(), node.replacementKey()));
                subscription = subscription.toBuilder().attributes(attributes).build();
                log.debug("Overpopulating subscription attributes with: " + event.eventProperties().toString());
            } catch (Exception e) {
                log.error("Failed to overpopulate attributes on subscription: ", subscription.name() != null ? subscription.name() : subscription.id());
            }
        }

        return createSubscription(subscription);
    }

    @Override
    public Subscription updateSubscription(Subscription subscription) throws SubscriptionValidationException, NotFoundException, SubscriptionAccessException {
        throwIfDisabled();
        Subscription updated = subscriptionService.update(subscription);
        if(updated != null){
            log.debug("Reactivating updated subscription: " + subscription.id());
            if(updated.active()) {
                updated = subscriptionService.activate(updated);
            } else {
                updated = subscriptionService.deactivate(updated);
            }
        }
        return updated;
    }

    @Override
    public void deleteSubscription(Long id) throws Exception {
        throwIfDisabled();
        subscriptionService.delete(id);
    }

    @Override
    public void throwExceptionIfNameExists(Subscription subscription) throws NrgServiceRuntimeException {
        subscriptionService.throwExceptionIfNameExists(subscription);
    }

    @Override
    public List<Subscription> getSubscriptions() throws SubscriptionAccessException{
        if(!prefs.getEnabled()) {
            return Arrays.asList();
        }
        return subscriptionService.getAllSubscriptions();
    }

    @Override
    public List<Subscription> getSubscriptions(String projectId) throws SubscriptionAccessException{
        if(!prefs.getEnabled()) {
            return Arrays.asList();
        }
        return subscriptionService.getSubscriptions(projectId);
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
        List<ActionProvider> providers = new ArrayList<>();
        for(EventServiceActionProvider ap : componentManager.getActionProviders()) {
            providers.add(toPojo(ap));
        }
        return providers;
    }

    @Override
    public List<ActionProvider> getActionProviders(String xsiType, String projectId) {
        List<ActionProvider> providers = new ArrayList<>();
        for(EventServiceActionProvider ap : componentManager.getActionProviders()) {
            providers.add(toPojo(ap));
        }
        return providers;
    }


    @Override
    public List<Action> getAllActions() {
        return actionManager.getAllActions();
    }

    @Override
    public List<Action> getActions(String xnatType, UserI user) {
        return getActions(Arrays.asList(xnatType), user);
    }

    @Override
    public List<Action> getActions(String projectId, String xnatType, UserI user) {
        return getActions(projectId, Arrays.asList(xnatType), user);
    }

    @Override
    public List<Action> getActions(List<String> xnatTypes, UserI user) {
        return actionManager.getActions(null, xnatTypes, user);
    }

    @Override
    public List<Action> getActions(String projectId, List<String> xnatTypes, UserI user) {
        return actionManager.getActions(projectId, xnatTypes, user);
    }

    @Override
    public List<Action> getActionsByEvent(String eventId, String projectId, UserI user) {
        return getActionsByEvent(componentManager.getEvent(eventId), projectId, user);
    }

    private List<Action> getActionsByEvent(@Nonnull EventServiceEvent event, String projectId, UserI user){
        List<Action> actions = new ArrayList<>();
        List<String> xsiTypes;
        if(event.getPayloadXnatType() != null) {
            xsiTypes = Arrays.asList(event.getPayloadXnatType());
        } else {
            xsiTypes = componentManager.getXsiTypes(event.getObjectClass());
        }
        if(event != null){
            if(StringUtils.isNullOrEmpty(projectId)){
                actions = getActions(xsiTypes, user);
            } else {
                actions = getActions(projectId, xsiTypes, user);
            }
        }
        return actions;    }

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
        EventServiceEvent event = componentManager.getEvent(eventId);
        if(event != null && !StringUtils.isNullOrEmpty(event.getPayloadXnatType())) {
            return eventPropertyService.generateEventFilterNodes(event);
        }
        return null;
    }

    @Override
    public List<EventPropertyNode> getEventPropertyNodes(String eventId) {
        EventServiceEvent event = componentManager.getEvent(eventId);
        if(event != null && !StringUtils.isNullOrEmpty(event.getPayloadXnatType())) {
            return eventPropertyService.generateEventPropertyKeys(event);
        }
        return null;
    }

    @Override
    public List<SimpleEvent> getEvents() throws Exception {
        return getEvents(false);
    }

    @Override
    public List<SimpleEvent> getEvents(Boolean loadDetails) throws Exception {
        List<SimpleEvent> events = new ArrayList();
        for(EventServiceEvent e : componentManager.getInstalledEvents()){
            // TODO: This is a stupid way to get all the events
            SimpleEvent simpleEvent = getEvent(e.getType(), loadDetails);
            events.add(simpleEvent);
        }
        return events;
    }


    @Override
    public SimpleEvent getEvent(@Nonnull final String eventId, Boolean loadDetails) throws Exception {
        for(EventServiceEvent e : componentManager.getInstalledEvents()){
            if(eventId.contentEquals(e.getType())){
                SimpleEvent simpleEvent = toPojo(e);
                if(loadDetails){
                    Map<String, JsonPathFilterNode> eventFilterNodes = getEventFilterNodes(simpleEvent.id());
                    if(eventFilterNodes != null && eventFilterNodes.size()>0){
                        simpleEvent = simpleEvent.toBuilder().nodeFilters(eventFilterNodes).build();
                    }
                    List<EventPropertyNode> eventPropertyNodes = getEventPropertyNodes(simpleEvent.id());
                    if(eventPropertyNodes != null && !eventPropertyNodes.isEmpty()){
                        simpleEvent = simpleEvent.toBuilder().eventProperties(eventPropertyNodes).build();
                    }
                }
                return simpleEvent;
            }
        }
        return null;
    }

    @Override
    @Deprecated
    public List<Listener> getInstalledListeners() {

        return null;
    }

    @Override
    public void reactivateAllSubscriptions() {

        List<Subscription> failedReactivations = new ArrayList<>();
        for (Subscription subscription:subscriptionService.getAllSubscriptions()) {
            if(subscription.active()) {
                log.debug("Reactivating subscription: " + Long.toString(subscription.id()));
                try {
                    Subscription active = subscriptionService.activate(subscription);
                    if(active == null || !active.active()){
                        failedReactivations.add(subscription);
                    }

                } catch (Exception e) {
                    log.error("Failed to reactivate and update subscription: " + Long.toString(subscription.id()));
                    log.error(e.getMessage());
                }
            }
        }
        if(!failedReactivations.isEmpty()){
            log.error("Failed to re-activate %i event subscriptions.", failedReactivations.size());
            for (Subscription fs:failedReactivations) {
                log.error("Subscription activation: <" + fs.toString() + "> failed.");
            }
        }
    }

    @Async
    @Override
    public void triggerEvent(EventServiceEvent event) {
        if (prefs != null && !prefs.getEnabled()){
            if(log.isDebugEnabled()){ log.debug("Preference: enabled == false. Skipping Event Service triggering");  }
            return;
        }

        try{
            log.debug("Firing EventService Event for Label: " + event.getDisplayName() + " : " + event.toString());
            eventBus.notify(event, Event.wrap(event));
            recentTriggers.add(event);
        } catch (Throwable e) {
            log.error("Exception Triggering Event: " + e.getMessage());
        }
    }


    @Override
    public void processEvent(EventServiceListener listener, Event event) {
        if (prefs != null && !prefs.getRespondToEvents()){
            if(log.isDebugEnabled()){ log.debug("Preference: respondToEvents == false. Skipping Event Service response");  }
            return;
        }

        try {
            log.debug("Event noticed by EventService: " + event.getData().getClass().getSimpleName());
            String jsonObject = null;
            XnatModelObject modelObject = null;
            if(event.getData() instanceof EventServiceEvent) {
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
                            Object eventPayloadObject = esEvent.getObject();
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

                            if(!Strings.isNullOrEmpty(jsonObject)) {
                                String objectSubString = org.apache.commons.lang.StringUtils.substring(jsonObject, 0, 200);
                                log.debug("Serialized Object: " + objectSubString + "...");
                                subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_SERIALIZED, new Date(), "Object Serialized: " + objectSubString + "...");
                            }
                        }catch(NullPointerException e){
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
                                    jsonFilter = jsonFilter.contains("'") ? jsonFilter.replace("'","\"") : jsonFilter;
                                    List<String> filterResult = JsonPath.using(jaywayConf).parse(jsonObject).read(jsonFilter);
                                    String objectSubString = org.apache.commons.lang.StringUtils.substring(jsonObject, 0, 200);
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
                        } catch (Throwable e){
                            log.error("Aborting Event Service object filtering. Exception: " + e.getMessage());
                            subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_FILTERING_FAULT, new Date(), Strings.isNullOrEmpty(e.getMessage()) ? e.getStackTrace().toString() : e.getMessage());
                            return;
                        }
                        try {
                            // ** Extract triggering event details and save to delivery entity ** //
                            String xsiUri = null;
                            String objectLabel = null;
                            if (modelObject != null) {
                                xsiUri = modelObject.getUri();
                                objectLabel = !Strings.isNullOrEmpty(modelObject.getLabel()) ? modelObject.getLabel() : modelObject.getId();
                            } else if (esEvent.getObject() != null) {
                                Object object = esEvent.getObject();
                                objectLabel = object.getClass().getSimpleName();
                                // TODO: Handle other object types
                            }
                            subscriptionDeliveryEntityService.setTriggeringEvent(
                                    deliveryId, esEvent.getDisplayName(), esEvent.getCurrentStatus().name(), esEvent.isPayloadXsiType(), esEvent.getPayloadXnatType(), xsiUri, objectLabel);
                        } catch (Throwable e){
                            log.error("Could not build TriggeringEventEntity ", e.getMessage(), e);
                        }
                         actionManager.processEvent(subscription, esEvent, actionUser, deliveryId);
                    } catch (UserNotFoundException |UserInitException e) {
                        log.error("Failed to process subscription:" + subscription.name());
                        log.error(e.getMessage());
                        e.printStackTrace();
                        if(deliveryId != null){
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
        return subscriptionService.activate(subscription);
    }


    @Override
    public Subscription deactivateSubscription(long id) throws NotFoundException {
        Subscription subscription = subscriptionService.getSubscription(id);
        return subscriptionService.deactivate(subscription);
    }

    @Override
    public Integer getSubscriptionDeliveriesCount(String projectId, Long subscriptionId, Boolean includeFilterMismatches) {
        return subscriptionDeliveryEntityService.count(projectId, subscriptionId, includeFilterMismatches);
    }

    @Override
    public List<SubscriptionDeliverySummary> getSubscriptionDeliverySummary(String projectId) {
        return subscriptionDeliveryEntityService.getSummaries(projectId);
    }

    @Override
    public SubscriptionDelivery getSubscriptionDelivery(Long id, String projectId) throws NotFoundException {
        return subscriptionDeliveryEntityService.get(id, projectId);
    }

    @Override
    public List<SubscriptionDelivery> getSubscriptionDeliveries(String projectId, Long subscriptionId, Boolean includeFilterMismatches) {
        return getSubscriptionDeliveries(projectId, subscriptionId, includeFilterMismatches, null, null);
    }

    @Override
    public List<SubscriptionDelivery> getSubscriptionDeliveries(String projectId, Long subscriptionId, Boolean includeFilterMismatches,
                                                                Integer firstResult, Integer maxResults) {
        return subscriptionDeliveryEntityService.get(projectId, subscriptionId,
                (includeFilterMismatches != null ? includeFilterMismatches : false),
                firstResult, maxResults);
    }

    @Override
    public String generateFilterRegEx(Map<String, JsonPathFilterNode> nodeFilters) {
        return eventPropertyService.generateJsonPathFilter(nodeFilters);
    }

    @Override
    public List<String> getRecentTriggers(Integer count) {
        List<EventServiceEvent> triggerEvents;
        if (count != null && count > 0 && recentTriggers != null && !recentTriggers.isEmpty()) {
            triggerEvents = recentTriggers.stream().limit(count).collect(Collectors.toList());
        } else {
            triggerEvents = recentTriggers.stream().collect(Collectors.toList());
        }
        List<String> triggers = new ArrayList<>();
        try {
            for (EventServiceEvent event : triggerEvents) {
                EventSignature eventSignature = EventSignature.builder()
                                                              .eventType(event.getType())
                                                              .projectId(Strings.isNullOrEmpty(event.getProjectId()) ? null : event.getProjectId())
                                                              .status(event.getCurrentStatus() != null ? event.getCurrentStatus().name() : null)
                                                              .payload(event.filterablePayload() ? event.getPayloadSignatureObject() : null)
                                                              .build();
                triggers.add(mapper.writeValueAsString(eventSignature));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return triggers;
    }

    @Override
    public EventServiceComponentManager getComponentManager() {
        return componentManager;
    }

    @Override
    public EventServicePrefsBean getPrefs() { return prefs; }

    @Override
    public EventServicePrefs getPrefsPojo() { return prefs.toPojo(); }

    @Override
    public void updatePrefs(EventServicePrefs prefs) {
        Boolean wasEnabled = this.prefs.getEnabled();
        this.prefs.update(prefs);
        Boolean isEnabled = this.prefs.getEnabled();
        if( !wasEnabled && isEnabled){
            if(log.isDebugEnabled()){
                log.debug("Enabling Event Service - Refreshing active subscriptions.");
            }
            reactivateAllSubscriptions();
        }
    }

    @Override
    @Scheduled(cron = "*/30 * * * * *")
    public void syncReactorRegistrations()
    {
        List<Subscription> allSubscriptions = subscriptionService.getAllSubscriptions();
        Set activeRegistrationSubscriptionIds = subscriptionService.getActiveRegistrationSubscriptionIds();

        // Activate non-active enabled subscriptions
        allSubscriptions.stream()
                           .filter(s -> s.active())
                           .filter(s -> !activeRegistrationSubscriptionIds.contains(s.id()))
                           .forEach(s -> subscriptionService.activate(s));

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
    }

    private void throwIfDisabled() throws SubscriptionAccessException{
        if (!prefs.getEnabled()){
            throw new SubscriptionAccessException("Event Service disabled.");
        }
    }

    public static SimpleEvent toPojo(@Nonnull EventServiceEvent event) {
        return SimpleEvent.builder()
                    .id(event.getType() == null ? "" : event.getType())
                    .statuses(event.getStatiStates())
                    .listenerService(
                            event instanceof EventServiceListener
                            ? ((EventServiceListener) event).getClass().getName()
                            : "")
                      .displayName(event.getDisplayName() == null ? "" : event.getDisplayName())
                      .description(event.getDescription() == null ? "" : event.getDescription())
                      .payloadClass(event.getObjectClass() == null ? "" : event.getObjectClass().getName())
                      .xnatType(event.getPayloadXnatType() == null ? "" : event.getPayloadXnatType())
                      .isXsiType(event.isPayloadXsiType() == null ? false : event.isPayloadXsiType())
                      .eventScope(event.getEventScope() == null ? "" : event.getEventScope().name())
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
