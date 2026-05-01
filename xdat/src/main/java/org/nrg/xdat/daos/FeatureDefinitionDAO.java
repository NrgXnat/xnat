/*
 * core: org.nrg.xdat.daos.FeatureDefinitionDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.FeatureDefinition;
import org.springframework.stereotype.Repository;

@Repository
public class FeatureDefinitionDAO extends AbstractHibernateDAO<FeatureDefinition>{
    public FeatureDefinition findByKey(String key) {
        return findByUniqueProperty("key", key);
    }
}
