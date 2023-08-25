package org.nrg.xnat.compute.repositories;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.nrg.xnat.compute.entities.HardwareConstraintEntity;
import org.nrg.xnat.compute.entities.HardwareEntity;
import org.nrg.xnat.compute.entities.HardwareScopeEntity;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class HardwareConfigDao extends AbstractHibernateDAO<HardwareConfigEntity> {

    // For testing
    public HardwareConfigDao(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /**
     * Initializes the entity, loading all collections and proxies.
     * @param entity The entity to initialize.
     */
    @Override
    public void initialize(final HardwareConfigEntity entity) {
        if (entity == null) {
            return;
        }

        Hibernate.initialize(entity);

        Hibernate.initialize(entity.getScopes());
        if (entity.getScopes() != null) {
            entity.getScopes().forEach((scope, hardwareScopeEntity) -> {
                initialize(hardwareScopeEntity);
            });
        }

        Hibernate.initialize(entity.getHardware());
        if (entity.getHardware() != null) {
            initialize(entity.getHardware());
        }

        if (entity.getComputeEnvironmentHardwareOptions() != null) {
            Hibernate.initialize(entity.getComputeEnvironmentHardwareOptions());
        }
    }

    /**
     * Initializes the entity, loading all collections and proxies.
     * @param entity The entity to initialize.
     */
    public void initialize(HardwareScopeEntity entity) {
        if (entity == null) {
            return;
        }

        Hibernate.initialize(entity);
        Hibernate.initialize(entity.getIds());
    }

    /**
     * Initializes the entity, loading all collections and proxies.
     * @param entity The entity to initialize.
     */
    public void initialize(final HardwareEntity entity) {
        if (entity == null) {
            return;
        }

        Hibernate.initialize(entity);

        Hibernate.initialize(entity.getConstraints());
        if (entity.getConstraints() != null) {
            for (final HardwareConstraintEntity constraint : entity.getConstraints()) {
                Hibernate.initialize(constraint.getConstraintValues());
            }
        }

        Hibernate.initialize(entity.getEnvironmentVariables());
        Hibernate.initialize(entity.getGenericResources());
    }
}
