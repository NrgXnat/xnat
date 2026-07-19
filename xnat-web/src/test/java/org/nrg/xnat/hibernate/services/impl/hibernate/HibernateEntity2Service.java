package org.nrg.xnat.hibernate.services.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.hibernate.entities.Entity2;
import org.nrg.xnat.hibernate.repositories.Entity2Repository;
import org.nrg.xnat.hibernate.services.Entity2Service;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@Transactional
@Slf4j
public class HibernateEntity2Service extends AbstractHibernateEntityService<Entity2, Entity2Repository> implements Entity2Service {
    @Override
    public Entity2 getByName(final String name) {
        return getDao().findByUniqueProperty("name", name);
    }
}
