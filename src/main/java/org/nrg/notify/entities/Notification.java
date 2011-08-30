/**
 * Notification
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
 * The Class Notification.
 */
@Auditable
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

    public String getParameterFormat() {
        return _format;
    }

    public void setFormat(String format) {
        _format = format;
    }

    private Definition _definition;
    private String _parameters;
    private String _format = "application/json";
}
