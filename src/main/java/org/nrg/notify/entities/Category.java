/**
 * Category
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.entities;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;
import org.nrg.notify.api.CategoryScope;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * The Category supports notification categories. These are basically a {@link CategoryScope scope} (e.g.
 * site, project, etc.) and an event, which is just a string that identifies an event. The unique constraint
 * for this class allows multiple disabled instances of a particular category, but only a single enabled
 * instance of that category. 
 */
@Auditable
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"scope", "event", "enabled", "disabled"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrgNotify")
public class Category extends AbstractHibernateEntity {
    public Category() {
        super();
        setScope(CategoryScope.Default);
        _event = null;
    }
    
    public CategoryScope getScope() {
        return _scope;
    }
    
    public void setScope(CategoryScope scope) {
        _scope = scope;
    }
    
    public String getEvent() {
        return _event;
    }

    public void setEvent(String event) {
        _event = event;
    }
    
    @Override
    public String toString() {
        return "[" + getId() + "] " + _scope + ": " + _event;
    }

    @Override
    public boolean equals(Object item) {
        if (item == null) {
            return false;
        }
        if (!(item instanceof Category)) {
            return false;
        }

        // TODO: Should equals be based on the ID? Or just the attributes?
        Category category = (Category) item;
        return category.getId() == getId() &&
               StringUtils.equals(category.getEvent(), _event) &&
               category.getScope() == _scope;
    }

    private CategoryScope _scope;
    private String _event;
}
