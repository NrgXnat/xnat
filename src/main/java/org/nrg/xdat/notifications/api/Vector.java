/**
 * Vector
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.xdat.notifications.api;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 * The Class Vector.
 */
@Entity
public class Vector {
    @Id
    @SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
    public long getId() {
        return _id;
    }

    public void setId(long id) {
        _id = id;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getService() {
        return _service;
    }

    public void setService(String service) {
        _service = service;
    }

    public String getFormat() {
        return _format;
    }

    public void setFormat(String format) {
        _format = format;
    }

    public String getTransformer() {
        return _transformer;
    }

    public void setTransformer(String transformer) {
        _transformer = transformer;
    }

    private long _id;
    private String _name;
    private String _service;
    private String _format;
    private String _transformer;
}
