/**
 * Subscription
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.notify.api;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

/**
 * The Class Subscription.
 */
@Entity
public class Subscription extends AbstractHibernateEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    public Definition getDefinition() {
        return _definition;
    }

    public void setDefinition (Definition definition) {
        _definition = definition;
    }

    /**
     * Each subscription maps a {@link #getDefinition() definition} to a single
     * {@link Subscriber subscriber}.
     * @return
     */
    @OneToOne(fetch = FetchType.LAZY)
    public Subscriber getSubscriber() {
        return _subscriber;
    }

    public void setSubscriber (Subscriber subscriber) {
        _subscriber = subscriber;
    }

    public String getSubscriberType() {
        return _subscriberType;
    }
    
    public void setSubscriberType(String subscriberType) {
        _subscriberType = subscriberType;
    }
    
    /**
     * Each {@link Channel vector} maps to multiple {@link Subscription subscriptions} and
     * each subscription can map to multiple vectors, but each vector doesn't need to know
     * about its subscriptions.
     * @return A list of {@link Channel vectors} for notifying the subscriber. 
     */
    @OneToMany(fetch = FetchType.LAZY)
    public List<Channel> getVectors() {
        return _vectors;
    }

    public void setVectors(List<Channel> vectors) {
        _vectors = vectors;
    }
    
    private Definition _definition;
    private Subscriber _subscriber;
    private String _subscriberType;
    private List<Channel> _vectors;
}
