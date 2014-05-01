/*
 * ddict.services.DataDictionaryService
 *
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * XNAT is an open-source project of the Neuroinformatics Research Group.
 * Released under the Simplified BSD.
 *
 * Last modified 5/1/14 11:06 AM
 */

package org.nrg.prefs.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.prefs.entities.Preference;

import java.util.Set;

/**
 * The preferences service interface is the primary means of working with preferences
 * within the XNAT service context.
 */
public interface PreferencesService extends BaseHibernateService<Preference> {
    public static String SERVICE_NAME = PreferencesService.class.getSimpleName();

    public abstract Set<String> getFeatureNames();
    public abstract Set<String> getFeatureProperties(final String feature);
    public abstract Object getRawPropertyValue(final String feature, final String property);
    public abstract Object getRawPropertyValue(final String feature, final String property, final String scope, final Object entityId);
}
