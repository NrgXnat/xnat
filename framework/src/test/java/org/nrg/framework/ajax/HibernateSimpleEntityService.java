/*
 * framework: org.nrg.framework.ajax.HibernateSimpleEntityService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.ajax;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@CacheConfig(cacheNames = "simpleEntity")
@Slf4j
public class HibernateSimpleEntityService extends AbstractHibernateEntityService<SimpleEntity, SimpleEntityDAO> implements SimpleEntityService {
    @Autowired
    public HibernateSimpleEntityService(final JCacheHelper cacheHelper) {
        cacheHelper.getCache("simpleEntityById", Long.class, SimpleEntity.class);
        cacheHelper.getCache("simpleEntityByName", String.class, SimpleEntity.class);
        log.info("Created simpleEntityById and simpleEntityByName caches");
    }

    @Caching(put = {@CachePut(cacheNames = "simpleEntityById", key = "#result.id", unless = "#result == null"),
                    @CachePut(cacheNames = "simpleEntityByName", key = "#name", unless = "#result == null")})
    @Override
    public SimpleEntity findByName(final String name) {
        final List<SimpleEntity> pacs = getDao().findByProperty("name", name);
        if (pacs.size() > 1) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Found multiple entities with name " + name + ", but name is a unique attribute.");
        }
        return pacs.getFirst();
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

    @CachePut(cacheNames = "simpleEntityByName", key = "#username")
    @Override
    public List<SimpleEntity> findAllByUsername(final String username) {
        return getDao().findAllByUsername(username);
    }

    @CachePut(cacheNames = "simpleEntityByName", key = "#username", unless = "#result.empty")
    @Override
    public List<SimpleEntity> findAllByUsername(final String username, final @Nonnull SimpleEntityPaginatedRequest request) {
        return getDao().findAllByUsername(username, request);
    }

    @Caching(put = {@CachePut(cacheNames = "simpleEntityById", key = "#id", unless = "#result == null"),
                    @CachePut(cacheNames = "simpleEntityByName", key = "#username", unless = "#result == null")})
    @Override
    public Optional<SimpleEntity> findByIdAndUsername(final long id, final String username) {
        return getDao().findByIdAndUsername(id, username);
    }

    @Override
    public List<SimpleEntity> findByExample(final SimpleEntity example, String... excludedProperties) {
        return getDao().findByExample(example, excludedProperties);
    }

    @Override
    public List<SimpleEntity> findByNamesAndDescription(List<String> names, String description) {
        return getDao().findByProperties(parameters("name", names, "description", description));
    }

    @Override
    public List<SimpleEntity> findByNamesAndTotal(List<String> names, int total) {
        return getDao().findByProperties(parameters("name", names, "total", total));
    }
}
