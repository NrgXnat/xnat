/*
 * web: org.nrg.xnat.node.dao.XnatNodeInfoDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.processor.dao;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.QueryBuilder;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class ArchiveProcessorInstanceDAO.
 */
@Repository
public class ArchiveProcessorInstanceDAO extends AbstractHibernateDAO<ArchiveProcessorInstance> {
    public static final String SCOPE = "scope";
    public static final String PROCESSOR_CLASS = "processorClass";
    public static final String PRIORITY = "priority";

    /**
     * Overridden to apply updates with {@code Session.merge()} rather than {@code Session.update()}. This entity is
     * audited, and processor updates arrive from {@link org.nrg.xapi.rest.dicom.ArchiveProcessorInstanceApi} as a
     * detached instance whose changed element collections (scpWhitelist, scpBlacklist, parameters, projectIdsList)
     * were replaced wholesale by {@link ArchiveProcessorInstance#update(ArchiveProcessorInstance)}. Reattaching such
     * an instance recreates the replaced collections without their prior state, so Envers records additions but never
     * removals, and revisions reconstruct removed whitelist/blacklist entries as though they were still present.
     * Merging applies the incoming state to the managed collections, so Hibernate — and therefore Envers — sees the
     * true delta. Note that {@link #saveOrUpdate} routes existing entities through this method as well.
     */
    @Override
    public void update(final ArchiveProcessorInstance entity) {
        getSession().merge(entity);
    }

    public List<ArchiveProcessorInstance> getSiteArchiveProcessors() {
        return findByProperty(SCOPE, Scope.Site.code());
    }

    public List<ArchiveProcessorInstance> getSiteArchiveProcessorsForClass(final String processorClass) {
        return findByProperties(parameters(SCOPE, Scope.Site.code(), PROCESSOR_CLASS, processorClass), asc(PRIORITY));
    }

    public List<ArchiveProcessorInstance> getEnabledSiteArchiveProcessors() {
        return findByProperty(SCOPE, Scope.Site.code());
    }

    public List<ArchiveProcessorInstance> getEnabledSiteArchiveProcessorsForAe(String aeAndPort) {
        QueryBuilder<ArchiveProcessorInstance> builder = newQueryBuilder();
        final List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.eq("scope", Scope.Site.code()));
        predicates.add(builder.eq("enabled", true));
        predicates.add(builder.or(builder.isEmpty("scpWhitelist"), builder.isMember("scpWhitelist", aeAndPort)));
        predicates.add(builder.or(builder.isEmpty("scpBlacklist"), builder.isNotMember("scpBlacklist", aeAndPort)));
        builder.where(builder.and(predicates));
        return builder.getResults();
    }

    @Transactional
    public List<ArchiveProcessorInstance> getEnabledSiteArchiveProcessorsInOrder() {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq(SCOPE, Scope.Site.code()));
        criteria.add(Restrictions.eq("enabled", true));
        criteria.addOrder(Order.asc(PRIORITY));
        return GenericUtils.convertToTypedList(criteria.list(), ArchiveProcessorInstance.class);
    }

    @Transactional
    public List<ArchiveProcessorInstance> getEnabledSiteArchiveProcessorsInOrderForLocation(final String location) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq(SCOPE, Scope.Site.code()));
        criteria.add(Restrictions.eq("location", location));
        criteria.add(Restrictions.eq("enabled", true));
        criteria.addOrder(Order.asc(PRIORITY));
        return GenericUtils.convertToTypedList(criteria.list(), ArchiveProcessorInstance.class);
    }

    @Transactional
    public ArchiveProcessorInstance getSiteArchiveProcessorInstanceByProcessorId(final long processorId) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("id", processorId));
        criteria.add(Restrictions.eq(SCOPE, Scope.Site.code()));
        final List<ArchiveProcessorInstance> processors = GenericUtils.convertToTypedList(criteria.list(), ArchiveProcessorInstance.class);
        return CollectionUtils.isNotEmpty(processors) ? processors.getFirst() : null;
    }
}
