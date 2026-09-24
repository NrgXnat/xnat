/*
 * web: org.nrg.xnat.node.services.impl.HibernateXnatNodeInfoService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.processor.services.impl;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.processor.dao.ArchiveProcessorInstanceDAO;
import org.nrg.xnat.processor.services.ArchiveProcessorInstanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class HibernateArchiveProcessorInstanceService extends AbstractHibernateEntityService<ArchiveProcessorInstance, ArchiveProcessorInstanceDAO> implements ArchiveProcessorInstanceService {
    @Override
    public List<ArchiveProcessorInstance> getAllSiteProcessors() {
        return getDao().getSiteArchiveProcessors();
    }

    @Override
    public List<ArchiveProcessorInstance> getAllSiteProcessorsForClass(final String processorClass) {
        return getDao().getSiteArchiveProcessorsForClass(processorClass);
    }

    @Override
    public List<ArchiveProcessorInstance> getAllEnabledSiteProcessors() {
        return getDao().getEnabledSiteArchiveProcessors();
    }

    @Override
    public List<ArchiveProcessorInstance> getAllEnabledSiteProcessorsForAe(String aeAndPort) {
        return getDao().getEnabledSiteArchiveProcessorsForAe(aeAndPort);
    }

    @Override
    public List<ArchiveProcessorInstance> getAllEnabledSiteProcessorsInOrder() {
        return getDao().getEnabledSiteArchiveProcessorsInOrder();
    }

    @Override
    public List<ArchiveProcessorInstance> getAllEnabledSiteProcessorsInOrderForLocation(final String location) {
        // The importer asks this for every received object at each of its processing locations, and each ask
        // was a Hibernate query: the single largest CPU cost of receiving an object. The answer is kept for a
        // few seconds per location; the instances are fully loaded (every collection is eager) and read-only
        // to the importer, so sharing them is safe. A processor enabled or changed in the UI takes effect
        // within that time rather than on the very next object.
        final CachedProcessors cached = _byLocation.get(location);
        final long             now    = System.nanoTime();
        if (cached != null && now - cached.readAt < CACHE_TTL_NANOS) {
            return cached.processors;
        }
        final List<ArchiveProcessorInstance> processors = getDao().getEnabledSiteArchiveProcessorsInOrderForLocation(location);
        _byLocation.put(location, new CachedProcessors(processors == null ? null : List.copyOf(processors), now));
        return processors;
    }

    private record CachedProcessors(List<ArchiveProcessorInstance> processors, long readAt) {}

    private final Map<String, CachedProcessors> _byLocation = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_NANOS = TimeUnit.SECONDS.toNanos(5);

    // A change made through this service is seen by this node at once; other nodes see it within the TTL.
    @Override
    public ArchiveProcessorInstance create(final ArchiveProcessorInstance entity) {
        final ArchiveProcessorInstance created = super.create(entity);
        _byLocation.clear();
        return created;
    }

    @Override
    public ArchiveProcessorInstance create(final Object... parameters) {
        final ArchiveProcessorInstance created = super.create(parameters);
        _byLocation.clear();
        return created;
    }

    @Override
    public void update(final ArchiveProcessorInstance entity) {
        super.update(entity);
        _byLocation.clear();
    }

    @Override
    public void delete(final ArchiveProcessorInstance entity) {
        super.delete(entity);
        _byLocation.clear();
    }

    @Override
    public void delete(final long id) {
        super.delete(id);
        _byLocation.clear();
    }

    @Override
    public ArchiveProcessorInstance findSiteProcessorById(final long processorId) {
        return getDao().getSiteArchiveProcessorInstanceByProcessorId(processorId);
    }
}
