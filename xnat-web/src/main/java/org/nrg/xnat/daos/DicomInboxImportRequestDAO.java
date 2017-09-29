/*
 * web: org.nrg.xnat.daos.HostInfoDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.daos;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.entities.DicomInboxImportRequest;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

import static org.nrg.xnat.entities.DicomInboxImportRequest.Status.Completed;
import static org.nrg.xnat.entities.DicomInboxImportRequest.Status.Failed;

@Repository
public class DicomInboxImportRequestDAO extends AbstractHibernateDAO<DicomInboxImportRequest> {
    public List<DicomInboxImportRequest> findAllOutstandingDicomInboxImportRequests() {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.not(Restrictions.or(Restrictions.in("status", NOT_OUTSTANDING_VALUES))));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        //noinspection unchecked
        return (List<DicomInboxImportRequest>) criteria.list();
    }

    private static final List<DicomInboxImportRequest.Status> NOT_OUTSTANDING_VALUES = Arrays.asList(Failed, Completed);
}
