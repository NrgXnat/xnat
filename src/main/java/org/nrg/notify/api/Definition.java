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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

/**
 * The Class Definition.
 */
@Entity
public class Definition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
    public long getId() {
        return _id;
    }

    public void setId(long id) {
        _id = id;
    }

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
        return _category.toString() + "/[" + _id + "] " + _entity;
    }

    private long _id;
    private Category _category;
    private long _entity;
}
