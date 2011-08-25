/**
 * Definition
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.notify.api;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

/**
 * The Class Definition.
 */
@Entity
public class Definition extends AbstractHibernateEntity {
    @ManyToOne(fetch = FetchType.LAZY)
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
    
    @Override
    @Transient
    public boolean isDeletable() {
        return false;
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
}
