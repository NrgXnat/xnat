package org.nrg.xnat.test.repositories;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.nrg.xnat.test.entities.UnauditedEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UnauditedEntityRepository {
    @Autowired
    public UnauditedEntityRepository(final SessionFactory factory) {
        _factory = factory;
    }

    public UnauditedEntity create(final UnauditedEntity entity) {
        return retrieve((long) getSession().save(entity));
    }

    public UnauditedEntity retrieve(final long id) {
        return (UnauditedEntity) getSession().get(UnauditedEntity.class, id);
    }

    public void update(final UnauditedEntity entity) {
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
