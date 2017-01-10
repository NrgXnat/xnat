/*
 * notify: org.nrg.notify.entities.Definition
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.entities;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

@Auditable
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class Definition extends AbstractHibernateEntity {
    private static final long serialVersionUID = 3209167902228816781L;

    @ManyToOne(fetch = FetchType.EAGER)
    public Category getCategory() {
        return _category;
    }

    public void setCategory(Category category) {
        _category = category;
    }
    
    public long getEntity() {
        return _entity;
    }

    public void setEntity(long entity) {
        _entity = entity;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy="definition")
    public List<Subscription> getSubscriptions() {
        return _subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        _subscriptions = subscriptions;
    }
    
    @Override
    public String toString() {
        return _category.toString() + "/[" + getId() + "] " + _entity;
    }
    
    @Override
    public boolean equals(Object item) {
        if (item == null) {
            return false;
        }
        if (!(item instanceof Definition)) {
            return false;
        }

        // TODO: Should equals be based on the ID? Or just the attributes?
        Definition definition = (Definition) item;
        return definition.getId() == getId() &&
               definition.getCategory().equals(_category) &&
               definition.getEntity() == _entity;
    }

    private Category _category;
    private long _entity;
    private List<Subscription> _subscriptions;
}
