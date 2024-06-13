/*
 * notify: org.nrg.notify.services.impl.hibernate.HibernateNotificationService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.services.impl.hibernate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.api.SubscriberType;
import org.nrg.notify.daos.NotificationDAO;
import org.nrg.notify.entities.Category;
import org.nrg.notify.entities.Channel;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.entities.Notification;
import org.nrg.notify.entities.Subscriber;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.exceptions.DuplicateDefinitionException;
import org.nrg.notify.exceptions.NoMatchingCategoryException;
import org.nrg.notify.exceptions.NoMatchingDefinitionException;
import org.nrg.notify.exceptions.NrgNotificationException;
import org.nrg.notify.services.CategoryService;
import org.nrg.notify.services.ChannelRendererService;
import org.nrg.notify.services.ChannelService;
import org.nrg.notify.services.DefinitionService;
import org.nrg.notify.services.NotificationDispatcherService;
import org.nrg.notify.services.NotificationService;
import org.nrg.notify.services.SubscriberService;
import org.nrg.notify.services.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernateNotificationService extends AbstractHibernateEntityService<Notification, NotificationDAO> implements NotificationService {

    /**
     * The ultimate convenience method. This creates a new {@link Notification notification}, setting it to the given
     * definition and dispatching it to all subscribers with the given parameters.
     * @param definition The notification definition from which the notification should be created.
     * @param parameters Any parameters for this particular notification.
     * @return The newly created and dispatched notification.
     * @see NotificationService#createNotification(Definition, String)
     * @see NotificationService#createNotification(Definition, Map)
     */
    @Override
    @Transactional
    public Notification createNotification(Definition definition, String parameters) {
        Notification notification = newEntity();
        notification.setDefinition(definition);
        notification.setParameters(parameters);
        create(notification);

        try {
            _dispatcherService.dispatch(notification);
        } catch (NrgNotificationException exception) {
            // TODO: There should be some way of indicating, upon method return, that there was an error during dispatch.
            _log.error("An error occurred while trying to dispatch a notification: " + notification, exception);
        }

        return notification;
    }

    /**
     * The ultimate convenience method. This creates a new {@link Notification notification}, setting it to the given
     * definition and dispatching it to all subscribers with the given parameters.
     * @param definition The notification definition from which the notification should be created.
     * @param parameters Any parameters for this particular notification. These are transformed through JSON to a string.
     * @return The newly created and dispatched notification.
     * @throws IOException Thrown when there's a problem converting the parameters to a string.
     * @see NotificationService#createNotification(Definition, Map)
     * @see NotificationService#createNotification(Definition, String)
     */
    @Override
    @Transactional
    public Notification createNotification(Definition definition, Map<String, Object> parameters) throws IOException {
        return createNotification(definition, MAPPER.writeValueAsString(parameters));
    }

    /**
     * This creates a new {@link CategoryScope system-scoped} {@link Notification notification}, setting 
     * it to the given event and dispatching it to all subscribers with the given parameters. Basically,
     * this uses the {@link Definition} with the given event and scope of {@link CategoryScope#Site}. There
     * is no {@link Definition#getEntity() entity property} required, since system-scoped events by definition
     * are not associated with a particular entity.
     * <b>Note:</b> This will create the required {@link Category} and {@link Definition} for the notification
     * if required.
     * @param event In conjunction with the scope {@link CategoryScope#Site}, specifies the {@link Definition} to use.
     * @param parameters Any parameters for this particular notification.
     * @return The newly created and dispatched notification.
     * @throws NoMatchingCategoryException Thrown when the category specified can't be found.
     * @throws NoMatchingDefinitionException Thrown when the definition specified can't be found.
     * @see NotificationService#createNotification(String, Map)
     */
    @Override
    @Transactional
    public Notification createNotification(String event, String parameters) throws NoMatchingCategoryException, NoMatchingDefinitionException {
        // First get the category.
        Category category = _categoryService.getCategoryByScopeAndEvent(CategoryScope.Site, event);
        if (category == null) {
            throw new NoMatchingCategoryException("Didn't find a category for site-scoped event: " + event);
        }

        // Now get the definition.
        List<Definition> definitions = _definitionService.getDefinitionsForCategory(category);
        if (definitions == null || definitions.size() == 0) {
            throw new NoMatchingDefinitionException("Didn't find a category for site-scoped event: " + event);
        }

        // Just get the first if there's more than one, which there shouldn't be, since they shouldn't differ based on entity.
        Definition definition = definitions.get(0);

        // Create the notification.
        return createNotification(definition, parameters);
    }

    /**
     * This creates a new {@link CategoryScope system-scoped} {@link Notification notification}, setting 
     * it to the given event and dispatching it to all subscribers with the given parameters. Basically,
     * this uses the {@link Definition} with the given event and scope of {@link CategoryScope#Site}. There
     * is no {@link Definition#getEntity() entity property} required, since system-scoped events by definition
     * are not associated with a particular entity.
     * <b>Note:</b> This will create the required {@link Category} and {@link Definition} for the notification
     * if required.
     * @param event In conjunction with the scope {@link CategoryScope#Site}, specifies the {@link Definition} to use.
     * @param parameters Any parameters for this particular notification. These are transformed through JSON to a string.
     * @return The newly created and dispatched notification.
     * @throws IOException Thrown when there's a problem converting the parameters to a string.
     * @throws NoMatchingCategoryException Thrown when the category specified can't be found.
     * @throws NoMatchingDefinitionException Thrown when the definition specified can't be found.
     * @see NotificationService#createNotification(String, String)
     */
    @Override
    @Transactional
    public Notification createNotification(String event, Map<String, Object> parameters) throws IOException, NoMatchingCategoryException, NoMatchingDefinitionException {
        return createNotification(event, MAPPER.writeValueAsString(parameters));
    }

    /**
     * Creates a {@link Definition definition} associated with the {@link Category category} associated with
     * the indicated {@link CategoryScope scope} and event. If there's already a category with the same scope
     * and event, the new definition is associated with that category. Otherwise a new category is created. 
     * @param scope The category scope.
     * @param event The category event.
     * @param entity The entity with which the definition is associated.
     * @return A newly created definition.
     * @throws DuplicateDefinitionException When a definition with the same scope, event, and entity association already exists.
     * @see NotificationService#createDefinition(CategoryScope, String, long)
     */
    @Override
    @Transactional
    public Definition createDefinition(CategoryScope scope, String event, long entity) throws DuplicateDefinitionException {
        Category category = getCategoryService().getCategoryByScopeAndEvent(scope, event);

        if (category == null) {
            category = getCategoryService().newEntity();
            category.setScope(scope);
            category.setEvent(event);
            getCategoryService().create(category);
        } else {
            Definition definition = getDefinitionService().getDefinitionForCategoryAndEntity(category, entity);
            if (definition != null) {
                throw new DuplicateDefinitionException("A definition already exists for the criteria, scope [" + category.getScope() + "], event [" + category.getEvent() + "], entity [" + entity + "]");
            }
        }

        Definition definition = getDefinitionService().newEntity();
        definition.setCategory(category);
        definition.setEntity(entity);
        getDefinitionService().create(definition);

        return definition;
    }

    /**
     * Creates a new subscription for the given {@link Definition definition}, {@link Subscriber subscriber}, and {@link
     * Channel channel}. This subscription indicates that the indicated subscriber wants to be notified of events
     * matching the definition using the given notification channel.
     * @param subscriber The subscriber.
     * @param subscriberType The type of subscriber.
     * @param definition The definition or event to which the subscriber wants to subscribe.
     * @param channel The channel by which the subscriber wants to be notified.
     * @see NotificationService#subscribe(Subscriber, SubscriberType, Definition, List)
     */
    @Override
    public Subscription subscribe(Subscriber subscriber, SubscriberType subscriberType, Definition definition, Channel channel) {
        return subscribe(subscriber, subscriberType, definition, Arrays.asList(new Channel[] {channel}));
    }

    /**
     * This is the same as the {@link #subscribe(Subscriber, SubscriberType, Definition, Channel)} method, except that
     * it allows the subscriber to specify multiple notification channels.
     * @param subscriber The subscriber.
     * @param subscriberType The type of subscriber.
     * @param definition The definition or event to which the subscriber wants to subscribe.
     * @param channels The channels by which the subscriber wants to be notified.
     * @see NotificationService#subscribe(Subscriber, SubscriberType, Definition, Channel)
     */
    @Override
    public Subscription subscribe(Subscriber subscriber, SubscriberType subscriberType, Definition definition, List<Channel> channels) {
        Subscription subscription = _subscriptionService.newEntity();
        subscription.setDefinition(definition);
        subscription.setSubscriber(subscriber);
        subscription.setSubscriberType(subscriberType);
        subscription.setChannels(channels);
        _subscriptionService.create(subscription);
        _subscriberService.refresh(subscriber);
        _definitionService.refresh(definition);
        return subscription;
    }

    @Override
    public void setCategoryService(CategoryService categoryService) {
        _categoryService = categoryService;
    }

    /**
     * Gets the current category service instance.
     * @return Returns the category service.
     * @see NotificationService#getCategoryService()
     */
    @Override
    public CategoryService getCategoryService() {
        return _categoryService;
    }

    /**
     * Sets the channel renderer service for this notification service.
     * @param channelRendererService The channel renderer service to set.
     * @see NotificationService#setChannelRendererService(ChannelRendererService)
     */
    @Override
    public void setChannelRendererService(ChannelRendererService channelRendererService) {
        _channelRendererService = channelRendererService;
    }

    /**
     * Gets the current channel renderer service instance.
     * @return Returns the channel renderer service.
     * @see NotificationService#getChannelRendererService()
     */
    @Override
    public ChannelRendererService getChannelRendererService() {
        return _channelRendererService;
    }

    /**
     * Sets the channel service for this notification service.
     * @param channelService The channel service to set.
     * @see NotificationService#setChannelService(ChannelService)
     */
    @Override
    public void setChannelService(ChannelService channelService) {
        _channelService = channelService;
    }

    /**
     * Gets the current channel service instance.
     * @return Returns the channel service.
     * @see NotificationService#getChannelService()
     */
    @Override
    public ChannelService getChannelService() {
        return _channelService;
    }

    /**
     * Sets the definition service for this notification service.
     * @param definitionService The definition service to set.
     * @see NotificationService#setDefinitionService(DefinitionService)
     */
    @Override
    public void setDefinitionService(DefinitionService definitionService) {
        _definitionService = definitionService;
    }

    /**
     * Gets the current definition service instance.
     * @return Returns the definition service.
     * @see NotificationService#getDefinitionService()
     */
    @Override
    public DefinitionService getDefinitionService() {
        return _definitionService;
    }

    /**
     * Sets the notification dispatcher service for this notification service.
     * @param dispatcherService The dispatcher service to set.
     * @see NotificationService#setNotificationDispatcherService(NotificationDispatcherService)
     */
    @Override
    public void setNotificationDispatcherService(NotificationDispatcherService dispatcherService) {
        _dispatcherService = dispatcherService;
    }

    /**
     * Gets the current notification dispatcher service instance.
     * @return Returns the notification dispatcher service.
     * @see NotificationService#getNotificationDispatcherService()
     */
    @Override
    public NotificationDispatcherService getNotificationDispatcherService() {
        return _dispatcherService;
    }

    /**
     * Sets the subscriber service for this notification service.
     * @param subscriberService The subscriber service to set.
     * @see NotificationService#setSubscriberService(SubscriberService)
     */
    @Override
    public void setSubscriberService(SubscriberService subscriberService) {
        _subscriberService = subscriberService;
    }

    /**
     * Gets the current subscriber service instance.
     * @return Returns the subscriber service.
     * @see NotificationService#getSubscriberService()
     */
    @Override
    public SubscriberService getSubscriberService() {
        return _subscriberService;
    }

    /**
     * Sets the subscription service for this notification service.
     * @param subscriptionService The subscription service to set.
     * @see NotificationService#setSubscriptionService(SubscriptionService)
     */
    @Override
    public void setSubscriptionService(SubscriptionService subscriptionService) {
        _subscriptionService = subscriptionService;
    }

    /**
     * Gets the current subscription service instance.
     * @return Returns the subscription service.
     * @see NotificationService#getSubscriptionService()
     */
    @Override
    public SubscriptionService getSubscriptionService() {
        return _subscriptionService;
    }

    private static final Logger _log = LoggerFactory.getLogger(HibernateNotificationService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private CategoryService _categoryService;
    @Autowired
    private ChannelRendererService _channelRendererService;
    @Autowired
    private ChannelService _channelService;
    @Autowired
    private DefinitionService _definitionService;
    @Autowired
    private NotificationDispatcherService _dispatcherService;
    @Autowired
    private SubscriberService _subscriberService;
    @Autowired
    private SubscriptionService _subscriptionService;
}
