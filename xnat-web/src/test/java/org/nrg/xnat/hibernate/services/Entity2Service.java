package org.nrg.xnat.hibernate.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.hibernate.entities.Entity2;

public interface Entity2Service extends BaseHibernateService<Entity2> {
    Entity2 getByName(final String name);
}
