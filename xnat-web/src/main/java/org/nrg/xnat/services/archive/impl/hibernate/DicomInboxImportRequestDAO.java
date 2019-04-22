/*
 * web: org.nrg.xnat.daos.HostInfoDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.archive.impl.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

import static org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest.Status.Completed;
import static org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest.Status.Failed;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DicomInboxImportRequestDAO extends AbstractHibernateDAO<DicomInboxImportRequest> {
    @SuppressWarnings("unchecked")
    @Transactional
    @Override
    public DicomInboxImportRequest findById(final long id) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("id", id));
        List<DicomInboxImportRequest> reqs = criteria.list();
        if(reqs!=null && reqs.size()>0){
            return reqs.get(0);
        }
        else{
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<DicomInboxImportRequest> findAllOutstandingDicomInboxImportRequests() {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.not(Restrictions.in("status", NOT_OUTSTANDING_VALUES)));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        //noinspection unchecked
        return (List<DicomInboxImportRequest>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<DicomInboxImportRequest> findAllOutstandingDicomInboxImportRequestsForUser(String username) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.not(Restrictions.in("status", NOT_OUTSTANDING_VALUES)));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        criteria.add(Restrictions.eq("username", username));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        //noinspection unchecked
        return (List<DicomInboxImportRequest>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<DicomInboxImportRequest> findAllDicomInboxImportRequestsForUser(String username) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        criteria.add(Restrictions.eq("username", username));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        //noinspection unchecked
        return (List<DicomInboxImportRequest>) criteria.list();
    }

    private static final List<DicomInboxImportRequest.Status> NOT_OUTSTANDING_VALUES = Arrays.asList(Failed, Completed);
}
