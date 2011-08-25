/**
 * Category
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.notify.api;

import javax.persistence.Entity;

import org.apache.commons.lang.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

/**
 * The class Category.
 */
@Entity
public class Category extends AbstractHibernateEntity {
    public Category() {
        super();
        setScope(CategoryScope.Default);
        setEvent(null);
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
