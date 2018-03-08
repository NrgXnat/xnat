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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
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
    public List<ArchiveProcessorInstance> getEnabledSiteArchiveProcessors() {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("scope", ArchiveProcessorInstance.SITE_SCOPE));
        criteria.add(Restrictions.eq("enabled", true));
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
    public ArchiveProcessorInstance getSiteArchiveProcessorInstanceByProcessorId(final int processorId) {
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
