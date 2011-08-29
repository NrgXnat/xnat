/**
 * Definition
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

/**
 * The Class Definition.
 */
@Auditable
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
