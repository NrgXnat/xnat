package org.nrg.xnat.hibernate.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.hibernate.entities.Entity1;

public interface Entity1Service extends BaseHibernateService<Entity1> {
    Entity1 getByName(String name);
}
