/*
 * web: org.nrg.xnat.node.dao.XnatNodeInfoDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.processor.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The Class ArchiveProcessorInstanceDAO.
 */
@Repository
public class ArchiveProcessorInstanceDAO extends AbstractHibernateDAO<ArchiveProcessorInstance> {

    @SuppressWarnings("unchecked")
    @Transactional
    public List<ArchiveProcessorInstance> getSiteArchiveProcessors() {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("scope", ArchiveProcessorInstance.SITE_SCOPE));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<ArchiveProcessorInstance> getSiteArchiveProcessorsForClass(final String processorClass) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("scope", ArchiveProcessorInstance.SITE_SCOPE));
        criteria.add(Restrictions.eq("processorClass", processorClass));
        criteria.addOrder(Order.asc("priority"));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<ArchiveProcessorInstance> getEnabledSiteArchiveProcessors() {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("scope", ArchiveProcessorInstance.SITE_SCOPE));
        criteria.add(Restrictions.eq("enabled", true));
        return criteria.list();
    }


    @SuppressWarnings("unchecked")
    @Transactional
    public List<ArchiveProcessorInstance> getEnabledSiteArchiveProcessorsForAe(String aeAndPort) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("scope", ArchiveProcessorInstance.SITE_SCOPE));
        criteria.add(Restrictions.eq("enabled", true));
        criteria.createAlias("scpWhitelist", "scpWhitelist", JoinType.LEFT_OUTER_JOIN);
        criteria.createAlias("scpBlacklist", "scpBlacklist", JoinType.LEFT_OUTER_JOIN);

        Disjunction whitelistEmptyOrHasAe = Restrictions.disjunction();
        whitelistEmptyOrHasAe.add(Restrictions.isEmpty("scpWhitelist"));
        whitelistEmptyOrHasAe.add(Restrictions.eq("scpWhitelist.elements", aeAndPort));

        Disjunction blacklistEmptyOrDoesNotHaveAe = Restrictions.disjunction();
        blacklistEmptyOrDoesNotHaveAe.add(Restrictions.isEmpty("scpBlacklist"));
        blacklistEmptyOrDoesNotHaveAe.add(Restrictions.ne("scpBlacklist.elements", aeAndPort));

        criteria.add(Restrictions.and(whitelistEmptyOrHasAe, blacklistEmptyOrDoesNotHaveAe));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<ArchiveProcessorInstance> getEnabledSiteArchiveProcessorsInOrder() {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("scope", ArchiveProcessorInstance.SITE_SCOPE));
        criteria.add(Restrictions.eq("enabled", true));
        criteria.addOrder(Order.asc("priority"));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<ArchiveProcessorInstance> getEnabledSiteArchiveProcessorsInOrderForLocation(final String location) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("scope", ArchiveProcessorInstance.SITE_SCOPE));
        criteria.add(Restrictions.eq("location", location));
        criteria.add(Restrictions.eq("enabled", true));
        criteria.addOrder(Order.asc("priority"));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public ArchiveProcessorInstance getSiteArchiveProcessorInstanceByProcessorId(final long processorId) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("id", processorId));
        criteria.add(Restrictions.eq("scope", ArchiveProcessorInstance.SITE_SCOPE));
        List<ArchiveProcessorInstance> processors = criteria.list();
        if(processors!=null && processors.size()>0){
            return processors.get(0);
        }
        else{
            return null;
        }
    }

}
