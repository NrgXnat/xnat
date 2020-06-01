package org.nrg.xnat.test.services;

import org.nrg.xnat.test.entities.AuditedEntity;

public interface AuditedEntitiesService {
    AuditedEntity create(final AuditedEntity entity);

    AuditedEntity retrieve(final long id);

    void update(final AuditedEntity entity);

    void delete(final long id);
}
