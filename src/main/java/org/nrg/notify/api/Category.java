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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import org.apache.commons.lang.StringUtils;

/**
 * The class Category.
 */
@Entity
public class Category {
    public Category() {
        setId(0);
        setScope(CategoryScope.Default);
        setEvent(null);
    }

    // TODO: @GeneratedValue won't force H2 to create table, but @SequenceGenerator will. Why?
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
    public long getId() {
        return _id;
    }

    public void setId(long id) {
        _id = id;
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
        return "[" + _id + "] " + _scope + ": " + _event;
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
        return category.getId() == _id &&
               StringUtils.equals(category.getEvent(), _event) &&
               category.getScope() == _scope;
    }
    
    private long _id;
    private CategoryScope _scope;
    private String _event;
}
