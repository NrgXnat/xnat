/**
 * Channel
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

/**
 * The channel class represents a notification channel that can be used to inform a subscriber that a
 * particular event occurred. This allows notifications to be created generically and then displayed
 * through a variety of means, such as email, IM, REST API calls, and so on.
 */
@Entity
public class Channel extends AbstractHibernateEntity {

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
