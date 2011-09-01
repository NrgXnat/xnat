/**
 * NotificationService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.api.SubscriberType;
import org.nrg.notify.entities.Category;
import org.nrg.notify.entities.Channel;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.entities.Notification;
import org.nrg.notify.entities.Subscriber;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.exceptions.DuplicateDefinitionException;


/**
 * The NotificationService interface. The notification service is the primary service for the notification system.
 * As such it has references to all of the other services required to create and maintain notifications and their
 * classifications, as well as dispatch notifications to subscribers through the configured channels.
  * @author Rick Herrick <rick.herrick@wustl.edu>
*/
public interface NotificationService extends BaseHibernateService<Notification> {
    public static String SERVICE_NAME = "NotificationService";

    /**
     * The ultimate convenience method. This creates a new {@link Notification notification}, setting it to the given
     * definition and dispatching it to all subscribers with the given parameters.
     * @param definition The notification definition from which the notification should be created.
     * @param parameters Any parameters for this particular notification.
     * @return The newly created and dispatched notification.
     * @see NotificationService#createNotification(Definition, Map))
     */
    public abstract Notification createNotification(Definition definition, String parameters);

    /**
     * The ultimate convenience method. This creates a new {@link Notification notification}, setting it to the given
     * definition and dispatching it to all subscribers with the given parameters.
     * @param definition The notification definition from which the notification should be created.
     * @param parameters Any parameters for this particular notification. These are transformed through JSON to a string.
     * @return The newly created and dispatched notification.
     * @throws IOException Thrown when there's a problem converting the parameters to  
     * @see NotificationService#createNotification(Definition, String)
     */
    public abstract Notification createNotification(Definition definition, Map<String, Object> parameters) throws IOException;
    
    /**
     * Creates a {@link Definition definition} associated with the {@link Category category} associated with
     * the indicated {@link CategoryScope scope} and event. If there's already a category with the same scope
     * and event, the new definition is associated with that category. Otherwise a new category is created. 
     * @param scope The category scope.
     * @param event The category event.
     * @param entity The entity with which the definition is associated.
     * @return A newly created definition.
     * @throws DuplicateDefinitionException When a definition with the same scope, event, and entity association already exists.
     */
    abstract public Definition createDefinition(CategoryScope scope, String event, long entity) throws DuplicateDefinitionException;

    /**
     * Creates a new subscription for the given {@link Definition definition}, {@link Subscriber subscriber}, 
     * and {@link Channel channel}. This subscription indicates that the indicated subscriber wants to be notified
     * of events matching the definition using the given notification channel. 
     * @param subscriber The subscriber.
     * @param subscriberType The type of subscriber.
     * @param definition The definition or event to which the subscriber wants to subscribe.
     * @param channel The channel by which the subscriber wants to be notified.
     */
    public abstract Subscription subscribe(Subscriber subscriber, SubscriberType subscriberType, Definition definition, Channel channel);

    /**
     * This is the same as the {@link #subscribe(Subscriber, SubscriberType, Definition, Channel)} method, except that
     * it allows the subscriber to specify multiple notification channels. 
     * @param subscriber The subscriber.
     * @param subscriberType The type of subscriber.
     * @param definition The definition or event to which the subscriber wants to subscribe.
     * @param channels The channels by which the subscriber wants to be notified.
     */
    public abstract Subscription subscribe(Subscriber subscriber, SubscriberType subscriberType, Definition definition, List<Channel> channels);

    /**
     * Getters and setters for all the dependent service instances.
     * @return
     */
    abstract public CategoryService getCategoryService();
    abstract public void setCategoryService(CategoryService categoryService);
    abstract public ChannelRendererService getChannelRendererService();
    abstract public void setChannelRendererService(ChannelRendererService channelRendererService);
    abstract public ChannelService getChannelService();
    abstract public void setChannelService(ChannelService channelService);
    abstract public DefinitionService getDefinitionService();
    abstract public void setDefinitionService(DefinitionService definitionService);
    abstract public NotificationDispatcherService getNotificationDispatcherService();
    abstract public void setNotificationDispatcherService(NotificationDispatcherService dispatcherService);
    abstract public SubscriberService getSubscriberService();
    abstract public void setSubscriberService(SubscriberService subscriberService);
    abstract public SubscriptionService getSubscriptionService();
    abstract public void setSubscriptionService(SubscriptionService subscriptionService);
}
