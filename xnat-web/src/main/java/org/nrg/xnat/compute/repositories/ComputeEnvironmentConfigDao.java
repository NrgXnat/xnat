package org.nrg.xnat.compute.repositories;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.compute.entities.ComputeEnvironmentConfigEntity;
import org.nrg.xnat.compute.entities.ComputeEnvironmentScopeEntity;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@Slf4j
public class ComputeEnvironmentConfigDao extends AbstractHibernateDAO<ComputeEnvironmentConfigEntity> {

    private final HardwareConfigDao hardwareConfigDao;

    // For testing
    public ComputeEnvironmentConfigDao(final SessionFactory sessionFactory,
                                       final HardwareConfigDao hardwareConfigDao) {
        super(sessionFactory);
        this.hardwareConfigDao = hardwareConfigDao;
    }

    @Autowired
    public ComputeEnvironmentConfigDao(HardwareConfigDao hardwareConfigDao) {
        this.hardwareConfigDao = hardwareConfigDao;
    }

    /**
     * Initializes the entity, loading all collections and proxies.
     * @param entity The entity to initialize.
     */
    @Override
    public void initialize(final ComputeEnvironmentConfigEntity entity) {
        if (entity == null) {
            return;
        }

        Hibernate.initialize(entity);

        Hibernate.initialize(entity.getConfigTypes());

        Hibernate.initialize(entity.getComputeEnvironment());
        if (entity.getComputeEnvironment() != null) {
            Hibernate.initialize(entity.getComputeEnvironment().getEnvironmentVariables());
            Hibernate.initialize(entity.getComputeEnvironment().getMounts());
        }

        Hibernate.initialize(entity.getScopes());
        if (entity.getScopes() != null) {
            entity.getScopes().forEach((scope, computeEnvironmentScopeEntity) -> {
                initialize(computeEnvironmentScopeEntity);
            });
        }

        Hibernate.initialize(entity.getHardwareOptions());
        if (entity.getHardwareOptions() != null) {
            Hibernate.initialize(entity.getHardwareOptions().getHardwareConfigs());

            if (entity.getHardwareOptions().getHardwareConfigs() != null) {
                for (HardwareConfigEntity hardwareConfig : entity.getHardwareOptions().getHardwareConfigs()) {
                    hardwareConfigDao.initialize(hardwareConfig);
                }
            }
        }
    }

    /**
     * Initializes the entity, loading all collections and proxies.
     * @param entity The entity to initialize.
     */
    public void initialize(ComputeEnvironmentScopeEntity entity) {
        if (entity == null) {
            return;
        }

        Hibernate.initialize(entity);
        Hibernate.initialize(entity.getIds());
    }

    /**
     * Finds all compute environment configs that have the specified type.
     * @param type The type to search for.
     * @return The list of compute environment configs that have the specified type.
     */
    public List<ComputeEnvironmentConfigEntity> findByType(String type) {
        // Need to use a criteria query because the configTypes field is a collection.
        Criteria criteria = getSession().createCriteria(ComputeEnvironmentConfigEntity.class)
                .createCriteria("configTypes")
                .add(Restrictions.eq("elements", type));

        List<ComputeEnvironmentConfigEntity> entities = criteria.list();

        if (entities == null) {
            return Collections.emptyList();
        }

        for (ComputeEnvironmentConfigEntity entity : entities) {
            initialize(entity);
        }

        return entities;
    }

}
