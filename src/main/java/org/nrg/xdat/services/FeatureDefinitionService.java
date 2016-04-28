package org.nrg.xdat.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.FeatureDefinition;

public interface FeatureDefinitionService extends BaseHibernateService<FeatureDefinition> {
    /**
     * Finds the specified role definition
     *
     * @param key The role key
     * @return The {@link FeatureDefinition feature definition} for the specified key.
     */
    FeatureDefinition findFeatureByKey(String key);
}
