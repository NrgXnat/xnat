package org.nrg.xnat.eventservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
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
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.model.ActionProvider;
import org.nrg.xnat.eventservice.model.EventPropertyNode;
import org.nrg.xnat.eventservice.model.EventSignature;
import org.nrg.xnat.eventservice.model.JsonPathFilterNode;
import org.nrg.xnat.eventservice.model.Listener;
import org.nrg.xnat.eventservice.model.SimpleEvent;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.model.SubscriptionDelivery;
import org.nrg.xnat.eventservice.model.xnat.XnatModelObject;
import org.nrg.xnat.eventservice.services.ActionManager;
import org.nrg.xnat.eventservice.services.EventPropertyService;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.EventServiceActionProvider;
import org.nrg.xnat.eventservice.services.EventServiceComponentManager;
import org.nrg.xnat.eventservice.services.EventSubscriptionEntityService;
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.FAILED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.OBJECT_FILTERED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.OBJECT_FILTER_MISMATCH_HALT;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.OBJECT_SERIALIZATION_FAULT;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.OBJECT_SERIALIZED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.SUBSCRIPTION_DISABLED_HALT;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.SUBSCRIPTION_TRIGGERED;

@Service
@EnableAsync
public class EventServiceImpl implements EventService {
    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private ContextService contextService;
    private EventSubscriptionEntityService subscriptionService;
    private EventBus eventBus;
    private EventServiceComponentManager componentManager;
    private ActionManager actionManager;
    private SubscriptionDeliveryEntityService subscriptionDeliveryEntityService;
    private UserManagementServiceI userManagementService;
    private EventPropertyService eventPropertyService;
    private ObjectMapper mapper;
    private Configuration jaywayConf = Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);

    @Autowired
    public EventServiceImpl(ContextService contextService,
                            EventSubscriptionEntityService subscriptionService, EventBus eventBus,
                            EventServiceComponentManager componentManager,
                            ActionManager actionManager,
                            SubscriptionDeliveryEntityService subscriptionDeliveryEntityService,
                            UserManagementServiceI userManagementService,
                            EventPropertyService eventPropertyService,
                            ObjectMapper mapper) {
        this.contextService = contextService;
        this.subscriptionService = subscriptionService;
        this.eventBus = eventBus;
        this.componentManager = componentManager;
        this.actionManager = actionManager;
        this.subscriptionDeliveryEntityService = subscriptionDeliveryEntityService;
        this.userManagementService = userManagementService;
        this.eventPropertyService = eventPropertyService;
        this.mapper = mapper;
    }

    @Override
    public Subscription createSubscription(Subscription subscription) throws SubscriptionValidationException {

        return subscriptionService.createSubscription(subscription);
    }

    @Override
    public Subscription createSubscription(Subscription subscription, Boolean overpopulateAttributes) throws SubscriptionValidationException {
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
    public Subscription updateSubscription(Subscription subscription) throws SubscriptionValidationException, NotFoundException {
        Subscription updated = subscriptionService.update(subscription);
        if(updated != null){
            log.debug("Reactivating updated subscription: " + subscription.id());
            updated = subscriptionService.activate(updated);
        }
        return updated;
    }

    @Override
    public void deleteSubscription(Long id) throws Exception {
        subscriptionService.delete(id);
    }

    @Override
    public void throwExceptionIfNameExists(Subscription subscription) throws NrgServiceRuntimeException {
        subscriptionService.throwExceptionIfNameExists(subscription);
    }

    @Override
    public List<Subscription> getSubscriptions() {
        return subscriptionService.getAllSubscriptions();
    }

    @Override
    public List<Subscription> getSubscriptions(String projectId) {
        return subscriptionService.getSubscriptions(projectId);
    }

    @Override
    public Subscription getSubscription(Long id) throws NotFoundException {
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
        return actionManager.getActions(xnatType, user);
    }

    @Override
    public List<Action> getActions(String projectId, String xnatType, UserI user) {
        return actionManager.getActions(projectId, xnatType, user);
    }

    @Override
    public List<Action> getActionsByEvent(String eventId, String projectId, UserI user) {
        List<Action> actions = new ArrayList<>();
        EventServiceEvent event = componentManager.getEvent(eventId);
        if(event != null && !StringUtils.isNullOrEmpty(event.getPayloadXnatType())){
            if(StringUtils.isNullOrEmpty(projectId)){
                actions = getActions(event.getPayloadXnatType(), user);
            } else {
                actions = getActions(projectId, event.getPayloadXnatType(), user);
            }
        }
        return actions;
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
            SimpleEvent simpleEvent = getEvent(e.getEventUUID(), loadDetails);
            events.add(simpleEvent);
        }
        return events;
    }


    @Override
    public SimpleEvent getEvent(UUID uuid, Boolean loadDetails) throws Exception {
         for(EventServiceEvent e :componentManager.getInstalledEvents()){
            if(e.getEventUUID().equals(uuid)){
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
                log.debug("Reactivating of subscription: " + Long.toString(subscription.id()));
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
        String projectId = event.getProjectId();
        // TODO: Extract project id from event payload
        triggerEvent(event, projectId);
    }

    @Async
    @Override
    public void triggerEvent(EventServiceEvent event, String projectId) {
        try {
            EventSignature eventSignature = EventSignature.builder()
                    .eventType(event.getType())
                    .projectId(projectId)
                    .status(event.getCurrentStatus() != null ?  event.getCurrentStatus().name() : null)
                    .build();
            String eventKey = mapper.writeValueAsString(eventSignature);
            eventBus.notify(eventKey, Event.wrap(event));
            log.debug("Fired EventService Event for Label: " + eventKey);
        } catch (Throwable e) {
            log.error("Exception Triggering Event", e.getMessage());
        }
    }


    @Override
    public void processEvent(EventServiceListener listener, Event event) {
        try {
            log.debug("Event noticed by EventService: " + event.getData().getClass().getSimpleName());
            String jsonObject = null;
            XnatModelObject modelObject = null;
            if(event.getData() instanceof EventServiceEvent) {
                EventServiceEvent esEvent = (EventServiceEvent) event.getData();
                for (Subscription subscription : subscriptionService.getSubscriptionsByKey(listener.getInstanceId().toString())) {
                    log.debug("RegKey matched for " + subscription.listenerRegistrationKey() + "  " + subscription.name());
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
                            jsonObject = eventPropertyService.serializePayloadObject(esEvent.getObject(), actionUser);
                            if(!Strings.isNullOrEmpty(jsonObject)) {
                                String objectSubString = org.apache.commons.lang.StringUtils.substring(jsonObject, 0, 60);
                                log.debug("Serialized Object: " + objectSubString + "...");
                                subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_SERIALIZED, new Date(), "Object Serialized: " + objectSubString + "...");
                            }
                        }catch(NullPointerException e){
                            log.error("Aborting Event Service object serialization. Exception serializing event object: " + esEvent.getObjectClass().getName());
                            log.error(e.getMessage());
                            subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_SERIALIZATION_FAULT, new Date(), e.getMessage());
                        }

                        //Filter on data object (if filter and object exist)
                        if( subscription.eventFilter() != null && subscription.eventFilter().jsonPathFilter() != null ) {
                            // ** Attempt to filter event if serialization was successful ** //
                            if(Strings.isNullOrEmpty(jsonObject)){
                                log.debug("Aborting event pipeline - Event: {} has no object that can be serialized and filtered.", esEvent.getType());
                                subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_FILTER_MISMATCH_HALT, new Date(), "Event has no object that can be serialized and filtered.");
                                return;
                            } else {
                                String jsonFilter = subscription.eventFilter().jsonPathFilter();
                                List<String> filterResult = JsonPath.using(jaywayConf).parse(jsonObject).read(jsonFilter);
                                String objectSubString = org.apache.commons.lang.StringUtils.substring(jsonObject, 0, 60);
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
                        try {
                            // ** Extract triggering event details and save to delivery entity ** //
                            String xsiUri = null;
                            String objectLabel = null;
                            if (modelObject != null) {
                                xsiUri = modelObject.getUri();
                                objectLabel = modelObject.getLabel();
                            } else if (esEvent.getObject() != null) {
                                Object object = esEvent.getObject();
                                // TODO: Handle other object types
                            }
                            subscriptionDeliveryEntityService.setTriggeringEvent(
                                    deliveryId, esEvent.getDisplayName(), esEvent.isPayloadXsiType(), esEvent.getPayloadXnatType(), xsiUri, objectLabel);
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
    public List<SubscriptionDelivery> getSubscriptionDeliveries(String projectId, Long subscriptionId, Boolean includeFilterMismatches) {
        return subscriptionDeliveryEntityService.get(projectId, subscriptionId, includeFilterMismatches);
    }

    @Override
    public String generateFilterRegEx(Map<String, JsonPathFilterNode> nodeFilters) {
        return eventPropertyService.generateJsonPathFilter(nodeFilters);
    }

    private SimpleEvent toPojo(@Nonnull EventServiceEvent event) {
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
                      .payloadSignature(event.providesPayloadSignature() ? event.getPayloadSignature() : null)
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
