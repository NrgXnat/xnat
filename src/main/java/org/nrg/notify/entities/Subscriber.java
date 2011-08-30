/**
 * Subscriber
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.entities;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

/**
 * The Class Subscriber.
 */
@Entity
public class Subscriber extends AbstractHibernateEntity {
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getEmails() {
        return _emails;
    }

    public void setEmails(String emails) {
        _emails = emails;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy="subscriber")
    public List<Subscription> getSubscriptions() {
        return _subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        _subscriptions = subscriptions;
    }
    
    private String _name;
    private String _emails;
    private List<Subscription> _subscriptions;
}
