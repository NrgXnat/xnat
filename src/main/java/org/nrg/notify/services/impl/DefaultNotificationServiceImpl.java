/**
 * DefaultNotificationServiceImpl
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.notify.services.impl;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.api.Notification;
import org.nrg.notify.daos.NotificationDAO;
import org.nrg.notify.services.CategoryService;
import org.nrg.notify.services.ChannelRendererService;
import org.nrg.notify.services.ChannelService;
import org.nrg.notify.services.DefinitionService;
import org.nrg.notify.services.NotificationDispatcherService;
import org.nrg.notify.services.NotificationService;
import org.nrg.notify.services.SubscriberService;
import org.nrg.notify.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides a default implementation for the notification service.
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Service
public class DefaultNotificationServiceImpl extends AbstractHibernateEntityService<Notification> implements NotificationService {
    /**
     * Creates a new {@link Notification notification} object.
     * @return A new {@link Notification notification} object.
     * @see AbstractHibernateEntityService#newEntity()
     */
    @Override
    public Notification newEntity() {
        return new Notification();
    }

    /**
     * Sets the category service for this notification service.
     * @param categoryService The category service to set.
     * @see NotificationService#setCategoryService(CategoryService)
     */
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

    /**
     * Gets the {@link NotificationDAO notification DAO} instance for this service.
     * @return The {@link NotificationDAO notification DAO} instance for this service.
     * @see AbstractHibernateEntityService#getDao()
     */
    @Override
    protected NotificationDAO getDao() {
        return _dao;
    }

    @Autowired
    private NotificationDAO _dao;

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
