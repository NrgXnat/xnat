/**
 * NotificationService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.notify.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.notify.api.Notification;

/**
 * The NotificationService interface. The notification service is the primary service for the notification system.
 * As such it has references to all of the other services required to create and maintain notifications and their
 * classifications, as well as dispatch notifications to subscribers through the configured channels.
  * @author Rick Herrick <rick.herrick@wustl.edu>
*/
public interface NotificationService extends BaseHibernateService<Notification> {
    public static String SERVICE_NAME = "NotificationService";

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
