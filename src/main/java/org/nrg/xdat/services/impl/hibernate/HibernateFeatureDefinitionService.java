package org.nrg.xdat.services.impl.hibernate;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.FeatureDefinitionDAO;
import org.nrg.xdat.entities.FeatureDefinition;
import org.nrg.xdat.services.FeatureDefinitionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernateFeatureDefinitionService extends AbstractHibernateEntityService<FeatureDefinition, FeatureDefinitionDAO> implements FeatureDefinitionService {

	@Override
	@Transactional
	public FeatureDefinition findFeatureByKey(String key) {
		return this.getDao().findByKey(key);
	}

}
