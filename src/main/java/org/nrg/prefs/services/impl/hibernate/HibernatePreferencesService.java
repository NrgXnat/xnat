/*
 * ddict.services.impl.hibernate.HibernateResourceService
 *
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * XNAT is an open-source project of the Neuroinformatics Research Group.
 * Released under the Simplified BSD.
 *
 * Last modified 5/1/14 11:06 AM
 */

package org.nrg.prefs.services.impl.hibernate;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.framework.orm.hibernate.BaseHibernateDAO;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.repositories.PreferenceRepository;
import org.nrg.prefs.services.PreferencesService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Set;

/**
 * Service for managing preferences within the XNAT system.
 */
@Service
    public class HibernatePreferencesService extends AbstractHibernateEntityService<Preference, PreferenceRepository> implements PreferencesService {

    @Override
    public Set<String> getFeatureNames() {
        return null;
    }

    @Override
    public Set<String> getFeatureProperties(final String feature) {
        return null;
    }

    @Override
    public Object getRawPropertyValue(final String feature, final String property) {
        return null;
    }

    @Override
    public Object getRawPropertyValue(final String feature, final String property, final String scope, final Object entityId) {
        return null;
    }
}
