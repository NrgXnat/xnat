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


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

/**
 * The Class Notification.
 */
@Entity
public class Notification extends AbstractHibernateEntity {
    @ManyToOne(fetch = FetchType.LAZY)
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

    private Definition _definition;
    private String _parameters;
    private String _format = "application/json";
}
