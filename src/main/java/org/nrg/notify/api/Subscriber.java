/**
 * Subscriber
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.notify.api;

import javax.persistence.Entity;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

/**
 * The Class Subscriber.
 */
@Entity
public class Subscriber extends AbstractHibernateEntity {
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getEmails() {
        return _emails;
    }

    public void setEmails(String emails) {
        _emails = emails;
    }

    private String _name;
    private String _emails;
}
