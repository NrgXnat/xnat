/**
 * Subscription
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.notify.api.SubscriberType;

/**
 * The Class Subscription.
 */
@Entity
public class Subscription extends AbstractHibernateEntity {
    /**
     * Gets the {@link Definition definition} associated with this subscription. The
     * definition essentially defines the event to which the {@link Subscriber subscriber}
     * is subscribed. 
     * @return The associated {@link Definition} object.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    public Definition getDefinition() {
        return _definition;
    }

    /**
     * Sets the associated {@link Definition definition} for this subscription.
     * @param definition The {@link Definition} object with which to associate this subscription.
     */
    public void setDefinition (Definition definition) {
        _definition = definition;
    }

    /**
     * Each subscription maps a {@link #getDefinition() definition} to a single
     * {@link Subscriber subscriber}.
     * @return
     */
    @ManyToOne(fetch = FetchType.EAGER)
    public Subscriber getSubscriber() {
        return _subscriber;
    }

    /**
     * Sets the {@link Subscriber subscriber} with which this subscription is associated.
     * @param subscriber
     */
    public void setSubscriber (Subscriber subscriber) {
        _subscriber = subscriber;
    }

    /**
     * Indicates the type of subscriber. This may indicate a user, system service, IM server,
     * and so on.
     * @return The type of subscriber.
     */
    public SubscriberType getSubscriberType() {
        return _subscriberType;
    }
    
    /**
     * Sets the subscriber type. This may indicate a user, system service, IM server,
     * and so on.
     * @param subscriberType The type of subscriber.
     */
    public void setSubscriberType(SubscriberType subscriberType) {
        _subscriberType = subscriberType;
    }
    
    /**
     * Each {@link Channel channel} maps to multiple {@link Subscription subscriptions} and
     * each subscription can map to multiple channels, but each channel doesn't need to know
     * about its subscriptions.
     * @return A list of {@link Channel channels} for notifying the subscriber. 
     */
    @ManyToMany(fetch = FetchType.LAZY)
    public List<Channel> getChannels() {
        return _channels;
    }

    /**
     * Sets the list of channels for this subscription.
     * @param channels
     */
    public void setChannels(List<Channel> channels) {
        _channels = channels;
    }
    
    /**
     * Adds a {@link Channel channel} to the subscription.
     * @param channel The channel to add to the subscription.
     */
    @Transient
    public void addChannel(Channel channel) {
        if (_channels == null) {
            _channels = new ArrayList<Channel>();
        }
        _channels.add(channel);
    }

    private Definition _definition;
    private Subscriber _subscriber;
    private SubscriberType _subscriberType;
    private List<Channel> _channels;
}
