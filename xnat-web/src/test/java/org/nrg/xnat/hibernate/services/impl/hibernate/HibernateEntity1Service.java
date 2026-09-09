package org.nrg.xnat.hibernate.services.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.hibernate.entities.Entity1;
import org.nrg.xnat.hibernate.repositories.Entity1Repository;
import org.nrg.xnat.hibernate.services.Entity1Service;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@Transactional
@Slf4j
public class HibernateEntity1Service extends AbstractHibernateEntityService<Entity1, Entity1Repository> implements Entity1Service {
    @Override
    public Entity1 getByName(final String name) {
        return getDao().findByUniqueProperty("name", name);
    }
}
