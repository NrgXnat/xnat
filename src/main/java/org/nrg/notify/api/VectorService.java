/**
 * VectorService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 18, 2011
 */
package org.nrg.notify.api;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 * @author rherrick
 *
 */
@Entity
public class VectorService {
    @Id
    @SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
    public long getId() {
        return _id;
    }

    public void setId(long id) {
        _id = id;
    }

    private long _id;
}
