/*
 * framework: org.nrg.framework.ajax.HibernateSimpleEntityService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.ajax;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HibernateSimpleEntityService extends AbstractHibernateEntityService<SimpleEntity, SimpleEntityDAO> implements SimpleEntityService {
    @Override
    public SimpleEntity findByName(final String name) {
        final List<SimpleEntity> pacs = getDao().findByProperty("name", name);
        if (pacs == null) {
            return null;
        }
        if (pacs.size() > 1) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Found multiple entities with name " + name + ", but name is a unique attribute.");
        }
        return pacs.get(0);
    }

    @Override
    public List<SimpleEntity> findAllOrderedByTimestamp() {
        return getDao().findAllOrderedByTimestamp();
    }

    @Override
    public List<SimpleEntity> findAllOrderedByTimestamp(final SimpleEntityPaginatedRequest request) {
        return getDao().findAllOrderedByTimestamp(request);
    }

    @Override
    public long getAllForUserCount(final String username) {
        return findAllByUsername(username).size();
    }

    @Override
    public List<SimpleEntity> findAllByUsername(final String username) {
        return getDao().findAllByUsername(username);
    }

    @Override
    public List<SimpleEntity> findAllByUsername(final String username, final @Nonnull SimpleEntityPaginatedRequest request) {
        return getDao().findAllByUsername(username, request);
    }

    @Override
    public Optional<SimpleEntity> findByIdAndUsername(final long id, final String username) {
        return getDao().findByIdAndUsername(id, username);
    }
}
