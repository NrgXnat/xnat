package org.nrg.config.daos;

import org.nrg.config.entities.ConfigurationData;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

@Repository
public class ConfigurationDataDAO  extends AbstractHibernateDAO<ConfigurationData>{

	//ConfigurationDatas are immutable, so override delete and update
	@Override
    public void delete(ConfigurationData entity) {
		throw new UnsupportedOperationException();
    }
	
	@Override
    public void update(ConfigurationData entity) {
		throw new UnsupportedOperationException();
    }
}
