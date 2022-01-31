package org.nrg.xnat.eventservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.daos.EventSubscriptionEntityDao;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.AbstractEventServiceEvent;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.EventFilter;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.services.*;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.bus.registry.Registry;
import reactor.bus.selector.Selector;
import reactor.bus.selector.Selectors;
import reactor.fn.Consumer;
import reactor.fn.Predicate;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Transient;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class EventSubscriptionEntityServiceImpl extends AbstractHibernateEntityService<SubscriptionEntity, EventSubscriptionEntityDao> implements EventSubscriptionEntityService {
    private EventBus eventBus;
    private ContextService contextService;
    private ActionManager actionManager;
    private EventServiceComponentManager componentManager;
    private EventService eventService;
    private ObjectMapper mapper;
    private UserManagementServiceI userManagementService;
    private SubscriptionDeliveryEntityService subscriptionDeliveryEntityService;
    private EventSchedulingService eventSchedulingService;
    private Map<Long, ActiveRegistration> activeRegistrations = new HashMap<>();


    @Autowired
    public EventSubscriptionEntityServiceImpl(final EventBus eventBus,
                                              final ContextService contextService,
                                              final ActionManager actionManager,
                                              final EventServiceComponentManager componentManager,
                                              @Lazy final EventService eventService,
                                              final ObjectMapper mapper,
                                              final UserManagementServiceI userManagementService,
                                              final SubscriptionDeliveryEntityService subscriptionDeliveryEntityService,
                                              final EventSchedulingService eventSchedulingService) {
        this.eventBus = eventBus;
        this.contextService = contextService;
        this.actionManager = actionManager;
        this.componentManager = componentManager;
        this.eventService = eventService;
        this.mapper = mapper;
        this.userManagementService = userManagementService;
        this.subscriptionDeliveryEntityService = subscriptionDeliveryEntityService;
        this.eventSchedulingService = eventSchedulingService;
        log.debug("EventSubscriptionService started normally.");

        configureJsonPath();
    }

    private void configureJsonPath() {
        // Set the default JayWay JSONPath configuration
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return Sets.newHashSet(Option.SUPPRESS_EXCEPTIONS);
            }
        });
    }

    @Override
    @Transient
    public Subscription validate(Subscription subscription) throws SubscriptionValidationException {
        UserI actionUser = null;
        try {
            actionUser = userManagementService.getUser(subscription.subscriptionOwner());
        } catch (UserNotFoundException|UserInitException e) {
            log.error("Could not load Subscription Owner for userID: " + subscription.subscriptionOwner() != null ? subscription.subscriptionOwner() : "null" + "\n" + e.getMessage());
            throw new SubscriptionValidationException("Could not load Subscription Owner for userID: " + subscription.subscriptionOwner() != null ? subscription.subscriptionOwner() : "null" + "\n" + e.getMessage());
        }
        Class<?> clazz;
        String eventType = subscription.eventFilter().eventType();
        try {
            clazz = Class.forName(eventType);
            if (clazz == null || !EventServiceEvent.class.isAssignableFrom(clazz)) {
                String message = "Event class cannot be found based on Event-Id: " + eventType != null ? eventType : "unknown";
                log.error(message);
                throw new SubscriptionValidationException(message);
            }
        } catch (NoSuchBeanDefinitionException|ClassNotFoundException e) {
            log.error("Could not load Event class: " + (eventType != null ? eventType : "unknown")+ "\n" + e.getMessage());
            throw new SubscriptionValidationException("Could not load Event class: " + eventType != null ? eventType : "unknown");
        }
        String listenerErrorMessage = "";
        try {
            // Check that event class has a valid default or custom listener
            Class<?> listenerClazz = null;
            if(subscription.customListenerId() != null){
                try {
                    listenerClazz = Class.forName(subscription.customListenerId());
                } catch (ClassNotFoundException e) {
                    listenerErrorMessage = "Could not load custom listener class: " + subscription.customListenerId();
                    throw new SubscriptionValidationException(listenerErrorMessage);
                }
            } else if(EventServiceListener.class.isAssignableFrom(clazz)) {
                listenerClazz = clazz;
            } else {
                    listenerErrorMessage = "Event class is not a listener and no custom listener found.";
            }
            //if(listenerClazz == null || !EventServiceListener.class.isAssignableFrom(listenerClazz) ){
            //    listenerErrorMessage = "Could not find bean of type EventServiceListener from: " + listenerClazz != null ? listenerClazz.getName() : "unknown";
            //    throw new NoSuchBeanDefinitionException(listenerErrorMessage);
            //}
        } catch (NoSuchBeanDefinitionException e) {
            log.error(listenerErrorMessage + "\n" + e.getMessage());
            throw new SubscriptionValidationException(listenerErrorMessage + "\n" + e.getMessage());
        }
        // ** Validate event filter ** //
        if (subscription.eventFilter() == null) {
            log.error("Missing EventFilter on subscription ", subscription.name());
            throw new SubscriptionValidationException("Missing Event Filter");
        } else {
            try {
                log.debug("Validating event filter contents.\n" + subscription.eventFilter().toString());
                String payloadJsonPath = buildPayloadJsonPath(subscription, componentManager.getEvent(eventType));
                if(!Strings.isNullOrEmpty(payloadJsonPath) && JsonPath.compile(payloadJsonPath) == null){
                    log.error("Could not build JsonPath filter for payload: " + payloadJsonPath);
                    throw new SubscriptionValidationException("Could not build JsonPath filter for Reactor: " + payloadJsonPath);
                }
                if (!Strings.isNullOrEmpty(subscription.eventFilter().jsonPathFilter())) {
                    if(subscription.eventFilter().jsonPathFilter().startsWith("$") || subscription.eventFilter().jsonPathFilter().contains("$[?")){
                        log.error("Payload JsonPath filter contains $ or $[?. Filter string should include only predicate path." + "\n" + subscription.eventFilter().jsonPathFilter());
                        throw new SubscriptionValidationException("\"Payload JsonPath filter contains $ or $[?. Filter string should include only predicate path.\"");
                    }
                    String jsonPathFilter = "$[?(" + subscription.eventFilter().jsonPathFilter() + ")]";
                        if (JsonPath.compile(jsonPathFilter) == null) {
                            log.error("Could not compile jsonPath filter: " + jsonPathFilter);
                            throw new SubscriptionValidationException("Could not compile jsonPath filter: " + jsonPathFilter);
                        }
                }
            } catch (Throwable e) {
                log.error("Could not compile jsonPath filter." + e.getMessage());
                throw new SubscriptionValidationException("Could not compile jsonPath filter. " +  e.getMessage());
            }

            try{
                final String schedule = subscription.eventFilter().schedule();
                final String status   = subscription.eventFilter().status();

                if(StringUtils.hasLength(schedule) && AbstractEventServiceEvent.Status.SCHEDULED.name().equals(status)){
                    log.debug("Validating cron expression {} for subscription {}", schedule, subscription.id());
                    validateCronExpression(schedule);
                }
            }catch(Throwable t){
                log.error("Invalid Cron Expression. " + t.getMessage());
                throw new SubscriptionValidationException("Invalid Cron Expression. " +  t.getMessage());
            }
        }

        try {
            // Check that Action is valid and service is accessible
            EventServiceActionProvider provider = actionManager.getActionProviderByKey(subscription.actionKey());
            if (provider == null) {
                log.error("Could not load Action Provider for key:" + subscription.actionKey());
                throw new SubscriptionValidationException("Could not load Action Provider for key:" + subscription.actionKey());
            }
            if (!actionManager.validateAction(subscription.actionKey(), subscription.eventFilter().projectIds(), actionUser)) {
                log.error("Could not validate Action Provider Class " + (subscription.actionKey() != null ? subscription.actionKey() : "unknown") + "for user:" + actionUser.getLogin());
                throw new SubscriptionValidationException("Could not validate Action Provider Class " + subscription.actionKey() != null ? subscription.actionKey() : "unknown");
            }
        } catch (Exception e){
            log.error("Could not validate Action: {} \n {}", subscription.actionKey(), e.getMessage());
            throw new SubscriptionValidationException("Could not validate Action: " + subscription.actionKey() + "\n" + e.getMessage());
        }
        return subscription;
    }

    private void validateCronExpression(String cronTrigger) throws SubscriptionValidationException{
            if(!org.springframework.util.StringUtils.hasLength(cronTrigger)){
                throw new SubscriptionValidationException("Cron trigger must not be null or empty.");
            }

            final List<String> cronExpression = Arrays.asList(cronTrigger.split(" "));
            if(cronExpression.size() != 6){
                throw new SubscriptionValidationException("Invalid cron expression. Expected 6 fields but found: " + cronExpression.size());
            }

            final String secondField = cronExpression.get(0);
            if(secondField.contains("*") || secondField.contains("/") || secondField.contains("-") || secondField.contains(",")){
                throw new SubscriptionValidationException("Cron expression must not have a wildcard(*), range(-), step value(/), or list(,) in second field.");
            }

            final String minuteField = cronExpression.get(1);
            if(minuteField.contains("*") || minuteField.contains("/") || minuteField.contains("-") || minuteField.contains(",")){
                throw new SubscriptionValidationException("Cron expression must not have a wildcard(*), range(-), step value(/), or list(,) in the minute field.");
            }
    }

    @Override
    public JsonPath compileJsonPathFilter(String jsonPathPredicate) throws InvalidPathException {
        return JsonPath.compile("$[?(" + jsonPathPredicate + ")]");
    }

    @Override
    public Subscription activate(Subscription subscription) {
        try {
            if(getActiveRegistrationSubscriptionIds().contains(subscription.id())){
                log.debug("Deactivating active subscription before reactivating.");
                deactivate(subscription);
            }
            String eventType = subscription.eventFilter().eventType();
            Class<?> eventClazz = Class.forName(eventType);
            EventServiceEvent event = componentManager.getEvent(eventType);

            EventServiceListener listener = null;
            // Is a custom listener defined and valid
            if(!Strings.isNullOrEmpty(subscription.customListenerId())){
                listener = componentManager.getListener(subscription.customListenerId());
            }
            if(listener == null && EventServiceListener.class.isAssignableFrom(eventClazz)) {
            // Is event class a combined event/listener
                listener = componentManager.getListener(eventType);
            }
            if(listener == null){
                // Default to the DefaultEventServiceListener
                listener = componentManager.getListener("DefaultEventServiceListener");
            }
            if(listener != null) {
                EventServiceListener uniqueListener = listener.getInstance();
                uniqueListener.setEventService(eventService);
                Predicate predicate = new SubscriptionPredicate(subscription);
                Selector predicateSelector = Selectors.predicate(predicate);
                subscription = addActiveRegistration(predicateSelector, subscription, uniqueListener);

            } else {
                log.error("Could not activate subscription:" + Long.toString(subscription.id()) + ". No appropriate listener found.");
                throw new SubscriptionValidationException("Could not activate subscription. No appropriate listener found.");
            }
            update(subscription);
            log.debug("Updated subscription: " + subscription.name() + " with registration key.");

        }
        catch (Throwable e) {
            log.error("Event subscription failed for " + subscription.toString());
            log.error(e.getMessage());
        }
        return subscription;
    }

    private String buildPayloadJsonPath(Subscription subscription, @Nonnull EventServiceEvent event){
        String payloadJsonPath = null;
        if(event.filterablePayload() && !Strings.isNullOrEmpty(subscription.eventFilter().jsonPathFilter())){
            log.debug("Creating payload filter for Reactor Selector:");
            payloadJsonPath = "$[?(" + subscription.eventFilter().jsonPathFilter() + ")]";
            log.debug("Final JSONPath Payload Filter : " + payloadJsonPath);
        }
        return payloadJsonPath;

    }

    @Override
    public Subscription deactivate(@Nonnull Subscription subscription) throws NotFoundException, EntityNotFoundException{
        Subscription deactivatedSubscription = null;
        try {
            if(subscription.id() == null) {
                throw new NotFoundException("Failed to deactivate subscription - Missing subscription ID");
            }
            log.debug("Deactivating subscription:" + Long.toString(subscription.id()));
            removeActiveRegistration(subscription.id());
            SubscriptionEntity entity = fromPojoWithTemplate(subscription);
            if(entity != null && entity.getId() != 0) {
                entity.setActive(false);
                deactivatedSubscription = toPojo(entity);
                update(entity);
                log.debug("Deactivated subscription:" + Long.toString(subscription.id()));
            }
            else {
                log.debug("Failed to deactivate subscription - no entity found for id:" + Long.toString(subscription.id()));
                throw new EntityNotFoundException("Could not retrieve EventSubscriptionEntity from id: " + subscription.id());
            }
        } catch(Throwable e){
            log.error("Failed to deactivate subscription.\n" + e.getMessage());

        }
        return deactivatedSubscription;
    }

    @Override
    public Subscription save(@Nonnull Subscription subscription) {
        Subscription saved = toPojo(create(fromPojo(subscription)));
        log.debug("Saved subscription with ID:" + Long.toString(saved.id()));
        return saved;
    }

    @Override
    public void delete(@Nonnull Long subscriptionId) throws NotFoundException {
        if(subscriptionId != null) {
            Subscription subscription = getSubscription(subscriptionId);
            deactivate(subscription);
            SubscriptionEntity entity = retrieve(subscriptionId);
            delete(entity);
            log.debug("Deleted subscription:" + Long.toString(subscription.id()));
        } else {
            log.error("Failed to delete subscription. Invalid or missing subscription ID.");
            throw new NotFoundException("Failed to delete subscription. Invalid or missing subscription ID");
        }
    }

    @Override
    public Subscription createSubscription(Subscription subscription) throws SubscriptionValidationException {
        log.debug("Validating subscription: " + subscription.name());
        subscription = validate(subscription);
        try {
            if(Strings.isNullOrEmpty(subscription.name())){
                String generatedName = autoGenerateUniqueSubscriptionName(subscription);
                if(!Strings.isNullOrEmpty(generatedName)) {
                    subscription = subscription.toBuilder().name(generatedName).build();
                }
            }
            log.debug("Saving subscription: " + subscription.name());
            subscription = save(subscription);
            if (subscription.active()) {
                subscription = activate(subscription);
                log.debug("Activated subscription: " + subscription.name());
            } else {
                log.debug("Subscription set to not active. Skipping activation.");
            }
        }catch (Exception e){
            log.error("Failed to save, activate & update new subscription: " + subscription.name());
            log.error(e.getMessage());
            return null;
        }
        return subscription;
    }

    @Override
    public Subscription update(final Subscription subscription) throws NotFoundException {
        SubscriptionEntity subscriptionEntity = SubscriptionEntity.fromPojoWithTemplate(subscription, retrieve(subscription.id()));
        super.update(subscriptionEntity);
        return toPojo(subscriptionEntity);
    }

    @Override
    public List<Subscription> getSubscriptions(String projectId) {
        List<Subscription> subscriptions = new ArrayList<>();
        super.getAll().stream()
             .filter(se -> se.getEventServiceFilterEntity() == null ||
                     se.getEventServiceFilterEntity().getProjectIds() == null ||
                     se.getEventServiceFilterEntity().getProjectIds().isEmpty() ||
                     se.getEventServiceFilterEntity().getProjectIds().contains(projectId))
             .forEach(se -> {
                 try {
                     subscriptions.add(getSubscription(se.getId()));
                 } catch (NotFoundException e) {
                     log.error("Could not find subscription for ID: " + Long.toString(se.getId()) + "\n" + e.getMessage());

                 }
             });
        return subscriptions;
    }

    @Override
    public List<Subscription> getAllSubscriptions() {
        List<Subscription> subscriptions = new ArrayList<>();
        for (SubscriptionEntity se : super.getAll()) {
            try {
                subscriptions.add(getSubscription(se.getId()));
            } catch (NotFoundException e) {
                log.error("Could not find subscription for ID: " + Long.toString(se.getId()) + "\n" + e.getMessage());
            }
        }
        return subscriptions;
    }

    @Override
    public Subscription getSubscription(Long id) throws NotFoundException {
        Subscription subscription = toPojo(super.get(id));
        try {
            subscription = validate(subscription).toBuilder().valid(true).validationMessage(null).build();
        } catch (SubscriptionValidationException e) {
            subscription = subscription.toBuilder().valid(false).validationMessage(e.getMessage()).build();
        }
        return subscription;
    }


    // Generate descriptive subscription name - unique to project combination
    private String autoGenerateUniqueSubscriptionName(Subscription subscription){
        String uniqueName = Strings.isNullOrEmpty(subscription.name()) ? "" : subscription.name();
        try {
            UserI actionUser = userManagementService.getUser(subscription.subscriptionOwner());
            String actionName = actionManager.getActionByKey(subscription.actionKey(), actionUser) != null ?
                    actionManager.getActionByKey(subscription.actionKey(), actionUser).displayName() : "Action";
            EventFilter eventFilter = subscription.eventFilter();
            String eventName = componentManager.getEvent(eventFilter.eventType()).getDisplayName();
            String status = eventFilter.status();
            String forProject = eventFilter.projectIds() == null || eventFilter.projectIds().isEmpty() ? "Site" :
                    eventFilter.projectIds().size() == 1 ? eventFilter.projectIds().get(0) : "Multiple Projects";
            uniqueName += Strings.isNullOrEmpty(actionName) ? "Action" : actionName;
            uniqueName += " on ";
            uniqueName += Strings.isNullOrEmpty(eventName) ? "Event" : eventName;
            uniqueName += status != null ? (" " + status) : "";
            uniqueName += " for ";
            uniqueName += forProject;

            String trialUniqueName = uniqueName;
            for(Integer indx = 2; indx < 100000; indx++) {
                if(this.getDao().findByName(trialUniqueName) == null){
                    return trialUniqueName;
                }else {
                    trialUniqueName = uniqueName + " v" + indx.toString();
                }
            }

        } catch (Throwable e){
            log.error("Exception attempting to auto generate subscription name.", e.getMessage(), e);
        }
        return uniqueName;
    }

    private SubscriptionEntity fromPojo(final Subscription eventSubscription) {
        return eventSubscription == null ? null : SubscriptionEntity.fromPojo(eventSubscription);
    }

    private SubscriptionEntity fromPojoWithTemplate(final Subscription eventSubscription) {
        if (eventSubscription == null) {
            return null;
        }
        return SubscriptionEntity.fromPojoWithTemplate(eventSubscription, retrieve(eventSubscription.id()));
    }

    private Registration loadReactorRegistration(@Nonnull Integer registrationHash){
        Registry<Object, Consumer<? extends Event<?>>> consumerRegistry = eventBus.getConsumerRegistry();
        Iterator<Registration<Object, ? extends Consumer<? extends Event<?>>>> registrationIterator = consumerRegistry.iterator();
        while(registrationIterator.hasNext()){
            Registration registration = registrationIterator.next();
            if(registration.hashCode() == registrationHash){
                return registration;
            }
        }
        return null;
    }

    @Transient
    @Override
    public Subscription toPojo(SubscriptionEntity entity) {
        return Subscription.builder()
                           .id(entity.getId())
                           .name(entity.getName())
                           .active(entity.getActive())
                           .customListenerId(entity.getCustomListenerId())
                           .actionKey(entity.getActionKey())
                           .attributes(entity.getAttributes())
                           .eventFilter(entity.getEventServiceFilterEntity() != null ?
                                   entity.getEventServiceFilterEntity().toPojo() : null)
                           .actAsEventUser(entity.getActAsEventUser())
                           .subscriptionOwner(entity.getSubscriptionOwner())
                           .created(entity.getCreated())
                           .build();
    }

    @Nonnull
    @Transient
    @Override
    public List<Subscription> toPojo(final List<SubscriptionEntity> subscriptionEntities) {
        List<Subscription> subscriptions = new ArrayList<>();
        if(subscriptionEntities!= null) {
            for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {
                subscriptions.add(this.toPojo(subscriptionEntity));
            }
        }
        return subscriptions;
    }


    class SubscriptionPredicate implements Predicate<Object> {
        Subscription subscription;
        private Configuration subscriptionConf = Configuration.defaultConfiguration()
                    .addOptions(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS);
        public SubscriptionPredicate(Subscription subscription) {
            this.subscription = subscription;
        }

        @Override
        public boolean test(Object object) {
            if(object == null || !(object instanceof EventServiceEvent)){
                return false;
            }
            try{
                EventServiceEvent event = (EventServiceEvent) object;
                EventFilter filter = subscription.eventFilter();
                if(filter == null) {
                    log.error("Subscription: " + subscription.name() +" Event Filter object is null - event cannot be detected without a filter.");
                    return false;
                }
                // Check for exclusion based on event type
                if (Strings.isNullOrEmpty(filter.eventType()) || !filter.eventType().contentEquals(event.getType())){
                    return false;
                }
                // Check for exclusion based on status
                if(!Strings.isNullOrEmpty(filter.status()) &&
                        !filter.status().contentEquals(event.getCurrentStatus() != null ?  event.getCurrentStatus().name() : "")) {
                    return false;
                }
                // Check for exclusion based on project id
                if (filter.projectIds() != null && !filter.projectIds().isEmpty() &&
                        filter.projectIds().stream().noneMatch(pid -> pid.contentEquals(event.getProjectId() != null ? event.getProjectId() : ""))){
                    return false;
                }

                if(AbstractEventServiceEvent.Status.SCHEDULED.name().equals(filter.status()) &&
                        !java.util.Objects.equals(subscription.id(), event.getSubscriptionId())){
                    return false;
                }

                // Check for exclusion based on payload filter (if available)
                if(!Strings.isNullOrEmpty(filter.jsonPathFilter()) && event.filterablePayload()) {
                    try {
                        Object payloadSignatureObject = event.getPayloadSignatureObject();
                        if (payloadSignatureObject != null) {
                            String jsonFilter = "$[?(" + subscription.eventFilter().jsonPathFilter() + ")]";
                            List<String> filterResult =
                                    JsonPath.using(subscriptionConf)
                                            .parse(
                                                    mapper.writeValueAsString(payloadSignatureObject))
                                            .read(jsonFilter);
                            if (filterResult.isEmpty()) {
                                return false;
                            }
                        }
                    } catch (JsonProcessingException e){
                        log.error("Exception attempting to filter EventService event on serialized payload.");
                        return false;
                    }
                }
            } catch (Throwable e){
                log.error("Exception thrown attempting to test Reactor key in SubscriptionPredicate.test \n" + e.getMessage());
                return false;
            }
            return true;
        }
    }


    @Transient
    @Override
    public List<Subscription> getSubscriptionsByListenerId(UUID listenerId) throws NotFoundException {
        List<Long> subscriptionIds = activeRegistrations.entrySet().stream()
                                         .filter(ar -> ar.getValue().listenerId == listenerId)
                                         .map(Map.Entry::getKey)
                                         .collect(Collectors.toList());

        List<Subscription> subscriptions = new ArrayList<>();
        for(Long sid : subscriptionIds) {
            SubscriptionEntity entity = getDao().findById(sid);
            if (entity != null) { subscriptions.add(toPojo(entity)); }
        }
        return subscriptions;
    }

    @Transient
    @Override
    public Set<Long> getActiveRegistrationSubscriptionIds() {
        return activeRegistrations.keySet();
    }

    @Override
    public Integer getActiveRegistrationCriteriaHash(Long subscriptionId) {
        return activeRegistrations.containsKey(subscriptionId) ?
                activeRegistrations.get(subscriptionId).reactorCriteriaHash : null;
    }

    @Transient
    @Override
    public UUID getListenerId(Long subscriptionId) {
        return activeRegistrations.containsKey(subscriptionId) ?
                activeRegistrations.get(subscriptionId).listenerId : null;
    }

    @Transient
    private Subscription addActiveRegistration(Selector selector, Subscription subscription, EventServiceListener listener) {
        Registration registration = eventBus.on(selector, listener);
        activeRegistrations.put(subscription.id(), new ActiveRegistration(registration, listener.getInstanceId(), subscription));
        log.debug("Activated Reactor Registration: "
                + registration.hashCode()
                + "  RegistrationKey: "
                + (listener.getInstanceId() == null ? "" : listener.getInstanceId().toString()));
        log.debug("Selector:\n" + ((SubscriptionPredicate)selector.getObject()).subscription.toString());
        return subscription.toBuilder()
                                   .active(true)
                                   .build();
    }

    @Transient
    @Override
    public void removeActiveRegistration(Long subscriptionId){
        if(activeRegistrations.containsKey(subscriptionId)){
            Registration reactorRegistration = activeRegistrations.get(subscriptionId).getRegistration();
            if (reactorRegistration != null ) { reactorRegistration.cancel(); }
            activeRegistrations.remove(subscriptionId);
        }

    }



    private class ActiveRegistration {
        final Registration registration;
        final UUID listenerId;
        final Integer registrationHash;
        final Integer reactorCriteriaHash;


        public Registration getRegistration() { return registration; }

        public ActiveRegistration(@Nonnull Registration registration, @Nonnull UUID listenerId, @Nonnull Subscription subscription) {
            this.registration = registration;
            this.listenerId = listenerId;
            this.registrationHash = registration.hashCode();
            this.reactorCriteriaHash = subscription.eventFilter().getReactorCriteriaHash();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ActiveRegistration)) return false;
            ActiveRegistration that = (ActiveRegistration) o;
            return Objects.equal(listenerId, that.listenerId) &&
                    Objects.equal(registrationHash, that.registrationHash);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(listenerId, registrationHash);
        }
    }

}
