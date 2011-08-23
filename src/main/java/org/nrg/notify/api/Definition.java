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
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

/**
 * The Class Definition.
 */
@Entity
public class Definition {
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
    
    private long _id;
    private Category _category;
    private long _entity;
}
