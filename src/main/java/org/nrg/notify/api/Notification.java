/**
 * Notification
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.notify.api;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

/**
 * The Class Notification.
 */
@Entity
public class Notification {
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
    public Definition getDefinition() {
        return _definition;
    }

    public void setDefinition (Definition definition) {
        _definition = definition;
    }
    
    public String getParameters() {
        return _parameters;
    }
    
    public void setParameters(String parameters) {
        _parameters = parameters;
    }

    public String getFormat() {
        return _format;
    }

    public void setFormat(String format) {
        _format = format;
    }

    public Date getTimestamp() {
        return _timestamp;
    }

    public void setTimestamp(Date timestamp) {
        _timestamp = timestamp;
    }

    private long _id;
    private Definition _definition;
    private String _parameters;
    private String _format = "application/json";
    private Date _timestamp = new Date();
}
