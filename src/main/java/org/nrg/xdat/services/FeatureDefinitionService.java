package org.nrg.xdat.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.FeatureDefinition;
import org.springframework.stereotype.Service;

@Service
public interface FeatureDefinitionService  extends BaseHibernateService<FeatureDefinition>{
    /**
     * Finds the specified role definition
     *
     * @param role The role key
     * @return @link RoleDefinition role defintions.
     */
    abstract public FeatureDefinition findFeatureByKey(String key);
    
}
