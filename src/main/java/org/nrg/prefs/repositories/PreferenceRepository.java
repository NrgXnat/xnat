/*
 * ddict.repositories.ResourceRepository
 *
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * XNAT is an open-source project of the Neuroinformatics Research Group.
 * Released under the Simplified BSD.
 *
 * Last modified 5/1/14 11:06 AM
 */

package org.nrg.prefs.repositories;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.prefs.entities.Preference;
import org.springframework.stereotype.Repository;

/**
 * Manages preferences within the preferences service framework.
 */
@Repository
public class PreferenceRepository extends AbstractHibernateDAO<Preference> {
}
