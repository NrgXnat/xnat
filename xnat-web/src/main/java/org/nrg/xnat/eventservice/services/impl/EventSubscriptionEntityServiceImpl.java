package org.nrg.xnat.eventservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.jayway.jsonpath.JsonPath;
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
import org.nrg.xnat.eventservice.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.bus.selector.Selector;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

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
        if(subscription.eventFilter() != null && !Strings.isNullOrEmpty(subscription.eventFilter().jsonPathFilter())) {
            String jsonPathFilter = subscription.eventFilter().jsonPathFilter();
            try {
                JsonPath compile = JsonPath.compile(jsonPathFilter);
                if(compile == null) {
                    log.error("Could not compile jsonPath filter: " + jsonPathFilter);
                    throw new SubscriptionValidationException("Could not compile jsonPath filter: " + jsonPathFilter);
                }
                } catch (Throwable e) {
                log.error("Could not compile jsonPath filter: " + jsonPathFilter, e.getMessage());
                throw new SubscriptionValidationException("Could not compile jsonPath filter: " + jsonPathFilter + e.getMessage());
            }
        }
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
            } else {
                if (subscription.customListenerId() == null) {
                    listenerErrorMessage = "Event class is not a listener and no custom listener found.";
                } else {
                    try {
                        listenerClazz = Class.forName(subscription.customListenerId());
                    } catch (ClassNotFoundException e) {
                        listenerErrorMessage = "Could not load custom listerner class: " + subscription.customListenerId();
                        throw new SubscriptionValidationException(listenerErrorMessage);
                    }
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

    @Override
    public Subscription save(@Nonnull Subscription subscription) {
        Subscription saved = toPojo(create(fromPojo(subscription)));
        log.debug("Saved subscription with ID:" + Long.toString(saved.id()));
        return saved;
    }

    @Override
    public void throwExceptionIfNameExists(Subscription subscription) throws NrgServiceRuntimeException {
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
    public Subscription update(Subscription subscription) throws NotFoundException, SubscriptionValidationException{
        SubscriptionEntity subscriptionEntity = retrieve(subscription.id());
        if(subscription.name() != null && !subscription.name().equals(subscriptionEntity.getName())){
            throwExceptionIfNameExists(subscription);
        }
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

    // Generate descriptive subscription name
    private String autoGenerateUniqueSubscriptionName(Subscription subscription){
        String uniqueName = subscription.name();
        try {
            UserI actionUser = userManagementService.getUser(subscription.subscriptionOwner());
            String actionName = actionManager.getActionByKey(subscription.actionKey(), actionUser) != null ?
                    actionManager.getActionByKey(subscription.actionKey(), actionUser).displayName() : "Action";
            String eventName = componentManager.getEvent(subscription.eventId()).getDisplayName();
            String forProject = Strings.isNullOrEmpty(subscription.projectId()) ? "Site" : subscription.projectId();
            uniqueName += Strings.isNullOrEmpty(actionName) ? "Action" : actionName;
            uniqueName += " on ";
            uniqueName += Strings.isNullOrEmpty(eventName) ? "Event" : eventName;
            uniqueName += " for ";
            uniqueName += forProject;

            String trialUniqueName = uniqueName;
            for(Integer indx = 2; indx < 10000; indx++) {
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
