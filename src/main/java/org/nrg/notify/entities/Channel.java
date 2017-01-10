/*
 * notify: org.nrg.notify.entities.Channel
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class Channel extends AbstractHibernateEntity {

    private static final long serialVersionUID = -729108847898106370L;

    @Column(unique=true, nullable=false)
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getFormat() {
        return _format;
    }

    public void setFormat(String format) {
        _format = format;
    }

    private String _name;
    private String _format;
}
