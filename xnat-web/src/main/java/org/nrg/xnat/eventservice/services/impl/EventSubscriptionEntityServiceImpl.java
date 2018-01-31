package org.nrg.xnat.eventservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.apache.commons.lang.StringUtils;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.daos.EventSubscriptionEntityDao;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.EventFilter;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.model.xnat.XnatModelObject;
import org.nrg.xnat.eventservice.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.bus.selector.Selector;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.*;
import static reactor.bus.selector.Selectors.R;

@Service
@Transactional
public class EventSubscriptionEntityServiceImpl
        extends AbstractHibernateEntityService<SubscriptionEntity, EventSubscriptionEntityDao>
        implements EventSubscriptionEntityService {

    private static final Logger log = LoggerFactory.getLogger(EventSubscriptionEntityService.class);
    private EventBus eventBus;
    private ContextService contextService;
    private ActionManager actionManager;
    private EventServiceComponentManager componentManager;
    private EventService eventService;
    private ObjectMapper mapper;
    private UserManagementServiceI userManagementService;
    private SubscriptionDeliveryEntityService subscriptionDeliveryEntityService;


    @Autowired
    public EventSubscriptionEntityServiceImpl(final EventBus eventBus,
                                              final ContextService contextService,
                                              final ActionManager actionManager,
                                              final EventServiceComponentManager componentManager,
                                              @Lazy final EventService eventService,
                                              final ObjectMapper mapper,
                                              final UserManagementServiceI userManagementService,
                                              final SubscriptionDeliveryEntityService subscriptionDeliveryEntityService) {
        this.eventBus = eventBus;
        this.contextService = contextService;
        this.actionManager = actionManager;
        this.componentManager = componentManager;
        this.eventService = eventService;
        this.mapper = mapper;
        this.userManagementService = userManagementService;
        this.subscriptionDeliveryEntityService = subscriptionDeliveryEntityService;
        log.debug("EventSubscriptionService started normally.");
    }


    @Override
    public Subscription validate(Subscription subscription) throws SubscriptionValidationException {
        UserI actionUser = null;
        try {
            actionUser = userManagementService.getUser(subscription.subscriptionOwner());
        } catch (UserNotFoundException|UserInitException e) {
            log.error("Could not load Subscription Owner for userID: " + subscription.subscriptionOwner() != null ? subscription.subscriptionOwner() : "null" + "\n" + e.getMessage());
            throw new SubscriptionValidationException("Could not load Subscription Owner for userID: " + subscription.subscriptionOwner() != null ? subscription.subscriptionOwner() : "null" + "\n" + e.getMessage());
        }
        Class<?> clazz;
        try {
            clazz = Class.forName(subscription.eventId());
            if (clazz == null || !EventServiceEvent.class.isAssignableFrom(clazz)) {
                String message = "Event class cannot be found based on Event-Id: " + subscription.eventId() != null ? subscription.eventId() : "unknown";
                log.error(message);
                throw new SubscriptionValidationException(message);
            }
        } catch (NoSuchBeanDefinitionException|ClassNotFoundException e) {
            log.error("Could not load Event class: " + subscription.eventId() != null ? subscription.eventId() : "unknown" + "\n" + e.getMessage());
            throw new SubscriptionValidationException("Could not load Event class: " + subscription.eventId() != null ? subscription.eventId() : "unknown");
        }
        String listenerErrorMessage = "";
        try {
            // Check that event class has a valid default or custom listener
            Class<?> listenerClazz = null;
            if(EventServiceListener.class.isAssignableFrom(clazz)) {
                listenerClazz = clazz;
            } else if(subscription.customListenerId() == null){
                listenerErrorMessage = "Event class is not a listener and no custom listener found.";
            } else {
                try {
                    listenerClazz = Class.forName(subscription.customListenerId());
                } catch (ClassNotFoundException e) {
                    listenerErrorMessage = "Could not load custom listerner class: " + subscription.customListenerId();
                    throw new SubscriptionValidationException(listenerErrorMessage);
                }
            }
            if(listenerClazz == null || !EventServiceListener.class.isAssignableFrom(listenerClazz) || contextService.getBean(listenerClazz) == null){
                listenerErrorMessage = "Could not find bean of type EventServiceListener from: " + listenerClazz != null ? listenerClazz.getName() : "unknown";
                throw new NoSuchBeanDefinitionException(listenerErrorMessage);
            }
        } catch (NoSuchBeanDefinitionException e) {
            log.error(listenerErrorMessage + "\n" + e.getMessage());
            throw new SubscriptionValidationException(listenerErrorMessage + "\n" + e.getMessage());
        }
        try {
            // Check that Action is valid and service is accessible
            EventServiceActionProvider provider = actionManager.getActionProviderByKey(subscription.actionKey());
            if (provider == null) {
                log.error("Could not load Action Provider for key:" + subscription.actionKey());
                throw new SubscriptionValidationException("Could not load Action Provider for key:" + subscription.actionKey());
            }
            if (!actionManager.validateAction(subscription.actionKey(), subscription.projectId(), null, actionUser)) {
                log.error("Could not validate Action Provider Class " + (subscription.actionKey() != null ? subscription.actionKey() : "unknown") + "for user:" + actionUser.getLogin());
                throw new SubscriptionValidationException("Could not validate Action Provider Class " + subscription.actionKey() != null ? subscription.actionKey() : "unknown");
            }
        } catch (Exception e){
            log.error("Could not validate Action: {} \n {}", subscription.actionKey(), e.getMessage());
            throw new SubscriptionValidationException("Could not validate Action: " + subscription.actionKey() + "\n" + e.getMessage());
        }
        return subscription;
    }

    @Override
    public Subscription activate(Subscription subscription) {
        try {
            Class<?> eventClazz = Class.forName(subscription.eventId());
            EventServiceListener listener = null;
            // Is a custom listener defined and valid
            if(!Strings.isNullOrEmpty(subscription.customListenerId())){
                listener = componentManager.getListener(subscription.customListenerId());
            }
            if(listener == null && EventServiceListener.class.isAssignableFrom(eventClazz)) {
            // Is event class a combined event/listener
                listener = componentManager.getListener(subscription.eventId());
            }
            if(listener != null) {
                EventServiceListener uniqueListener = listener.getInstance();
                uniqueListener.setEventService(eventService);
                String eventFilterRegexMatcher;
                if(subscription.eventFilter() == null){
                    eventFilterRegexMatcher = EventFilter.builder().build().toRegexMatcher(eventClazz.getName(), subscription.projectId());
                } else {
                    eventFilterRegexMatcher = subscription.eventFilter().toRegexMatcher(eventClazz.getName(), subscription.projectId());
                }
                Selector selector = R(eventFilterRegexMatcher);
                log.debug("Building Reactor RegEx Selector on matcher: " + eventFilterRegexMatcher);
                Registration registration = eventBus.on(selector, uniqueListener);
                log.debug("Activated Reactor Registration: " + registration.hashCode() + "  RegistrationKey: " + (uniqueListener.getInstanceId() == null ? "" : uniqueListener.getInstanceId().toString()));
                subscription = subscription.toBuilder()
                                           .listenerRegistrationKey(uniqueListener.getInstanceId() == null ? "" : uniqueListener.getInstanceId().toString())
                                           .active(true)
                                           .build();
            } else {
                log.error("Could not activate subscription:" + Long.toString(subscription.id()) + ". No appropriate listener found.");
                throw new SubscriptionValidationException("Could not activate subscription. No appropriate listener found.");
            }
            update(subscription);
            log.debug("Updated subscription: " + subscription.name() + " with registration key: " + subscription.listenerRegistrationKey());

        }
        catch (SubscriptionValidationException | ClassNotFoundException | NotFoundException e) {
            log.error("Event subscription failed for " + subscription.toString());
            log.error(e.getMessage());
        }
        return subscription;
    }



    @Transactional
    @Override
    public Subscription deactivate(@Nonnull Subscription subscription) throws NotFoundException, EntityNotFoundException{
        Subscription deactivatedSubscription = null;
        try {
            if(subscription.id() == null) {
                throw new NotFoundException("Failed to deactivate subscription - Missing subscription ID");
            }
            log.debug("Deactivating subscription:" + Long.toString(subscription.id()));
            SubscriptionEntity entity = fromPojoWithTemplate(subscription);
            if(entity != null && entity.getId() != 0) {
                entity.setActive(false);
                entity.setListenerRegistrationKey(null);
                deactivatedSubscription = entity.toPojo();
                update(entity);
                log.debug("Deactivated subscription:" + Long.toString(subscription.id()));
            }
            else {
                log.error("Failed to deactivate subscription - no entity found for id:" + Long.toString(subscription.id()));
                throw new EntityNotFoundException("Could not retrieve EventSubscriptionEntity from id: " + subscription.id());
            }
        } catch(NotFoundException|EntityNotFoundException e){
            log.error("Failed to deactivate subscription.\n" + e.getMessage());

        }
        return deactivatedSubscription;
    }

    @Transactional
    @Override
    public Subscription save(@Nonnull Subscription subscription) {
        Subscription saved = toPojo(create(fromPojo(subscription)));
        log.debug("Saved subscription with ID:" + Long.toString(saved.id()));
        return saved;
    }

    @Override
    public void throwExceptionIfExists(Subscription subscription) throws NrgServiceRuntimeException {
        String name = subscription.name();
        SubscriptionEntity existing = null;
        try {
            existing = this.getDao().findByName(name);
        } catch (Exception e) {
            throw new NrgServiceRuntimeException("Could not check database for duplication subscription name.");
        }
        if (existing != null) {
            throw new NrgServiceRuntimeException("Subscription with the name :" + name + " exists.");
        }
    }

    @Transactional
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
    public Subscription update(Subscription subscription) throws NotFoundException {
        SubscriptionEntity subscriptionEntity = retrieve(subscription.id());
        subscriptionEntity.update(subscription);
        super.update(subscriptionEntity);
        return toPojo(subscriptionEntity);
    }

    @Override
    public List<Subscription> getAllSubscriptions() {
        List<Subscription> subscriptions = new ArrayList<>();
        //Registry<Object, Consumer<? extends Event<?>>> consumerRegistry = eventBus.getConsumerRegistry();
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
    public List<Subscription> getSubscriptionsByKey(String key) throws NotFoundException {
        List<SubscriptionEntity> subscriptionEntities = getDao().findByKey(key);
        return SubscriptionEntity.toPojo(getDao().findByKey(key));
    }

    @Override
    public Subscription getSubscription(Long id) throws NotFoundException {
        Subscription subscription = super.get(id).toPojo();
        try {
            subscription = validate(subscription).toBuilder().valid(true).validationMessage(null).build();
        } catch (SubscriptionValidationException e) {
            subscription = subscription.toBuilder().valid(false).validationMessage(e.getMessage()).build();
        }
        return subscription;
    }

    @Override
    public void processEvent(EventServiceListener listener, Event event) throws NotFoundException {
        String jsonObject = null;
        if(event.getData() instanceof EventServiceEvent) {
            EventServiceEvent esEvent = (EventServiceEvent) event.getData();
            for (Subscription subscription : getSubscriptionsByKey(listener.getInstanceId().toString())) {
                log.debug("RegKey matched for " + subscription.listenerRegistrationKey() + "  " + subscription.name());
                // Create subscription delivery entry
                Long deliveryId = subscriptionDeliveryEntityService.create(fromPojoWithTemplate(subscription),
                        esEvent,
                        listener,
                        subscription.actAsEventUser() ? esEvent.getUser() : subscription.subscriptionOwner(),
                        subscription.projectId() == null ? "" : subscription.projectId(),
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

                    try {
                        XnatModelObject modelObject = componentManager.getModelObject(esEvent.getObject(), actionUser);
                        if (modelObject != null && mapper.canSerialize(modelObject.getClass())) {
                            // Serialize data object
                            log.debug("Serializing event object as known Model Object.");
                            jsonObject = mapper.writeValueAsString(modelObject);
                        } else if (esEvent.getObject() != null && mapper.canSerialize(esEvent.getObject().getClass())) {
                            log.debug("Serializing event object as unknown object type.");
                            jsonObject = mapper.writeValueAsString(esEvent.getObject());
                        } else {
                            log.error("Could not serialize event object in: " + esEvent.toString());
                            return;
                        }
                        String objectSubString = StringUtils.substring(jsonObject, 0, 60);
                        log.debug("Serialized Object: " + objectSubString + "...");
                        subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_SERIALIZED, new Date(), "Object Serialized: " + objectSubString + "...");
                    }catch(JsonProcessingException e){
                        log.error("Aborting Event Service processEvent. Exception serializing event object: " + esEvent.getObjectClass());
                        log.error(e.getMessage());
                        return;
                    }
                    //Filter on data object (if filter exists)
                    if( subscription.eventFilter() != null && subscription.eventFilter().jsonPathFilter() != null ) {
                        String jsonFilter = subscription.eventFilter().jsonPathFilter();
                        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);
                        List<String> filterResult =  JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
                        String objectSubString = StringUtils.substring(jsonObject, 0, 60);
                        if(filterResult.isEmpty()){
                            log.debug("Aborting event pipeline - Serialized event:\n" + objectSubString + "..." + "\ndidn't match JSONPath Filter:\n" + jsonFilter);
                            subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_FILTER_MISMATCH_HALT, new Date(), "Event objected failed filter test.");
                            return;
                        } else {
                            log.debug("JSONPath Filter Match - Serialized event:\n" + objectSubString + "..." + "\nJSONPath Filter:\n" + jsonFilter);
                            subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_FILTERED, new Date(), "Event objected passed filter test.");
                        }

                    }
                    // call Action Manager with payload
                    SubscriptionEntity subscriptionEntity = super.get(subscription.id());

                    actionManager.processEvent(subscriptionEntity, esEvent, actionUser, deliveryId);
                } catch (UserNotFoundException|UserInitException e) {
                    log.error("Failed to process subscription:" + subscription.name());
                    log.error(e.getMessage());
                    e.printStackTrace();
                    if(deliveryId != null){
                        subscriptionDeliveryEntityService.addStatus(deliveryId, FAILED, new Date(), e.getMessage());
                    }
                }
            }
        }
    }





    private Subscription toPojo(final SubscriptionEntity eventSubscriptionEntity) {
        return eventSubscriptionEntity == null ? null : eventSubscriptionEntity.toPojo();
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



}
