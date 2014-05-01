/*
 * ddict.entities.Resource
 *
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * XNAT is an open-source project of the Neuroinformatics Research Group.
 * Released under the Simplified BSD.
 *
 * Last modified 5/1/14 11:06 AM
 */

package org.nrg.prefs.entities;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.Entity;

/**
 * Represents a preference within the preferences service.
 */
@Entity
public class Preference extends AbstractHibernateEntity {

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    private String _name;
}
