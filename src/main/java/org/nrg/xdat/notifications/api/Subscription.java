/**
 * Subscription
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.xdat.notifications.api;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

/**
 * The Class Subscription.
 */
@Entity
public class Subscription {
    @Id
    @SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
    public long getId() {
        return _id;
    }

    public void setId(long id) {
        _id = id;
    }

    @OneToMany(fetch = FetchType.LAZY)
    // @JoinColumn(name = "id", nullable = false)
    public Definition getDefinition() {
        return _definition;
    }

    public void setDefinition (Definition definition) {
        _definition = definition;
    }
    
    @OneToMany(fetch = FetchType.LAZY)
    // @JoinColumn(name = "id", nullable = false)
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    public List<Vector> getVectors() {
        return _vectors;
    }

    public void setVectors(List<Vector> vectors) {
        _vectors = vectors;
    }
    
    private long _id;
    private Definition _definition;
    private Subscriber _subscriber;
    private String _subscriberType;
    private List<Vector> _vectors;
}
