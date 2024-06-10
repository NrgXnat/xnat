package org.nrg.xnat.test.repositories;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.nrg.xnat.test.entities.AuditedEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AuditedEntityRepository {
    @Autowired
    public AuditedEntityRepository(final SessionFactory factory) {
        _factory = factory;
    }

    public AuditedEntity create(final AuditedEntity entity) {
        return retrieve((long) getSession().save(entity));
    }

    public AuditedEntity retrieve(final long id) {
        return (AuditedEntity) getSession().get(AuditedEntity.class, id);
    }

    public void update(final AuditedEntity entity) {
        getSession().update(entity);
    }

    public void delete(final long id) {
        getSession().delete(retrieve(id));
    }

    private Session getSession() {
        return _factory.getCurrentSession();
    }

    private final SessionFactory _factory;
}
