/*
 * web: org.nrg.xnat.daos.HostInfoDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.archive.impl.hibernate;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest.Status.Completed;
import static org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest.Status.Failed;

@Repository
public class DicomInboxImportRequestDAO extends AbstractHibernateDAO<DicomInboxImportRequest> {
    @SuppressWarnings("unchecked")
    @Transactional
    @Override
    public DicomInboxImportRequest findById(final long id) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("id", id));
        final List<DicomInboxImportRequest> reqs = criteria.list();
        if (reqs != null && !reqs.isEmpty()) {
            return reqs.get(0);
        } else {
            return null;
        }
    }

    @Transactional
    public List<DicomInboxImportRequest> findAllOutstandingDicomInboxImportRequests() {
        return findDicomInboxInportRequests(null, true);
    }

    @Transactional
    public List<DicomInboxImportRequest> findAllOutstandingDicomInboxImportRequestsForUser(final String username) {
        return findDicomInboxInportRequests(username, true);
    }

    @Transactional
    public List<DicomInboxImportRequest> findAllDicomInboxImportRequestsForUser(final String username) {
        return findDicomInboxInportRequests(username, false);
    }

    private List<DicomInboxImportRequest> findDicomInboxInportRequests(final String username, final boolean limitToOutstanding) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        if (limitToOutstanding) {
            criteria.add(Restrictions.not(Restrictions.in("status", NOT_OUTSTANDING_VALUES)));
        }
        if (StringUtils.isNotBlank(username)) {
            criteria.add(Restrictions.eq("username", username));
        }
        //noinspection unchecked
        return (List<DicomInboxImportRequest>) criteria.list();
    }

    private static final List<DicomInboxImportRequest.Status> NOT_OUTSTANDING_VALUES = Arrays.asList(Failed, Completed);
}
