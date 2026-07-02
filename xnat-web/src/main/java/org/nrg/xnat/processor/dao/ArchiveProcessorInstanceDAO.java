/*
 * web: org.nrg.xnat.node.dao.XnatNodeInfoDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.processor.dao;

import org.nrg.framework.constants.Scope;
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
        return findByProperties(parameters(SCOPE, Scope.Site.code(), ENABLED_PROPERTY, true), asc(PRIORITY));
    }

    @Transactional
    public List<ArchiveProcessorInstance> getEnabledSiteArchiveProcessorsInOrderForLocation(final String location) {
        return findByProperties(parameters(SCOPE, Scope.Site.code(), "location", location, ENABLED_PROPERTY, true), asc(PRIORITY));
    }

    @Transactional
    public ArchiveProcessorInstance getSiteArchiveProcessorInstanceByProcessorId(final long processorId) {
        return instance(findByProperties(parameters("id", processorId, SCOPE, Scope.Site.code())));
    }
}
