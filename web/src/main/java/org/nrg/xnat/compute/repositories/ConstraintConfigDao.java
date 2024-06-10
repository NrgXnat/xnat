package org.nrg.xnat.compute.repositories;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.compute.entities.ConstraintConfigEntity;
import org.nrg.xnat.compute.entities.ConstraintEntity;
import org.nrg.xnat.compute.entities.ConstraintScopeEntity;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class ConstraintConfigDao extends AbstractHibernateDAO<ConstraintConfigEntity> {

    // For testing
    public ConstraintConfigDao(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /**
     * Initialize the constraint config entity.
     * @param entity The entity to initialize.
     */
    @Override
    public void initialize(final ConstraintConfigEntity entity) {
        if (entity == null) {
            return;
        }

        initialize(entity.getConstraint());

        Hibernate.initialize(entity.getScopes());
        if (entity.getScopes() != null) {
            entity.getScopes().forEach((scope, constraintScopeEntity) -> {
                initialize(constraintScopeEntity);
            });
        }
    }

    /**
     * Initialize the constraint entity.
     * @param entity The entity to initialize.
     */
    void initialize(final ConstraintEntity entity) {
        if (entity == null) {
            return;
        }

        Hibernate.initialize(entity);
        if (entity.getConstraintValues() != null) {
            Hibernate.initialize(entity.getConstraintValues());
        }
    }

    /**
     * Initialize the constraint scope entity.
     * @param entity The entity to initialize.
     */
    public void initialize(ConstraintScopeEntity entity) {
        if (entity == null) {
            return;
        }

        Hibernate.initialize(entity);
        Hibernate.initialize(entity.getIds());
    }
}
