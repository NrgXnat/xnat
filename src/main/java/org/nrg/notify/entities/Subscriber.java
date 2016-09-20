/*
 * org.nrg.notify.entities.Subscriber
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class Subscriber extends AbstractHibernateEntity {
    private static final long serialVersionUID = 6707256690115392905L;

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

    public void removeSubscription(Subscription subscription) {
        _subscriptions.remove(subscription);
    }

    @Transient
    public List<String> getEmailList() {
        if (StringUtils.isBlank(_emails)) {
            return new ArrayList<String>();
        }
        return Arrays.asList(_emails.split("[\\s]*,[\\s]*"));
    }

    @Override
    @Transient
    public String toString() {
        List<String> emails = getEmailList();
        return emails.size() == 0 ? _name : _name + " <" + emails.get(0) + ">"; 
    }

    @Override
    @Transient
    public int hashCode() {
        return new HashCodeBuilder().append(_name).append(_emails).toHashCode();
    }
    
    private String _name;
    private String _emails;
    private List<Subscription> _subscriptions;
}
