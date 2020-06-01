package org.nrg.xnat.test.services;

import org.nrg.xnat.test.entities.UnauditedEntity;

public interface UnauditedEntitiesService {
    UnauditedEntity create(final UnauditedEntity entity);

    UnauditedEntity retrieve(final long id);

    void update(final UnauditedEntity entity);

    void delete(final long id);
}
