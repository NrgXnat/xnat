/*
 * org.nrg.notify.entities.Subscription
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.notify.api.SubscriberType;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class Subscription extends AbstractHibernateEntity {
    private static final long serialVersionUID = -5390321566529122884L;

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
     * @return The {@link Subscriber subscriber} associated with this subscription.
     */
    @ManyToOne
    @LazyCollection(LazyCollectionOption.FALSE)
    public Subscriber getSubscriber() {
        return _subscriber;
    }

    /**
     * Sets the {@link Subscriber subscriber} with which this subscription is associated.
     * @param subscriber    The {@link Subscriber subscriber} to be associated with this subscription.

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
    @ManyToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    public List<Channel> getChannels() {
        return _channels;
    }

    /**
     * Sets the list of channels for this subscription.
     * @param channels    The {@link Channel channels} to associated with this subscription.
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
