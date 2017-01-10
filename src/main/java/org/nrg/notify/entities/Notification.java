/*
 * notify: org.nrg.notify.entities.Notification
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

@Auditable
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class Notification extends AbstractHibernateEntity {
    private static final long serialVersionUID = -8178787179582506271L;

    @ManyToOne(fetch = FetchType.LAZY)
    public Definition getDefinition() {
        return _definition;
    }

    public void setDefinition (Definition definition) {
        _definition = definition;
    }

    @Column(columnDefinition="TEXT") 
    public String getParameters() {
        return _parameters;
    }
    
    public void setParameters(String parameters) {
        _parameters = parameters;
    }

    public String getParameterFormat() {
        return _format;
    }

    public void setParameterFormat(String format) {
        _format = format;
    }

    private Definition _definition;
    private String _parameters;
    private String _format = "application/json";
}
